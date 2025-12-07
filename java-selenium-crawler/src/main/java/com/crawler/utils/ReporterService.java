package com.crawler.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ReporterService {
    private List<Map<String, Object>> results = new ArrayList<>();
    private double totalDuration = 0;

    public void logResult(String name, String url, String status, double duration, String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("url", url);
        result.put("status", status);
        result.put("duration", duration);
        if (error != null) {
            result.put("error", error);
        }
        results.add(result);
    }

    public void setTotalDuration(double duration) {
        this.totalDuration = duration;
    }

    public void printSummary() {
        System.out.println("\n--- Test Execution Summary ---");
        System.out.println(String.format("%-20s | %-10s | %-10s | %s", "Name", "Status", "Duration", "Error"));
        System.out.println("----------------------------------------------------------------------");
        for (Map<String, Object> r : results) {
            System.out.println(String.format("%-20s | %-10s | %-10.2f | %s",
                    r.get("name"), r.get("status"), r.get("duration"), r.getOrDefault("error", "-")));
        }
        System.out.println("Total Test Duration: " + String.format("%.2f", totalDuration) + " ms");
    }

    public void printAsciiChart() {
        System.out.println("\n--- Performance Chart (Duration) ---");
        double maxDuration = results.stream().mapToDouble(r -> (double) r.get("duration")).max().orElse(1.0);
        double scale = 50.0 / maxDuration;

        for (Map<String, Object> r : results) {
            double duration = (double) r.get("duration");
            int barLength = (int) (duration * scale);
            String bar = "█".repeat(barLength);
            System.out.println(String.format("%-20s | %s (%.2f ms)", r.get("name"), bar, duration));
        }
    }

    public void saveReport(String filePath) {
        try {
            ensureDirectory(filePath);
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("totalDuration", totalDuration);
            reportData.put("results", results);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), reportData);
            System.out.println("\nJSON Report saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save JSON report: " + e.getMessage());
        }
    }

    public void saveHtmlReport(String filePath) {
        try {
            ensureDirectory(filePath);
            StringBuilder html = new StringBuilder();
            html.append(
                    "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Crawler Test Report</title>");
            html.append(
                    "<style>body{font-family:Arial,sans-serif;margin:20px;background-color:#f4f4f9}h1{color:#333}.summary{background:#fff;padding:15px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);margin-bottom:20px}table{width:100%;border-collapse:collapse;background:#fff;box-shadow:0 2px 4px rgba(0,0,0,0.1);border-radius:8px;overflow:hidden}th,td{padding:12px;text-align:left;border-bottom:1px solid #ddd}th{background-color:#4CAF50;color:white}tr:hover{background-color:#f1f1f1}.passed{color:green;font-weight:bold}.failed{color:red;font-weight:bold}</style></head><body>");
            html.append("<h1>Crawler Execution Report</h1>");
            html.append(String.format(
                    "<div class='summary'><h2>Summary</h2><p><strong>Total Duration:</strong> %.2f ms</p><p><strong>Total Links Processed:</strong> %d</p></div>",
                    totalDuration, results.size()));
            html.append(
                    "<h2>Detailed Results</h2><table><thead><tr><th>Name</th><th>URL</th><th>Status</th><th>Duration (ms)</th><th>Error</th></tr></thead><tbody>");

            for (Map<String, Object> r : results) {
                html.append("<tr>");
                html.append("<td>").append(r.get("name")).append("</td>");
                html.append("<td><a href='").append(r.get("url")).append("' target='_blank'>").append(r.get("url"))
                        .append("</a></td>");
                html.append("<td class='").append(r.get("status")).append("'>")
                        .append(((String) r.get("status")).toUpperCase()).append("</td>");
                html.append("<td>").append(String.format("%.2f", r.get("duration"))).append("</td>");
                html.append("<td>").append(r.getOrDefault("error", "-")).append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table></body></html>");

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(html.toString());
                System.out.println("HTML Report saved to: " + filePath);
            }

        } catch (IOException e) {
            System.err.println("Failed to save HTML report: " + e.getMessage());
        }
    }

    private void ensureDirectory(String filePath) throws IOException {
        Path path = Paths.get(filePath).getParent();
        if (path != null && !Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
