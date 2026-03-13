import java.util.*;

public class FCFS {

    public static SchedulingResult run(List<Process> input) {
        // Deep copy processes and sort by arrival time
        List<Process> processes = new ArrayList<>();
        for (Process p : input) processes.add(p.copy());
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        List<GanttBlock> gantt = new ArrayList<>();
        int time = 0;

        for (Process p : processes) {
            if (time < p.arrivalTime) {
                gantt.add(new GanttBlock(-1, time, p.arrivalTime)); // idle
                time = p.arrivalTime;
            }
            p.startTime = time;
            p.waitingTime = time - p.arrivalTime;
            time += p.burstTime;
            p.finishTime = time;
            p.turnaroundTime = p.finishTime - p.arrivalTime;
            gantt.add(new GanttBlock(p.pid, p.startTime, p.finishTime));
        }

        return new SchedulingResult("First Come First Served (FCFS)", processes, gantt);
    }
}
