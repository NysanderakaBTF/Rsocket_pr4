package org.example;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class Main {
    private final RSocket rSocket;

    public static void main(String[] args) {
        var rSocket = RSocketConnector.create()
                .connect(TcpClientTransport.create("localhost", 7000))
                .block();

        // 1. Request-Response: Получение задачи по ID
        Mono<String> response = rSocket.requestResponse(DefaultPayload.create("1")) // taskId = 1
                .map(payload -> payload.getDataUtf8());
        response.subscribe(task -> System.out.println("Received task: " + task));

        // 2. Request-Stream: Получение потока всех задач
        Flux<String> taskStream = rSocket.requestStream(DefaultPayload.create("Get All Tasks"))
                .map(payload -> payload.getDataUtf8());
        taskStream.subscribe(task -> System.out.println("Task: " + task));

        // 3. Fire-and-Forget: Создание новой задачи
        Mono<Void> fireAndForget = rSocket.fireAndForget(DefaultPayload.create("New Task: Fix bugs"));
        fireAndForget.subscribe(null, Throwable::printStackTrace, () -> System.out.println("Task created"));

        // 4. Channel: Двусторонний обмен задачами. Отправляем несколько описаний задач.
        Flux<String> descriptions = Flux.just("Task 1", "Task 2", "Task 3").delayElements(Duration.ofSeconds(1));
        Flux<String> taskChannel = rSocket.requestChannel(descriptions.map(DefaultPayload::create))
                .map(payload -> payload.getDataUtf8());
        taskChannel.subscribe(task -> System.out.println("Created via channel: " + task));

        rSocket.dispose();
    }
}
