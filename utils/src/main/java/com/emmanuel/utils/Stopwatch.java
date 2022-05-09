package com.emmanuel.utils;

/**
 * Provides a set of methods and properties that you can use to accurately measure elapsed time.
 */
public class Stopwatch {

    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;

    /**
     * Gets the total elapsed time measured by the current instance in milliseconds.
     *
     * @return A read-only long integer representing the total number of milliseconds
     * measured by the current instance.
     */
    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return stopTime - startTime;
    }


    /**
     * Gets the total elapsed time measured by the current instance in seconds.
     *
     * @return A read-only double representing the total number of seconds
     * measured by the current instance.
     */
    public double getElapsedTimeSecs() {
        if (running) {
            return ((System.currentTimeMillis() - startTime) / 1000.0);
        }
        return ((stopTime - startTime) / 1000.0);
    }

    /**
     * Gets a value that indicating the stopwatch instance is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Starts, or resumes, measuring elapsed time for an interval.
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    /**
     * Stops measuring elapsed time for an interval.
     */
    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    /**
     * Stops time interval measurement and resets the elapsed time to zero.
     */
    public void reset() {
        stop();
        startTime = 0;
        stopTime = 0;
    }

    /**
     * Stops time interval measurement, resets the elapsed time to zero, and starts measuring elapsed time.
     */
    public void restart() {
        reset();
        start();
    }
}
