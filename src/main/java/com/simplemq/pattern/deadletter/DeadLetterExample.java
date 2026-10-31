package com.simplemq.pattern.deadletter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterExample {

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentLinkedDeque<String> deadLetters = new ConcurrentLinkedDeque<>();

    public DeadLetterExample(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publishes a work message that the worker will reject. */
    public void publish(String text) {
        rabbitTemplate.convertAndSend(
                DeadLetterConfig.EXCHANGE,
                DeadLetterConfig.ROUTING_KEY,
                new DeadLetterMessage(text));
    }

    /** Rejects the message so the broker moves it to the dead-letter queue. */
    @RabbitListener(queues = DeadLetterConfig.WORK_QUEUE)
    public void onWork(DeadLetterMessage message) {
        throw new AmqpRejectAndDontRequeueException("worker refused: " + message.text());
    }

    /** Stores a message after it arrives on the dead-letter queue. */
    @RabbitListener(queues = DeadLetterConfig.DEAD_QUEUE)
    public void onDeadLetter(DeadLetterMessage message) {
        deadLetters.addFirst(message.text());
    }

    /** Returns messages that landed in the dead-letter queue, newest first. */
    public List<String> deadLetters() {
        return new ArrayList<>(deadLetters);
    }
}
