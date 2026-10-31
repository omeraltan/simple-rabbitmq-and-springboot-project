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

    /** Declares the one queue both workers read from. */
    @Bean
    Queue competingQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    /** Binds that single queue. Two listeners compete for messages on it. */
    @Bean
    Binding competingBinding() {
        return BindingBuilder.bind(competingQueue()).to(competingExchange()).with(ROUTING_KEY);
    }
}
