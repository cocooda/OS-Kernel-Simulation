package Execution;

import Instruction.InstructionType;
import Kernel.KernelAPI;
import Process.PCB;
import java.util.concurrent.BlockingQueue;

public class CPUExSwitchAck extends CPUEx {

    public CPUExSwitchAck(BlockingQueue<PCB> cpuQueue, KernelAPI kernel) {
        super(cpuQueue, kernel);
    }

    @Override
    protected void onSwitchToIO(PCB pcb, InstructionType type) {
        String currentThread = Thread.currentThread().getName();
        System.out.println("[SwitchAck] " + currentThread
                + " -> IOEx thread for PID " + pcb.pid + " (" + type + ")");
    }

    @Override
    protected void onThreadActive(PCB pcb) {
        SwitchAckTracker.recordAndReack(Thread.currentThread());
    }
}
