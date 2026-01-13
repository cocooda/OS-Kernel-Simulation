package Instruction;

public class Value {
    public enum Kind {
        INT,
        DOUBLE,
        BOOLEAN,
        STRING
    }

    public final Kind kind;
    public final Object data;

    public Value(Kind kind, Object data) {
        this.kind = kind;
        this.data = data;
    }

    public static Value ofInt(int value) {
        return new Value(Kind.INT, value);
    }

    public static Value ofDouble(double value) {
        return new Value(Kind.DOUBLE, value);
    }

    public static Value ofBoolean(boolean value) {
        return new Value(Kind.BOOLEAN, value);
    }

    public static Value ofString(String value) {
        return new Value(Kind.STRING, value);
    }

    public static Value parseToken(String token) {
        if (token == null) {
            return new Value(Kind.STRING, "");
        }
        if (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false")) {
            return new Value(Kind.BOOLEAN, Boolean.parseBoolean(token));
        }
        if (isInteger(token)) {
            return new Value(Kind.INT, Integer.parseInt(token));
        }
        if (isDouble(token)) {
            return new Value(Kind.DOUBLE, Double.parseDouble(token));
        }
        return new Value(Kind.STRING, token);
    }

    @Override
    public String toString() {
        return kind + ":" + data;
    }

    public Object getData() {
        return this.data;
    }

    public Kind getKind() {
        return this.kind;
    }

    public static Value add(Value left, Value right) {
        if (left.kind == Kind.STRING || right.kind == Kind.STRING) {
            return new Value(Kind.STRING, String.valueOf(left.data) + String.valueOf(right.data));
        }
        if (left.kind == Kind.BOOLEAN && right.kind == Kind.BOOLEAN) {
            return new Value(Kind.BOOLEAN, (Boolean) left.data || (Boolean) right.data);
        }
        if (isNumeric(left) && isNumeric(right)) {
            return numericOp(left, right, "+");
        }
        throw new IllegalArgumentException("Unsupported add types: " + left.kind + " + " + right.kind);
    }

    public static Value sub(Value left, Value right) {
        if (left.kind == Kind.BOOLEAN && right.kind == Kind.BOOLEAN) {
            return new Value(Kind.BOOLEAN, (Boolean) left.data ^ (Boolean) right.data);
        }
        if (isNumeric(left) && isNumeric(right)) {
            return numericOp(left, right, "-");
        }
        throw new IllegalArgumentException("Unsupported sub types: " + left.kind + " - " + right.kind);
    }

    public static Value mul(Value left, Value right) {
        if (left.kind == Kind.BOOLEAN && right.kind == Kind.BOOLEAN) {
            return new Value(Kind.BOOLEAN, (Boolean) left.data && (Boolean) right.data);
        }
        if (isNumeric(left) && isNumeric(right)) {
            return numericOp(left, right, "*");
        }
        throw new IllegalArgumentException("Unsupported mul types: " + left.kind + " * " + right.kind);
    }

    public static Value div(Value left, Value right) {
        if (isNumeric(left) && isNumeric(right)) {
            return numericOp(left, right, "/");
        }
        throw new IllegalArgumentException("Unsupported div types: " + left.kind + " / " + right.kind);
    }

    public static Value mod(Value left, Value right) {
        if (isNumeric(left) && isNumeric(right)) {
            return numericOp(left, right, "%");
        }
        throw new IllegalArgumentException("Unsupported mod types: " + left.kind + " % " + right.kind);
    }

    private static boolean isNumeric(Value value) {
        return value.kind == Kind.INT || value.kind == Kind.DOUBLE;
    }

    private static Value numericOp(Value left, Value right, String op) {
        boolean useDouble = left.kind == Kind.DOUBLE || right.kind == Kind.DOUBLE;
        double l = ((Number) left.data).doubleValue();
        double r = ((Number) right.data).doubleValue();

        switch (op) {
            case "+":
                return useDouble ? new Value(Kind.DOUBLE, l + r) : new Value(Kind.INT, (int) l + (int) r);
            case "-":
                return useDouble ? new Value(Kind.DOUBLE, l - r) : new Value(Kind.INT, (int) l - (int) r);
            case "*":
                return useDouble ? new Value(Kind.DOUBLE, l * r) : new Value(Kind.INT, (int) l * (int) r);
            case "/":
                if (r == 0.0) {
                    throw new ArithmeticException("Divide by zero");
                }
                return useDouble ? new Value(Kind.DOUBLE, l / r) : new Value(Kind.INT, (int) l / (int) r);
            case "%":
                if (r == 0.0) {
                    throw new ArithmeticException("Modulo by zero");
                }
                return useDouble ? new Value(Kind.DOUBLE, l % r) : new Value(Kind.INT, (int) l % (int) r);
            default:
                throw new IllegalArgumentException("Unknown op: " + op);
        }
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return s.contains(".");
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
