import java.util.List;

public class SchedulingResult {
    public List<Process> processes;
    public List<GanttBlock> gantt;
    public double avgWaitingTime;
    public double avgTurnaroundTime;
    public String algorithm;

    public SchedulingResult(String algorithm, List<Process> processes, List<GanttBlock> gantt) {
        this.algorithm = algorithm;
        this.processes = processes;
        this.gantt = gantt;

        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        this.avgWaitingTime = totalWT / processes.size();
        this.avgTurnaroundTime = totalTAT / processes.size();
    }
}
