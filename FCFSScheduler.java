import java.util.ArrayList;
import java.util.Comparator;

public class FCFSScheduler implements Scheduler {
    private final ArrayList<Process> processes;

    public FCFSScheduler(ArrayList<Process> processes) {
        this.processes = processes;
    }

    @Override
    public void execute() {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        int currentTime = 0;

        for(Process process : processes) {
            if(currentTime < process.getArrivalTime()){
                currentTime = process.getArrivalTime();
            }

            currentTime += process.getBurstTime();
            process.setCompletionTime(currentTime);

            process.setTurnaroundTime(process.getCompletionTime() - process.getArrivalTime());

            process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
        }
    }

    @Override
    public void setVisible(boolean b) {

    }

    @Override
    public void updateExecutionHistory() {

    }

    @Override
    public void updateStatistics(String scheduler, int n, double a, double b) {

    }
}
