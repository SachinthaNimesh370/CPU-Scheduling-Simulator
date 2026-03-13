import java.util.*;

// Shortest Process Next (Non-preemptive Shortest Job First)
public class ShortestProcessNext {

    public static SchedulingResult run(List<Process> input) {
        List<Process> processes = new ArrayList<>();
        for (Process p : input) processes.add(p.copy());

        List<Process> remaining = new ArrayList<>(processes);
        List<GanttBlock> gantt = new ArrayList<>();
        int time = 0;
        int completed = 0;
        int n = processes.size();

        while (completed < n) {
            // Find process with shortest burst that has arrived and not yet started
            Process selected = null;
            for (Process p : remaining) {
                if (p.arrivalTime <= time) {
                    if (selected == null || p.burstTime < selected.burstTime
                            || (p.burstTime == selected.burstTime && p.arrivalTime < selected.arrivalTime)) {
                        selected = p;
                    }
                }
            }

            if (selected == null) {
                // CPU idle — jump to next arrival
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : remaining) nextArrival = Math.min(nextArrival, p.arrivalTime);
                gantt.add(new GanttBlock(-1, time, nextArrival));
                time = nextArrival;
                continue;
            }

            remaining.remove(selected);
            selected.startTime = time;
            selected.waitingTime = time - selected.arrivalTime;
            time += selected.burstTime;
            selected.finishTime = time;
            selected.turnaroundTime = selected.finishTime - selected.arrivalTime;
            gantt.add(new GanttBlock(selected.pid, selected.startTime, selected.finishTime));
            completed++;
        }

        return new SchedulingResult("Shortest Process Next (SPN/SJF)", processes, gantt);
    }
}
