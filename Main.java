import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== OS Kernel Simulation ===");
        System.out.println("Select Use Case:");
        System.out.println("1. Round Robin Scheduling Correctness");
        System.out.println("2. Priority-Based Scheduling Correctness");
        System.out.println("3. CPU Burst and I/O Blocking Correctness");
        System.out.println("4. Priority Adjustment on I/O Completion");
        System.out.println("5. Two Concurrent Kernel Modules");

        int choice = sc.nextInt();

        switch (choice) {
            case 1 -> useCaseRoundRobin();
            case 2 -> useCasePriorityScheduling();
            case 3 -> useCaseCpuIoBlocking();
            case 4 -> useCasePriorityBoostOnIO();
            case 5 -> useCaseTwoKernels();
            default -> System.out.println("Invalid choice");
        }
    }

    // ================= USE CASES =================

    private static void useCaseRoundRobin() {
        Kernel kernel = new Kernel();

        kernel.readyQueue.add(new PCB(simpleProgram(), 3));
        kernel.readyQueue.add(new PCB(simpleProgram(), 3));

        kernel.readyQueue.forEach(p -> p.state = ProcessState.READY);
        kernel.start();
    }

    private static void useCasePriorityScheduling() {
        Kernel kernel = new Kernel();
        kernel.enablePriorityScheduling(true);

        kernel.readyQueue.add(new PCB(simpleProgram(), 5));
        kernel.readyQueue.add(new PCB(simpleProgram(), 1));

        kernel.readyQueue.forEach(p -> p.state = ProcessState.READY);
        kernel.start();
    }

    private static void useCaseCpuIoBlocking() {
        Kernel kernel = new Kernel();

        ProgramCode ioHeavy = new ProgramCode(List.of(
            new Instruction(InstructionType.CPU_COMPUTE, 1),
            new Instruction(InstructionType.READ_FILE, 3),
            new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));

        kernel.readyQueue.add(new PCB(ioHeavy, 3));
        kernel.readyQueue.forEach(p -> p.state = ProcessState.READY);
        kernel.start();
    }

    private static void useCasePriorityBoostOnIO() {
        Kernel kernel = new Kernel();
        kernel.enablePriorityScheduling(true);

        ProgramCode program = new ProgramCode(List.of(
            new Instruction(InstructionType.READ_FILE, 2),
            new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));

        kernel.readyQueue.add(new PCB(program, 10));
        kernel.readyQueue.forEach(p -> p.state = ProcessState.READY);
        kernel.start();
    }

    private static void useCaseTwoKernels() {
        Kernel kernel1 = new Kernel();
        Kernel kernel2 = new Kernel();

        kernel1.readyQueue.add(new PCB(simpleProgram(), 3));
        kernel2.readyQueue.add(new PCB(simpleProgram(), 3));

        kernel1.readyQueue.forEach(p -> p.state = ProcessState.READY);
        kernel2.readyQueue.forEach(p -> p.state = ProcessState.READY);

        new Thread(kernel1::start, "Kernel-1").start();
        new Thread(kernel2::start, "Kernel-2").start();
    }

    // ================= PROGRAM TEMPLATE =================

    private static ProgramCode simpleProgram() {
        return new ProgramCode(List.of(
            new Instruction(InstructionType.CPU_COMPUTE, 2),
            new Instruction(InstructionType.CPU_COMPUTE, 1)
        ));
    }
}
