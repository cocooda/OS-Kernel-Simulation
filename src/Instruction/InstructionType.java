package Instruction;

public enum InstructionType {
    DISPLAY,       // I/O-bound, latency-sensitive
    READ_FILE,     // I/O-bound
    WRITE_FILE,    // I/O-bound
    CPU_COMPUTE    // CPU-bound
}
