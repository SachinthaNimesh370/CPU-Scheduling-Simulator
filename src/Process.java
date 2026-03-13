public class Process {
    public int pid;
    public int arrivalTime;
    public int burstTime;
    public int priority;
    public int remainingTime;

    // Computed results
    public int startTime;
    public int finishTime;
    public int waitingTime;
    public int turnaroundTime;

    public Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.startTime = -1;
        this.finishTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }

    public Process copy() {
        return new Process(pid, arrivalTime, burstTime, priority);
    }
}
