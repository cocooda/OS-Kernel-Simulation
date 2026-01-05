public class Instruction {
    public final InstructionType type;
    public final int duration;

    public Instruction(InstructionType type, int duration) {
        this.type = type;
        this.duration = duration;
    }
}
