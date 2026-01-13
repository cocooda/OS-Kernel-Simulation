package Instruction;

import Process.ProcessContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
@FunctionalInterface
public interface Action {
    ExecutionResult execute(ProcessContext ctx) throws Exception;
}

class AddAct implements Action {
    private final Value a;
    private final Value b;

    public AddAct(Value a, Value b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            ctx.result = Value.add(a, b);
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class SubAct implements Action {
    private final Value a;
    private final Value b;

    public SubAct(Value a, Value b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            ctx.result = Value.sub(a, b);
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class MulAct implements Action {
    private final Value a;
    private final Value b;

    public MulAct(Value a, Value b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            ctx.result = Value.mul(a, b);
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class DivAct implements Action {
    private final Value a;
    private final Value b;

    public DivAct(Value a, Value b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            ctx.result = Value.div(a, b);
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class ModAct implements Action {
    private final Value a;
    private final Value b;

    public ModAct(Value a, Value b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            ctx.result = Value.mod(a, b);
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class DisplayAct implements Action {
    private final String content;

    public DisplayAct(String contentIn) {
        this.content = contentIn;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        System.out.println(content);
        return ExecutionResult.DONE;
    }
}

class PrintValueAct implements Action {
    private final String expression;

    public PrintValueAct(String expression) {
        this.expression = expression;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            Value value = InstructionFactory.evaluateExpression(expression, ctx);
            System.out.println(value.getData());
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class WriteFileAct implements Action {
    private final Path path;

    public WriteFileAct(String filename) {
        this.path = Path.of(filename);
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            if (ctx.result == null) {
                return ExecutionResult.FAULT;
            }
            String output = String.valueOf(ctx.result.getData());
            Files.writeString(
                path,
                output + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            return ExecutionResult.DONE;
        } catch (IOException e) {
            return ExecutionResult.FAULT;
        }
    }
}

class AssignAct implements Action {
    private final String name;
    private final String expression;

    public AssignAct(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            Value value = InstructionFactory.evaluateExpression(expression, ctx);
            ctx.setVar(name, value);
            ctx.result = value;
            return ExecutionResult.DONE;
        } catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    }
}

class ReadFileAct implements Action {
    private final Path path;

    public ReadFileAct(String filePath) {
        this.path = Path.of(filePath);
    }

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        try {
            String content = Files.readString(path);
            // System.out.println("Read file:\n" + content);
            ctx.setPath(this.path);
            ctx.setContent(content);
            return ExecutionResult.DONE;
        } catch (IOException e) {
            return ExecutionResult.FAULT;
        }
    }
}

// class WriteFileAct implements Action {

//     private final Path path;
//     private final String content;

//     public WriteFileAct(String filename, String content) {
//         this.path = Path.of(filename);
//         this.content = content;
//     }

//     @Override
//     public ExecutionResult execute(ProcessContext ctx) throws IOException {
//         try {
//             Files.writeString(
//             path,
//             content,
//             StandardOpenOption.CREATE,
//             StandardOpenOption.APPEND
//             );
//             return ExecutionResult.DONE;
//         } catch (IOException e) {
//             return ExecutionResult.FAULT;
//         }
        
//     }
// }
