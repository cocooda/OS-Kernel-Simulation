import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler implements Runnable {

    private final PriorityBlockingQueue<PCB> readyQueue;
    private final BlockingQueue<PCB> dispatchQueue;
    private final boolean priorityMode;

    public Scheduler(PriorityBlockingQueue<PCB> readyQueue,
                     BlockingQueue<PCB> dispatchQueue,
                     boolean priorityMode) {
        this.readyQueue = readyQueue;
        this.dispatchQueue = dispatchQueue;
        this.priorityMode = priorityMode;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PCB pcb = readyQueue.take(); // blocks safely
                pcb.state = ProcessState.RUNNING;
                System.out.println("[SCHEDULER] Selected → " + pcb);
                dispatchQueue.put(pcb);
            }
        } catch (InterruptedException e) {
            System.out.println("[SCHEDULER] Stopped");
        }
    }
}
