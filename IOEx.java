import java.util.concurrent.BlockingQueue;

public class IOEx implements Runnable {

    private final BlockingQueue<PCB> ioQueue;
    private final KernelAPI kernel;

    private static final long IO_UNIT = 300L;

    public IOEx(BlockingQueue<PCB> ioQueue, KernelAPI kernel) {
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

                Thread.sleep(instr.duration * IO_UNIT);

                pcb.pc++;

                // Adaptive priority boost after I/O
                pcb.priority = Math.max(0, pcb.priority - 1);

                kernel.handleIOCompletion(pcb);
            }
        } catch (InterruptedException e) {
            System.out.println("[IO] stopped");
        }
    }
}
