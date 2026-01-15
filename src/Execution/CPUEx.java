package Execution;

import Instruction.Instruction;
import Instruction.InstructionType;
import Kernel.*;
import Process.PCB;
import Process.ProcessState;
import java.util.concurrent.BlockingQueue;

public class CPUEx implements Runnable {

    private final BlockingQueue<PCB> cpuQueue;
    private final KernelAPI kernel;

    // CPU work simulation and fixed time-sharing quantum.
    private static final long CPU_UNIT = 1L;
    private static final long TIME_QUANTUM_MS = 20L;
    private static final long TIME_QUANTUM_NS = TIME_QUANTUM_MS * 1_000_000L;
    private static final long TIME_UNIT_NS = 1_000_000L;

    public CPUEx(BlockingQueue<PCB> cpuQueue, KernelAPI kernel) {
        this.cpuQueue = cpuQueue;
        this.kernel = kernel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PCB pcb = cpuQueue.take(); // RUNNING assigned by kernel
                onThreadActive(pcb);

                long sliceStart = System.nanoTime();
                boolean preempted = false;

                while (true) {
                    Instruction instr = pcb.program.getInstruction(pcb.pc);
                if (instr == null) {
                    System.out.println("[CPU] PID " + pcb.pid + " TERMINATED");
                    kernel.handleTermination(pcb);
                    break;
                }

                    System.out.println("[CPU] PID " + pcb.pid +
                            " executing " + instr.type);

                    if (instr.type == InstructionType.CPU_COMPUTE) {
                        kernel.onSchedulingEvent(pcb, ProcessState.RUNNING);
                        long start = System.nanoTime();
                        instr.action.execute(pcb.ctx);
                        long end = System.nanoTime();
                        int osTimeUnits = mapToOSTime(end - start);

                        Thread.sleep(osTimeUnits * CPU_UNIT);
                        pcb.pc++;

                        if (System.nanoTime() - sliceStart >= TIME_QUANTUM_NS) {
                            preempted = true;
                            break;
                        }
                    } else {
                        // I/O request → kernel decides BLOCKED vs BLOCKED_SUSPENDED
                        System.out.println("[CPU] PID " + pcb.pid +
                            " BLOCKED dues to I/O calling for " + instr.type);
                        onSwitchToIO(pcb, instr.type);
                        kernel.handleBlocked(pcb);
                        break;
                    }
                }

                if (preempted) {
                    pcb.state = ProcessState.READY;
                    kernel.onSchedulingEvent(pcb, ProcessState.READY);
                    kernel.getReadyQueue().put(pcb); // return to scheduler
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[CPU] stopped");
        } catch (Exception e) {
            System.out.println("[CPU] fault: " + e.getMessage());
        }
    }

    private static int mapToOSTime(long ns) {
        return Math.max(1, (int) (ns / TIME_UNIT_NS));
    }

    protected void onSwitchToIO(PCB pcb, InstructionType type) {
        // Hook for subclasses to acknowledge CPU -> IO thread switch.
    }

    protected void onThreadActive(PCB pcb) {
        // Hook for subclasses to note when CPU thread begins work on a PCB.
    }
}
