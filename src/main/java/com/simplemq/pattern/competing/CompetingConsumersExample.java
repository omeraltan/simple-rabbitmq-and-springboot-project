package com.simplemq.pattern.competing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CompetingConsumersExample {

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentLinkedDeque<String> workerA = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String> workerB = new ConcurrentLinkedDeque<>();

    public CompetingConsumersExample(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publishes one work item onto the shared queue. */
    public void publish(String text) {
        rabbitTemplate.convertAndSend(
                CompetingConfig.EXCHANGE,
                CompetingConfig.ROUTING_KEY,
                new CompetingMessage(text));
    }

    /** Takes a message for the first worker. The other worker does not see this copy. */
    @RabbitListener(queues = CompetingConfig.QUEUE)
    public void onWorkerA(CompetingMessage message) {
        workerA.addFirst(message.text());
    }

    /** Takes a message for the second worker. RabbitMQ hands each message to only one of them. */
    @RabbitListener(queues = CompetingConfig.QUEUE)
    public void onWorkerB(CompetingMessage message) {
        workerB.addFirst(message.text());
    }

    /** Returns messages taken by the first worker, newest first. */
    public List<String> workerA() {
        return new ArrayList<>(workerA);
    }

    /** Returns messages taken by the second worker, newest first. */
    public List<String> workerB() {
        return new ArrayList<>(workerB);
    }
}
