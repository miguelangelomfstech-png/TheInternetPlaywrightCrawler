/**
 * Service to handle performance measurements for test operations.
 * Allows starting and stopping timers to calculate duration.
 */
export class PerformanceService {
  private startTime: number = 0;
  private endTime: number = 0;

  /**
   * Starts the performance timer.
   */
  startTimer(): void {
    this.startTime = performance.now();
  }

  /**
   * Stops the performance timer.
   */
  stopTimer(): void {
    this.endTime = performance.now();
  }

  /**
   * Returns the duration in milliseconds.
   * @returns number Duration in ms
   */
  getDuration(): number {
    return this.endTime - this.startTime;
  }

  /**
   * Resets the timer values.
   */
  resetTimer(): void {
    this.startTime = 0;
    this.endTime = 0;
  }
}
