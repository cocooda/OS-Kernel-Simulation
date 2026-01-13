package Kernel;

import Execution.CPUEx;
import Execution.IOEx;
import Process.PCB;
import Process.ProcessState;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Kernel implements KernelAPI {

    // ====== RAM-RESIDENT QUEUES ======
    public final BlockingQueue<PCB> readyQueue = new LinkedBlockingQueue<>();
    public final BlockingQueue<PCB> cpuQueue   = new LinkedBlockingQueue<>();
    public final BlockingQueue<PCB> ioQueue    = new LinkedBlockingQueue<>();

    // ====== SUSPENDED (DISK) QUEUES ======
    private final List<PCB> readySuspendedQueue   = new ArrayList<>();
    private final List<PCB> blockedSuspendedQueue = new ArrayList<>();

    // ====== CORE KERNEL LOOP ======
    private Thread kernelThread;
    private Thread cpuThread;
    private Thread ioThread;

    // ====== THREADS ======
    private Thread kernelThread;
    private Thread cpuThread;
    private Thread ioThread;

    // ====== DAVE'S CONFIG & LOGIC CONSTANTS ======
    private boolean priorityScheduling = false;
    private static final int MAX_READY_IN_RAM = 3;
    private static final int MAX_PRIORITY = 10;
    private static final int MIN_PRIORITY = 0;
    private static final int READY_AGING_THRESHOLD = 10;
    private static final int RECLASSIFY_SLICES = 4;
    private static final long SCHEDULER_POLL_MS = 50L;

    private final AtomicInteger activeProcessCount = new AtomicInteger(0);
    private volatile boolean running = true;

    public void enablePriorityScheduling(boolean enable) {
        this.priorityScheduling = enable;
    }

    @Override
    public BlockingQueue<PCB> getReadyQueue() {
        return readyQueue;
    }

    private boolean ramHasSpace() {
        return readyQueue.size() < MAX_READY_IN_RAM;
    }

    // ====== PROCESS LIFECYCLE ======

    public void admitProcess(PCB pcb) throws InterruptedException {
        if (ramHasSpace()) {
            pcb.state = ProcessState.READY;
            readyQueue.put(pcb);
            System.out.println("[KERNEL] Admit PID " + pcb.pid + " to READY");
        } else {
            pcb.state = ProcessState.READY_SUSPENDED;
            readySuspendedQueue.add(pcb);
            System.out.println("[KERNEL] Admit PID " + pcb.pid + " to READY_SUSPENDED");
        }
        activeProcessCount.incrementAndGet();
    }

  
    public void stop() {
        running = false;
        if (this.kernelThread != null) this.kernelThread.interrupt();
        if (this.cpuThread != null) this.cpuThread.interrupt();
        if (this.ioThread != null) this.ioThread.interrupt();
    }

    public void start() {
        this.kernelThread = Thread.currentThread();

        this.cpuThread = new Thread(new CPUEx(cpuQueue, this), "CPU-Exec");
        this.ioThread  = new Thread(new IOEx(ioQueue, this), "IO-Exec");

        this.cpuThread.start();
        this.ioThread.start();

        try {
            while (this.running) {

                // (1) Activate suspended processes if RAM frees up
                for (PCB pcb : readyQueue) {
                    onSchedulingEvent(pcb, ProcessState.READY);
                }
                if (ramHasSpace() && !blockedSuspendedQueue.isEmpty()) {
                    PCB pcb = blockedSuspendedQueue.remove(0);
                    pcb.state = ProcessState.BLOCKED;
                    System.out.println("[KERNEL] Activate PID " + pcb.pid +
                            " (BLOCKED_SUSPENDED to BLOCKED)");
                }
                
                if (ramHasSpace() && !readySuspendedQueue.isEmpty()) {
                    PCB pcb = readySuspendedQueue.remove(0);
                    pcb.state = ProcessState.READY;
                    readyQueue.put(pcb);
                    System.out.println("[KERNEL] Activate PID " + pcb.pid + " (READY_SUSPENDED to READY)");
                }

                // (2) Select next READY process
                PCB next = readyQueue.poll(SCHEDULER_POLL_MS, TimeUnit.MILLISECONDS); // Retrieve & wait for certain time range if not get any -> return null
                if (next == null) {
                    if (shouldStop()) {
                        stop();
                        break;
                    }
                    continue;
                }

                // (3) Priority-based preemption / suspension (READY -> READY_SUSPEND)
                if (priorityScheduling) {
                    PCB victim = findLowerPriorityVictim(next.priority);
                    if (victim != null) {
                        readyQueue.remove(victim);
                        victim.state = ProcessState.READY_SUSPENDED;
                        readySuspendedQueue.add(victim);
                        System.out.println("[KERNEL] Suspend PID " + victim.pid +
                                " due to higher-priority PID " + next.pid);
                    }
                }

                // (4) Dispatch
                next.state = ProcessState.RUNNING;
                onSchedulingEvent(next, ProcessState.RUNNING);
                cpuQueue.put(next);
                System.out.println("[KERNEL] Dispatch PID " + next.pid + " to RUNNING");
            }
        } catch (InterruptedException ignored) {}
        System.out.println("[KERNEL] Scheduler stopped");
    }

    // ====== CORE LOGIC (DAVE'S REFINEMENTS) ======

    private PCB findLowerPriorityVictim(int incomingPriority) {
        for (PCB pcb : readyQueue) {
            if (pcb.priority < incomingPriority) {
                return pcb; // lower priority = smaller number
            }
        }
        return null;
    }

    // ====== BLOCKED SUSPENSION HOOK ======
    @Override
    public void handleBlocked(PCB pcb) {
        if (!ramHasSpace()) {
            pcb.state = ProcessState.BLOCKED_SUSPENDED;
            blockedSuspendedQueue.add(pcb);
            System.out.println("[KERNEL] PID " + pcb.pid + " to BLOCKED_SUSPENDED");
            ioQueue.add(pcb);
        } else {
            pcb.state = ProcessState.BLOCKED;
        }
        onSchedulingEvent(pcb, ProcessState.BLOCKED);
    }

    // ====== IO COMPLETION ======
    @Override
    public void handleIOCompletion(PCB pcb) throws InterruptedException {
        if (pcb.state == ProcessState.BLOCKED_SUSPENDED) {
            blockedSuspendedQueue.remove(pcb);
            pcb.state = ProcessState.READY_SUSPENDED;
            readySuspendedQueue.add(pcb);
            System.out.println("[KERNEL] I/O done PID " + pcb.pid + " (BLOCKED_SUSPENDED to READY_SUSPENDED)");
        } else {
            pcb.state = ProcessState.READY;
            readyQueue.put(pcb);
        }
    }

    @Override
    public BlockingQueue<PCB> getReadyQueue() {
        return readyQueue;
    }


    public void joinAll() throws InterruptedException {
        if (cpuThread != null) cpuThread.join();
        if (ioThread != null) ioThread.join();
    }


    public void handleTermination(PCB pcb) {
        if (pcb.state != ProcessState.TERMINATED) {
            pcb.state = ProcessState.TERMINATED;
        }
        activeProcessCount.decrementAndGet();
    }

    private void onSchedulingEvent(PCB pcb, ProcessState state) {
        if (state == ProcessState.RUNNING) {
            pcb.cpuTicks += 1;
            pcb.readyWaitSteps = 0;
            pcb.reclassifyCounter += 1;
        } else if (state == ProcessState.BLOCKED) {
            pcb.waitTicks += 1;
            pcb.readyWaitSteps = 0;
            pcb.reclassifyCounter += 1;
        } else if (state == ProcessState.READY) {
            pcb.readyWaitSteps += 1;
        }
        // Prevent the Process starvation 
        if (pcb.readyWaitSteps >= READY_AGING_THRESHOLD) {
            pcb.priority = Math.min(pcb.priority + 1, MAX_PRIORITY);
            pcb.readyWaitSteps = 0;
        }

        if (pcb.reclassifyCounter >= RECLASSIFY_SLICES) {
            int total = pcb.cpuTicks + pcb.waitTicks;
            if (total > 0) {
                pcb.ioRatio = (double) pcb.waitTicks / total;
                if (pcb.ioRatio > 0.6) {
                    pcb.priority = Math.min(pcb.priority + 1, MAX_PRIORITY);
                } else if (pcb.ioRatio < 0.4) {
                    pcb.priority = Math.max(pcb.priority - 1, MIN_PRIORITY);
                }
            }
            pcb.cpuTicks = 0;
            pcb.waitTicks = 0;
            pcb.reclassifyCounter = 0;
        }
    }

    private boolean shouldStop() {
        return activeProcessCount.get() == 0
                && readyQueue.isEmpty()
                && cpuQueue.isEmpty()
                && ioQueue.isEmpty()
                && readySuspendedQueue.isEmpty()
                && blockedSuspendedQueue.isEmpty();
    }
}
