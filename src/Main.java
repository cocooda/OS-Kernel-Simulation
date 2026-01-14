import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Execution.CPUEx;
import Execution.IOEx;
import Kernel.Kernel;
import Kernel.KernelAPI;
import Process.PCB;
import Process.ProcessContext;
import Process.ProcessState;
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
        Thread cpuThread = new Thread(new CPUEx(cpuQueue, kernelStub), "CPUEx");
        Thread ioThread  = new Thread(new IOEx(ioQueue, kernelStub), "IOEx");

        System.out.println("Starting CPU and IO modules");

        cpuThread.start();
        ioThread.start();

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

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        playgroundCPUAndIOThreadsOnly();
    }
}
