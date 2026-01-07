# API Documentation

## Instruction package

### Action (interface)
- `ExecutionResult execute(ProcessContext ctx)`: Run an instruction against the given process context.

### AddAct
- `AddAct(Value a, Value b)`: Store operands for addition.
- `ExecutionResult execute(ProcessContext ctx)`: Compute `a + b` via `Value.add` and store in `ctx.result`.

### SubAct
- `SubAct(Value a, Value b)`: Store operands for subtraction.
- `ExecutionResult execute(ProcessContext ctx)`: Compute `a - b` via `Value.sub` and store in `ctx.result`.

### MulAct
- `MulAct(Value a, Value b)`: Store operands for multiplication.
- `ExecutionResult execute(ProcessContext ctx)`: Compute `a * b` via `Value.mul` and store in `ctx.result`.

### DivAct
- `DivAct(Value a, Value b)`: Store operands for division.
- `ExecutionResult execute(ProcessContext ctx)`: Compute `a / b` via `Value.div` and store in `ctx.result`.

### ModAct
- `ModAct(Value a, Value b)`: Store operands for modulo.
- `ExecutionResult execute(ProcessContext ctx)`: Compute `a % b` via `Value.mod` and store in `ctx.result`.

### DisplayAct
- `DisplayAct(String contentIn)`: Store a literal string to print.
- `ExecutionResult execute(ProcessContext ctx)`: Print the stored literal.

### PrintValueAct
- `PrintValueAct(String expression)`: Store an expression string to evaluate at runtime.
- `ExecutionResult execute(ProcessContext ctx)`: Evaluate expression via `InstructionFactory.evaluateExpression` and print the value.

### WriteResultAct
- `WriteResultAct(String filename)`: Store a file path to write to.
- `ExecutionResult execute(ProcessContext ctx)`: Append `ctx.result` to the file.

### AssignAct
- `AssignAct(String name, String expression)`: Store variable name and expression.
- `ExecutionResult execute(ProcessContext ctx)`: Evaluate expression and assign into `ctx.vars` (also sets `ctx.result`).

### ReadFileAct
- `ReadFileAct(String filePath)`: Store a file path to read.
- `ExecutionResult execute(ProcessContext ctx)`: Read file into `ctx.content` and set `ctx.path`.

### WriteFileAct
- `WriteFileAct(String filename, String content)`: Store file path and literal content.
- `ExecutionResult execute(ProcessContext ctx)`: Append literal content to file.

### ParseValueAct
- `ExecutionResult execute(ProcessContext ctx)`: Split `ctx.content` by whitespace and populate `ctx.values` with parsed `Value` tokens.

### Instruction
- `Instruction(InstructionType type, Action action)`: Bundle an instruction type and action.

### InstructionType
- Enum values: `DISPLAY`, `READ_FILE`, `WRITE_FILE`, `CPU_COMPUTE`.

### ExecutionResult
- Enum values: `DONE`, `BLOCKED`, `EXIT`, `FAULT`.

### Value
- `Value(Kind kind, Object data)`: Construct a typed value.
- `static Value ofInt(int value)`: Create INT value.
- `static Value ofDouble(double value)`: Create DOUBLE value.
- `static Value ofBoolean(boolean value)`: Create BOOLEAN value.
- `static Value ofString(String value)`: Create STRING value.
- `static Value parseToken(String token)`: Parse a token into INT/DOUBLE/BOOLEAN/STRING.
- `Object getData()`: Return underlying data.
- `Kind getKind()`: Return value kind.
- `static Value add(Value left, Value right)`: Add/concat values (string/boolean/numeric rules).
- `static Value sub(Value left, Value right)`: Subtract values (boolean XOR or numeric).
- `static Value mul(Value left, Value right)`: Multiply values (boolean AND or numeric).
- `static Value div(Value left, Value right)`: Divide numeric values.
- `static Value mod(Value left, Value right)`: Mod numeric values.

### InstructionFactory
- `static Instruction fromLine(String line, ProcessContext ctx)`: Parse a single script line into an `Instruction`.
- `static Instruction fromLine(String line, ProcessContext ctx, String writeContent)`: Parse `write("...")` with literal content.
- `static Value evaluateExpression(String expression, ProcessContext ctx)`: Evaluate an expression into a `Value`.
- Private helpers:
  - `extractString(String line)`: Extract quoted string content.
  - `extractArgument(String line)`: Extract argument inside parentheses.
  - `extractLeftOperand(String expression, ProcessContext ctx)`: Resolve left operand.
  - `extractRightOperand(String expression, ProcessContext ctx)`: Resolve right operand.
  - `splitExpression(String expression)`: Split by operator.
  - `splitOnOperator(String expression, String operator)`: Safe split via regex quoting.
  - `resolveOperand(String token, ProcessContext ctx)`: Resolve literal, `value[n]`, or variable.

## Execution package

### CPUEx
- `CPUEx(BlockingQueue<PCB> cpuQueue, Kernel kernel)`: Construct a CPU executor with queues and kernel.
- `void run()`: Main loop; executes CPU instructions or defers I/O to kernel; advances `pc` and requeues.
- `private static int mapToOSTime(long ns)`: Map real time to OS time units.

## Process package

### ProcessContext
- `ProcessContext()`: Initialize default values, buffers, and variable map.
- `void setA(Value a)`: Set variable `a` and update `vars`.
- `void setB(Value b)`: Set variable `b` and update `vars`.
- `void setRes(Value res)`: Set `result` and update `vars`.
- `void setContent(String content)`: Update `content` from I/O.
- `void setPath(Path path)`: Store path for last read.
- `void setVar(String name, Value value)`: Set a named variable (special-cases `a`, `b`, `result`).
- `Value getVar(String name)`: Get a variable (special-cases `a`, `b`, `result`).

## Program package

### ProgramCode
- `ProgramCode(List<Instruction> instructions)`: Construct a program from instruction list.
- `Instruction getInstruction(int pc)`: Return instruction at program counter or `null` if finished.
- `static ProgramCode fromScript(Path scriptPath, ProcessContext ctx)`: Compile a script file line-by-line into a `ProgramCode`.

## Test package

### InstructionTest
- `InstructionTest()`: Default constructor.
- `void runtest()`: Runs a sequence of instruction-level tests (math, print, read, write, parse, assignment).
