public enum InstructionType {
    DISPLAY,       // Highest priority
    READ_FILE,
    WRITE_FILE,
    CPU_COMPUTE    // Lowest priority
}