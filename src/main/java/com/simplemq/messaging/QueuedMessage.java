package com.simplemq.messaging;

import java.time.Instant;

public record QueuedMessage(String text, Instant sentAt) {
}
