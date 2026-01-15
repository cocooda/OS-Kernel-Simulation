import Execution.CPUExSwitchAck;
import Execution.IOExSwitchAck;
import Execution.SwitchAckTracker;
import Kernel.Kernel;
import Kernel.KernelAPI;
import Process.PCB;
import Process.ProcessContext;
import Process.ProcessState;
import Program.ProgramCode;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    public static void playgroundWith2Processes(String enablePrior) {
        try {
            
            Kernel kernel = new Kernel();
            PCB pcb = createProcessByFile("src/script2.txt");
            PCB pcb2 = createProcessByFile("src/script.txt");
            // activate the priority scheduling
            boolean enable = true;
            if (enablePrior.equals("N")) {
                enable = false;
            }
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

    public static void playgroundWith3Processes(String enablePrior) {
    try {
        Kernel kernel = new Kernel();

        // Create 3 independent processes (3 programs)
        PCB pcb1 = createProcessByFile("src/script.txt");
        PCB pcb2 = createProcessByFile("src/script2.txt");
        PCB pcb3 = createProcessByFile("src/script3.txt");
        PCB pcb4 = createProcessByFile("src/script3.txt");
        // Enable priority scheduling
        boolean enable = true;
        if (enablePrior.equals("N")) {
            enable = false;
        } 
        kernel.enablePriorityScheduling(enable);

        // Admit processes to the kernel
        kernel.admitProcess(pcb1);
        kernel.admitProcess(pcb2);
        kernel.admitProcess(pcb3);
        kernel.admitProcess(pcb4);
  
        // Start kernel (scheduler + CPU + IO threads)
        Thread kernelThread = new Thread(kernel::start, "Kernel");
        System.out.println("Start the Simulation (4  Processes)");

        kernelThread.start();
        kernelThread.join();

        System.out.println("Simulation Ended");
    } catch (Exception e) {
        e.printStackTrace();    
    }
}

    public static void playgroundCPUAndIOThreadsOnly() {
    try {
        // 1. Queues connecting CPU and IO
        BlockingQueue<PCB> cpuQueue = new LinkedBlockingQueue<>();
        BlockingQueue<PCB> ioQueue  = new LinkedBlockingQueue<>();

        // 2. Minimal kernel adapter (NO scheduler)
        KernelAPI kernelStub = new KernelAPI() {

            @Override
            public void handleBlocked(PCB pcb) {
                try {
                    pcb.state = ProcessState.BLOCKED;
                    ioQueue.put(pcb);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void handleIOCompletion(PCB pcb) throws InterruptedException {
                pcb.state = ProcessState.READY;
                cpuQueue.put(pcb);
            }

            @Override
            public void handleTermination(PCB pcb) {
                pcb.state = ProcessState.TERMINATED;
                System.out.println("[KernelStub] PID " + pcb.pid + " TERMINATED");
            }

            @Override
            public BlockingQueue<PCB> getReadyQueue() {
                return cpuQueue;
            }

            @Override
            public void onSchedulingEvent(PCB pcb, ProcessState state) {
                pcb.state = state;
            }
        };

        // 3. Start real CPU and IO modules
        System.out.println("Initializing 2 thread the IO-Thead & CPU-Thread");
        Thread cpuThread = new Thread(new CPUExSwitchAck(cpuQueue, kernelStub), "CPUEx");
        Thread ioThread  = new Thread(new IOExSwitchAck(ioQueue, kernelStub), "IOEx");

        try {
            cpuThread.start();
            System.out.println("Start the cpuThread successfully");
        } catch (Exception e) {
            System.out.println("Start the cpuThread fail");
        }
        try {
            ioThread.start();
            System.out.println("Start the IOThread successfully");
        } catch (Exception e) {
            System.out.println("Start the IOThread fail");
        }

        System.out.println("Current thread is running " + Thread.currentThread().getName());

        // 4. Load real script → real instructions
        PCB pcb = createProcessByFile("src/script_re2.txt");
        pcb.state = ProcessState.READY;

        // 5. Kick execution by sending PCB to CPU
        cpuQueue.put(pcb);

        // Let it run for a short demo window
        Thread.sleep(3000);

        cpuThread.interrupt();
        ioThread.interrupt();

        cpuThread.join();
        ioThread.join();

        System.out.println("CPU + IO module simulation ended");
        SwitchAckTracker.recordAndReack(Thread.currentThread());

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("=================================");
            System.out.println(" OS Kernel Simulation Playground ");
            System.out.println("=================================");
            System.out.println("1. Run CPU + IO Threads Only");
            System.out.println("2. Run Kernel with 2 Processes");
            System.out.println("3. Run Kernel with 4 Processes");
            System.out.print("Choose an option (1–3): ");
            int choice = scanner.nextInt();
            System.out.println("Do you want to use priority-based scheduling - enter Y or N");
            String choiceStr = scanner.next();
            switch (choice) {
                case 1:
                    playgroundCPUAndIOThreadsOnly();
                    break;

                case 2:
                    playgroundWith2Processes(choiceStr.toUpperCase());
                    break;

                case 3:
                    playgroundWith3Processes(choiceStr.toUpperCase());
                    break;

                default:
                    System.out.println("Invalid option. Exiting.");
                }
            scanner.close();
            System.out.println("\nSimmulation Finished");
        } catch (Exception e) {
        }
        
       
    }
}
