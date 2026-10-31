package com.simplemq.pattern.competing;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/patterns/competing")
public class CompetingController {

    private final CompetingConsumersExample example;

    public CompetingController(CompetingConsumersExample example) {
        this.example = example;
    }

    /** Publishes one work item onto the shared queue. */
    @PostMapping
    public void publish(@Valid @RequestBody TextRequest request) {
        example.publish(request.text());
    }

    /** Returns work taken by the first consumer. */
    @GetMapping("/worker-a")
    public List<String> workerA() {
        return example.workerA();
    }

    /** Returns work taken by the second consumer. */
    @GetMapping("/worker-b")
    public List<String> workerB() {
        return example.workerB();
    }

    public record TextRequest(@NotBlank String text) {
    }
}
