import * as fs from 'fs';
import * as path from 'path';

export interface TestResult {
  url: string;
  name: string;
  status: 'passed' | 'failed';
  duration: number;
  error?: string;
}

/**
 * Service to manage test reporting, including console output (tables, charts) and file storage.
 */
export class ReporterService {
  private results: TestResult[] = [];
  private totalDuration: number = 0;

  /**
   * Adds a test result to the collection.
   * @param result TestResult object
   */
  logResult(result: TestResult): void {
    this.results.push(result);
  }

  /**
   * Sets the total execution time of the test suite.
   * @param duration Total duration in ms
   */
  setTotalDuration(duration: number): void {
    this.totalDuration = duration;
  }

  /**
   * Prints a summary table of the results to the console.
   */
  printSummary(): void {
    console.log('\n--- Test Execution Summary ---');
    console.table(this.results.map(({ url, name, status, duration, error }) => ({
      Name: name,
      Status: status,
      'Duration (ms)': duration.toFixed(2),
      Error: error || 'N/A'
    })));
    console.log(`Total Test Duration: ${this.totalDuration.toFixed(2)} ms`);
  }

  /**
   * Prints a basic ASCII chart of performance to the console.
   */
  printAsciiChart(): void {
    console.log('\n--- Performance Chart (Duration) ---');
    const maxDuration = Math.max(...this.results.map(r => r.duration));
    const scale = 50 / (maxDuration || 1); // Scale to max 50 chars width

    this.results.forEach(r => {
      const barLength = Math.floor(r.duration * scale);
      const bar = '█'.repeat(barLength);
      console.log(`${r.name.padEnd(20)} | ${bar} (${r.duration.toFixed(2)} ms)`);
    });
  }

  /**
   * Saves the results to a JSON file.
   * @param filePath Relative path to save the report
   */
  saveReport(filePath: string): void {
    const absolutePath = path.resolve(process.cwd(), filePath);
    const directory = path.dirname(absolutePath);

    if (!fs.existsSync(directory)) {
      fs.mkdirSync(directory, { recursive: true });
    }

    const reportData = {
      totalDuration: this.totalDuration,
      results: this.results
    };

    fs.writeFileSync(absolutePath, JSON.stringify(reportData, null, 2));
    console.log(`\nMonitor JSON saved to: ${absolutePath}`);
  }

  /**
   * Saves the results to an HTML file.
   * @param filePath Relative path to save the report
   */
  saveHtmlReport(filePath: string): void {
    const absolutePath = path.resolve(process.cwd(), filePath);
    const directory = path.dirname(absolutePath);

    if (!fs.existsSync(directory)) {
      fs.mkdirSync(directory, { recursive: true });
    }

    const htmlContent = `
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Crawler Test Report</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f9; }
          h1 { color: #333; }
          .summary { background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
          table { width: 100%; border-collapse: collapse; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }
          th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
          th { background-color: #4CAF50; color: white; }
          tr:hover { background-color: #f1f1f1; }
          .passed { color: green; font-weight: bold; }
          .failed { color: red; font-weight: bold; }
        </style>
      </head>
      <body>
        <h1>Crawler Execution Report</h1>
        <div class="summary">
          <h2>Summary</h2>
          <p><strong>Total Duration:</strong> ${this.totalDuration.toFixed(2)} ms</p>
          <p><strong>Total Links Processed:</strong> ${this.results.length}</p>
        </div>
        
        <h2>Detailed Results</h2>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>URL</th>
              <th>Status</th>
              <th>Duration (ms)</th>
              <th>Error</th>
            </tr>
          </thead>
          <tbody>
            ${this.results.map(r => `
              <tr>
                <td>${r.name}</td>
                <td><a href="${r.url}" target="_blank">${r.url}</a></td>
                <td class="${r.status}">${r.status.toUpperCase()}</td>
                <td>${r.duration.toFixed(2)}</td>
                <td>${r.error || '-'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </body>
      </html>
    `;

    fs.writeFileSync(absolutePath, htmlContent);
    console.log(`\nHTML Report saved to: ${absolutePath}`);
  }
}
