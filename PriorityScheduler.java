import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class PriorityScheduler extends JFrame implements Scheduler {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 500;
    private final List<Process> processes;
    private final int contextSwitchTime;

    private JPanel graphPanel;
    private JTable statsTable;
    private JTextArea statsTextArea;
    private JTextArea executionHistoryTextArea;
    private final List<ExecutionSlot> executionHistory = new ArrayList<>();

    public PriorityScheduler(ArrayList<Process> processes, int contextSwitchTime) {
        this.processes = new ArrayList<>(processes);
        this.contextSwitchTime = contextSwitchTime;

        setTitle("CPU Scheduling Graph");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        add(graphPanel, BorderLayout.CENTER);

        statsTextArea = new JTextArea();
        statsTextArea.setEditable(false);
        statsTextArea.setBackground(Color.LIGHT_GRAY);
        statsTextArea.setText("Schedule Name: Shortest Remaining Time First (SRTF)\nAWT: 0\nATAT: 0");
        JScrollPane statsScroll = new JScrollPane(statsTextArea);
        statsScroll.setPreferredSize(new Dimension(WIDTH / 2, 60));
        add(statsScroll, BorderLayout.SOUTH);

        String[] columnNames = {"Process", "Name", "Priority", "BurstTime", "ArrivalTime"};
        Object[][] data = new Object[processes.size()][5];
        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            data[i] = new Object[]{"P" + p.getId(), p.getName(), p.getPriority(), p.getBurstTime(), p.getArrivalTime()};
        }
        statsTable = new JTable(data, columnNames);

        TableColumnModel columnModel = statsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(100);
        statsTable.setRowHeight(20);

        statsTable.setPreferredScrollableViewportSize(new Dimension(500, 100));
        statsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        executionHistoryTextArea = new JTextArea();
        executionHistoryTextArea.setEditable(false);
        executionHistoryTextArea.setBackground(Color.WHITE);
        executionHistoryTextArea.setText("Execution History:\n");
        JScrollPane historyScroll = new JScrollPane(executionHistoryTextArea);
        historyScroll.setPreferredSize(new Dimension(500, 300));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JScrollPane tableScroll = new JScrollPane(statsTable);
        rightPanel.add(tableScroll);
        rightPanel.add(historyScroll);

        add(rightPanel, BorderLayout.EAST);

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT)); 
        
        JScrollPane scrollPane = new JScrollPane(graphPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER); 
    }

    @Override
    public void execute() {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparingInt(Process::getPriority));

        int currentTime = 0;
        ArrayList<Process> readyQueue = new ArrayList<>();
        
        StringBuilder executionOrder = new StringBuilder();

        while (!processes.isEmpty() || !readyQueue.isEmpty()) {
            while (!processes.isEmpty() && processes.get(0).getArrivalTime() <= currentTime) {
                readyQueue.add(processes.remove(0));
                logEvent("Process P" + readyQueue.get(readyQueue.size() - 1).getId() + " added to ready queue at time " + currentTime);
                 
            }

            if (readyQueue.isEmpty()) {
                if (!processes.isEmpty()) {
                    currentTime = processes.get(0).getArrivalTime();
                }
                continue;
            }

            readyQueue.sort(Comparator.comparingInt(Process::getPriority)
                    .thenComparingInt(Process::getArrivalTime));

            Process currentProcess = readyQueue.remove(0);
            executionOrder.append("P" + currentProcess.getId() + " "); 

            if (currentTime > 0 && currentProcess.getArrivalTime() != currentTime) {
                currentTime += contextSwitchTime;
                logEvent("Context switch at time " + currentTime + " - Switching to process P" + currentProcess.getId());
            }
            logEvent("Starting execution of process P" + currentProcess.getId() + " at time " + (currentTime));
            
            ExecutionSlot slot = new ExecutionSlot(currentProcess, currentTime , currentTime + currentProcess.getRemainingBurstTime());
            executionHistory.add(slot);
            currentTime += currentProcess.getBurstTime();
            currentProcess.setCompletionTime(currentTime);

            currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
            currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
            logEvent("Process P" + currentProcess.getId() + " completed at time " + currentTime);
            graphPanel.repaint();
        }

        logEvent("\nExecution order: " + executionOrder.toString().trim());
    }

    private void logEvent(String event) {
        executionHistoryTextArea.append(event + "\n");
    }

    private void drawGraph(Graphics g) {
        int timeSlotWidth = 30;
        int verticalOffset = 60;
        int processHeight = 50;

        int maxTime = 0;
        for (ExecutionSlot slot : executionHistory) {
            maxTime = Math.max(maxTime, slot.endTime);
        }
        int graphWidth = (maxTime * timeSlotWidth) + 30;

        graphPanel.setPreferredSize(new Dimension(graphWidth, HEIGHT));
        graphPanel.revalidate();

        for (ExecutionSlot slot : executionHistory) {
            int startX = slot.startTime * timeSlotWidth;
            int width = (slot.endTime - slot.startTime) * timeSlotWidth;

            g.setColor(slot.process.getColor());
            g.fillRect(startX, verticalOffset, width, processHeight);

            g.setColor(Color.BLACK);
            String processName = slot.process.getName();
            int textX = startX + width / 2 - g.getFontMetrics().stringWidth(processName) / 2;
            int textY = verticalOffset + processHeight + 15;
            g.drawString(processName, textX, textY);

            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(slot.startTime), startX, verticalOffset - 10);
            g.drawString(String.valueOf(slot.endTime), startX + width, verticalOffset - 10);
        }
    } 

    @Override
    public void updateStatistics(String scheduler, int n, double AWT, double ATAT) {
        statsTextArea.setText("Scheduler Name: " + scheduler + "\nAWT: " + AWT + "\nATAT: " + ATAT);
    }

    @Override
    public void updateExecutionHistory() {
        executionHistoryTextArea.setText(executionHistoryTextArea.getText());
    }

}
