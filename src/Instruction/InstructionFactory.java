package Instruction;

import Process.ProcessContext;

public class InstructionFactory {

    public static Instruction fromLine(String line, ProcessContext ctx) {
        if (line.contains("=") && !line.startsWith("print") && !line.startsWith("read") && !line.startsWith("write")) {
            String[] parts = line.split("=", 2);
            String name = parts[0].trim();
            String expr = parts[1].trim();
            if (name.isEmpty() || expr.isEmpty()) {
                throw new IllegalArgumentException("Invalid assignment: " + line);
            }
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new AssignAct(name, expr)
            );
        }

        if (line.startsWith("print")) {
            if (line.contains("\"")) {
                String content = extractString(line);
                return new Instruction(
                    InstructionType.DISPLAY,
                    new DisplayAct(content)
                );
            }
            String expr = extractArgument(line);
            return new Instruction(
                InstructionType.DISPLAY,
                new PrintValueAct(expr)
            );
        }

        if (line.startsWith("read")) {
            String filename = extractString(line);
            return new Instruction(
                InstructionType.READ_FILE,
                new ReadFileAct(filename)
            );
        }
        if (line.startsWith("parse")) {
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new ParseValueAct()
            );
        }
        if (line.startsWith("write")) {
            String filename = extractString(line);
            return new Instruction(
                InstructionType.WRITE_FILE,
                new WriteResultAct(filename)
            );
        }

        if (line.contains("+")) {
            Value a = extractLeftOperand(line, ctx);
            Value b = extractRightOperand(line, ctx);
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new AddAct(a, b)
            );
        }

        if (line.contains("-")) {
            Value a = extractLeftOperand(line, ctx);
            Value b = extractRightOperand(line, ctx);
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new SubAct(a, b)
            );
        }

        if (line.contains("%")) {
            Value a = extractLeftOperand(line, ctx);
            Value b = extractRightOperand(line, ctx);
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new ModAct(a, b)
            );
        }

        if (line.contains("*")) {
            Value a = extractLeftOperand(line, ctx);
            Value b = extractRightOperand(line, ctx);
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new MulAct(a, b)
            );
        }

        if (line.contains("/")) {
            Value a = extractLeftOperand(line, ctx);
            Value b = extractRightOperand(line, ctx);
            return new Instruction(
                InstructionType.CPU_COMPUTE,
                new DivAct(a, b)
            );
        }

        throw new IllegalArgumentException("Unknown instruction");
    }

    public static Instruction fromLine(String line, ProcessContext ctx, String writeContent) {
        if (line.startsWith("write")) {
            String filename = extractString(line);
            return new Instruction(
                InstructionType.WRITE_FILE,
                new WriteFileAct(filename, writeContent)
            );
        }
        return fromLine(line, ctx);
    }

    // Extract string inside print("...")
    private static String extractString(String line) {
        int start = line.indexOf("\"");
        int end = line.lastIndexOf("\"");
        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("Invalid print syntax: " + line);
        }
        return line.substring(start + 1, end);
    }

    private static String extractArgument(String line) {
        int start = line.indexOf("(");
        int end = line.lastIndexOf(")");
        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("Invalid print syntax: " + line);
        }
        return line.substring(start + 1, end).trim();
    }

    // Extract left operand (either literal int or variable a/b from context)
    private static Value extractLeftOperand(String expression, ProcessContext ctx) {
        String[] parts = splitExpression(expression);
        return resolveOperand(parts[0].trim(), ctx);
    }

    // Extract right operand (either literal int or variable a/b from context)
    private static Value extractRightOperand(String expression, ProcessContext ctx) {
        String[] parts = splitExpression(expression);
        return resolveOperand(parts[1].trim(), ctx);
    }

    // Split binary expression like "a + b" or "a % b"
    private static String[] splitExpression(String expression) {
        if (expression.contains("+")) {
            return splitOnOperator(expression, "+");
        }
        if (expression.contains("-")) {
            return splitOnOperator(expression, "-");
        }
        if (expression.contains("%")) {
            return splitOnOperator(expression, "%");
        }
        if (expression.contains("*")) {
            return splitOnOperator(expression, "*");
        }
        if (expression.contains("/")) {
            return splitOnOperator(expression, "/");
        }
        throw new IllegalArgumentException("Unsupported expression: " + expression);
    }

    private static String[] splitOnOperator(String expression, String operator) {
        return expression.split(java.util.regex.Pattern.quote(operator));
    }

    // Resolve operand value
    private static Value resolveOperand(String token, ProcessContext ctx) {
        if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            String unquoted = token.substring(1, token.length() - 1);
            return Value.ofString(unquoted);
        }
        if (token.startsWith("value[")) {
            int end = token.indexOf(']');
            if (end > 6) {
                String indexText = token.substring(6, end).trim();
                int idx = Integer.parseInt(indexText);
                if (idx >= 0 && idx < ctx.values.size()) {
                    // System.out.println("Get " +  ctx.values.get(idx));
                    return ctx.values.get(idx);
                }
            }
            throw new IllegalArgumentException("Invalid value index: " + token);
        }
        Value existing = ctx.getVar(token);
        if (existing != null) return existing;

        return Value.parseToken(token);
    }

    static Value evaluateExpression(String expression, ProcessContext ctx) {
        if (expression.contains("+")) {
            Value a = extractLeftOperand(expression, ctx);
            Value b = extractRightOperand(expression, ctx);
            return Value.add(a, b);
        }
        if (expression.contains("-")) {
            Value a = extractLeftOperand(expression, ctx);
            Value b = extractRightOperand(expression, ctx);
            return Value.sub(a, b);
        }
        if (expression.contains("%")) {
            Value a = extractLeftOperand(expression, ctx);
            Value b = extractRightOperand(expression, ctx);
            return Value.mod(a, b);
        }
        if (expression.contains("*")) {
            Value a = extractLeftOperand(expression, ctx);
            Value b = extractRightOperand(expression, ctx);
            return Value.mul(a, b);
        }
        if (expression.contains("/")) {
            Value a = extractLeftOperand(expression, ctx);
            Value b = extractRightOperand(expression, ctx);
            return Value.div(a, b);
        }
        return resolveOperand(expression.trim(), ctx);
    }
}
