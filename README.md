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

- **Authentication** - sign up and log in; accounts are stored in the
  database with PBKDF2-hashed passwords
- **Behavioural / Theory practice** - AI-generated interview questions with a
  free-text answer box, evaluated against a grading rubric
- **Coding practice** - LeetCode-style coding questions with a syntax-highlighted
  code editor
- **Saved questions and answers** - a **Save** button on each practice page
  bookmarks the current question and stores your current answer with it; the
  **Saved Questions** page lists your bookmarks and saved answers and lets you
  remove a question (see "Saving questions and answers" below)
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

`.env` also contains the database settings (`DB_*`). See "Database setup" below.

## Database setup

The app uses a MySQL database running in Docker. Flyway creates and updates
the tables automatically when the app starts.

1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) and make sure it is running.
2. Fill in the `DB_*` values in your `.env` file (see "OpenAI API key setup" above).
3. Start the database from the project root:

```bash
docker compose up -d
```

To reset the database (deletes all local data; tables are recreated on the next run):

```bash
docker compose down -v
```

To check that it works, run the app once (see "Run the application"), then:

```bash
docker exec -it interviewprep-mysql mysql -u appuser -p appdb
```

Replace `appuser` and `appdb` with your own `DB_USER` and `DB_NAME` from `.env`.
Enter your `DB_PASSWORD` when prompted (the input stays hidden), then run
`SHOW TABLES;`. You should see `user_account`, `saved_question`,
`code_submission` and `flyway_schema_history`.

The database is required to sign up, log in and use saved questions: accounts,
bookmarks and saved answers are all stored in it.

## Saving questions and answers

Questions are never saved automatically - saving is opt-in.

- Generate a question on the Behavioural / Theory or Coding page, then click
  **Save**. The question is bookmarked (with its type and difficulty) and, if
  the answer box or code editor has content, that answer is stored with it.
- Clicking **Save** again with the same answer says "Already saved."; changing
  the answer and clicking **Save** stores a new saved answer for the question.
- Click **Saved Questions** (top right of either practice page) to browse your
  bookmarks. Selecting one shows the question and its saved answers; **Back**
  returns to the page you came from.
- **Remove question** deletes the bookmark **and all answers saved with it**.
  **Remove answer** deletes just the selected saved answer and keeps the
  question.
- Saved data is per user, and database access runs in the background so the
  window stays responsive while saving or loading.

AI evaluation results are not stored.

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

Database tests run against an in-memory H2 database with the real Flyway
migrations applied, so Docker is not needed to run the tests.

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
- **Accounts are stored in the database** (`user_account` table) with PBKDF2-hashed
  passwords; the app needs the MySQL connection settings described above.
  Accounts from the earlier `accounts.json` file were not migrated, so those
  users need to sign up again.
- **Java Version 25** will lead to failing tests. The project itself compiles and runs but the test will fail.
  To resolve it, a lower version is necessary. Either Java 17 or 21