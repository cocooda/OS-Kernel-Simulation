import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Kernel {

    // ====== KERNEL DATA STRUCTURES ======
    public final BlockingQueue<PCB> readyQueue = new LinkedBlockingQueue<>();
    public final BlockingQueue<PCB> cpuQueue   = new LinkedBlockingQueue<>();
    public final BlockingQueue<PCB> ioQueue    = new LinkedBlockingQueue<>();

    private boolean priorityScheduling = false;

    // ====== CONFIGURATION ======
    public void enablePriorityScheduling(boolean enable) {
        this.priorityScheduling = enable;
    }

    // ====== KERNEL EXECUTION ====== (Monolithic Kernel have the parralelling the CPU execution & I.O Execution)
    public void start() {

        // Start kernel modules (threads)
        new Thread(new CPUEx(cpuQueue, readyQueue, ioQueue), "CPU-Thread").start(); // CPU execution
        new Thread(new IOEx(ioQueue, readyQueue), "IO-Thread").start(); // I.O Driver execution

        // // Scheduler loop (logical dispatcher)
        // try {
        //     while (true) {
        //         PCB pcb = readyQueue.take(); // READY → RUNNING candidate

        //         if (priorityScheduling) {
        //             // simple aging / boost hook
        //             pcb.priority = Math.max(0, pcb.priority - 1);
        //         }

        //         cpuQueue.put(pcb); // hand to CPU
        //     }
        // } catch (InterruptedException ignored) {}
    }
}
