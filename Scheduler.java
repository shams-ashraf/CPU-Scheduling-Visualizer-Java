public interface Scheduler  {
    void execute();

    void setVisible(boolean b);

    void updateExecutionHistory();

    void updateStatistics(String scheduler,int n, double a ,double b);
}
