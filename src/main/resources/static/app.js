const form = document.querySelector("#form");
const text = document.querySelector("#text");
const status = document.querySelector("#status");
const list = document.querySelector("#list");
const empty = document.querySelector("#empty");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = text.value.trim();
    if (!body) {
        return;
    }
    status.textContent = "Sending…";
    const response = await fetch("/api/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: body })
    });
    if (!response.ok) {
        status.textContent = "The broker did not take it.";
        return;
    }
    text.value = "";
    status.textContent = "Sent.";
    await load();
});

async function load() {
    const response = await fetch("/api/messages");
    if (!response.ok) {
        return;
    }
    const messages = await response.json();
    list.replaceChildren();
    empty.classList.toggle("hidden", messages.length > 0);
    for (const message of messages) {
        const item = document.createElement("li");
        const body = document.createElement("p");
        body.textContent = message.text;
        const time = document.createElement("time");
        time.dateTime = message.receivedAt;
        time.textContent = new Date(message.receivedAt).toLocaleString();
        item.append(body, time);
        list.append(item);
    }
}

load();
setInterval(load, 2000);
