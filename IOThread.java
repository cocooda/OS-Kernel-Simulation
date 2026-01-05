import java.util.concurrent.BlockingQueue;

public class IOThread implements Runnable {

    private final BlockingQueue<PCB> ioQueue;
    private final BlockingQueue<PCB> readyQueue;

    public IOThread(BlockingQueue<PCB> ioQueue,
                    BlockingQueue<PCB> readyQueue) {
        this.ioQueue = ioQueue;
        this.readyQueue = readyQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PCB pcb = ioQueue.take();
                Instruction instr = pcb.program.getInstruction(pcb.pc);

                System.out.println("[IO] PID " + pcb.pid + " handling " + instr.type);

                Thread.sleep(instr.duration * 300L);

                pcb.pc++;
                pcb.priority = Math.max(0, pcb.priority - 1);
                pcb.state = ProcessState.READY;

                readyQueue.put(pcb);
            }
        } catch (InterruptedException ignored) {}
    }
}
