import java.awt.Color;

public class Process {
    private final int id;
    private final String name;
    private final Color color;
    private final int priority;
    private final int arrivalTime;
    private final int burstTime;
    private int waitTime;
    
    private int remainingBurstTime;
    private int quantum;
    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;
    private int countAging = 0;
    private double fcaiFactor;
    public boolean isCompleted=false;
    boolean isAged = false; //TODO

    // Constructor
    public Process(int id, String name, Color color, int priority, int arrivalTime, int burstTime, int quantum) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingBurstTime = burstTime;
        this.quantum = quantum;
        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
        this.fcaiFactor = 0.0;
        this.waitTime = 0;
        this.color = color;
        this.isCompleted = false;
    }

    ///////////////////////////////////////////////////////Setters///////////////////////////////////////////////////////
    public void setRemainingBurstTime(int remainingBurstTime) {
        this.remainingBurstTime = remainingBurstTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setFcaiFactor(double fcaiFactor) {
        this.fcaiFactor = fcaiFactor;
    }

    public void setCountAging(int countAging) {
        this.countAging = countAging;
    }
    public void updateFcaiFactor(double v1, double v2) {
        this.fcaiFactor = (10 - priority) + Math.ceil(arrivalTime / v1) + Math.ceil(remainingBurstTime / v2);
    }
    public void setCompleted(boolean Flag) {
        this.isCompleted = Flag;
    } //TODO

    ///////////////////////////////////////////////////////Getters///////////////////////////////////////////////////////
    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public int getQuantum() {
        return quantum;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public double getFcaiFactor() {
        return fcaiFactor;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getCountAging() {
        return countAging;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }
}
