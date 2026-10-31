package com.simplemq.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simplemq.messaging.MessagePublisher;
import com.simplemq.messaging.ReceivedMessage;
import com.simplemq.messaging.ReceivedMessageLog;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessagePublisher publisher;
    private final ReceivedMessageLog log;

    public MessageController(MessagePublisher publisher, ReceivedMessageLog log) {
        this.publisher = publisher;
        this.log = log;
    }

    @PostMapping
    public void publish(@Valid @RequestBody MessageRequest request) {
        publisher.publish(request.text());
    }

    @GetMapping
    public List<ReceivedMessage> list() {
        return log.list();
    }
}
