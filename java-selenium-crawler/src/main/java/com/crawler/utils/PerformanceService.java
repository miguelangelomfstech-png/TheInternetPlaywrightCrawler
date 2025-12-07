package com.crawler.utils;

/**
 * Service to handle performance measurements using system nanoseconds.
 */
public class PerformanceService {
    private long startTime;
    private long endTime;

    /**
     * Starts the performance timer.
     */
    public void startTimer() {
        this.startTime = System.nanoTime();
    }

    /**
     * Stops the performance timer.
     */
    public void stopTimer() {
        this.endTime = System.nanoTime();
    }

    /**
     * Returns the duration in milliseconds.
     * @return double Duration in ms (with fractional precision)
     */
    public double getDuration() {
        return (this.endTime - this.startTime) / 1_000_000.0;
    }

    /**
     * Resets the timer.
     */
    public void resetTimer() {
        this.startTime = 0;
        this.endTime = 0;
    }
}
