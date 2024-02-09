# Sample Rate Limit app

This is an implementation of a rate limiter that can be used by a client
calling a service which is being rate limited by a server.

RateLimiterArray will allow the execution of maxThreads while not exceeding the
number of maxPermis in the given timeframe (timeAmount of the specified
timeUnit).

## Usage

Instantiate a new RateLimiterArray object with the maxPermits and timeframe.
Call execute() with a Runnable to add items to the queue to be executed with
the given rate limit. Call shutdown() after you have placed all of the tasks
onto the queue - the shutdown will wait until there are no running tasks before
it shuts down the executor.

## Explaining the logic

Given the example of processing 30 items limited to 10 items per second, 10
buckets will be created. When a process starts, it must wait until one of the
buckets is available. Once a bucket is occupied, it will not be released until
one second after the process has finished.

### Why hold the bucket after the process is finished?

If each bucket is held for the time period (1 second in our example) from the
time that the process starts, you could result in multiple processes running
within the same time period. For example, if the first 10 processes all start
at 16:15:05.510, the second batch would be allowed to start at 16:15:06.510. If
the first batch all take 900ms to complete (or to actually execute the inner
call that needs to be rate limited), and the second batch run much vaster,
taking 100ms to complete, this could result in the twice the number of calls to
be made between 16:15:06.410 and 16:15:06.610.

Therefore, the bucket is not released until the time period has passed after
the process has finished.
