package com.ki4hdu.ratelimit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Sample {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void main(String[] args) {
        try {
            new Sample().run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    int threads = 15;

    public void run() throws InterruptedException {
        RateLimiterArray rateLimiter = new RateLimiterArray(2, 10, 1, TimeUnit.SECONDS);

        // sleep before we begin adding jobs to run to simulate a rate limter service sitting idle
        Thread.sleep(500);

        // start a bunch of jobs....
        for (int i = 0; i < threads; i++) {
            // System.out.println("Submitting task: " + i);
            rateLimiter.execute(new MyData(i));
            // sleep up to 3 seconds between each job
            // Thread.sleep((long)(Math.random() * 1000));
        }
        System.err.println("Finished");
        rateLimiter.shutdown();
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
                System.out.printf("%s\n", System.currentTimeMillis()/1000); // Print the second that this finishes
                System.err.printf("%s\n", sdf.format(new Date(System.currentTimeMillis())));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
