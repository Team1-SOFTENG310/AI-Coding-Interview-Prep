# AI Coding Interview Preparation

**Team Name:** Team 1
**Team Members:** Scott Wallace, Gabriel Liu, Dylan Liddle, Neia Tererei, Kenny Geng, Dandan Wu, Shenol Peiris

This project is associated with the University of Auckland course SOFTENG 310
(Software Evolution and Maintenance).

AI Coding Interview Preparation is a JavaFX desktop app that helps software
engineering students practice for technical interviews: it generates
behavioural, theory, and LeetCode-style coding questions with OpenAI, grades
written answers with AI feedback, and lets you answer by voice instead of
typing.

## Features

- **Authentication** - sign up and log in, with account details persisted
  between sessions
- **Behavioural / Theory practice** - AI-generated interview questions with a
  free-text answer box, evaluated against a grading rubric
- **Coding practice** - LeetCode-style coding questions with a syntax-highlighted
  code editor
- **AI question generation and evaluation** - OpenAI generates the questions
  and grades submitted answers, with a rating out of 10 and written feedback
- **Voice input** - a "Record Answer" button transcribes speech into the
  answer box, fully offline (see below), so it works without any OpenAI
  access

## Technology Stack

- Frontend: JavaFX
- Backend: Java
- Build Tool: Maven
- Testing: JUnit

## Prerequisites

- Java 17 JDK installed
- Maven wrapper is included; a separate Maven install is optional

## OpenAI API key setup

Question generation (Behavioural / Theory / Coding) and AI answer evaluation
require an OpenAI API key. Voice input does not - it runs fully offline (see
below).

1. Copy `.env.example` to a new file named `.env` in the project root.
2. Replace `your-key-here` with a real OpenAI API key.
3. `OPENAI_MODEL` is optional and defaults to `gpt-5-nano`.

Without a key, "Generate new question" and answer evaluation will fail with
an error message rather than crash the app.

## Run the application

From the project root:

On Windows:

```powershell
./mvnw.cmd javafx:run
```

On macOS/Linux:

```bash
./mvnw javafx:run
```

## Run tests

On Windows:

```powershell
./mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

## Voice input (Practice tab)

The "Record Answer" button transcribes speech offline using Vosk, so it
works without any OpenAI API access. It needs the speech model present at
`models/vosk-model-en-us-0.22-lgraph/` in the project root (~200MB) - if
that folder is missing, download and unzip it from:

https://alphacephei.com/vosk/models/vosk-model-en-us-0.22-lgraph.zip

## Known limitations / troubleshooting

- **Question generation and grading need internet access and an OpenAI
  API key** - see "OpenAI API key setup" above. Each generated question and
  each evaluation is a paid API call; the app doesn't work fully offline
  except for voice input.
- **Voice input accuracy is limited** by the offline speech model - it can
  come back empty on unclear audio, and background noise is filtered out
  rather than transcribed as junk text (see "Voice input" below).
- **The Vosk model is not tracked in git** on purpose (it's ~200MB of binary
  data) - if `models/vosk-model-en-us-0.22-lgraph/` is missing locally, voice
  input will show a clear error telling you to download it; every other
  feature works without it.
- **Account data is stored as plaintext JSON** in
  `src/main/resources/authorisation/accounts.json` - this is fine for local
  development and demos, but isn't representative of how a real production
  auth system would store credentials.
- **Java Version 25** will lead to failing tests. The project itself compiles and runs but the test will fail.
  To resolve it, a lower version is necessary. Either Java 17 or 21