import {getCsrfToken} from "./auth.js";
import {API_CONFIG} from "./config.js";

// Add credentials: 'include' if your server requires cookies or authentication
export default async function askAI(prompt) {
    const response = await fetch(API_CONFIG.ENDPOINTS.PROMPT, {
            method: "POST",
            credentials: 'include',
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": getCsrfToken() || ""
            },
            body: JSON.stringify({prompt: prompt}),
        }
    );
    return await response.json();
}
