package com.emmanuel.utils.customTimers;

public abstract class CustomCountDownTimer implements ITimer {
    private final CustomTimer timer;
//    private final long timeOutMillis;
//    private final long intervalMillis;
//    private long millisUntilFinished;

    public CustomCountDownTimer(final long timeOutMillis, final long intervalMillis) {
//        this.timeOutMillis = timeOutMillis;
//        this.intervalMillis = intervalMillis;
//        this.millisUntilFinished = intervalMillis;
        this.timer = new CustomTimer(intervalMillis) {
            @Override
            public void onTick(long timeElapsedMillis) {
                CustomCountDownTimer.this.onTick(timeOutMillis - timeElapsedMillis);
                if(timeElapsedMillis >= timeOutMillis){
                    this.cancel();
                    onFinish();
                }
            }
        };
    }

    @Override
    public void startTimer() {
        timer.startTimer();
    }

    @Override
    public void cancel() {
        timer.cancel();
    }

    public abstract void onTick(long millisUntilFinished);

    public abstract void onFinish();
}
