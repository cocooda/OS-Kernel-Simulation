package Kernel;
import Process.PCB;
import java.util.concurrent.BlockingQueue;

public interface KernelAPI {

    // Called by CPU executor when I/O is requested
    void handleBlocked(PCB pcb);

    // Called by IO executor when I/O finishes
    void handleIOCompletion(PCB pcb) throws InterruptedException;

    // Called by CPU when a process finishes its work
    void handleTermination(PCB pcb);

    // Scheduler-facing queue (optional but useful)
    BlockingQueue<PCB> getReadyQueue();
}
