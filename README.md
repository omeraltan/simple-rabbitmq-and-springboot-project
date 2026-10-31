# simple-rabbitmq-and-springboot-project

A small Spring Boot app for getting used to RabbitMQ. One page, one queue.

Type a message, the app publishes it, a listener takes it off the queue, and the same page lists what came back. The RabbitMQ management UI is separate if you want to watch the queue fill and drain.

## What is running

RabbitMQ is already up in Docker. This repo does not start the broker.

| | |
|---|---|
| Image | `rabbitmq:4-management-alpine` |
| AMQP | `localhost:5672` |
| Management UI | http://localhost:15672 |
| Virtual host | `/` |
| User | `egitim` |

The password stays on your machine. It is not in git.

## The app

Java 17 and Spring Boot. Messaging uses `spring-boot-starter-amqp`.

There is one direct exchange, one durable queue, and one routing key. `RabbitTemplate` publishes. A `@RabbitListener` consumes. Received messages are kept in memory so the page can show them; restarting the app clears that list. The queue itself is RabbitMQ's.

- `POST /api/messages` publishes a text message
- `GET /api/messages` returns what the listener has taken
- `/` is the page

```bash
mvn spring-boot:run
```

Open http://localhost:8080.

Copy `src/main/resources/application.yml.example` to `src/main/resources/application.yml` and put the broker password there. `application.yml` is ignored by git.
