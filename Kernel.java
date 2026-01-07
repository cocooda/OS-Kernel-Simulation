import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Kernel implements KernelAPI {

    // ====== RAM-RESIDENT QUEUES ======
    public final BlockingQueue<PCB> readyQueue = new LinkedBlockingQueue<>();
    public final BlockingQueue<PCB> cpuQueue   = new LinkedBlockingQueue<>();
    public final BlockingQueue<PCB> ioQueue    = new LinkedBlockingQueue<>();

    // ====== SUSPENDED (DISK) QUEUES ======
    private final List<PCB> readySuspendedQueue   = new ArrayList<>();
    private final List<PCB> blockedSuspendedQueue = new ArrayList<>();

    // ====== CONFIG ======
    private boolean priorityScheduling = false;
    private static final int MAX_READY_IN_RAM = 3;

    public void enablePriorityScheduling(boolean enable) {
        this.priorityScheduling = enable;
    }

    // ====== MEMORY CHECK ======
    private boolean ramHasSpace() {
        return readyQueue.size() < MAX_READY_IN_RAM;
    }

    // ====== ADMISSION ======
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
    }

    // ====== CORE KERNEL LOOP ======
    private Thread kernelThread;
    private Thread cpuThread;
    private Thread ioThread;

    private volatile boolean running = true;

    public void stop() {
        running = false;
        if (kernelThread != null) kernelThread.interrupt();
        if (cpuThread != null) cpuThread.interrupt();
        if (ioThread != null) ioThread.interrupt();
    }

    public void start() {
        kernelThread = Thread.currentThread();

        cpuThread = new Thread(new CPUEx(cpuQueue, this), "CPU-Exec");
        ioThread  = new Thread(new IOEx(ioQueue, this), "IO-Exec");

        cpuThread.start();
        ioThread.start();

        try {
            while (running) {

                // (1) Activate suspended processes if RAM frees up
                if (ramHasSpace() && !readySuspendedQueue.isEmpty()) {
                    PCB pcb = readySuspendedQueue.remove(0);
                    pcb.state = ProcessState.READY;
                    readyQueue.put(pcb);
                    System.out.println("[KERNEL] Activate PID " + pcb.pid + " (READY_SUSPENDED to READY)");
                }

                // (2) Select next READY process
                PCB next = readyQueue.take();

                // (3) Priority-based preemption / suspension
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
                cpuQueue.put(next);
                System.out.println("[KERNEL] Dispatch PID " + next.pid + " to RUNNING");

            }
        } catch (InterruptedException ignored) {}
        System.out.println("[KERNEL] Scheduler stopped");
    }

    // ====== PRIORITY VICTIM SELECTION ======
    private PCB findLowerPriorityVictim(int incomingPriority) {
        for (PCB pcb : readyQueue) {
            if (pcb.priority > incomingPriority) {
                return pcb; // lower priority = bigger number
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
        } else {
            pcb.state = ProcessState.BLOCKED;
            ioQueue.add(pcb);
        }
    }

    // ====== IO COMPLETION ======
    @Override
    public void handleIOCompletion(PCB pcb) throws InterruptedException {
        if (pcb.state == ProcessState.BLOCKED_SUSPENDED) {
            pcb.state = ProcessState.READY_SUSPENDED;
            readySuspendedQueue.add(pcb);
            System.out.println("[KERNEL] I/O done PID " + pcb.pid +
                    " (BLOCKED_SUSPENDED to READY_SUSPENDED)");
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

}
