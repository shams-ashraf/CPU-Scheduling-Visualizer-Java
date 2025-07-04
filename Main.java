import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                runProcessInput();
            }
        });
    }

    public static void runProcessInput() {
        Scanner scanner = new Scanner(System.in);

        ArrayList<Process> processes = new ArrayList<>();
        System.out.println("CPU Scheduling Program");
        System.out.println("=======================");
        System.out.println("1. First Come First Serve (FCFS)");
        System.out.println("2. Shortest Job First (SJF)");
        System.out.println("3. Shortest Remaining Time First (SRTF)");
        System.out.println("4. Priority Scheduling");
        System.out.println("5. FCAI Scheduler");
        System.out.print("Enter the number of the scheduling algorithm you want to use: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter the number of processes: ");
        int numberOfProcesses = scanner.nextInt();

        int contextSwitchTime = 0;
        if (choice == 3 || choice == 4) {
            System.out.print("Enter context switching time: ");
            contextSwitchTime = scanner.nextInt();
            if (contextSwitchTime < 0) {
                System.out.println("Invalid context switching time");

            }
        }
        int priority = 0;
        int quantum = 0;

        for (int i = 0; i < numberOfProcesses; i++) {
            System.out.println("Enter details for Process " + (i + 1) + ":");
            System.out.print("ID: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Priority: ");
            priority = scanner.nextInt();

            if (choice == 5) {
                System.out.print("Quantum: ");
                quantum = scanner.nextInt();
            }

            System.out.print("Arrival Time: ");
            int arrivalTime = scanner.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = scanner.nextInt();
            System.out.println("Color for Process " + (i + 1) + ":");
            Color color = JColorChooser.showDialog(null, "Select Color for process" + i, Color.RED);

            processes.add(new Process(id, name, color != null ? color : Color.BLACK, priority, arrivalTime, burstTime, quantum));
        }

        scanner.close();

        Scheduler scheduler;
        String s = " ";
        switch (choice) {
            case 1:
                scheduler = new FCFSScheduler(processes);
                scheduler.setVisible(true);
                s = "FCFS";
                break;
            case 2:
                scheduler = new SJFScheduler(processes);
                scheduler.setVisible(true);
                s = "SJF";
                break;
            case 3:
                scheduler = new SRTFSchedulerGui(processes, contextSwitchTime);
                scheduler.setVisible(true);
                s = "SRTF";
                break;
            case 4:
                scheduler = new PriorityScheduler(processes, contextSwitchTime);
                scheduler.setVisible(true);
                s = "Priority";
                break;
            case 5:
                scheduler = new FCAIScheduler(processes);
                scheduler.setVisible(true);
                s = "FCAI";
                break;

            default:
                System.out.println("Invalid choice. Exiting program.");
                return;
        }

        scheduler.execute();
            System.out.println("+----------+-----------------+------------+-------------------+");
            System.out.println("| Process  | Completion Time | Turnaround Time | Waiting Time |");
            System.out.println("+----------+-----------------+------------+----------------+--+");

            for (Process process : processes) {
                if (process.getWaitingTime() < 0) process.setWaitingTime(0);
                System.out.println("| P" + process.getId() +
                        "              | " + process.getCompletionTime() +
                        "              | " + process.getTurnaroundTime() +
                        "              | " + process.getWaitingTime() + "           |");
            }
            System.out.println("+----------+--------------+------------+----------------+----------+");

        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        for (Process process : processes) {
            totalWaitingTime += process.getWaitingTime();
            totalTurnaroundTime += process.getTurnaroundTime();
        }

        double AWT = (double) totalWaitingTime / processes.size();
        double ATAT = (double) totalTurnaroundTime / processes.size();
        scheduler.updateExecutionHistory();
        scheduler.updateStatistics(s, numberOfProcesses, AWT, ATAT);
    }
}
