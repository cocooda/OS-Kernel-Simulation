package Test;

import Instruction.Action;
import Instruction.ExecutionResult;
import Instruction.Instruction;
import Instruction.InstructionFactory;
import Instruction.ParseValueAct;
import Instruction.Value;
import Process.ProcessContext;

public class InstructionTest {
    public InstructionTest() {

    } ;

    public void runtest() {        

        // Read The file (contain 4 values) - doing addition 2 first, multiplication 2 next - record the values to the out
       
        try {

            InstructionFactory factory = new InstructionFactory();

            System.out.println("\nTesting the binary execution");
            ProcessContext ctx = new ProcessContext();
            ctx.setA(Value.ofInt(10));
            ctx.setB(Value.ofInt(100));
            Instruction testIns = factory.fromLine("a / b", ctx);

            ProcessContext ctx2 = new ProcessContext();
            System.out.println("Testing the print execution");
            Instruction testPrint = factory.fromLine("print(\" Hi World \")", ctx2);

        
            // Test ReadFile 
            System.out.println("Testing the file reading instruction");
            ProcessContext ctx3 = new ProcessContext();
            Instruction testRead = factory.fromLine("read(\"src/read_in.txt\")", ctx3);

            // Test the write file (write anything: data, numeric, string) - WriteFile
            System.out.println("Testing the writing instruction");
            ProcessContext ctx4 = new ProcessContext();
            Instruction testWriteIns = factory.fromLine("write(\"src/out.txt\")", ctx4, "WriteFile test: 123456 \n");
            
       
            ExecutionResult res1 =  testIns.action.execute(ctx);
            ExecutionResult res2 = testPrint.action.execute(ctx2);
            ExecutionResult res3 = testRead.action.execute(ctx3);
            ExecutionResult res4 = testWriteIns.action.execute(ctx4);
            System.out.println("About to print results");
            System.out.println(res1);
            System.out.println(ctx.result);
            System.out.println(res3);
            System.out.println(res4);
       



            ProcessContext ctx5 = new ProcessContext();
            Instruction testRead5 = factory.fromLine("read(\"src/in_2.txt\")", ctx5);
            ExecutionResult testRead5Res = testRead5.action.execute(ctx5);
            String res = ctx5.content;

            Action parseAct = new ParseValueAct();
            parseAct.execute(ctx5);
            System.out.println("The parser of ctx5," + ctx5.values);
            Value value1 = ctx5.values.get(0);
            Value value2 = ctx5.values.get(1);
            // Doing the Instruction 6  - add 2 first values
            ProcessContext ctx6 = new ProcessContext();
            ctx6.setA(value1);
            ctx6.setB(value2);
            Instruction testAdd6 = factory.fromLine("a + b", ctx6);
            ExecutionResult signalRes6 = testAdd6.action.execute(ctx6);
            Value res6 = ctx6.result;
            System.out.println(res6);
            // Doing the Instruction 7 - multiply 2 next
            
            ProcessContext ctx7 = new ProcessContext();
            Value value3 = ctx5.values.get(2);
            Value value4 = ctx5.values.get(3);
            ctx7.setA(value3);
            ctx7.setB(value4);
            Instruction testMul7 = factory.fromLine("a * b", ctx7);
            ExecutionResult signalRes7 = testMul7.action.execute(ctx7);
            Value res7 = ctx7.result;
            System.out.println(res7);

            if (signalRes6 == ExecutionResult.DONE && signalRes7 == ExecutionResult.DONE) {
                // Recording to the file
                System.out.println("Doing write to out.txt");
                ProcessContext ctx8 = new ProcessContext(); 
                Instruction testWrite8 = factory.fromLine("write(\"src/out.txt\")", 
                ctx8, 
                '\n' + res6.getData().toString() + '\n' + res7.getData().toString());
                ExecutionResult signalIns8 = testWrite8.action.execute(ctx8);
                if (signalIns8 == ExecutionResult.DONE) {
                    System.out.println("Success");
                }
            } else {
                System.out.println("One of the ops failed");
            }


            // Test the Assignment Task
            System.out.println("Assigninment testing");
            // Assign the flag = true
            ProcessContext ctx9 = new ProcessContext();
            Value valFlag = Value.ofBoolean(true);
            ctx9.setVar("flag", valFlag);
            Instruction testAssign = factory.fromLine("flag = true", ctx9);

            System.out.println("The Variable Assignment " + ctx9.getVar("flag"));
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    };
}
