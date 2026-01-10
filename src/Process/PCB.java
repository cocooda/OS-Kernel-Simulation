package Process;
import Program.ProgramCode;
import java.util.concurrent.atomic.AtomicInteger;


public class PCB {

    private static final AtomicInteger PID_GEN = new AtomicInteger(1);
    private final int DEFAULT_PRIORITY = 5;
    public final int pid;
    public int priority;
    public ProcessState state;

    public int pc = 0;                 // program counter
    public final ProgramCode program;  // encapsulated program
    public final ProcessContext ctx;   // per-process context
    public int cpuTicks = 0;
    public int waitTicks = 0;
    public double ioRatio = 0.0;
    public int readyWaitSteps = 0;
    public int reclassifyCounter = 0;

    public PCB(ProgramCode program, ProcessContext ctx) {
        this.pid = PID_GEN.getAndIncrement();
        this.program = program;
        this.priority = DEFAULT_PRIORITY;
        this.state = ProcessState.NEW;
        this.ctx = ctx;
    }

    public PCB() { // Empty Process
        this.pid = PID_GEN.getAndIncrement();
        this.program = null;
        this.priority = DEFAULT_PRIORITY;
        this.state = ProcessState.NEW;
        this.ctx = new ProcessContext();
    }
}
