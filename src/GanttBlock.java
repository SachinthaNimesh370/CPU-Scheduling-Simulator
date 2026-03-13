public class GanttBlock {
    public int pid;      // -1 for idle
    public int start;
    public int end;

    public GanttBlock(int pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }
}
