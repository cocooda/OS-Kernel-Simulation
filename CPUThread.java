import java.util.concurrent.BlockingQueue;

public class CPUThread implements Runnable {

    private final BlockingQueue<PCB> cpuQueue;
    private final BlockingQueue<PCB> readyQueue;
    private final BlockingQueue<PCB> ioQueue;

    public CPUThread(BlockingQueue<PCB> cpuQueue,
                     BlockingQueue<PCB> readyQueue,
                     BlockingQueue<PCB> ioQueue) {
        this.cpuQueue = cpuQueue;
        this.readyQueue = readyQueue;
        this.ioQueue = ioQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PCB pcb = cpuQueue.take();
                pcb.state = ProcessState.RUNNING;

                Instruction instr = pcb.program.getInstruction(pcb.pc);
                if (instr == null) {
                    pcb.state = ProcessState.TERMINATED;
                    System.out.println("[CPU] PID " + pcb.pid + " TERMINATED");
                    continue;
                }

                System.out.println("[CPU] PID " + pcb.pid + " executing " + instr.type);

                if (instr.type == InstructionType.CPU_COMPUTE) {
                    Thread.sleep(instr.duration * 200L);
                    pcb.pc++;
                    pcb.state = ProcessState.READY;
                    readyQueue.put(pcb);
                } else {
                    pcb.state = ProcessState.BLOCKED;
                    ioQueue.put(pcb);
                }
            }
        } catch (InterruptedException ignored) {}
    }
}
