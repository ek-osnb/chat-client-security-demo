import {API_CONFIG} from "./config.js";

export async function bootstrapCsrfAndMaybeGetUser() {
    const response = await fetch(API_CONFIG.ENDPOINTS.USER);

    if (response.status === 401) {
        throw new Error("User is not logged in.");
    }

    if (!response.ok) {
        throw new Error("Failed to initialize application.");
    }

    return await response.json();
}

export async function handleLogin(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);
    const username = formData.get("username");
    const password = formData.get("password");

    try {
        const csrfToken = getCsrfToken();
        const response = await fetch(API_CONFIG.ENDPOINTS.LOGIN, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-XSRF-TOKEN": csrfToken
            },
            body: new URLSearchParams({username, password})
        });

        if (!response.ok) {
            throw new Error("Login failed. Please check your credentials.");
        }
        form.reset();
    } catch (error) {

    }
}

export async function handleLogout() {
    try {
        const csrfToken = getCsrfToken();

        const response = await fetch(API_CONFIG.ENDPOINTS.LOGOUT, {
            method: "POST",
            headers: {
                "X-XSRF-TOKEN": csrfToken || ""
            }
        });

        if (!response.ok) {
            throw new Error("Logout failed.");
        }
        // Optional: call /api/user again so Spring can refresh/recreate the CSRF token if needed
        try {
            await bootstrapCsrfAndMaybeGetUser();
        } catch {
            // Expected after logout because the user is anonymous
        }
    } catch (error) {

    }
}

export function getCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
}