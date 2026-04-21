import {handleChatSubmit, handleKeyDown} from "./formSubmit.js";
import {bootstrapCsrfAndMaybeGetUser, handleLogin, handleLogout} from "./auth.js";


window.addEventListener("DOMContentLoaded", initApp);

async function initApp() {
    setupEventlisteners();
    try {
        const user = await bootstrapCsrfAndMaybeGetUser();
        hideLogin(user);
    } catch (error) {
        showLogin();
    }
}

function showLogin() {
    const main = document.querySelector("main");
    const inputArea = document.querySelector("#input-area");
    const loginForm = document.querySelector("#login-form");
    const logoutButton = document.querySelector("#logout-button");
    main.style = "display: none;";
    inputArea.style = "display: none;";
    loginForm.style = "display: flex;";
    logoutButton.style = "display: none;";

    const userInfo = document.querySelector("#user-info");
    userInfo.textContent = "";

    resetChat();
}

function resetChat() {
    const chat = document.querySelector("#chat");
    const child = chat.children.item(0);
    chat.innerHTML = "";
    if (child) {
        chat.appendChild(child);
    }
}

function hideLogin(user) {
    const main = document.querySelector("main");
    const inputArea = document.querySelector("#input-area");
    const loginForm = document.querySelector("#login-form");
    const logoutButton = document.querySelector("#logout-button");
    main.style = "display: flex;";
    inputArea.style = "display: block;";
    loginForm.style = "display: none;";
    logoutButton.style = "display: block;";

    const userInfo = document.querySelector("#user-info");
    userInfo.textContent = `Logged in as: ${user.username}`;

}

function setupEventlisteners() {
    const chatForm = document.querySelector("#chat-form");
    chatForm.addEventListener("submit", handleChatSubmit);
    const form = document.getElementById('chat-form');
    const textarea = form.querySelector('textarea');
    textarea.addEventListener("keydown", handleKeyDown);

    const loginForm = document.querySelector("#login-form");
    loginForm.addEventListener("submit", async (event) => {
        try {
            await handleLogin(event);
            const user = await bootstrapCsrfAndMaybeGetUser();
            hideLogin(user);
        } catch (error) {
            console.error("Login error:", error);
        }
    });

    const logoutButton = document.querySelector("#logout-button");
    logoutButton.addEventListener("click", async () => {
        try {
            await handleLogout();
            showLogin();
        } catch (error) {
            console.error("Logout error:", error);
        }
    })
}

