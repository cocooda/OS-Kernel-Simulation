package Program;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import Instruction.Instruction;
import Instruction.InstructionFactory;
import Process.ProcessContext;

public class ProgramCode {

    private final List<Instruction> instructions;

    public ProgramCode(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Instruction getInstruction(int pc) {
        return (pc < instructions.size()) ? instructions.get(pc) : null;
    }

    public static ProgramCode fromScript(Path scriptPath, ProcessContext ctx) throws IOException {
        List<String> lines = Files.readAllLines(scriptPath);
        List<Instruction> ins = new ArrayList<>();

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            ins.add(InstructionFactory.fromLine(line, ctx));
        }

        return new ProgramCode(ins);
    }
}
