package com.ki4hdu.ratelimit;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

public class Sample {
    public static void main(String[] args) {
        new Sample().run();
    }

    int counter = 0;

    public void run() {
        RateLimiter rateLimiter = new RateLimiter(1000, 10, 1, TimeUnit.SECONDS);
        for (int i = 0; i < 100; i++) {
            // System.out.println("Submitting task: " + i);
            rateLimiter.execute(new MyData(i));
        }
        while( counter < 99 ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.err.println("Finished");
        rateLimiter.shutdown();
    }

    synchronized int increment() {
        return counter++;
    }

    class MyData implements Runnable {
        int i;

        public MyData(int i) {
            this.i = i;
        }

        public void run() {
            try {
                // Simulate fetching data by waiting for a random amount of time
                // System.out.printf("%s %d\n", System.currentTimeMillis(), i);
                Thread.sleep((long) (Math.random() * 5000));
                System.out.printf("%s\n", System.currentTimeMillis() / 1000);
                increment();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
