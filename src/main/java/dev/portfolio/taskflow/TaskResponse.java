package dev.portfolio.taskflow;

public record TaskResponse(long id, String title, TaskService.Status status) {
    static TaskResponse from(TaskService.Task task) {
        return new TaskResponse(task.id(), task.title(), task.status());
    }
}
