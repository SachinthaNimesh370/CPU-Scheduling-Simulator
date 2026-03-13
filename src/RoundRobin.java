import java.util.*;

public class RoundRobin {

    public static SchedulingResult run(List<Process> input, int quantum) {
        List<Process> processes = new ArrayList<>();
        for (Process p : input) processes.add(p.copy());
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        List<GanttBlock> gantt = new ArrayList<>();
        Queue<Process> readyQueue = new LinkedList<>();
        int time = 0;
        int i = 0; // index into sorted processes

        // Add processes that arrive at time 0
        while (i < processes.size() && processes.get(i).arrivalTime <= time) {
            readyQueue.add(processes.get(i++));
        }

        while (!readyQueue.isEmpty() || i < processes.size()) {
            if (readyQueue.isEmpty()) {
                // CPU is idle, jump to next arrival
                int nextArrival = processes.get(i).arrivalTime;
                gantt.add(new GanttBlock(-1, time, nextArrival));
                time = nextArrival;
                while (i < processes.size() && processes.get(i).arrivalTime <= time) {
                    readyQueue.add(processes.get(i++));
                }
                continue;
            }

            Process p = readyQueue.poll();
            if (p.startTime == -1) p.startTime = time;

            int runFor = Math.min(quantum, p.remainingTime);
            gantt.add(new GanttBlock(p.pid, time, time + runFor));
            time += runFor;
            p.remainingTime -= runFor;

            // Enqueue any newly arrived processes
            while (i < processes.size() && processes.get(i).arrivalTime <= time) {
                readyQueue.add(processes.get(i++));
            }

            if (p.remainingTime > 0) {
                readyQueue.add(p); // put back
            } else {
                p.finishTime = time;
                p.turnaroundTime = p.finishTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
            }
        }

        return new SchedulingResult("Round Robin (Quantum=" + quantum + ")", processes, gantt);
    }
}
