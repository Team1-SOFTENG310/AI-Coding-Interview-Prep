# AI Coding Interview Preparation

**Team Name:** Team 1
**Team Members:** Scott Wallace, Gabriel Liu, Dylan Liddle, Neia Tererei, Kenny Geng, Dandan Wu, Shenol Peiris

This repository contains the setup scaffolding for the SOFTENG 310 A1 project.

## What is included

- Maven build configuration in `pom.xml`
- JavaFX application scaffold in `src/main/java/com/aicodinginterviewprep/App.java`
- Basic JUnit test in `src/test/java/com/aicodinginterviewprep/AppTest.java`
- `.gitignore` to exclude build artifacts

## Technology Stack

- Frontend: JavaFX
- Backend: Java
- Build Tool: Maven
- Testing: JUnit

## Prerequisites

- Java 17 JDK installed
- Maven wrapper is included; a separate Maven install is optional

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

## Notes

- This is setup-only scaffolding for the project; feature implementation can be added on top of this structure.

