package com.simplemq.pattern.publishsubscribe;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/patterns/publish-subscribe")
public class PublishSubscribeController {

    private final PublishSubscribeExample example;

    public PublishSubscribeController(PublishSubscribeExample example) {
        this.example = example;
    }

    /** Publishes one message that both queues should receive. */
    @PostMapping
    public void publish(@Valid @RequestBody TextRequest request) {
        example.publish(request.text());
    }

    /** Returns the inbox copy of published messages. */
    @GetMapping("/inbox")
    public List<String> inbox() {
        return example.inbox();
    }

    /** Returns the audit copy of published messages. */
    @GetMapping("/audit")
    public List<String> audit() {
        return example.audit();
    }

    public record TextRequest(@NotBlank String text) {
    }
}
