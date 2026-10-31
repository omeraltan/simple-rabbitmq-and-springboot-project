# simple-rabbitmq-and-springboot-project

A small Spring Boot app with four RabbitMQ patterns. Each pattern has its own package, its own queues, and its own card on the page.

Hover the info mark on a card for a short explanation. The curl calls below hit the same endpoints.

## Broker

RabbitMQ is already up in Docker. This repo does not start the broker.

| | |
|---|---|
| Image | `rabbitmq:4-management-alpine` |
| AMQP | `localhost:5672` |
| Management UI | http://localhost:15672 |
| Virtual host | `/` |
| User | `egitim` |

The password stays on your machine. It is not in git.

## Run

Java 17, Maven, and `spring-boot-starter-amqp`.

```bash
mvn spring-boot:run
```

Open http://localhost:8080.

Copy `src/main/resources/application.yml.example` to `src/main/resources/application.yml` and put the broker password there. `application.yml` is ignored by git.

`RabbitTemplate` publishes. A `@RabbitListener` consumes. Lists on the page are in memory, so a restart clears them. The queues stay in RabbitMQ.

## Patterns

Each pattern lives under `com.simplemq.pattern`. Send a JSON body `{"text":"hello"}`.

**Point to point** (`pointtopoint`). One direct exchange, one queue, one listener.

```bash
curl -X POST http://localhost:8080/api/patterns/point-to-point \
  -H 'Content-Type: application/json' -d '{"text":"hello"}'
curl http://localhost:8080/api/patterns/point-to-point
```

**Publish and subscribe** (`publishsubscribe`). A fanout exchange copies the message onto `pattern.fanout.inbox` and `pattern.fanout.audit`. Both lists show the same text.

```bash
curl -X POST http://localhost:8080/api/patterns/publish-subscribe \
  -H 'Content-Type: application/json' -d '{"text":"hello"}'
curl http://localhost:8080/api/patterns/publish-subscribe/inbox
curl http://localhost:8080/api/patterns/publish-subscribe/audit
```

**Competing consumers** (`competing`). One direct exchange, one queue (`pattern.competing`), two listeners. Each message is delivered to only one of them.

```bash
curl -X POST http://localhost:8080/api/patterns/competing \
  -H 'Content-Type: application/json' -d '{"text":"job-1"}'
curl http://localhost:8080/api/patterns/competing/worker-a
curl http://localhost:8080/api/patterns/competing/worker-b
```

**Dead letter** (`deadletter`). The worker rejects the message. The broker moves it from `pattern.dead.work` to `pattern.dead.dlq`.

```bash
curl -X POST http://localhost:8080/api/patterns/dead-letter \
  -H 'Content-Type: application/json' -d '{"text":"cannot-handle"}'
curl http://localhost:8080/api/patterns/dead-letter
```

In the management UI, open Queues and look for the `pattern.*` names.
