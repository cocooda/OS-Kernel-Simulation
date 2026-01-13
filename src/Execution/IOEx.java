package Execution;

import Instruction.Instruction;
import Kernel.*;
import Process.PCB;
import java.util.concurrent.BlockingQueue;

public class IOEx implements Runnable {

    private final BlockingQueue<PCB> ioQueue;
    private final KernelAPI kernel;

    private static final long IO_UNIT = 300L;
    private static final long TIME_UNIT_NS = 1_000_000L;

    public IOEx(BlockingQueue<PCB> ioQueue, KernelAPI kernel) {
        this.ioQueue = ioQueue;
        this.kernel = kernel;
    }

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        try {
            while (true) {
                PCB pcb = ioQueue.take();

                Instruction instr = pcb.program.getInstruction(pcb.pc);
                System.out.println("[IO] PID " + pcb.pid +
                        " handling " + instr.type);

                long start = System.nanoTime();
                instr.action.execute(pcb.ctx);
                long end = System.nanoTime();
                int osTimeUnits = mapToOSTime(end - start);

                Thread.sleep(osTimeUnits * IO_UNIT);  // Add the sleep for simmulating the latency between the memory (disk) & the CPU

                pcb.pc++;

                // Adaptive priority boost after I/O
                pcb.priority = pcb.priority + 1;

                kernel.handleIOCompletion(pcb);
            }
        } catch (InterruptedException e) {
            System.out.println("[IO] stopped");
        } catch (Exception e) {
            System.out.println("[IO] fault: " + e.getMessage());
        }
    }

    private static int mapToOSTime(long ns) {
        return Math.max(1, (int) (ns / TIME_UNIT_NS));
    }
}
