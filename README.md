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

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','primaryTextColor':'#17211c','lineColor':'#5d6b64','clusterBkg':'#f7f8f7','clusterBorder':'#d5dbd7'}}}%%
flowchart LR
    page(["Page"])
    exchange{{"direct exchange\npattern.point"}}
    queue[("queue\npattern.point")]
    listener["Listener"]

    page -->|publish| exchange
    exchange -->|routing key| queue
    queue -->|deliver once| listener

    style exchange fill:#1c4b38,color:#ffffff,stroke:#143628
    style queue fill:#e7f0eb,color:#17211c,stroke:#1c4b38
    style page fill:#ffffff,stroke:#c9d2cc
    style listener fill:#ffffff,stroke:#c9d2cc
```

A bank transfer is the shape of this pattern. The mobile app publishes one debit. The exchange routes it to a single queue. One payment service takes it and posts the ledger line. A second service never sees that debit, so the customer is not charged twice.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','actorBkg':'#1c4b38','actorTextColor':'#ffffff','actorBorder':'#143628','signalColor':'#1c4b38','noteBkgColor':'#e7f0eb','noteBorderColor':'#1c4b38'}}}%%
sequenceDiagram
    autonumber
    actor Customer
    participant App as Mobile app
    participant Exchange as pattern.point
    participant Queue as Transfer queue
    participant Payment as Payment service
    participant Ledger as Ledger

    Customer->>App: Send 250 to Ada
    App->>Exchange: Publish the debit once
    Exchange->>Queue: Route with key pattern.point
    Payment->>Queue: Take the only copy
    Payment->>Ledger: Post one ledger line
    Note over Payment,Ledger: A second service never receives this debit
```

```bash
curl -X POST http://localhost:8080/api/patterns/point-to-point \
  -H 'Content-Type: application/json' -d '{"text":"hello"}'
curl http://localhost:8080/api/patterns/point-to-point
```

The queue is durable and each message expires after 60 seconds if nobody takes it. The listener in this app is fast, so you only see that expiry when the listener is stopped.

Where it shows up:

1. A bank transfer is handed to one payment service. A second copy would charge the customer twice.
2. A courier app assigns one delivery to one driver. The same parcel is not offered to every driver at once.
3. An airline check-in kiosk sends a boarding pass job to one printer queue.
4. A hospital lab sends one blood sample order to the analyser that is free to run it.
5. A payroll run sends each employee's payslip to one generator, then the PDF is stored once.

**Publish and subscribe** — `com.simplemq.pattern.publishsubscribe`

A fanout exchange, `pattern.fanout`, copies one publish onto `pattern.fanout.inbox` and `pattern.fanout.audit`. Both cards show the same text. The routing key is ignored.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','primaryTextColor':'#17211c','lineColor':'#5d6b64'}}}%%
flowchart LR
    page(["Page"])
    exchange{{"fanout exchange\npattern.fanout"}}
    inbox[("queue\npattern.fanout.inbox")]
    audit[("queue\npattern.fanout.audit")]
    inboxListener["Inbox listener"]
    auditListener["Audit listener"]

    page -->|publish once| exchange
    exchange -->|copy| inbox
    exchange -->|copy| audit
    inbox --> inboxListener
    audit --> auditListener

    style exchange fill:#1c4b38,color:#ffffff,stroke:#143628
    style inbox fill:#e7f0eb,color:#17211c,stroke:#1c4b38
    style audit fill:#e7f0eb,color:#17211c,stroke:#1c4b38
    style page fill:#ffffff,stroke:#c9d2cc
    style inboxListener fill:#ffffff,stroke:#c9d2cc
    style auditListener fill:#ffffff,stroke:#c9d2cc
```

An order confirmation is the shape of this pattern. Checkout publishes one event. The exchange copies it onto every bound queue. Warehouse, email, and fraud each work from their own copy. One of them finishing does not remove the others.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','actorBkg':'#1c4b38','actorTextColor':'#ffffff','actorBorder':'#143628','signalColor':'#1c4b38','noteBkgColor':'#e7f0eb','noteBorderColor':'#1c4b38'}}}%%
sequenceDiagram
    autonumber
    actor Customer
    participant Checkout
    participant Exchange as pattern.fanout
    participant Warehouse as Warehouse queue
    participant Mail as Email queue
    participant Fraud as Fraud queue

    Customer->>Checkout: Pay for order 1042
    Checkout->>Exchange: Publish OrderConfirmed once
    par One copy on each queue
        Exchange->>Warehouse: Copy
        Exchange->>Mail: Copy
        Exchange->>Fraud: Copy
    end
    Warehouse->>Warehouse: Pick and pack
    Mail->>Customer: Send the receipt
    Fraud->>Fraud: Score the order
```

```bash
curl -X POST http://localhost:8080/api/patterns/publish-subscribe \
  -H 'Content-Type: application/json' -d '{"text":"hello"}'
curl http://localhost:8080/api/patterns/publish-subscribe/inbox
curl http://localhost:8080/api/patterns/publish-subscribe/audit
```

Where it shows up:

1. A shop confirms an order once. Warehouse, email, and fraud check each get their own copy.
2. A news app publishes a breaking alert. Push, the site banner, and the archive each subscribe.
3. A ride arrives. The rider's phone, the driver's phone, and the trip log all hear the same event.
4. A bank posts a card payment. The customer notification, the ledger, and the spending-insights service each keep a copy.
5. A factory sensor crosses a limit. The control room, the maintenance ticket system, and the daily report each receive it.

**Competing consumers** — `com.simplemq.pattern.competing`

One direct exchange and one queue, both named `pattern.competing`. Two listeners read that queue. Each message is handed to only one of them. Send a few and compare Worker A with Worker B.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','primaryTextColor':'#17211c','lineColor':'#5d6b64'}}}%%
flowchart LR
    page(["Page"])
    exchange{{"direct exchange\npattern.competing"}}
    queue[("queue\npattern.competing\nmax 50, priority 0-10")]
    workerA["Worker A"]
    workerB["Worker B"]

    page -->|publish| exchange
    exchange -->|one message| queue
    queue -->|taken by one worker| workerA
    queue -->|taken by one worker| workerB

    style exchange fill:#1c4b38,color:#ffffff,stroke:#143628
    style queue fill:#e7f0eb,color:#17211c,stroke:#1c4b38
    style page fill:#ffffff,stroke:#c9d2cc
    style workerA fill:#ffffff,stroke:#c9d2cc
    style workerB fill:#ffffff,stroke:#c9d2cc
```

A checkout desk is the shape of this pattern. Charges wait on one queue. Whichever payment server is free takes the next charge. Server A and server B never both settle the same card payment. A higher priority, such as a retry, is delivered first when the queue is backed up.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','actorBkg':'#1c4b38','actorTextColor':'#ffffff','actorBorder':'#143628','signalColor':'#1c4b38','noteBkgColor':'#e7f0eb','noteBorderColor':'#1c4b38'}}}%%
sequenceDiagram
    autonumber
    participant Checkout
    participant Exchange as pattern.competing
    participant Queue as Charge queue
    participant A as Payment server A
    participant B as Payment server B

    Checkout->>Exchange: Charge 1042, priority 8
    Exchange->>Queue: Store one message
    Checkout->>Exchange: Charge 1043, priority 0
    Exchange->>Queue: Store one message
    A->>Queue: Take 1042
    B->>Queue: Take 1043
    Note over A,B: 1042 is not delivered to server B
```

```bash
curl -X POST http://localhost:8080/api/patterns/competing \
  -H 'Content-Type: application/json' -d '{"text":"job-1","priority":8}'
curl http://localhost:8080/api/patterns/competing/worker-a
curl http://localhost:8080/api/patterns/competing/worker-b
```

The queue holds at most 50 messages. Past that, the oldest is dropped. Priority runs from 0 to 10. A higher number is delivered first when messages are waiting. The page has a priority field; leaving it empty sends 0. The list shows the priority next to the text, for example `job-1 · p8`.

Where it shows up:

1. Several checkout servers share one payment queue. Each charge is taken by whichever server is free.
2. A photo app has many resize workers. Each uploaded picture is processed by one of them.
3. A call centre puts incoming chats on one queue. The next free agent takes the next chat.
4. A tax office runs several machines over the same filing queue at the deadline. Each return is processed once.
5. A map service splits route calculations across a pool of workers. One journey is not calculated twice.

**Dead letter** — `com.simplemq.pattern.deadletter`

The worker on `pattern.dead.work` rejects every message and does not put it back. The queue is declared with a dead-letter exchange, so the broker moves the message to `pattern.dead.dlq`.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','primaryTextColor':'#17211c','lineColor':'#5d6b64'}}}%%
flowchart LR
    page(["Page"])
    exchange{{"direct exchange\npattern.dead"}}
    work[("work queue\npattern.dead.work")]
    worker["Worker rejects"]
    dlx{{"dead-letter exchange\npattern.dead.dlx"}}
    dlq[("review queue\npattern.dead.dlq\nTTL 10 min")]

    page -->|publish| exchange
    exchange -->|routing key| work
    work -->|reject, do not requeue| worker
    worker -->|dead-letter| dlx
    dlx --> dlq

    style exchange fill:#1c4b38,color:#ffffff,stroke:#143628
    style dlx fill:#8a2b2b,color:#ffffff,stroke:#6d2222
    style work fill:#e7f0eb,color:#17211c,stroke:#1c4b38
    style dlq fill:#f8ecec,color:#17211c,stroke:#8a2b2b
    style page fill:#ffffff,stroke:#c9d2cc
    style worker fill:#ffffff,stroke:#c9d2cc
```

A malformed card payment is the shape of this pattern. The live worker refuses the message. The broker does not put it back on the work queue, where it would block the next valid charge. It moves the message through the dead-letter exchange onto a review queue. Support fixes the card number, then publishes a new message. The parked copy expires after 10 minutes in this app.

```mermaid
%%{init: {'theme':'base','themeVariables': {'fontFamily':'ui-sans-serif, system-ui, sans-serif','actorBkg':'#1c4b38','actorTextColor':'#ffffff','actorBorder':'#143628','signalColor':'#1c4b38','noteBkgColor':'#f8ecec','noteBorderColor':'#8a2b2b'}}}%%
sequenceDiagram
    autonumber
    participant Checkout
    participant Work as pattern.dead.work
    participant Worker as Payment worker
    participant Dlx as pattern.dead.dlx
    participant Review as pattern.dead.dlq
    actor Support

    Checkout->>Work: Charge with an invalid card number
    Worker->>Work: Reject and do not requeue
    Work->>Dlx: Dead-letter the message
    Dlx->>Review: Park it for a person
    Support->>Review: Read the failed charge
    Note over Review: This app drops the parked message after 10 minutes
```

```bash
curl -X POST http://localhost:8080/api/patterns/dead-letter \
  -H 'Content-Type: application/json' -d '{"text":"cannot-handle"}'
curl http://localhost:8080/api/patterns/dead-letter
```

A message on the dead-letter queue expires after 10 minutes.

Where it shows up:

1. A card payment is rejected because the card number is malformed. It leaves the live queue and waits for a person to look at it.
2. An insurance claim file is missing a required page. The claim is parked instead of blocking the queue behind it.
3. A shipping label service cannot read an address. The order sits aside until support fixes the address.
4. A payroll row names an employee who is not in the system. That row is set aside so the rest of the run continues.
5. A video upload fails transcoding three times. The file moves to a review queue instead of being retried forever.

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
