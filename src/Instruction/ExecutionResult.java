package Instruction;

public enum ExecutionResult {
    DONE,      // instruction completed normally
    BLOCKED,   // instruction caused blocking I/O
    EXIT,      // process finished
    FAULT      // error / exception
}
