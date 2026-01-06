import java.util.concurrent.BlockingQueue;

public class CPUEx implements Runnable {

    private final BlockingQueue<PCB> cpuQueue; // Running process
    private final BlockingQueue<PCB> readyQueue; // RAM for ready
    private final BlockingQueue<PCB> ioQueue; // Blocked by IO

    public CPUEx(BlockingQueue<PCB> cpuQueue, // pass by reference
                     BlockingQueue<PCB> readyQueue,
                     BlockingQueue<PCB> ioQueue) {
        this.cpuQueue = cpuQueue;
        this.readyQueue = readyQueue;
        this.ioQueue = ioQueue;
    }

    @Override
    public void run() {
        try {
            while (true) { // adding the logic for handling the process
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
                    Thread.sleep(instr.duration * 200L); // Purpose show the CPU is busy
                    pcb.pc++;
                    pcb.state = ProcessState.READY;
                    readyQueue.put(pcb);
                } else {
                    pcb.state = ProcessState.BLOCKED; // this is IO - push IO queue for IO driver
                    ioQueue.put(pcb);
                }
            }
        } catch (InterruptedException ignored) {}
    }
}
