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
    private Map<Integer, Long> permits = new HashMap<>();
    private Map<Integer, Long> synchronizedPermits = Collections.synchronizedMap(permits);
    private ExecutorService executor;

    public RateLimiterArray(int maxThreads, int maxPermits, long timeAmount, TimeUnit timeUnit) {
        this.maxPermits = maxPermits;
        this.executor = Executors.newFixedThreadPool(maxThreads); 
        for (int i = 0; i < maxPermits; i++) {
            synchronizedPermits.put(i, 0L);
        }
    }

    public RateLimiterArray(int maxPermits, long timeAmount, TimeUnit timeUnit) {
        this(maxPermits, maxPermits, timeAmount, timeUnit);
    }

    // public RateLimiterArray(int maxPermits) {
    //     this.maxPermits = maxPermits;
    //     this.executor = Executors.newFixedThreadPool(maxPermits);
    //     for (int i = 0; i < maxPermits; i++) {
    //         synchronizedPermits.put(i, 0L);
    //     }
    // }

    public int aquirePermit() throws InterruptedException {
        int i = 0;
        while (true) {
            if (synchronizedPermits.get(i) == 0) {
                synchronizedPermits.put(i, System.currentTimeMillis());
                return i;
            }
            i = (i + 1) % maxPermits;
        }
    }

    public void releasePermit(int i) {
        while (System.currentTimeMillis() - synchronizedPermits.get(i) < 1000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronizedPermits.put(i, 0L);
    }

    public void execute(Runnable task) {
        executor.execute(new RateLitedRunnable(task, this));
    }

    public void shutdown() {
        System.err.println("Shutdown pending...");
        while(true) {
            Set<Long> values = new HashSet<Long>(synchronizedPermits.values());
            if (values.size() == 1 && values.contains(0L)) {
                System.err.println("Shutting down");
                executor.shutdown();
                System.err.println("Shutdown complete");
                break;
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
            try {
                int myid = rateLimiter.aquirePermit();
                task.run();
                rateLimiter.releasePermit(myid);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
