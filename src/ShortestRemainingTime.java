import java.util.*;

// Shortest Remaining Time Next (Preemptive SJF / SRTF)
public class ShortestRemainingTime {

    public static SchedulingResult run(List<Process> input) {
        List<Process> processes = new ArrayList<>();
        for (Process p : input) processes.add(p.copy());

        int n = processes.size();
        List<GanttBlock> gantt = new ArrayList<>();
        int completed = 0;
        int time = 0;

        // Collect all event times (arrivals + final finish time)
        while (completed < n) {
            // Find process with shortest remaining time that has arrived
            Process selected = null;
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    if (selected == null || p.remainingTime < selected.remainingTime
                            || (p.remainingTime == selected.remainingTime && p.arrivalTime < selected.arrivalTime)) {
                        selected = p;
                    }
                }
            }

            if (selected == null) {
                // Find next arrival
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : processes) {
                    if (p.remainingTime > 0) nextArrival = Math.min(nextArrival, p.arrivalTime);
                }
                if (!gantt.isEmpty() && gantt.get(gantt.size() - 1).pid == -1) {
                    gantt.get(gantt.size() - 1).end = nextArrival;
                } else {
                    gantt.add(new GanttBlock(-1, time, nextArrival));
                }
                time = nextArrival;
                continue;
            }

            if (selected.startTime == -1) selected.startTime = time;

            // Run until next arrival or process completes
            int nextEvent = Integer.MAX_VALUE;
            for (Process p : processes) {
                if (p.arrivalTime > time && p.remainingTime > 0)
                    nextEvent = Math.min(nextEvent, p.arrivalTime);
            }
            int runFor = Math.min(selected.remainingTime, nextEvent - time);
            if (runFor <= 0) runFor = 1;

            // Add to gantt — merge consecutive same-process blocks
            if (!gantt.isEmpty() && gantt.get(gantt.size() - 1).pid == selected.pid) {
                gantt.get(gantt.size() - 1).end = time + runFor;
            } else {
                gantt.add(new GanttBlock(selected.pid, time, time + runFor));
            }

            selected.remainingTime -= runFor;
            time += runFor;

            if (selected.remainingTime == 0) {
                selected.finishTime = time;
                selected.turnaroundTime = selected.finishTime - selected.arrivalTime;
                selected.waitingTime = selected.turnaroundTime - selected.burstTime;
                completed++;
            }
        }

        return new SchedulingResult("Shortest Remaining Time Next (SRTN)", processes, gantt);
    }
}
