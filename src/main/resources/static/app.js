const form = document.querySelector("#form");
const text = document.querySelector("#text");
const status = document.querySelector("#status");
const list = document.querySelector("#list");
const empty = document.querySelector("#empty");
const count = document.querySelector("#count");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = text.value.trim();
    if (!body) {
        return;
    }
    setStatus("Sending…", "");
    const response = await fetch("/api/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: body })
    });
    if (!response.ok) {
        setStatus("The broker did not take it.", "bad");
        return;
    }
    text.value = "";
    setStatus("Sent.", "ok");
    await load();
});

function setStatus(message, kind) {
    status.textContent = message;
    status.className = kind;
}

async function load() {
    const response = await fetch("/api/messages");
    if (!response.ok) {
        return;
    }
    const messages = await response.json();
    list.replaceChildren();
    count.textContent = String(messages.length);
    empty.classList.toggle("hidden", messages.length > 0);
    for (const message of messages) {
        const item = document.createElement("li");
        const body = document.createElement("p");
        body.textContent = message.text;
        const time = document.createElement("time");
        time.dateTime = message.receivedAt;
        time.textContent = "Received " + new Date(message.receivedAt).toLocaleString();
        item.append(body, time);
        list.append(item);
    }
}

load();
setInterval(load, 2000);
