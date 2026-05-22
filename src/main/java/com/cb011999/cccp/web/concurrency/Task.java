package com.cb011999.cccp.web.concurrency;

/**
 * Represents a unit of work to be processed by the request queue.
 * 
 * Each task has:
 * - A Runnable (the actual work to do)
 * - A result object (set after execution, read by the servlet)
 * - A completed flag (used with wait/notify for synchronization)
 * - An error message (if something went wrong)
 * 
 * The servlet creates a task, submits it to the queue, and waits.
 * The worker thread executes it, sets the result, and notifies the servlet.
 */
public class Task {

    private final Runnable work;
    private Object result;
    private boolean completed;
    private String error;

    public Task(Runnable work) {
        this.work = work;
        this.completed = false;
        this.error = null;
    }


    public void execute() {
        work.run();
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.completed = true;
    }

    public boolean hasError() {
        return error != null;
    }
}