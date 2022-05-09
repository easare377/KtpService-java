package com.emmanuel.utils.customTimers;

import java.util.Timer;
import java.util.TimerTask;

public abstract class CustomTimer implements ITimer {
    private final Timer timer;
    private final long intervalMillis;
    private final long delay;
    private long timeElapsedMillis;

    public CustomTimer(long intervalMillis) {
        if(intervalMillis == 0)
            throw new IllegalArgumentException("intervalMillis cannot be less than or equals to 0!");
        this.intervalMillis = intervalMillis;
        delay = 0;
        timer = new Timer();
    }

    public CustomTimer(long intervalMillis, long delay) {
        if(intervalMillis == 0)
            throw new IllegalArgumentException("intervalMillis cannot be less than or equals to 0!");
        if(delay < 0)
            throw new IllegalArgumentException("delay cannot be less than or equals to 0!");
        this.intervalMillis = intervalMillis;
        this.delay = delay;
        timer = new Timer();
    }

    public void startTimer() {
        start();
    }

    public void cancel() {
        timer.cancel();
    }

    private void start() {
        TimerTask task;
        task = new TimerTask() {
            @Override
            public void run() {
                if (delay > 0 && timeElapsedMillis == 0)
                    timeElapsedMillis = delay;
                else
                    timeElapsedMillis += intervalMillis;
                onTick(timeElapsedMillis);
            }
        };
        timer.schedule(task, delay, intervalMillis);
    }

    public abstract void onTick(long timeElapsedMillis);
}
