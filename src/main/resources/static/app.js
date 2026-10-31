const patternLists = {
    point: "/api/patterns/point-to-point",
    inbox: "/api/patterns/publish-subscribe/inbox",
    audit: "/api/patterns/publish-subscribe/audit",
    "worker-a": "/api/patterns/competing/worker-a",
    "worker-b": "/api/patterns/competing/worker-b",
    dead: "/api/patterns/dead-letter"
};

const refreshGroups = {
    point: ["point"],
    fanout: ["inbox", "audit"],
    competing: ["worker-a", "worker-b"],
    dead: ["dead"]
};

document.querySelectorAll("form[data-send]").forEach((patternForm) => {
    const patternStatus = patternForm.querySelector(".status");
    const field = patternForm.querySelector("textarea");
    patternForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const body = field.value.trim();
        if (!body) {
            return;
        }
        const priorityField = patternForm.querySelector("[name=priority]");
        const payload = { text: body };
        if (priorityField) {
            payload.priority = Number(priorityField.value);
        }
        setPatternStatus(patternStatus, "Sending…", "");
        const response = await fetch(patternForm.dataset.send, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            setPatternStatus(patternStatus, "The broker did not take it.", "bad");
            return;
        }
        field.value = "";
        setPatternStatus(patternStatus, "Sent.", "ok");
        await refreshPattern(patternForm.dataset.refresh);
    });
});

function setPatternStatus(element, message, kind) {
    element.textContent = message;
    element.className = "status" + (kind ? " " + kind : "");
}

async function refreshPattern(name) {
    await Promise.all(refreshGroups[name].map(fillList));
}

async function fillList(name) {
    const response = await fetch(patternLists[name]);
    if (!response.ok) {
        return;
    }
    const messages = await response.json();
    const target = document.querySelector('[data-list="' + name + '"]');
    const emptyLine = document.querySelector('[data-empty="' + name + '"]');
    const countLine = document.querySelector('[data-count="' + name + '"]');
    target.replaceChildren();
    countLine.textContent = String(messages.length);
    emptyLine.classList.toggle("hidden", messages.length > 0);
    for (const message of messages) {
        const item = document.createElement("li");
        const body = document.createElement("p");
        body.textContent = message;
        item.append(body);
        target.append(item);
    }
}

function refreshPatterns() {
    return Promise.all(Object.keys(patternLists).map(fillList));
}

refreshPatterns();
setInterval(refreshPatterns, 2000);
