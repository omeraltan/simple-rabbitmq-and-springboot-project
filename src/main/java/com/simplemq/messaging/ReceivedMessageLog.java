package com.simplemq.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Component;

@Component
public class ReceivedMessageLog {

    private static final int LIMIT = 50;

    private final ConcurrentLinkedDeque<ReceivedMessage> messages = new ConcurrentLinkedDeque<>();

    public void add(ReceivedMessage message) {
        messages.addFirst(message);
        while (messages.size() > LIMIT) {
            messages.removeLast();
        }
    }

    public List<ReceivedMessage> list() {
        return new ArrayList<>(messages);
    }
}
