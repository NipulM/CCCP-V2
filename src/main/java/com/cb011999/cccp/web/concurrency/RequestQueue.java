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

    private RequestQueue() {
        this.taskQueue = new LinkedBlockingQueue<>();

        // Create and start the worker thread
        // This thread runs continuously, waiting for tasks in the queue
        this.workerThread = new Thread(this::processQueue, "SYOS-Worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();

        System.out.println("SYOS Request Queue started with worker thread.");
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
     * @param task The work to be done
     */
    public void submitTask(Task task) {
        try {
            // Put the task into the queue — thread-safe operation
            taskQueue.put(task);

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
     */
    private void processQueue() {
        while (running) {
            try {
                // take() blocks until a task is available
                Task task = taskQueue.take();

                // Process the task
                try {
                    task.execute();
                } catch (Exception e) {
                    task.setError(e.getMessage());
                }

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
     * Shut down the worker thread gracefully.
     */
    public void shutdown() {
        running = false;
        workerThread.interrupt();
        System.out.println("SYOS Request Queue shut down.");
    }
}