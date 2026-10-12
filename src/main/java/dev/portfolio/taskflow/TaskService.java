package dev.portfolio.taskflow;

import java.util.*;

/** Small in-memory domain service. Synchronization makes transitions atomic. */
public final class TaskService {
    public enum Status { TODO, IN_PROGRESS, DONE }
    public record Task(long id, String title, Status status) {}
    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private long sequence;

    public synchronized Task create(String title) {
        if (title == null || title.isBlank() || title.strip().length() > 120)
            throw new IllegalArgumentException("Title must contain 1–120 characters");
        Task task = new Task(++sequence, title.strip(), Status.TODO);
        tasks.put(task.id(), task);
        return task;
    }

    public synchronized List<Task> list() { return List.copyOf(tasks.values()); }

    public synchronized Task get(long id) {
        Task task = tasks.get(id);
        if (task == null) throw new NoSuchElementException("Task not found");
        return task;
    }

    public synchronized Task advance(long id) {
        Task current = get(id);
        Status next = switch (current.status()) {
            case TODO -> Status.IN_PROGRESS;
            case IN_PROGRESS -> Status.DONE;
            case DONE -> throw new IllegalStateException("Completed tasks cannot advance");
        };
        Task updated = new Task(id, current.title(), next);
        tasks.put(id, updated);
        return updated;
    }
}
