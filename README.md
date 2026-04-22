# AI Chat Client Security Demo

A Spring Boot demo application that shows how to call an AI REST API from a backend service — with session-based security, a vanilla JS frontend, and a multi-step flow that combines three external APIs.

---

## What Does This App Do?

A user types a weather question — *"What is the weather like in Copenhagen right now?"* — and gets an AI-generated answer backed by real weather data.

Behind the scenes, the backend:
1. Uses an AI provider to **extract the city** from the user's question.
2. Calls a **geocoding API** to get the latitude/longitude of that city.
3. Calls a **weather API** to get the actual current weather data.
4. Sends that weather data back to the AI to **compose a user-friendly answer**.

---

### Key Packages

| Package | Responsibility |
|---|---|
| `security` | Spring Security config — login, logout, session |
| `user` | `/api/user` endpoint — who is logged in |
| `weather` | Main feature: controller, service, and all external API clients |
| `ai` | `AiClient` interface, `AiProperties` config, and `AiRequest`/`AiResponse` models |
| `ai/openai` | OpenAI implementation of `AiClient` — REST client and request/response models |
| `ai/ollama` | Ollama implementation of `AiClient` — REST client and request/response models |
| `weather/geoapi` | Geocoding client (city → coordinates) |
| `weather/weatherapi` | Weather data client (coordinates → weather) |
| `config` | Shared `RestClient.Builder` bean and weather/geocoding `RestClient` beans |

---

## AI Provider Abstraction

The application uses an **`AiClient` interface** to decouple the weather service from any specific AI provider:

```java
public interface AiClient {
    AiResponse generate(AiRequest request);
}
```

The active provider is selected via the `app.ai.provider` property. Each implementation uses `@ConditionalOnProperty` so only the matching bean is loaded at startup:

```java
@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "openai")
class OpenAiClient implements AiClient { ... }

@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama")
class OllamaClient implements AiClient { ... }
```

### Adding a New Provider

To add a new provider (e.g. Gemini):

1. Create a new package, e.g. `ai/gemini/`.
2. Implement `AiClient` and annotate it:
   ```java
   @Service
   @ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "gemini")
   class GeminiClient implements AiClient { ... }
   ```
3. Set the provider in `application.properties`:
   ```properties
   app.ai.provider=gemini
   ```

No other code needs to change — `AiWeatherService` injects `AiClient` and is unaware of which provider is active.

---

## Ollama — Local AI via Docker

[Ollama](https://ollama.com) lets you run AI models locally. In this project, Ollama runs as a Docker container and is called over HTTP — no API key required.

### How It Works

`OllamaApi.java` sends a `POST` request to Ollama's `/api/chat` endpoint:

```
POST http://localhost:11434/api/chat
Content-Type: application/json
```

With a JSON body like this:

```json
{
  "model": "llama3.2",
  "messages": [
    { "role": "system", "content": "You are a weather assistant..." },
    { "role": "user",   "content": "What is the weather like in Copenhagen?" }
  ],
  "stream": false
}
```

And you get back something like:

```json
{
  "message": {
    "role": "assistant",
    "content": "It is currently 12°C in Copenhagen with light winds."
  }
}
```

### How This Maps to Java

**Request** → `OllamaRequest.java`
```java
record OllamaRequest(
    String model,
    List<OllamaMessage> messages,
    boolean stream       // set to false to get a single complete response
) {}
```

**Message** → `OllamaMessage.java`
```java
record OllamaMessage(String role, String content) {}
```

**Response** → `OllamaResponse.java`
```java
record OllamaResponse(OllamaMessage message) {}
```

### First Request May Return a 404

When Ollama starts for the first time, it needs to **pull the model** (e.g. `llama3.2`) from the registry. During this time, the model is not yet available, and API calls will return `404 Not Found`. This is expected behavior — just wait a few minutes and try again.

---

## How the OpenAI REST API Works

### The Raw HTTP Request

When you call OpenAI, you send a `POST` request to:

```
POST https://api.openai.com/v1/responses
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
```

With a JSON body like this:

```json
{
  "model": "gpt-4.1",
  "input": "What is the weather like in Copenhagen?",
  "instructions": "You are a weather assistant. Answer only using the weather data provided.",
  "max_output_tokens": 800
}
```

And you get back something like:

```json
{
  "id": "resp_abc123",
  "output": [
    {
      "type": "message",
      "content": [
        {
          "type": "output_text",
          "text": "It is currently 12°C in Copenhagen with light winds."
        }
      ]
    }
  ]
}
```

### How This Maps to Java

The project uses Java **records** to model the request and response directly:

**Request** → `OpenAiRequest.java`
```java
record OpenAiRequest(
    String model,                                   // which AI model to use
    String input,                                   // the user's message / question
    String instructions,                            // system-level rules for the AI
    @JsonProperty("max_output_tokens") Integer maxOutputTokens  // limit response length
) {}
```

**Response** → `OpenAiResponse.java`, `OpenAiOutput.java`, `OpenAiContent.java`

The response JSON is nested, so it maps to nested records:
```
OpenAiResponse
  └── List<OpenAiOutput>  (output[])
        └── List<OpenAiContent>  (content[])
              └── String text    ← this is the actual answer
```

### How the HTTP Call is Made

Spring's `RestClient` handles the HTTP call in `OpenAiApi.java`:

```java
return client.post()
    .uri("/v1/responses")
    .contentType(MediaType.APPLICATION_JSON)
    .body(request)               // Java record → JSON (Jackson)
    .retrieve()
    .body(OpenAiResponse.class); // JSON → Java record (Jackson)
```

Jackson automatically converts between the Java records and JSON — `@JsonProperty("max_output_tokens")` maps the camelCase Java field to the snake_case JSON key.

The `Authorization: Bearer ...` header is set once in `OpenAiApi.java` when the `RestClient` is constructed — you never need to set it per call. The shared `RestClientConfig` only handles the weather and geocoding clients.

---

## The Two-Step AI Flow

This demo intentionally calls OpenAI **twice** for a single user question:

```
User prompt
    │
    ▼
[OpenAI Call 1] "Extract the city from this question"
    │  → "Copenhagen"
    ▼
[Geocoding API] "Get lat/lon for Copenhagen"
    │  → { lat: 55.67, lon: 12.56 }
    ▼
[Weather API] "Get weather for 55.67, 12.56"
    │  → { temperature: 12°C, windspeed: 15 km/h }
    ▼
[OpenAI Call 2] "Answer the user's question using this weather data"
    │  → "It is currently 12°C in Copenhagen with light winds."
    ▼
Response to user
```

> **Note — OpenAI Tools:**
> OpenAI has a built-in feature called [function calling / tools](https://platform.openai.com/docs/guides/function-calling) that would allow the AI model itself to decide when to call the geocoding and weather APIs. This demo **intentionally does not use that feature** — instead, the orchestration is written manually in `AiWeatherService.java` so that each step is visible and easy to follow. In a production application, tools would be the more natural approach.

---

## Security

The app uses **session-based authentication** — the same mechanism as a classic web login form, made SPA-friendly.

| Endpoint | Method | Description |
|---|---|---|
| `/api/login` | `POST` | Log in with username + password |
| `/api/logout` | `POST` | Log out |
| `/api/user` | `GET` | Get the currently logged-in user |
| `/api/prompt` | `POST` | Send a weather question |

All endpoints require authentication. If you are not logged in, you get `401 Unauthorized` (not a redirect).

**Demo users** (defined in `SecurityConfig.java`):

| Username | Password | Role |
|---|---|---|
| `user` | `pw` | USER |
| `admin` | `pw` | ADMIN |

---

## Setup & Running

### Prerequisites
- Java 25
- Maven (or use the included `./mvnw`)
- Docker (for Nginx frontend and Ollama)
- An OpenAI API key *(only required when using the OpenAI provider)*

### 1. Set Your API Key *(OpenAI only)*

```bash
export OPENAI_API_KEY=sk-...
```
> Or set it in IntelliJ run configuration environment variables.
> Skip this step if you are using Ollama.

### 2. Start the Backend

```bash
./mvnw spring-boot:run
```

The API is now available at `http://localhost:8080`.

### 3. Start the Frontend

```bash
docker compose up -d
```

Open your browser at `http://localhost`.

> nginx serves the static frontend files and proxies API requests to the Spring Boot backend.
> When using Ollama, `docker compose` also starts the Ollama container. The first time it runs, it will pull the configured model — this may take a few minutes.

---

## Configuration

All external URLs and the API key are configured in `src/main/resources/application.properties`.

### Using OpenAI

```properties
app.ai.provider=openai
app.ai.model=gpt-4.1
app.ai.max-output-tokens=800
app.ai.base-url=https://api.openai.com
app.ai.api-key=${OPENAI_API_KEY}        # loaded from environment variable
```

### Using Ollama (default)

```properties
app.ai.provider=ollama
app.ai.model=llama3.2
app.ai.max-output-tokens=800
app.ai.base-url=http://localhost:11434
# no API key needed
```

### External APIs (shared)

```properties
external.api.weather.baseUrl=https://api.open-meteo.com/v1
external.api.geocoding.baseUrl=https://geocoding-api.open-meteo.com/v1
```

All `app.ai.*` properties are bound to `AiProperties.java` via `@ConfigurationProperties(prefix = "app.ai")`.
