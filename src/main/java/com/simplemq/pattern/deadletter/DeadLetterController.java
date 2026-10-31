package com.simplemq.pattern.deadletter;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/patterns/dead-letter")
public class DeadLetterController {

    private final DeadLetterExample example;

    public DeadLetterController(DeadLetterExample example) {
        this.example = example;
    }

    /** Publishes a message the worker will refuse. */
    @PostMapping
    public void publish(@Valid @RequestBody TextRequest request) {
        example.publish(request.text());
    }

    /** Returns messages that arrived on the dead-letter queue. */
    @GetMapping
    public List<String> deadLetters() {
        return example.deadLetters();
    }

    public record TextRequest(@NotBlank String text) {
    }
}
