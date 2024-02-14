package com.ki4hdu.ratelimit;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RateLimiterArray
 */
public class RateLimiterArray {

    private int maxPermits;
    private long timeFrame;
    private Map<Integer, Long> permits = new HashMap<>();
    private Map<Integer, Long> finishedProcesses = new HashMap<>();
    private Map<Integer, Long> synchronizedPermits = Collections.synchronizedMap(permits);
    private Map<Integer, Long> synchronizedFinished = Collections.synchronizedMap(finishedProcesses);
    private ExecutorService executor;

    public RateLimiterArray(int maxThreads, int maxPermits, long timeAmount, TimeUnit timeUnit) {
        this.maxPermits = maxPermits;
        this.timeFrame = timeUnit.toMillis(timeAmount);
        this.executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxPermits; i++) {
            synchronizedPermits.put(i, 0L);
            synchronizedFinished.put(i, 0L);
        }
    }

    public RateLimiterArray(int maxPermits, long timeAmount, TimeUnit timeUnit) {
        this(maxPermits, maxPermits, timeAmount, timeUnit);
    }

    protected void ulockFinished() {
        for (int j = 0; j < synchronizedFinished.size(); j++) {
            if ((synchronizedFinished.get(j) != 0)
                    && (System.currentTimeMillis() - synchronizedFinished.get(j) > timeFrame)) {
                synchronizedPermits.put(j, 0L);
                synchronizedFinished.put(j, 0L);
                    }
        }
    }

    protected int aquirePermit() throws InterruptedException {
        int i = 0;
        while (true) {
            synchronized (synchronizedPermits) {
                if (i == 0) {
                    ulockFinished();
                }
                if (synchronizedPermits.get(i) == 0) {
                    synchronizedPermits.put(i, System.currentTimeMillis());
                    // System.err.printf("Acquire permit %d\n", i);
                    return i;
                }
            }
            i = (i + 1) % maxPermits;
        }
    }

    protected void releasePermit(int i) {
        synchronizedFinished.put(i, System.currentTimeMillis());
        // System.err.printf("Release permit %d\n", i);
    }

    public void execute(Runnable task) {
        executor.execute(new RateLitedRunnable(task, this));
    }

    public void shutdown() {
        System.err.println("Shutdown pending...");
        while (true) {
            ulockFinished();
            Set<Long> values = new HashSet<Long>(synchronizedPermits.values());
            if (values.size() == 1 && values.contains(0L)) {
                System.err.println("Shutting down");
                executor.shutdown();
                System.err.println("Shutdown complete");
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // System.err.printf("Still waiting for shutdown %s\n", values);
            }
        }
    }

    private class RateLitedRunnable implements Runnable {
        private final Runnable task;
        private final RateLimiterArray rateLimiter;

        RateLitedRunnable(Runnable task, RateLimiterArray rateLimiter) {
            this.task = task;
            this.rateLimiter = rateLimiter;
        }

        public void run() {
            int myid = -1;
            try {
                myid = rateLimiter.aquirePermit();
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if( myid != -1) {
                    rateLimiter.releasePermit(myid);
                }
            }
        }
    }
}
