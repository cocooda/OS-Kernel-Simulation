package Instruction;

import Process.ProcessContext;

public class ParseValueAct implements Action {

    @Override
    public ExecutionResult execute(ProcessContext ctx) {
        ctx.values.clear();
        try {
            if (ctx.content == null || ctx.content.isEmpty()) {
                return ExecutionResult.DONE;
            }

            String[] tokens = ctx.content.split("\\s+");

            for (String t : tokens) {
                ctx.values.add(Value.parseToken(t));
            }
            return ExecutionResult.DONE;
        }
        catch (Exception e) {
            return ExecutionResult.FAULT;
        }
    };
}
