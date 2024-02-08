package com.ki4hdu.ratelimit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private int maxPermits;
    private final Semaphore started = new Semaphore(maxPermits);
    private final Semaphore finished = new Semaphore(maxPermits);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ExecutorService executor;

    public RateLimiter(int maxThreads, int maxPermits, long timeAmount, TimeUnit timeUnit) {
        this.maxPermits = maxPermits;
        this.executor = Executors.newFixedThreadPool(maxThreads); 
        scheduler.scheduleAtFixedRate(this::refillPermits, 0, timeAmount, timeUnit);
    }

    public RateLimiter(int maxPermits, long timeAmount, TimeUnit timeUnit) {
        this(maxPermits, maxPermits, timeAmount, timeUnit);
    }

    private void aquirePermit() throws InterruptedException {
        started.acquire();
    }

    private void releasePermit() throws InterruptedException {
        finished.acquire();
    }

    public void execute(Runnable task) {
        executor.execute(new RateLitedRunnable(task, this));
    }

    /**
     * Add the number of finished permits to the number available to be started
     * Reset the finished permits (zero finished this second)
     *
     * This is the only place where started permits are released to ensure no more htan max are started each second
     */
    private void refillPermits() {
        int finishedCount = maxPermits - finished.availablePermits();
        // System.out.printf("%d %d %d %d\n", System.currentTimeMillis(), finishedCount, started.availablePermits(),
        //         finished.availablePermits());
        started.release(finishedCount);
        finished.release(finishedCount);
    }

    public void shutdown() {
        System.err.println("Shutting down");
        scheduler.shutdown();
        executor.shutdown();
        System.err.println("Shutdown complete");
    }

    private class RateLitedRunnable implements Runnable {
        private final Runnable task;
        private final RateLimiter rateLimiter;

        RateLitedRunnable(Runnable task, RateLimiter rateLimiter) {
            this.task = task;
            this.rateLimiter = rateLimiter;
        }

        public void run() {
            try {
                rateLimiter.aquirePermit();
                task.run();
                rateLimiter.releasePermit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

