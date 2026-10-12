package dev.portfolio.taskflow;

import java.util.NoSuchElementException;
import java.util.concurrent.*;

/** Run with -ea. No test-library download required. */
public final class TaskServiceTest {
    public static void main(String[] args) throws Exception {
        TaskService service = new TaskService();
        var task = service.create("  Learn Java  ");
        assert task.title().equals("Learn Java");
        assert task.status() == TaskService.Status.TODO;
        var snapshot = service.list();
        assert service.advance(task.id()).status() == TaskService.Status.IN_PROGRESS;
        assert service.advance(task.id()).status() == TaskService.Status.DONE;
        assert snapshot.get(0).status() == TaskService.Status.TODO;
        expect(IllegalStateException.class, () -> service.advance(task.id()));
        expect(NoSuchElementException.class, () -> service.advance(999));
        expect(IllegalArgumentException.class, () -> service.create(" "));
        expect(IllegalArgumentException.class, () -> service.create(null));
        expect(IllegalArgumentException.class, () -> service.create("x".repeat(121)));
        assert Application.quote("a\n\"\\").equals("\"a\\u000a\\\"\\\\\"");
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            var futures = new java.util.ArrayList<Future<?>>();
            for (int i = 0; i < 100; i++) futures.add(pool.submit(() -> service.create("Concurrent")));
            for (var future : futures) future.get();
        } finally { pool.shutdown(); }
        assert service.list().size() == 101;
        assert service.list().stream().map(TaskService.Task::id).distinct().count() == 101;
        System.out.println("All domain tests passed");
    }
    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try { action.run(); throw new AssertionError("Expected " + type.getSimpleName()); }
        catch (RuntimeException exception) {
            if (!type.isInstance(exception)) throw exception;
        }
    }
}
