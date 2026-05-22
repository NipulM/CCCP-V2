package com.cb011999.cccp.web.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Central request queue for the SYOS server.
 *
 * Uses the Producer-Consumer pattern:
 * - Servlet threads are PRODUCERS — they put tasks into the queue
 * - The worker thread is the CONSUMER — it takes tasks out and processes them
 */
public class RequestQueue {
    private final BlockingQueue<Task> taskQueue;
    private final Thread workerThread;
    private volatile boolean running = true;

    private static RequestQueue instance;

    private int totalSubmitted = 0;
    private int totalProcessed = 0;

    private final Object counterLock = new Object();

    private RequestQueue() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workerThread = new Thread(this::processQueue, "SYOS-Worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();

        System.out.println("[RequestQueue] Started with worker thread.");
    }
    public static synchronized RequestQueue getInstance() {
        if (instance == null) {
            instance = new RequestQueue();
        }
        return instance;
    }

    public void submitTask(Task task) {
        try {
            int submitted;
            int queueSize;

            synchronized (counterLock) {
                totalSubmitted++;
                submitted = totalSubmitted;
            }

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