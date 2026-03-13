/* ════════════════════════════════════════════════
   app.js — CPU Scheduling Simulator Frontend Logic
   ════════════════════════════════════════════════ */

const API = 'http://localhost:8080/api/schedule';
let pidCounter = 1;

// ── Initialise ───────────────────────────────────
window.onload = () => {
  loadSample();
  toggleQuantum();
};

// ── Algorithm selector ───────────────────────────
function toggleQuantum() {
  const algo = document.getElementById('algorithm').value;
  document.getElementById('quantum-field').style.display = (algo === 'RR') ? 'block' : 'none';

  // Show/hide priority column
  const isPriority = (algo === 'PRIORITY');
  document.querySelectorAll('.priority-cell').forEach(el => {
    el.style.display = isPriority ? '' : 'none';
  });
  document.getElementById('priority-col').style.display = isPriority ? '' : 'none';
}

// ── Process Table ────────────────────────────────
function addRow(pid, at, bt, pri) {
  const tbody = document.getElementById('process-body');
  const tr = document.createElement('tr');
  const p  = pid ?? pidCounter++;

  tr.innerHTML = `
    <td><strong>P${p}</strong></td>
    <td><input type="number" class="at"  value="${at  ?? 0}" min="0" /></td>
    <td><input type="number" class="bt"  value="${bt  ?? 4}" min="1" /></td>
    <td class="priority-cell" style="display:none">
      <input type="number" class="pri" value="${pri ?? p}" min="1" />
    </td>
    <td><button class="btn btn-delete" onclick="this.closest('tr').remove()">✕</button></td>
  `;

  tbody.appendChild(tr);
  toggleQuantum(); // sync priority column visibility
}

function clearTable() {
  document.getElementById('process-body').innerHTML = '';
  pidCounter = 1;
}

function loadSample() {
  clearTable();
  const sample = [
    [1, 0, 8, 3],
    [2, 1, 4, 1],
    [3, 2, 9, 4],
    [4, 3, 5, 2],
    [5, 4, 2, 5],
  ];
  sample.forEach(([p, at, bt, pri]) => addRow(p, at, bt, pri));
  pidCounter = sample.length + 1;
}

function getProcesses() {
  const rows = document.querySelectorAll('#process-body tr');
  const algo = document.getElementById('algorithm').value;
  const list = [];
  let id = 1;
  for (const row of rows) {
    const at  = parseInt(row.querySelector('.at').value)  || 0;
    const bt  = parseInt(row.querySelector('.bt').value)  || 1;
    const pri = row.querySelector('.pri') ? (parseInt(row.querySelector('.pri').value) || 1) : 1;
    list.push({ pid: id++, arrivalTime: at, burstTime: bt, priority: pri });
  }
  return list;
}

// ── Run Scheduler ────────────────────────────────
async function runScheduler() {
  const processes = getProcesses();
  if (!processes.length) { alert('Please add at least one process.'); return; }

  const algorithm = document.getElementById('algorithm').value;
  const quantum   = parseInt(document.getElementById('quantum').value) || 2;

  const btn = document.getElementById('run-btn');
  btn.classList.add('loading');
  btn.disabled = true;

  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ algorithm, quantum, processes }),
    });

    if (!res.ok) throw new Error('Server error ' + res.status);
    const data = await res.json();
    renderResults(data);
    showCompareSection();
  } catch (e) {
    alert('❌ Could not connect to the Java server.\nMake sure Scheduler.java is running on port 8080.\n\nError: ' + e.message);
  } finally {
    btn.classList.remove('loading');
    btn.disabled = false;
  }
}

// ── Render Results ───────────────────────────────
function renderResults(data) {
  document.getElementById('results').style.display = 'block';
  document.getElementById('result-algo').textContent = data.algorithm;
  document.getElementById('avg-wt').textContent  = data.avgWaitingTime;
  document.getElementById('avg-tat').textContent = data.avgTurnaroundTime;

  renderGantt(data.gantt, data.processes.length);
  renderResultTable(data.processes);
  renderRecommendation(data);

  document.getElementById('results').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ── Gantt Chart ──────────────────────────────────
function renderGantt(gantt, numProcesses) {
  const chart  = document.getElementById('gantt-chart');
  const labels = document.getElementById('gantt-labels');
  chart.innerHTML = '';
  labels.innerHTML = '';

  if (!gantt || gantt.length === 0) return;

  const total = gantt[gantt.length - 1].end - gantt[0].start;
  const SCALE = Math.max(40, Math.min(120, Math.floor(800 / total))); // px per time unit

  gantt.forEach((block, i) => {
    const duration = block.end - block.start;
    const width    = duration * SCALE;

    const div = document.createElement('div');
    div.className = block.pid === -1
      ? 'gantt-block idle'
      : `gantt-block c${block.pid % 10}`;
    div.style.width = width + 'px';
    div.title = block.pid === -1
      ? `Idle (${block.start}–${block.end})`
      : `P${block.pid} (${block.start}–${block.end}, duration=${duration})`;

    if (width > 28) {
      div.textContent = block.pid === -1 ? 'Idle' : `P${block.pid}`;
    }
    chart.appendChild(div);

    // Add time label at start of each block, and final end time
    const lbl = document.createElement('div');
    lbl.className = 'gantt-label';
    lbl.style.width = width + 'px';
    lbl.style.minWidth = width + 'px';
    lbl.textContent = block.start;
    labels.appendChild(lbl);

    if (i === gantt.length - 1) {
      const endLbl = document.createElement('div');
      endLbl.className = 'gantt-label';
      endLbl.style.width = '30px';
      endLbl.textContent = block.end;
      labels.appendChild(endLbl);
    }
  });
}

// ── Results Table ────────────────────────────────
function renderResultTable(processes) {
  const tbody = document.getElementById('result-body');
  tbody.innerHTML = '';

  const minWT  = Math.min(...processes.map(p => p.waitingTime));
  const minTAT = Math.min(...processes.map(p => p.turnaroundTime));

  processes.forEach(p => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>P${p.pid}</strong></td>
      <td>${p.arrivalTime}</td>
      <td>${p.burstTime}</td>
      <td>${p.startTime}</td>
      <td>${p.finishTime}</td>
      <td>${p.waitingTime}</td>
      <td>${p.turnaroundTime}</td>
    `;
    if (p.waitingTime === minWT) tr.classList.add('result-table-highlight');
    tbody.appendChild(tr);
  });
}

// ── Recommendation ───────────────────────────────
function renderRecommendation(data) {
  const box = document.getElementById('recommendation');

  const tips = {
    'FCFS':     'FCFS is simple and fair for batch jobs but suffers from the convoy effect with long bursts.',
    'RR':       'Round Robin ensures fairness and good response times — ideal for time-sharing systems.',
    'SPN':      'Shortest Process Next minimises average waiting time but can starve long processes.',
    'SRTN':     'SRTN achieves the optimal (minimum) average waiting time among non-idle algorithms.',
    'PRIORITY': 'Priority Scheduling is flexible but may starve low-priority processes without aging.',
  };
  const key = Object.keys(tips).find(k => data.algorithm.includes(k)) || 'FCFS';

  box.innerHTML = `
    <strong>💡 Analysis:</strong> ${tips[key]}<br/>
    <span style="color:var(--text-muted); font-size:.82rem;">
      Avg Waiting Time: <strong style="color:var(--yellow)">${data.avgWaitingTime} ms</strong> &nbsp;|&nbsp;
      Avg Turnaround: <strong style="color:var(--accent2)">${data.avgTurnaroundTime} ms</strong>
    </span>
  `;
  box.classList.add('show');
}

// ── Compare All Algorithms ───────────────────────
function showCompareSection() {
  document.getElementById('compare-section').style.display = 'block';
}

async function compareAll() {
  const processes = getProcesses();
  if (!processes.length) { alert('Add some processes first.'); return; }
  const quantum = parseInt(document.getElementById('quantum').value) || 2;

  const algorithms = ['FCFS', 'RR', 'SPN', 'SRTN', 'PRIORITY'];
  const results = [];

  for (const algo of algorithms) {
    try {
      const res = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: algo, quantum, processes }),
      });
      if (res.ok) results.push(await res.json());
    } catch (e) { /* skip */ }
  }

  if (!results.length) { alert('Could not reach the server.'); return; }

  // Best = lowest avg waiting time
  const best = results.reduce((a, b) => a.avgWaitingTime <= b.avgWaitingTime ? a : b);

  const container = document.getElementById('compare-results');
  container.innerHTML = '<div class="compare-grid"></div>';
  const grid = container.querySelector('.compare-grid');

  results.forEach(r => {
    const card = document.createElement('div');
    card.className = 'compare-card' + (r.algorithm === best.algorithm ? ' best' : '');
    card.innerHTML = `
      <h3>${r.algorithm}</h3>
      <div class="compare-metric">Avg Waiting Time <span>${r.avgWaitingTime} ms</span></div>
      <div class="compare-metric">Avg Turnaround Time <span>${r.avgTurnaroundTime} ms</span></div>
    `;
    grid.appendChild(card);
  });

  container.scrollIntoView({ behavior: 'smooth', block: 'start' });
}
