import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ScheduleHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers so the browser can call us
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        // Read request body
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            // Parse JSON manually (no external libraries)
            String algorithm = extractStr(body, "\"algorithm\"");
            int quantum = extractInt(body, "\"quantum\"", 2);
            List<Process> processes = parseProcesses(body);

            if (processes.isEmpty()) {
                sendError(exchange, 400, "No processes provided");
                return;
            }

            SchedulingResult result = dispatch(algorithm, processes, quantum);
            String json = toJson(result);

            byte[] resp = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();

        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private SchedulingResult dispatch(String algorithm, List<Process> processes, int quantum) {
        return switch (algorithm) {
            case "FCFS"     -> FCFS.run(processes);
            case "RR"       -> RoundRobin.run(processes, quantum);
            case "SPN"      -> ShortestProcessNext.run(processes);
            case "SRTN"     -> ShortestRemainingTime.run(processes);
            case "PRIORITY" -> PriorityScheduling.run(processes);
            default         -> FCFS.run(processes);
        };
    }

    // ── Simple JSON serialiser ─────────────────────────────────────────────
    private String toJson(SchedulingResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"algorithm\":\"").append(r.algorithm).append("\",");
        sb.append("\"avgWaitingTime\":").append(String.format("%.2f", r.avgWaitingTime)).append(",");
        sb.append("\"avgTurnaroundTime\":").append(String.format("%.2f", r.avgTurnaroundTime)).append(",");

        sb.append("\"processes\":[");
        for (int i = 0; i < r.processes.size(); i++) {
            Process p = r.processes.get(i);
            sb.append("{");
            sb.append("\"pid\":").append(p.pid).append(",");
            sb.append("\"arrivalTime\":").append(p.arrivalTime).append(",");
            sb.append("\"burstTime\":").append(p.burstTime).append(",");
            sb.append("\"priority\":").append(p.priority).append(",");
            sb.append("\"startTime\":").append(p.startTime).append(",");
            sb.append("\"finishTime\":").append(p.finishTime).append(",");
            sb.append("\"waitingTime\":").append(p.waitingTime).append(",");
            sb.append("\"turnaroundTime\":").append(p.turnaroundTime);
            sb.append("}");
            if (i < r.processes.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"gantt\":[");
        for (int i = 0; i < r.gantt.size(); i++) {
            GanttBlock g = r.gantt.get(i);
            sb.append("{");
            sb.append("\"pid\":").append(g.pid).append(",");
            sb.append("\"start\":").append(g.start).append(",");
            sb.append("\"end\":").append(g.end);
            sb.append("}");
            if (i < r.gantt.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // ── Minimal JSON parser (no libraries) ────────────────────────────────
    private List<Process> parseProcesses(String json) {
        List<Process> list = new ArrayList<>();
        int idx = json.indexOf("\"processes\"");
        if (idx == -1) return list;
        int arrStart = json.indexOf('[', idx);
        int arrEnd = json.lastIndexOf(']');
        String arr = json.substring(arrStart + 1, arrEnd);

        // Split objects by "}," pattern
        String[] objs = arr.split("\\},\\s*\\{");
        for (String obj : objs) {
            obj = obj.replaceAll("[{}\\[\\]]", "");
            int pid = extractInt(obj, "\"pid\"", -1);
            int at  = extractInt(obj, "\"arrivalTime\"", 0);
            int bt  = extractInt(obj, "\"burstTime\"", 1);
            int pri = extractInt(obj, "\"priority\"", 1);
            if (pid >= 0) list.add(new Process(pid, at, bt, pri));
        }
        return list;
    }

    private String extractStr(String json, String key) {
        int ki = json.indexOf(key);
        if (ki == -1) return "";
        int ci = json.indexOf(':', ki) + 1;
        int q1 = json.indexOf('"', ci) + 1;
        int q2 = json.indexOf('"', q1);
        return json.substring(q1, q2);
    }

    private int extractInt(String json, String key, int def) {
        int ki = json.indexOf(key);
        if (ki == -1) return def;
        int ci = json.indexOf(':', ki) + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = ci; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '-') sb.append(c);
            else if (sb.length() > 0) break;
        }
        try { return Integer.parseInt(sb.toString()); } catch (Exception e) { return def; }
    }

    private void sendError(HttpExchange ex, int code, String msg) throws IOException {
        String body = "{\"error\":\"" + msg + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }
}
