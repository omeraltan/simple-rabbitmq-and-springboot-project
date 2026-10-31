# simple-rabbitmq-and-springboot-project

Four small RabbitMQ examples in one Spring Boot app. Each example is a package, a pair of broker objects, and a card on the page.

## What you need

RabbitMQ is already running in Docker. This repo does not start it.

| | |
|---|---|
| Image | `rabbitmq:4-management-alpine` |
| AMQP | `localhost:5672` |
| Management UI | http://localhost:15672 |
| Virtual host | `/` |
| User | `egitim` |

Java 17 and Maven. The broker password stays in `src/main/resources/application.yml`, which git ignores. Copy `application.yml.example` and fill the password there.

## Run

```bash
mvn spring-boot:run
```

Open http://localhost:8080. The info mark on a card explains that pattern. The same calls work with curl. Send `{"text":"hello"}`.

Lists on the page live in memory. Restarting the app clears them. The queues stay in RabbitMQ until you delete them.

`RabbitTemplate` publishes. A `@RabbitListener` consumes. `RabbitConfig` turns the Java record into JSON on the wire.

## The four patterns

**Point to point** — `com.simplemq.pattern.pointtopoint`

One direct exchange, one queue, one listener. The name of all three is `pattern.point`. A message is delivered once.

```bash
curl -X POST http://localhost:8080/api/patterns/point-to-point \
  -H 'Content-Type: application/json' -d '{"text":"hello"}'
curl http://localhost:8080/api/patterns/point-to-point
```

The queue is durable and each message expires after 60 seconds if nobody takes it. The listener in this app is fast, so you only see that expiry when the listener is stopped.

**Publish and subscribe** — `com.simplemq.pattern.publishsubscribe`

A fanout exchange, `pattern.fanout`, copies one publish onto `pattern.fanout.inbox` and `pattern.fanout.audit`. Both cards show the same text. The routing key is ignored.

```bash
curl -X POST http://localhost:8080/api/patterns/publish-subscribe \
  -H 'Content-Type: application/json' -d '{"text":"hello"}'
curl http://localhost:8080/api/patterns/publish-subscribe/inbox
curl http://localhost:8080/api/patterns/publish-subscribe/audit
```

**Competing consumers** — `com.simplemq.pattern.competing`

One direct exchange and one queue, both named `pattern.competing`. Two listeners read that queue. Each message is handed to only one of them. Send a few and compare Worker A with Worker B.

```bash
curl -X POST http://localhost:8080/api/patterns/competing \
  -H 'Content-Type: application/json' -d '{"text":"job-1","priority":8}'
curl http://localhost:8080/api/patterns/competing/worker-a
curl http://localhost:8080/api/patterns/competing/worker-b
```

The queue holds at most 50 messages. Past that, the oldest is dropped. Priority runs from 0 to 10. A higher number is delivered first when messages are waiting. The page has a priority field; leaving it empty sends 0. The list shows the priority next to the text, for example `job-1 · p8`.

**Dead letter** — `com.simplemq.pattern.deadletter`

The worker on `pattern.dead.work` rejects every message and does not put it back. The queue is declared with a dead-letter exchange, so the broker moves the message to `pattern.dead.dlq`.

```bash
curl -X POST http://localhost:8080/api/patterns/dead-letter \
  -H 'Content-Type: application/json' -d '{"text":"cannot-handle"}'
curl http://localhost:8080/api/patterns/dead-letter
```

A message on the dead-letter queue expires after 10 minutes.

## Queue arguments in this app

RabbitMQ stores these on the queue the first time it is declared. Changing them later means declaring a new queue, or deleting the old one first.

| Queue | Argument | Why it is here |
|---|---|---|
| `pattern.point` | durable, message TTL 60s | The queue survives a broker restart. An unread message does not sit forever. |
| `pattern.fanout.inbox`, `pattern.fanout.audit` | durable | Each subscriber keeps its own copy. |
| `pattern.competing` | durable, max length 50, drop oldest, max priority 10 | A work queue with two workers, a cap, and an order when it backs up. |
| `pattern.dead.work` | durable, dead-letter exchange and routing key | A rejected message has somewhere to go. |
| `pattern.dead.dlq` | durable, message TTL 10 min | The shelf for rejected messages empties itself. |

Two settings are left off on purpose. Single active consumer would let only one of the competing workers run, which is a different pattern. A quorum queue would replace the classic queues this app declares; the examples do not need a replicated queue.

In the management UI, open a queue and look at its arguments if you want to see the same values the code declared.
