package com.simplemq.messaging;

import java.time.Instant;

public record ReceivedMessage(String text, Instant sentAt, Instant receivedAt) {
}
