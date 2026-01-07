import Kernel.Kernel;
import Process.PCB;
import Process.ProcessContext;
import Process.ProcessState;
import Program.ProgramCode;

public class Main {
    public static void main(String args[]) {
        try {
            ProcessContext ctx = new ProcessContext();
            ProgramCode program = ProgramCode.fromScript(java.nio.file.Path.of("src/script.txt"), ctx);

            Kernel kernel = new Kernel();
            PCB pcb = new PCB(program, 3);
            kernel.admitProcess(pcb);

            Thread kernelThread = new Thread(kernel::start, "Kernel");
            kernelThread.start();

            while (pcb.state != ProcessState.TERMINATED) {
                Thread.sleep(50);
            }

            kernel.stop();
            kernelThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    };
}
