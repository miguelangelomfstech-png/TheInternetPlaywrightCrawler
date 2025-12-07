using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Text;

namespace Crawler.Utils
{
    public class ReporterService
    {
        private List<TestResult> _results = new List<TestResult>();
        private double _totalDuration;

        public void LogResult(string name, string url, string status, double duration, string? error = null)
        {
            _results.Add(new TestResult
            {
                Name = name,
                Url = url,
                Status = status,
                Duration = duration,
                Error = error
            });
        }

        public void SetTotalDuration(double duration)
        {
            _totalDuration = duration;
        }

        public void PrintSummary()
        {
            Console.WriteLine("\n--- Test Execution Summary ---");
            Console.WriteLine(string.Format("{0,-20} | {1,-10} | {2,-10} | {3}", "Name", "Status", "Duration", "Error"));
            Console.WriteLine("----------------------------------------------------------------------");
            foreach (var r in _results)
            {
                Console.WriteLine(string.Format("{0,-20} | {1,-10} | {2,-10:F2} | {3}", 
                    r.Name, r.Status, r.Duration, r.Error ?? "-"));
            }
            Console.WriteLine($"Total Test Duration: {_totalDuration:F2} ms");
        }

        public void PrintAsciiChart()
        {
            Console.WriteLine("\n--- Performance Chart (Duration) ---");
            if (!_results.Any()) return;

            double maxDuration = _results.Max(r => r.Duration);
            double scale = 50.0 / (maxDuration > 0 ? maxDuration : 1);

            foreach (var r in _results)
            {
                int barLength = (int)(r.Duration * scale);
                string bar = new string('█', barLength);
                Console.WriteLine(string.Format("{0,-20} | {1} ({2:F2} ms)", r.Name, bar, r.Duration));
            }
        }

        public void SaveReport(string filePath)
        {
            try
            {
                EnsureDirectory(filePath);
                var reportData = new
                {
                    totalDuration = _totalDuration,
                    results = _results
                };

                var json = JsonSerializer.Serialize(reportData, new JsonSerializerOptions { WriteIndented = true });
                File.WriteAllText(filePath, json);
                Console.WriteLine($"\nJSON Report saved to: {Path.GetFullPath(filePath)}");
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine($"Failed to save JSON report: {ex.Message}");
            }
        }

        public void SaveHtmlReport(string filePath)
        {
            try
            {
                EnsureDirectory(filePath);
                var sb = new StringBuilder();
                sb.Append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Crawler Test Report</title>");
                sb.Append("<style>body{font-family:Arial,sans-serif;margin:20px;background-color:#f4f4f9}h1{color:#333}.summary{background:#fff;padding:15px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);margin-bottom:20px}table{width:100%;border-collapse:collapse;background:#fff;box-shadow:0 2px 4px rgba(0,0,0,0.1);border-radius:8px;overflow:hidden}th,td{padding:12px;text-align:left;border-bottom:1px solid #ddd}th{background-color:#4CAF50;color:white}tr:hover{background-color:#f1f1f1}.passed{color:green;font-weight:bold}.failed{color:red;font-weight:bold}</style></head><body>");
                sb.Append("<h1>Crawler Execution Report</h1>");
                sb.Append($"<div class='summary'><h2>Summary</h2><p><strong>Total Duration:</strong> {_totalDuration:F2} ms</p><p><strong>Total Links Processed:</strong> {_results.Count}</p></div>");
                sb.Append("<h2>Detailed Results</h2><table><thead><tr><th>Name</th><th>URL</th><th>Status</th><th>Duration (ms)</th><th>Error</th></tr></thead><tbody>");

                foreach (var r in _results)
                {
                    sb.Append("<tr>");
                    sb.Append($"<td>{r.Name}</td>");
                    sb.Append($"<td><a href='{r.Url}' target='_blank'>{r.Url}</a></td>");
                    sb.Append($"<td class='{r.Status}'>{r.Status.ToUpper()}</td>");
                    sb.Append($"<td>{r.Duration:F2}</td>");
                    sb.Append($"<td>{r.Error ?? "-"}</td>");
                    sb.Append("</tr>");
                }
                sb.Append("</tbody></table></body></html>");

                File.WriteAllText(filePath, sb.ToString());
                Console.WriteLine($"HTML Report saved to: {Path.GetFullPath(filePath)}");
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine($"Failed to save HTML report: {ex.Message}");
            }
        }

        private void EnsureDirectory(string filePath)
        {
            var directory = Path.GetDirectoryName(filePath);
            if (!string.IsNullOrEmpty(directory) && !Directory.Exists(directory))
            {
                Directory.CreateDirectory(directory);
            }
        }
    }

    public class TestResult
    {
        public string Name { get; set; } = "";
        public string Url { get; set; } = "";
        public string Status { get; set; } = "";
        public double Duration { get; set; }
        public string? Error { get; set; }
    }
}
