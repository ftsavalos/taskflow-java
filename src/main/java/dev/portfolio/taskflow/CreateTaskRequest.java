package dev.portfolio.taskflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must contain at most 120 characters")
        String title) {
    public CreateTaskRequest {
        if (title != null) title = title.strip();
    }
}
