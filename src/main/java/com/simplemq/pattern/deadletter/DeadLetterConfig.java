package com.simplemq.pattern.deadletter;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeadLetterConfig {

    public static final String EXCHANGE = "pattern.dead";
    public static final String WORK_QUEUE = "pattern.dead.work";
    public static final String ROUTING_KEY = "pattern.dead.work";
    public static final String DEAD_EXCHANGE = "pattern.dead.dlx";
    public static final String DEAD_QUEUE = "pattern.dead.dlq";
    public static final String DEAD_ROUTING_KEY = "pattern.dead.dlq";

    /** Declares the exchange that receives a new work message. */
    @Bean
    DirectExchange deadWorkExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    /** Declares the work queue and tells the broker where to send a rejected message. */
    @Bean
    Queue deadWorkQueue() {
        return QueueBuilder.durable(WORK_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(DEAD_ROUTING_KEY)
                .build();
    }

    /** Binds the work queue to its exchange. */
    @Bean
    Binding deadWorkBinding() {
        return BindingBuilder.bind(deadWorkQueue()).to(deadWorkExchange()).with(ROUTING_KEY);
    }

    /** Declares the exchange that carries rejected messages. */
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_EXCHANGE, true, false);
    }

    /**
     * Declares the queue that keeps messages the worker refused.
     * A parked message is discarded after 10 minutes so the shelf does not grow forever.
     */
    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_QUEUE)
                .ttl(600_000)
                .build();
    }

    /** Binds the dead-letter queue to the dead-letter exchange. */
    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_ROUTING_KEY);
    }
}
