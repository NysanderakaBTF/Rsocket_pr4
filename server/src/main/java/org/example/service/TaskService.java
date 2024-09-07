package org.example.service;

import org.example.model.Task;
import org.example.repository.TaskRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Mono<Task> createTask(String description) {
        Task task = new Task();
        task.setDescription(description);
        task.setCreatedAt(LocalDateTime.now());
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    public Flux<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Mono<Object> completeTask(Long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setCompleted(true);
                }).map(o -> taskRepository.save(o));
    }
    public Mono<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }
}
