package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cb011999.cccp.web.concurrency.Task;

/**
 * Tests for the Task class used by the request queue.
 * Verifies task execution, result handling, completion flags, and error handling.
 */
public class TaskTest {

    @Test
    public void testTaskExecutesWork() {
        // Arrange
        final boolean[] executed = {false};
        Task task = new Task(() -> {
            executed[0] = true;
        });

        // Act
        task.execute();

        // Assert
        assertTrue(executed[0]);
    }

    @Test
    public void testTaskStartsNotCompleted() {
        // Arrange
        Task task = new Task(() -> {});

        // Act & Assert
        assertFalse(task.isCompleted());
    }

    @Test
    public void testTaskCompletionFlag() {
        // Arrange
        Task task = new Task(() -> {});

        // Act
        task.setCompleted(true);

        // Assert
        assertTrue(task.isCompleted());
    }

    @Test
    public void testTaskResultStorage() {
        // Arrange
        Task task = new Task(() -> {});

        // Act
        task.setResult(42);

        // Assert
        assertEquals(42, task.getResult());
    }

    @Test
    public void testTaskResultStorageWithString() {
        // Arrange
        Task task = new Task(() -> {});

        // Act
        task.setResult("checkout successful");

        // Assert
        assertEquals("checkout successful", task.getResult());
    }

    @Test
    public void testTaskResultIsNullByDefault() {
        // Arrange
        Task task = new Task(() -> {});

        // Act & Assert
        assertNull(task.getResult());
    }

    @Test
    public void testTaskErrorHandling() {
        // Arrange
        Task task = new Task(() -> {});

        // Act
        task.setError("Something went wrong");

        // Assert
        assertTrue(task.hasError());
        assertEquals("Something went wrong", task.getError());
    }

    @Test
    public void testTaskNoErrorByDefault() {
        // Arrange
        Task task = new Task(() -> {});

        // Act & Assert
        assertFalse(task.hasError());
        assertNull(task.getError());
    }

    @Test
    public void testTaskErrorSetsCompleted() {
        // Arrange
        Task task = new Task(() -> {});

        // Act
        task.setError("Error occurred");

        // Assert — setting an error should also mark the task as completed
        assertTrue(task.isCompleted());
        assertTrue(task.hasError());
    }

    @Test
    public void testTaskExecutesSetsResult() {
        // Arrange — simulate what a synchronized service does
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            int result = 10 + 20;
            holder[0].setResult(result);
        });

        // Act
        holder[0].execute();

        // Assert
        assertEquals(30, holder[0].getResult());
    }
}