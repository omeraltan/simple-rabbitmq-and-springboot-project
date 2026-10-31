package com.simplemq.pattern.competing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
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

    /**
     * Publishes one work item.
     * The priority is also set on the AMQP message so the broker can order a backlog.
     */
    public void publish(String text, int priority) {
        int level = Math.min(10, Math.max(0, priority));
        CompetingMessage body = new CompetingMessage(text, level);
        rabbitTemplate.convertAndSend(
                CompetingConfig.EXCHANGE,
                CompetingConfig.ROUTING_KEY,
                body,
                withPriority(level));
    }

    /** Copies the body priority onto the AMQP priority property. */
    private MessagePostProcessor withPriority(int level) {
        return (Message message) -> {
            message.getMessageProperties().setPriority(level);
            return message;
        };
    }

    /** Takes a message for the first worker. The other worker does not see this copy. */
    @RabbitListener(queues = CompetingConfig.QUEUE)
    public void onWorkerA(CompetingMessage message) {
        workerA.addFirst(label(message));
    }

    /** Takes a message for the second worker. RabbitMQ hands each message to only one of them. */
    @RabbitListener(queues = CompetingConfig.QUEUE)
    public void onWorkerB(CompetingMessage message) {
        workerB.addFirst(label(message));
    }

    /** Renders the text with the priority the publisher set. */
    private String label(CompetingMessage message) {
        return message.text() + " · p" + message.priority();
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
