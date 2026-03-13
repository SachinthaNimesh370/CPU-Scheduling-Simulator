# 🖥️ CPU Scheduling Simulator

An interactive **CPU Scheduling Algorithms Simulator** built for an Operating Systems assignment.  
The **frontend** is built with HTML & CSS, and all **scheduling logic** is implemented in pure **Java** (no external libraries).

---

## 📁 Project Structure

```
scheduler/
├── src/                            # Java source files
│   ├── Process.java                # Process data model
│   ├── GanttBlock.java             # Gantt chart block model
│   ├── SchedulingResult.java       # Result holder (Avg WT, TAT)
│   ├── FCFS.java                   # First Come First Served
│   ├── RoundRobin.java             # Round Robin
│   ├── ShortestProcessNext.java    # Shortest Process Next (SPN/SJF)
│   ├── ShortestRemainingTime.java  # Shortest Remaining Time Next (SRTN)
│   ├── PriorityScheduling.java     # Priority Scheduling
│   ├── ScheduleHandler.java        # HTTP API request handler
│   └── Scheduler.java              # Main HTTP server entry point
├── frontend/
│   ├── index.html                  # UI layout
│   ├── style.css                   # Dark premium styling
│   └── app.js                      # Frontend logic & Gantt rendering
├── bin/                            # Compiled .class files (auto-generated)
└── run.bat                         # One-click build & run script
```

---

## ⚙️ Scheduling Algorithms

| Algorithm | Type | Description |
|---|---|---|
| **FCFS** | Non-preemptive | Executes in order of arrival; simple but prone to convoy effect |
| **Round Robin** | Preemptive | Each process gets a fixed time quantum; ensures fairness |
| **SPN (SJF)** | Non-preemptive | Picks the shortest job next; minimises average waiting time |
| **SRTN** | Preemptive | Preempts if a shorter job arrives; optimal average waiting time |
| **Priority** | Non-preemptive | Executes highest priority first (lower number = higher priority) |

---

## 🚀 How to Run

### Prerequisites
- **Java JDK 11+** must be installed  
  Verify with: `java -version`

### Option 1 — Double-click (Windows)
Double-click **`run.bat`** in the `scheduler/` folder.

### Option 2 — Command Line
```powershell
cd C:\Users\sachi\Desktop\OS\scheduler

# Compile
javac -d bin src\Process.java src\GanttBlock.java src\SchedulingResult.java `
             src\FCFS.java src\RoundRobin.java src\ShortestProcessNext.java `
             src\ShortestRemainingTime.java src\PriorityScheduling.java `
             src\ScheduleHandler.java src\Scheduler.java

# Run
java -cp bin Scheduler
```

Then open your browser at: **http://localhost:8080**

---

## 🖱️ How to Use

1. **Select an algorithm** from the dropdown
2. If **Round Robin** is selected, set the **Time Quantum**
3. **Add processes** (Arrival Time, Burst Time, Priority) or click **Load Sample**
4. Click **▶ Run Scheduler** to see:
   - 📊 **Gantt Chart** — colour-coded timeline
   - 📋 **Process Details** — Start, Finish, Waiting & Turnaround Times
   - 💡 **Analysis** — brief algorithm insight
5. Click **Compare All Algorithms** to benchmark all 5 on the same process set and identify the best one

---

## 📊 Sample Output (5 processes)

| Algorithm | Avg Waiting Time | Avg Turnaround Time |
|---|---|---|
| FCFS | 11.4 ms | 17.0 ms |
| Round Robin (Q=2) | 13.0 ms | 18.6 ms |
| SPN / SJF | 8.2 ms | 13.8 ms |
| **SRTN** ⭐ | **6.6 ms** | **12.2 ms** |
| Priority | 10.6 ms | 16.2 ms |

> **Best for this input:** SRTN — achieves the lowest average waiting time.

---

## 🏗️ Architecture

```
Browser (HTML/CSS/JS)
        │
        │  POST /api/schedule  (JSON)
        ▼
Java HTTP Server (port 8080)
        │
        ├── FCFS.java
        ├── RoundRobin.java
        ├── ShortestProcessNext.java
        ├── ShortestRemainingTime.java
        └── PriorityScheduling.java
```

- The Java server uses **`com.sun.net.httpserver`** (built into the JDK — no Maven/Gradle needed)
- JSON is parsed and serialised **manually** — no external libraries required
- The server also **serves the frontend** static files at `GET /`

---

## 👤 Author

Operating Systems Assignment — Faculty of Computing
