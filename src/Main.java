import Kernel.Kernel;
import Process.PCB;
import Process.ProcessContext;
import Program.ProgramCode;

public class Main {
 
    public static PCB createProcessByFile(String filename) throws Exception{
        ProcessContext ctx = new ProcessContext();
        try {
            ProgramCode program = ProgramCode.fromScript(java.nio.file.Path.of(filename), ctx);
            PCB pcb = new PCB(program, ctx);
            return pcb;
        } catch (Exception e) {
            System.out.println("Fail to yeild process");
            return null;
        }
    };  

    public static void playgroundWith2Processes() {
        try {
            
            Kernel kernel = new Kernel();
            PCB pcb = createProcessByFile("src/script2.txt");
            PCB pcb2 = createProcessByFile("src/script.txt");
            // activate the priority scheduling
            boolean enable = true;
            kernel.enablePriorityScheduling(enable);
            kernel.admitProcess(pcb);
            kernel.admitProcess(pcb2);

            Thread kernelThread = new Thread(kernel::start, "Kernel");
            System.out.println("Start the Simmulation");

            kernelThread.start();

            kernelThread.join();
            System.out.println("Simmulation Endded");
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    };

    public static void playgroundWith3Processes() {
    try {
        Kernel kernel = new Kernel();

        // Create 3 independent processes (3 programs)
        PCB pcb1 = createProcessByFile("src/script.txt");
        PCB pcb2 = createProcessByFile("src/script2.txt");
        PCB pcb3 = createProcessByFile("src/script3.txt");

        // Enable priority scheduling
        kernel.enablePriorityScheduling(true);

        // Admit processes to the kernel
        kernel.admitProcess(pcb1);
        kernel.admitProcess(pcb2);
        kernel.admitProcess(pcb3);

        // Start kernel (scheduler + CPU + IO threads)
        Thread kernelThread = new Thread(kernel::start, "Kernel");
        System.out.println("Start the Simulation (3 Processes)");

        kernelThread.start();
        kernelThread.join();

        System.out.println("Simulation Ended");
    } catch (Exception e) {
        e.printStackTrace();    
    }
}


    public static void main(String[] args) {
        playgroundWith2Processes();
    }
}
