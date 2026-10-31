package com.simplemq.pattern.publishsubscribe;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublishSubscribeConfig {

    public static final String EXCHANGE = "pattern.fanout";
    public static final String INBOX_QUEUE = "pattern.fanout.inbox";
    public static final String AUDIT_QUEUE = "pattern.fanout.audit";

    /** Declares a fanout exchange. It copies each message to every bound queue. */
    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange(EXCHANGE, true, false);
    }

    /** Declares the inbox copy of a published message. */
    @Bean
    Queue fanoutInboxQueue() {
        return QueueBuilder.durable(INBOX_QUEUE).build();
    }

    /** Declares the audit copy of the same published message. */
    @Bean
    Queue fanoutAuditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    /** Binds the inbox queue. A fanout binding ignores the routing key. */
    @Bean
    Binding fanoutInboxBinding() {
        return BindingBuilder.bind(fanoutInboxQueue()).to(fanoutExchange());
    }

    /** Binds the audit queue to the same exchange. */
    @Bean
    Binding fanoutAuditBinding() {
        return BindingBuilder.bind(fanoutAuditQueue()).to(fanoutExchange());
    }
}
