package com.simplemq.pattern.competing;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/patterns/competing")
public class CompetingController {

    private final CompetingConsumersExample example;

    public CompetingController(CompetingConsumersExample example) {
        this.example = example;
    }

    /** Publishes one work item. A missing priority is treated as 0. */
    @PostMapping
    public void publish(@Valid @RequestBody TextRequest request) {
        int priority = request.priority() == null ? 0 : request.priority();
        example.publish(request.text(), priority);
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

    public record TextRequest(
            @NotBlank String text,
            @Min(0) @Max(10) Integer priority
    ) {
    }
}
