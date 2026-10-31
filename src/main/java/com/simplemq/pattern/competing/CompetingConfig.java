package com.simplemq.pattern.competing;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompetingConfig {

    public static final String EXCHANGE = "pattern.competing";
    public static final String QUEUE = "pattern.competing";
    public static final String ROUTING_KEY = "pattern.competing";

    /** Declares the exchange that receives work for the shared queue. */
    @Bean
    DirectExchange competingExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    /**
     * Declares the shared work queue.
     * It keeps at most 50 messages and drops the oldest when that cap is hit.
     * Messages may carry a priority from 0 to 10; a higher number is delivered first
     * when the queue is waiting.
     */
    @Bean
    Queue competingQueue() {
        return QueueBuilder.durable(QUEUE)
                .maxLength(50)
                .overflow(QueueBuilder.Overflow.dropHead)
                .maxPriority(10)
                .build();
    }

    /** Binds that single queue. Two listeners compete for messages on it. */
    @Bean
    Binding competingBinding() {
        return BindingBuilder.bind(competingQueue()).to(competingExchange()).with(ROUTING_KEY);
    }
}
