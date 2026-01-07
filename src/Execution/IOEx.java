package Execution;

import java.util.concurrent.BlockingQueue;
import Instruction.Instruction;
import Kernel.Kernel;
import Process.PCB;

public class IOEx implements Runnable {

    private final BlockingQueue<PCB> ioQueue;
    private final Kernel kernel;

    private static final long IO_UNIT = 300L;
    private static final long TIME_UNIT_NS = 1_000_000L;

    public IOEx(BlockingQueue<PCB> ioQueue, Kernel kernel) {
        this.ioQueue = ioQueue;
        this.kernel = kernel;
    }

    @Override
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

                Thread.sleep(osTimeUnits * IO_UNIT);

                pcb.pc++;

                // Adaptive priority boost after I/O
                pcb.priority = Math.max(0, pcb.priority - 1);

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
