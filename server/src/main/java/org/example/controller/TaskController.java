package org.example.controller;

import org.example.model.Task;
import org.example.service.TaskService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Controller
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. Request-Response: Получение задачи по ID
    @MessageMapping("request-response")
    public Mono<Task> getTaskById(Long taskId) {
        return taskService.getTaskById(taskId);
    }

    // 2. Request-Stream: Получение всех задач в потоке
    @MessageMapping("request-stream")
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // 3. Fire-and-Forget: Создание новой задачи (не возвращает ответ)
    @MessageMapping("fire-and-forget")
    public Mono<Void> createTask(Long taskId) {
        taskService.completeTask(taskId);
        return Mono.empty();
    }

    // 4. Channel: Двусторонний обмен данными. Получаем поток описаний задач и создаем их.
    @MessageMapping("channel")
    public Flux<Task> taskChannel(Flux<String> descriptions) {
        return descriptions.flatMap(taskService::createTask);
    }
}
