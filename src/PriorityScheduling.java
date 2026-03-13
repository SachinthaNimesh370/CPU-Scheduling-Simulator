import java.util.*;

// Non-preemptive Priority Scheduling (lower number = higher priority)
public class PriorityScheduling {

    public static SchedulingResult run(List<Process> input) {
        List<Process> processes = new ArrayList<>();
        for (Process p : input) processes.add(p.copy());

        List<Process> remaining = new ArrayList<>(processes);
        List<GanttBlock> gantt = new ArrayList<>();
        int time = 0;
        int n = processes.size();
        int completed = 0;

        while (completed < n) {
            // Among arrived processes, pick highest priority (lowest priority number)
            Process selected = null;
            for (Process p : remaining) {
                if (p.arrivalTime <= time) {
                    if (selected == null || p.priority < selected.priority
                            || (p.priority == selected.priority && p.arrivalTime < selected.arrivalTime)) {
                        selected = p;
                    }
                }
            }

            if (selected == null) {
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

        return new SchedulingResult("Priority Scheduling (Non-Preemptive)", processes, gantt);
    }
}
