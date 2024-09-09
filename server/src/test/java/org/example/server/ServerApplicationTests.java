package org.example.server;

import org.example.server.model.Task;
import org.example.server.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ServerApplicationTests {

    @Autowired
    private RSocketRequester.Builder requesterBuilder;

    private RSocketRequester requester;

    @MockBean
    private TaskService taskService;

    private Task task1;
    private Task task2;

    @BeforeEach
    public void setUp() {
        task1 = new Task();
        task1.setId(1L);
        task1.setDescription("Sample Task 1");

        task2 = new Task();
        task2.setId(2L);
        task2.setDescription("Sample Task 2");

        this.requester = requesterBuilder.tcp("localhost", 7000);

    }

    @Test
    public void testGetTask() {
        when(taskService.getTaskById(1L)).thenReturn(Mono.just(task1));

        requester.route("task/1")
                .data(1L)
                .retrieveMono(Task.class)
                .as(StepVerifier::create)
                .expectNext(task1)
                .verifyComplete();
    }

    @Test
    public void testGetAllTasks() {
        when(taskService.getAllTasks()).thenReturn(Flux.just(task1, task2));

        requester.route("task/all")
                .retrieveFlux(Task.class)
                .as(StepVerifier::create)
                .expectNext(task1, task2)
                .verifyComplete();
    }

    @Test
    public void testCreateTask() {
        when(taskService.createTask("New Task")).thenAnswer(invocation -> {
            Task task = new Task();
            task.setDescription(invocation.getArgument(0));
            return Mono.just(task);
        });


        var task = new Task();

        task.setDescription("kek");
        requester.route("task/new")
                .data(task)
                .retrieveMono(Task.class)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    public void testBatchCreateTasks() {
        when(taskService.createTask("Task 1")).thenReturn(Mono.just(task1));
        when(taskService.createTask("Task 2")).thenReturn(Mono.just(task2));

        requester.route("task/batchcreate")
                .data(Flux.just("Task 1", "Task 2"))
                .retrieveFlux(Task.class)
                .as(StepVerifier::create)
                .expectNext(task1, task2)
                .verifyComplete();
    }
}