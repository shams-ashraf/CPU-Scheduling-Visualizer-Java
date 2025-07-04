import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class SJFScheduler extends JFrame implements Scheduler {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 500;
    private final List<Process> processes;
    private JPanel graphPanel;
    private final JTable statsTable;
    private final JTextArea statsTextArea;
    private final JTextArea executionHistoryTextArea;
    private final List<ExecutionSlot> executionHistory = new ArrayList<>();
    private final List<Process> completedProcesses = new ArrayList<>();

    public SJFScheduler(List<Process> processes) {
        this.processes = new ArrayList<>(processes);

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
        int currentTime = 0;
        Queue<Process> readyQueue = new PriorityQueue<>((p1, p2) -> {
            if (p1.getRemainingBurstTime() != p2.getRemainingBurstTime()) {
                return Integer.compare(p1.getRemainingBurstTime(), p2.getRemainingBurstTime());
            }
            if (p1.getArrivalTime() != p2.getArrivalTime()) {
                return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            }
            return Integer.compare(p1.getPriority(), p2.getPriority());
        });

        Process currentProcess = null;
        StringBuilder executionOrder = new StringBuilder();

        while (!processes.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            Iterator<Process> iterator = processes.iterator();
            while (iterator.hasNext()) {
                Process p = iterator.next();
                if (p.getArrivalTime() <= currentTime) {
                    readyQueue.add(p);
                    iterator.remove();
                    logEvent("Process P" + p.getId() + " added to ready queue at time " + currentTime);
                }
            }

            for (Process p : readyQueue) {
                p.setWaitTime(p.getWaitTime() + 1);
                if (p.getWaitTime() >= 5) {
                    p.setRemainingBurstTime(p.getRemainingBurstTime() - 1);
                    p.isAged = true;
                    p.setWaitTime(0);
                    logEvent("Aging applied to process P" + p.getId() + ": Remaining Time reduced to " + p.getRemainingBurstTime());
                    p.setCountAging(p.getCountAging() + 1);
                }
            }

            if (currentProcess == null && !readyQueue.isEmpty()) {
                currentProcess = readyQueue.poll();
                currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() + currentProcess.getCountAging());
                currentProcess.setCountAging(0);
                logEvent("Starting execution of process P" + currentProcess.getId() + " at time " + currentTime);
            }
            if (currentProcess != null) {
                executionHistory.add(new ExecutionSlot(currentProcess, currentTime, currentTime + 1));
                currentProcess.setRemainingBurstTime(currentProcess.getRemainingBurstTime() - 1);
                if (currentProcess.getRemainingBurstTime() == 0) {
                    currentProcess.setCompletionTime(currentTime + 1);
                    currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                    currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                    completedProcesses.add(currentProcess);
                    logEvent("Process P" + currentProcess.getId() + " completed at time " + (currentTime + 1));
                    executionOrder.append("P" + currentProcess.getId() + " ");
                    currentProcess = null;
                }
            }
            currentTime++;
            graphPanel.repaint();
        }

        updateExecutionHistory();

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
        statsTextArea.setText("Scheduler Name:" + scheduler + "\nAWT: " + AWT + "\nATAT: " + ATAT);
    }

    @Override
    public void updateExecutionHistory() {
        executionHistoryTextArea.setText(executionHistoryTextArea.getText());
    }
}
