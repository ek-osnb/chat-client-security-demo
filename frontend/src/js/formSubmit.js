import askAI from "./apiService.js";

export async function handleChatSubmit(event) {
    event.preventDefault();
    const form = new FormData(event.target);
    const prompt = form.get("prompt");

    addToChat(prompt, "user");

    event.target.reset();

    // MAKE BUTTON UNCLICKABLE WHILE WAITING FOR RESPONSE
    const submitButton = event.target.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    submitButton.style.backgroundColor = "#ccc"; // Optional: visually indicate it's disabled

    const data = await askAI(prompt);

    addToChat(data.response, "assistant")
    // RE-ENABLE BUTTON AFTER RESPONSE
    submitButton.disabled = false;
    submitButton.style.backgroundColor = ""; // Reset to original color
}

export function addToChat(message, sender) {
    const messages = document.querySelector("#chat");
    const messageElement = document.createElement("div");
    messageElement.classList.add("message", sender);

    const bubble = document.createElement("div");
    bubble.classList.add("bubble");
    bubble.textContent = message;

    messageElement.appendChild(bubble);
    messages.appendChild(messageElement);

    messages.scrollTop = messages.scrollHeight;
}


export function handleKeyDown(e) {
    const form = document.getElementById('chat-form');
    if (e.key === 'Enter' && !e.ctrlKey && !e.shiftKey) {
        e.preventDefault();   // stop newline
        form.requestSubmit(); // submit the form
    }
}