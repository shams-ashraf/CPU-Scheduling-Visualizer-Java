public class ExecutionSlot {
    Process process;
    int startTime;
    int endTime;

    public ExecutionSlot(Process process, int startTime, int endTime) {
        this.process = process;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}