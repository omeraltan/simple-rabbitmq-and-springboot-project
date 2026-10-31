package com.simplemq.pattern.pointtopoint;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PointToPointConfig {

    public static final String EXCHANGE = "pattern.point";
    public static final String QUEUE = "pattern.point";
    public static final String ROUTING_KEY = "pattern.point";

    /** Declares the direct exchange used by the point-to-point example. */
    @Bean
    DirectExchange pointExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    /**
     * Declares the single durable queue for this pattern.
     * A message nobody takes is discarded after 60 seconds.
     */
    @Bean
    Queue pointQueue() {
        return QueueBuilder.durable(QUEUE)
                .ttl(60_000)
                .build();
    }

    /** Binds that queue to the exchange with one routing key. */
    @Bean
    Binding pointBinding() {
        return BindingBuilder.bind(pointQueue()).to(pointExchange()).with(ROUTING_KEY);
    }
}
