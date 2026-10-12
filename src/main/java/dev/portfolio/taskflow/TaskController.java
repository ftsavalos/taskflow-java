package dev.portfolio.taskflow;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) { this.service = service; }

    @GetMapping
    public List<TaskResponse> list() {
        return service.list().stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable long id) {
        return TaskResponse.from(service.get(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        var task = service.create(request.title());
        return ResponseEntity.created(URI.create("/tasks/" + task.id()))
                .body(TaskResponse.from(task));
    }

    @PostMapping("/{id}/advance")
    public TaskResponse advance(@PathVariable long id) {
        return TaskResponse.from(service.advance(id));
    }
}
