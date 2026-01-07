package Process;

import java.util.concurrent.atomic.AtomicInteger;
import Program.ProgramCode;

public class PCB {

    private static final AtomicInteger PID_GEN = new AtomicInteger(1);

    public final int pid;
    public int priority;
    public ProcessState state;

    public int pc = 0;                 // program counter
    public final ProgramCode program;  // encapsulated program
    public final ProcessContext ctx;   // per-process context

    public PCB(ProgramCode program, int priority) {
        this.pid = PID_GEN.getAndIncrement();
        this.program = program;
        this.priority = priority;
        this.state = ProcessState.NEW;
        this.ctx = new ProcessContext();
    }
}
