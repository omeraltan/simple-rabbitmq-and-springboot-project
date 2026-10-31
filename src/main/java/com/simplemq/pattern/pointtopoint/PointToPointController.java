package com.simplemq.pattern.pointtopoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/patterns/point-to-point")
public class PointToPointController {

    private final PointToPointExample example;

    public PointToPointController(PointToPointExample example) {
        this.example = example;
    }

    /** Publishes one message onto the point-to-point queue. */
    @PostMapping
    public void publish(@Valid @RequestBody TextRequest request) {
        example.publish(request.text());
    }

    /** Returns what the single listener has taken from that queue. */
    @GetMapping
    public List<String> received() {
        return example.received();
    }

    public record TextRequest(@NotBlank String text) {
    }
}
