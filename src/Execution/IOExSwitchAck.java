package Execution;

import Kernel.KernelAPI;
import Process.PCB;
import java.util.concurrent.BlockingQueue;

public class IOExSwitchAck extends IOEx {

    public IOExSwitchAck(BlockingQueue<PCB> ioQueue, KernelAPI kernel) {
        super(ioQueue, kernel);
    }

    @Override
    protected void onSwitchToCPU(PCB pcb) {
        String currentThread = Thread.currentThread().getName();
        System.out.println("[SwitchAck] " + currentThread
                + " -> CPUEx thread for PID " + pcb.pid);
    }

    @Override
    protected void onThreadActive(PCB pcb) {
        SwitchAckTracker.recordAndReack(Thread.currentThread());
    }
}
