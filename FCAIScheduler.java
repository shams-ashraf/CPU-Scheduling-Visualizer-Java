import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

public class FCAIScheduler extends JFrame implements Scheduler {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 500;
    private final List<Process> processes;

    private JPanel graphPanel;
    private JTable statsTable;
    private JTextArea statsTextArea;
    private JTextArea executionHistoryTextArea;
    private final List<ExecutionSlot> executionHistory = new ArrayList<>();

    public FCAIScheduler(List<Process> processes) {
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
        statsTextArea.setText("Schedule Name:FCAI \nAWT: 0\nATAT: 0");
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
        logEvent("Execution started.");
        
        int n = processes.size();
        StringBuilder executionOrder = new StringBuilder(); 
        
        int lastArrivalTime = processes.stream().mapToInt(Process::getArrivalTime).max().orElse(0);
        int maxBurstTime = processes.stream().mapToInt(Process::getBurstTime).max().orElse(0);
        double V1 = lastArrivalTime / 10.0;
        double V2 = maxBurstTime / 10.0;

        for (int i = 0; i < n; i++) {
            processes.get(i).updateFcaiFactor(V1, V2);
            logEvent("Updated FCAI factor for process P" + processes.get(i).getId() + ". FCAI Factor: " + processes.get(i).getFcaiFactor());
        }

        Process currentProcess = processes.get(0);
        Deque<Process> dq = new ArrayDeque<>();
        int currentTime = processes.get(0).getArrivalTime();

        dq.push(currentProcess);
        processes.remove(currentProcess);
        int all_done = 0;

        while (all_done != n) {
            while (all_done != n) {
                if (dq.isEmpty()) {
                    dq.addFirst(processes.get(0));
                    processes.remove(0);
                    logEvent("Process P" + dq.getFirst().getId() + " added to ready queue at time " + currentTime);
                }

                logEvent("Process P" + dq.getFirst().getId() + " started execution at time " + currentTime);

                int old_quan = dq.getFirst().getQuantum();
                int exc = (int) Math.ceil(0.4 * dq.getFirst().getQuantum());
                exc = Math.min(exc, dq.getFirst().getRemainingBurstTime());
                dq.getFirst().setRemainingBurstTime(dq.getFirst().getRemainingBurstTime() - exc);
                currentTime += exc;
                // executionHistory.add(new ExecutionSlot(dq.getFirst(), currentTime - exc, currentTime));  

                executionOrder.append("P").append(dq.getFirst().getId()).append(" "); 
                logEvent("Current Execution Order: " + executionOrder.toString().trim());

                int unused = dq.getFirst().getQuantum() - exc;

                if (dq.getFirst().getRemainingBurstTime() <= 0) {
                    logRemainingBurstTime(dq.getFirst()); 
                    executionHistory.add(new ExecutionSlot(dq.getFirst(), currentTime - exc, currentTime)); 
                    currentProcess = dq.getFirst();
                    currentProcess.setCompletionTime(currentTime);
                    currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                    currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                    
                    logEvent("Process P" + dq.getFirst().getId() + " completed at time " + currentTime);
                    logEvent("Process P" + dq.getFirst().getId() + " Waiting Time: " + currentProcess.getWaitingTime());
                    logEvent("Process P" + dq.getFirst().getId() + " Turnaround Time: " + currentProcess.getTurnaroundTime());

                    all_done++;
                    dq.remove(dq.getFirst());
                    break;
                }

                boolean isPrem = false;

                Process canPreempt = RR(dq, (int) dq.getFirst().getFcaiFactor());
                if (canPreempt != null) {
                    logRemainingBurstTime(dq.getFirst()); 
                    executionHistory.add(new ExecutionSlot(dq.getFirst(), currentTime - exc, currentTime));  
                    logEvent("Preemption occurred at time " + currentTime + " - Process P" + dq.getFirst().getId() + " is preempted by P" + canPreempt.getId());
                    dq.getFirst().updateFcaiFactor(V1, V2);
                    dq.getFirst().setQuantum(old_quan + unused);
                    logEvent("Updated quantum for process P" + dq.getFirst().getId() + " to " + dq.getFirst().getQuantum());
                    logEvent("Updated FCAI factor for process P" + dq.getFirst().getId() + " after preemption. FCAI Factor: " + dq.getFirst().getFcaiFactor());
                    Process temp = dq.getFirst();
                    dq.remove(canPreempt);
                    dq.remove(dq.getFirst());
                    dq.addFirst(canPreempt);
                    dq.addLast(temp);
                } else {
                    while (unused > 0 && dq.getFirst().getRemainingBurstTime() > 0) {
                        dq.getFirst().setRemainingBurstTime(dq.getFirst().getRemainingBurstTime() - 1);
                        logRemainingBurstTime(dq.getFirst()); 
                        executionHistory.add(new ExecutionSlot(dq.getFirst(), currentTime - exc , currentTime + 1));  
                        unused--;
                        currentTime++;
                        Process add = add(currentTime);
                        if (add != null) {
                            dq.addLast(add);
                            Process temp = RR(dq, (int) dq.getFirst().getFcaiFactor());
                            if (temp != null) {

                                Process temp2 = dq.getFirst();
                                dq.remove(temp);
                                dq.remove(dq.getFirst());
                                dq.addFirst(temp);
                                dq.addLast(temp2);
                                isPrem = true;
                                break;
                            }
                        }
                    }
                    if (dq.getFirst().getRemainingBurstTime() <= 0) {
                        currentProcess = dq.getFirst();
                        currentProcess.setCompletionTime(currentTime);
                        currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                        currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
    
                        logEvent("Process P" + dq.getFirst().getId() + " completed at time " + currentTime);
                        logEvent("Process P" + dq.getFirst().getId() + " Waiting Time: " + currentProcess.getWaitingTime());
                        logEvent("Process P" + dq.getFirst().getId() + " Turnaround Time: " + currentProcess.getTurnaroundTime());
    
                        all_done++;
                        dq.remove(dq.getFirst());
                        break;
                    }
    
                    if (unused == 0) {
                        if (!isPrem) {
                           logEvent("Process P" + dq.getFirst().getId() + " finished its quantum, added back to the queue with new quantum");
                            dq.addLast(dq.getFirst());
                            dq.remove(dq.getFirst());
                        }
                        dq.getLast().setQuantum(dq.getLast().getQuantum() + 2);
                        logEvent("Updated quantum for process P" + dq.getLast().getId() + " to " + dq.getLast().getQuantum());
                    } else {
                        dq.getLast().setQuantum(dq.getLast().getQuantum() + unused);
                        logEvent("Updated quantum for process P" + dq.getLast().getId() + " to " + dq.getLast().getQuantum());
                    }
                    dq.getLast().updateFcaiFactor(V1, V2);
                    logEvent("Updated FCAI factor for process P" + dq.getLast().getId() + ". FCAI Factor: " + dq.getLast().getFcaiFactor());
                }
            }
            graphPanel.repaint();
        }
        updateExecutionHistory();
        logEvent("Execution completed at time " + currentTime);
    }

    public Process add(int time) {
        if (processes.size() > 0 && processes.get(0).getArrivalTime() <= time) {
            Process ret = processes.get(0);
            processes.remove(0);
            return ret;
        } else {
            return null;
        }
    }

    public Process RR(Deque<Process> dq, int factor) {
        int minFactor = (int) 1e9;
        Process ret = null;
        for (Process p : dq) {
            if (p.getFcaiFactor() < factor) {
                if (p.getFcaiFactor() < minFactor) {
                    ret = p;
                    minFactor = (int) p.getFcaiFactor();
                }
            }
        }
        return ret;
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

    private void logEvent(String event) {
        executionHistoryTextArea.append(event + "\n");
        System.out.println(event); 
    }

    private void logRemainingBurstTime(Process process) {
        logEvent("Process P" + process.getId() + " Remaining Burst Time: " + process.getRemainingBurstTime());
    }
}
