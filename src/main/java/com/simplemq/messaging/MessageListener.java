package com.simplemq.messaging;

import java.time.Instant;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.simplemq.config.RabbitConfig;

@Component
public class MessageListener {

    private final ReceivedMessageLog log;

    public MessageListener(ReceivedMessageLog log) {
        this.log = log;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(QueuedMessage message) {
        log.add(new ReceivedMessage(message.text(), message.sentAt(), Instant.now()));
    }
}
