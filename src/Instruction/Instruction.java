package Instruction;

public class Instruction {
    public final InstructionType type;
    public final Action action;  // never work with pcb.priority  - never change the process-state  & never call scheduler dirrectly
    /*
       Drop the duration (it's hard & the running time is determined by the execution)
       -> Execution can be vary for same instruction
       -> It's may never should be a actual pre-defined values
       -> Focus on the actions of execution

    */
    public Instruction(InstructionType type, Action action) {
        this.type = type;
        this.action = action;
    }
}
