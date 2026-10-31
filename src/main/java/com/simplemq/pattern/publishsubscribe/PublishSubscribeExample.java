package com.simplemq.pattern.publishsubscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublishSubscribeExample {

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentLinkedDeque<String> inbox = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String> audit = new ConcurrentLinkedDeque<>();

    public PublishSubscribeExample(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publishes once. The fanout exchange delivers a copy to each bound queue. */
    public void publish(String text) {
        rabbitTemplate.convertAndSend(PublishSubscribeConfig.EXCHANGE, "", new FanoutMessage(text));
    }

    /** Stores the copy delivered to the inbox queue. */
    @RabbitListener(queues = PublishSubscribeConfig.INBOX_QUEUE)
    public void onInbox(FanoutMessage message) {
        inbox.addFirst(message.text());
    }

    /** Stores the copy delivered to the audit queue. */
    @RabbitListener(queues = PublishSubscribeConfig.AUDIT_QUEUE)
    public void onAudit(FanoutMessage message) {
        audit.addFirst(message.text());
    }

    /** Returns messages taken from the inbox queue, newest first. */
    public List<String> inbox() {
        return new ArrayList<>(inbox);
    }

    /** Returns messages taken from the audit queue, newest first. */
    public List<String> audit() {
        return new ArrayList<>(audit);
    }
}
