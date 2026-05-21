package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cb011999.cccp.web.concurrency.RequestQueue;
import com.cb011999.cccp.web.concurrency.Task;

/**
 * Tests for the RequestQueue (Producer-Consumer pattern).
 * Verifies task submission, sequential processing, and result delivery.
 */
public class RequestQueueTest {

    @Test
    public void testSingletonInstance() {
        // Act
        RequestQueue queue1 = RequestQueue.getInstance();
        RequestQueue queue2 = RequestQueue.getInstance();

        // Assert — same instance every time
        assertSame(queue1, queue2);
    }

    @Test
    public void testSubmitTaskAndGetResult() {
        // Arrange
        RequestQueue queue = RequestQueue.getInstance();
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            holder[0].setResult(100);
        });

        // Act
        queue.submitTask(holder[0]);

        // Assert — task should be completed with result
        assertTrue(holder[0].isCompleted());
        assertEquals(100, holder[0].getResult());
    }

    @Test
    public void testSubmitTaskMarksCompleted() {
        // Arrange
        RequestQueue queue = RequestQueue.getInstance();
        Task task = new Task(() -> {
            // Do some work but don't set a result
        });

        // Act
        queue.submitTask(task);

        // Assert
        assertTrue(task.isCompleted());
    }

    @Test
    public void testSubmitMultipleTasksProcessedInOrder() {
        // Arrange
        RequestQueue queue = RequestQueue.getInstance();
        final StringBuilder order = new StringBuilder();

        Task task1 = new Task(() -> order.append("A"));
        Task task2 = new Task(() -> order.append("B"));
        Task task3 = new Task(() -> order.append("C"));

        // Act — submit sequentially from the same thread
        queue.submitTask(task1);
        queue.submitTask(task2);
        queue.submitTask(task3);

        // Assert — all completed and processed in order
        assertTrue(task1.isCompleted());
        assertTrue(task2.isCompleted());
        assertTrue(task3.isCompleted());
        assertEquals("ABC", order.toString());
    }

    @Test
    public void testTaskWithExceptionSetsError() {
        // Arrange
        RequestQueue queue = RequestQueue.getInstance();
        Task task = new Task(() -> {
            throw new RuntimeException("Test exception");
        });

        // Act
        queue.submitTask(task);

        // Assert — task should be completed but with an error
        assertTrue(task.isCompleted());
        assertTrue(task.hasError());
        assertEquals("Test exception", task.getError());
    }

    @Test
    public void testQueueSizeIsZeroAfterProcessing() {
        // Arrange
        RequestQueue queue = RequestQueue.getInstance();
        Task task = new Task(() -> {});

        // Act
        queue.submitTask(task);

        // Assert — queue should be empty after task is processed
        assertEquals(0, queue.getQueueSize());
    }

    @Test
    public void testConcurrentSubmissionFromMultipleThreads() throws InterruptedException {
        // Arrange
        RequestQueue queue = RequestQueue.getInstance();
        final int[] counter = {0};
        final Object counterLock = new Object();
        int threadCount = 10;

        Thread[] threads = new Thread[threadCount];

        // Act — submit tasks from multiple threads simultaneously
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                Task task = new Task(() -> {
                    synchronized (counterLock) {
                        counter[0]++;
                    }
                });
                queue.submitTask(task);
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            t.join();
        }

        // Assert — all 10 tasks should have incremented the counter
        assertEquals(threadCount, counter[0]);
    }
}