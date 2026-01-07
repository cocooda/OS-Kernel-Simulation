package Execution;

import java.util.concurrent.BlockingQueue;
import Instruction.Instruction;
import Instruction.InstructionType;
import Kernel.Kernel;
import Process.PCB;
import Process.ProcessState;

public class CPUEx implements Runnable {

    private final BlockingQueue<PCB> cpuQueue;
    private final Kernel kernel;

    // One instruction = one time quantum
    private static final long CPU_UNIT = 200L;
    private static final long TIME_UNIT_NS = 1_000_000L;

    public CPUEx(BlockingQueue<PCB> cpuQueue, Kernel kernel) {
        this.cpuQueue = cpuQueue;
        this.kernel = kernel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PCB pcb = cpuQueue.take(); // RUNNING assigned by kernel

                Instruction instr = pcb.program.getInstruction(pcb.pc);
                if (instr == null) {
                    pcb.state = ProcessState.TERMINATED;
                    System.out.println("[CPU] PID " + pcb.pid + " TERMINATED");
                    continue;
                }

                System.out.println("[CPU] PID " + pcb.pid +
                        " executing " + instr.type);

                if (instr.type == InstructionType.CPU_COMPUTE) {
                    long start = System.nanoTime();
                    instr.action.execute(pcb.ctx);
                    long end = System.nanoTime();
                    int osTimeUnits = mapToOSTime(end - start);

                    Thread.sleep(osTimeUnits * CPU_UNIT);
                    pcb.pc++;

                    pcb.state = ProcessState.READY;
                    kernel.readyQueue.put(pcb); // return to scheduler

                } else {
                    // I/O request → kernel decides BLOCKED vs BLOCKED_SUSPENDED
                    kernel.handleBlocked(pcb);
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
}
