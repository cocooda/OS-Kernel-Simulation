import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== OS Kernel Simulation ===");
            System.out.println("1. Round Robin Scheduling Correctness");
            System.out.println("2. Priority-Based Scheduling Correctness");
            System.out.println("3. CPU Burst and I/O Blocking Correctness");
            System.out.println("4. Priority Adjustment on I/O Completion");
            System.out.println("5. Two Concurrent Kernel Modules");
            System.out.println("6. Suspend States Demo");
            System.out.println("0. Exit");
            System.out.print("Select: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            if (choice == 0) {
                System.out.println("Exiting simulation.");
                break;
            }

            Kernel kernel = new Kernel();

            switch (choice) {
                case 1 -> useCaseRoundRobin(kernel);
                case 2 -> {
                    kernel.enablePriorityScheduling(true);
                    useCasePriorityScheduling(kernel);
                }
                case 3 -> useCaseCpuIoBlocking(kernel);
                case 4 -> {
                    kernel.enablePriorityScheduling(true);
                    useCasePriorityBoostOnIO(kernel);
                }
                case 5 -> {
                    useCaseTwoKernels();
                    continue; // kernel threads managed internally
                }
                case 6 -> {
                    kernel.enablePriorityScheduling(true);
                    setupSuspendDemo(kernel);
                }
                default -> {
                    System.out.println("Invalid choice");
                    continue;
                }
            }

            // start kernel
            Thread kernelThread = new Thread(kernel::start, "Kernel");
            kernelThread.start();

            System.out.println("\nPress ENTER to stop simulation...");
            sc.nextLine();

            kernel.stop();
            kernelThread.join();

            System.out.println("Simulation stopped. Returning to menu...");
        }

        sc.close();
    }

    // ================= USE CASES =================

    private static void useCaseRoundRobin(Kernel kernel) throws Exception {
        kernel.admitProcess(new PCB(simpleProgram(), 3));
        kernel.admitProcess(new PCB(simpleProgram(), 3));
    }

    private static void useCasePriorityScheduling(Kernel kernel) throws Exception {
        kernel.admitProcess(new PCB(simpleProgram(), 5)); // low priority
        kernel.admitProcess(new PCB(simpleProgram(), 1)); // high priority
    }

    private static void useCaseCpuIoBlocking(Kernel kernel) throws Exception {
        ProgramCode ioHeavy = new ProgramCode(List.of(
                new Instruction(InstructionType.CPU_COMPUTE, 1),
                new Instruction(InstructionType.READ_FILE, 3),
                new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));
        kernel.admitProcess(new PCB(ioHeavy, 3));
    }

    private static void useCasePriorityBoostOnIO(Kernel kernel) throws Exception {
        ProgramCode program = new ProgramCode(List.of(
                new Instruction(InstructionType.READ_FILE, 2),
                new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));
        kernel.admitProcess(new PCB(program, 10));
    }

    private static void useCaseTwoKernels() throws Exception {
        Kernel kernel1 = new Kernel();
        Kernel kernel2 = new Kernel();

        kernel1.admitProcess(new PCB(simpleProgram(), 3));
        kernel2.admitProcess(new PCB(simpleProgram(), 3));

        new Thread(kernel1::start, "Kernel-1").start();
        new Thread(kernel2::start, "Kernel-2").start();

        System.out.println(
        "This demo runs two kernels concurrently.\n" +
        "They cannot be stopped from the menu.\n" +
        "Restart the program to stop them."
        );
    }

    private static void setupSuspendDemo(Kernel kernel) throws Exception {
        ProgramCode longCpu = new ProgramCode(List.of(
                new Instruction(InstructionType.CPU_COMPUTE, 5),
                new Instruction(InstructionType.CPU_COMPUTE, 5)
        ));

        ProgramCode ioTask = new ProgramCode(List.of(
                new Instruction(InstructionType.READ_FILE, 3),
                new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));

        // Fill RAM → force suspension
        kernel.admitProcess(new PCB(longCpu, 10)); // low priority
        kernel.admitProcess(new PCB(longCpu, 9));
        kernel.admitProcess(new PCB(longCpu, 8));

        // High-priority process should cause READY_SUSPENDED / BLOCKED_SUSPENDED
        kernel.admitProcess(new PCB(ioTask, 0));
    }

    private static ProgramCode simpleProgram() {
        return new ProgramCode(List.of(
                new Instruction(InstructionType.CPU_COMPUTE, 2),
                new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));
    }
}
