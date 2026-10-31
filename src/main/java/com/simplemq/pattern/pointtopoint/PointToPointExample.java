package com.simplemq.pattern.pointtopoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PointToPointExample {

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentLinkedDeque<String> received = new ConcurrentLinkedDeque<>();

    public PointToPointExample(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publishes one message. Only the single bound queue can receive it. */
    public void publish(String text) {
        rabbitTemplate.convertAndSend(
                PointToPointConfig.EXCHANGE,
                PointToPointConfig.ROUTING_KEY,
                new PointMessage(text));
    }

    /** Stores the message taken by the only consumer of this queue. */
    @RabbitListener(queues = PointToPointConfig.QUEUE)
    public void onMessage(PointMessage message) {
        received.addFirst(message.text());
    }

    /** Returns messages this consumer has taken, newest first. */
    public List<String> received() {
        return new ArrayList<>(received);
    }
}
