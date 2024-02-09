package com.ki4hdu.ratelimit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Sample {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) {
        new Sample().run();
    }

    int counter = 0;
    int threads = 30;

    public void run() {
        RateLimiterArray rateLimiter = new RateLimiterArray(10, 1, TimeUnit.SECONDS);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < threads; i++) {
            // System.out.println("Submitting task: " + i);
            rateLimiter.execute(new MyData(i));
        }
        while( counter < threads ) {
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
                Thread.sleep((long) (Math.random() * 100));
                System.out.printf("%s\n", System.currentTimeMillis()/1000);
                System.err.printf("%s\n", sdf.format(new Date(System.currentTimeMillis())));
                increment();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
