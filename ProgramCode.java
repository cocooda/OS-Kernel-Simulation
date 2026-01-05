import java.util.List;

public class ProgramCode {

    private final List<Instruction> instructions;

    public ProgramCode(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Instruction getInstruction(int pc) {
        return (pc < instructions.size()) ? instructions.get(pc) : null;
    }
}
