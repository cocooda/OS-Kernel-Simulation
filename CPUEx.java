import java.util.concurrent.BlockingQueue;

public class CPUEx implements Runnable {

    private final BlockingQueue<PCB> cpuQueue;
    private final KernelAPI kernel;

    // One instruction = one time quantum
    private static final long CPU_UNIT = 200L;

    public CPUEx(BlockingQueue<PCB> cpuQueue, KernelAPI kernel) {
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
                    // Simulate one quantum
                    Thread.sleep(instr.duration * CPU_UNIT);
                    pcb.pc++;

                    pcb.state = ProcessState.READY;
                    kernel.getReadyQueue().put(pcb); // return to scheduler

                } else {
                    // I/O request → kernel decides BLOCKED vs BLOCKED_SUSPENDED
                    kernel.handleBlocked(pcb);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[CPU] stopped");
        }
    }
}
