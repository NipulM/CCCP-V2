package com.cb011999.cccp.web.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Central request queue for the SYOS server.
 *
 * Uses the Producer-Consumer pattern:
 * - Servlet threads are PRODUCERS — they put tasks into the queue
 * - The worker thread is the CONSUMER — it takes tasks out and processes them
 *
 * A single worker Thread processes tasks one at a time from a BlockingQueue.
 * BlockingQueue is thread-safe, so multiple servlets can add tasks simultaneously
 * without causing data corruption.
 *
 * This ensures that critical operations like checkout and stock reduction
 * happen one at a time, preventing race conditions.
 */
public class RequestQueue {

    // The queue holds tasks waiting to be processed
    // LinkedBlockingQueue is thread-safe — multiple threads can put/take safely
    private final BlockingQueue<Task> taskQueue;

    // Single worker thread that processes tasks from the queue
    private final Thread workerThread;

    // Flag to control the worker thread's lifecycle
    private volatile boolean running = true;

    private static RequestQueue instance;

    // Counters for monitoring how many tasks have been submitted and processed
    private int totalSubmitted = 0;
    private int totalProcessed = 0;

    // Lock object to safely update counters from multiple threads
    private final Object counterLock = new Object();

    private RequestQueue() {
        this.taskQueue = new LinkedBlockingQueue<>();

        // Create and start the worker thread
        // This thread runs continuously, waiting for tasks in the queue
        this.workerThread = new Thread(this::processQueue, "SYOS-Worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();

        System.out.println("[RequestQueue] Started with worker thread.");
    }

    /**
     * Singleton — one queue for the entire application.
     * synchronized ensures only one instance is created even if multiple
     * servlet threads call this at the same time.
     */
    public static synchronized RequestQueue getInstance() {
        if (instance == null) {
            instance = new RequestQueue();
        }
        return instance;
    }

    /**
     * Submit a task to the queue.
     *
     * The calling servlet thread puts the task in the queue, then waits
     * on the task object until the worker thread has finished processing it.
     *
     * Also increments the submission counter and logs diagnostic info
     * so you can monitor queue depth and throughput in the server logs.
     *
     * @param task The work to be done
     */
    public void submitTask(Task task) {
        try {
            int submitted;
            int queueSize;

            // Increment the submission counter under a lock so concurrent
            // servlet threads don't overwrite each other's counts
            synchronized (counterLock) {
                totalSubmitted++;
                submitted = totalSubmitted;
            }

            // Put the task into the queue — thread-safe operation
            taskQueue.put(task);
            queueSize = taskQueue.size();

            System.out.println("[RequestQueue] Task #" + submitted + " submitted | "
                    + "Queue size: " + queueSize + " | "
                    + "Thread: " + Thread.currentThread().getName());

            // Wait until the worker thread has processed this task
            // synchronized + wait/notify is the basic Java mechanism
            // for one thread to wait for another thread to finish something
            synchronized (task) {
                while (!task.isCompleted()) {
                    task.wait();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setError("Request interrupted");
        }
    }

    /**
     * The worker thread runs this method continuously.
     *
     * taskQueue.take() BLOCKS if the queue is empty — the thread just
     * sleeps until a new task arrives. This is efficient because the
     * thread isn't using CPU while waiting.
     *
     * When a task arrives, the worker processes it and notifies the
     * waiting servlet thread that the result is ready.
     *
     * After each task, logs the outcome (OK or ERROR) and current
     * queue depth so you can spot backlogs or failures at a glance.
     */
    private void processQueue() {
        while (running) {
            try {
                // take() blocks until a task is available
                Task task = taskQueue.take();

                // Snapshot remaining queue depth before processing
                int remaining = taskQueue.size();

                // Process the task
                try {
                	Thread.sleep(2000); 
                    task.execute();
                } catch (Exception e) {
                    task.setError(e.getMessage());
                }

                // Increment the processed counter and capture its value
                int processed;
                synchronized (counterLock) {
                    totalProcessed++;
                    processed = totalProcessed;
                }

                String status = task.hasError() ? "ERROR" : "OK";
                System.out.println("[RequestQueue] Task #" + processed + " processed [" + status + "] | "
                        + "Remaining in queue: " + remaining + " | "
                        + "Total processed: " + processed + "/" + totalSubmitted);

                // Notify the servlet thread that this task is done
                synchronized (task) {
                    task.setCompleted(true);
                    task.notifyAll();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Returns current number of tasks waiting in the queue.
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * Returns the total number of tasks ever submitted to this queue.
     * Safe to call from any thread.
     */
    public int getTotalSubmitted() {
        synchronized (counterLock) {
            return totalSubmitted;
        }
    }

    /**
     * Returns the total number of tasks successfully dequeued and executed
     * (regardless of whether the task itself threw an error).
     * Safe to call from any thread.
     */
    public int getTotalProcessed() {
        synchronized (counterLock) {
            return totalProcessed;
        }
    }

    /**
     * Shut down the worker thread gracefully.
     * Logs final submitted/processed counts so you can confirm no tasks were lost.
     */
    public void shutdown() {
        running = false;
        workerThread.interrupt();
        synchronized (counterLock) {
            System.out.println("[RequestQueue] Shutting down. Total submitted: "
                    + totalSubmitted + " | Total processed: " + totalProcessed);
        }
    }
}