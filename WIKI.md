# AI Coding Interview Prep Wiki

## Project Overview

AI Coding Interview Prep is an AI-supported interview preparation tool that helps students and job seekers practise coding and software engineering questions.

## Team

- Team Name: Team 1
- Members: Scott Wallace, Gabriel Liu, Dylan Liddle, Neia Tererei, Kenny Geng, Dandan Wu, Shenol Peiris
- Course: University of Auckland SOFTENG 310

## Setup

1. Install Java 17 JDK.
2. Use the included Maven wrapper:
   - Windows: `./mvnw.cmd`
   - macOS/Linux: `./mvnw`
3. Run the app with `./mvnw javafx:run`.
4. Run tests with `./mvnw test`.

## Quality Tools

- SonarLint should be enabled in the IDE for continuous code analysis.
- SonarCloud is configured for repository scanning on `main` and pull requests.
- Snyk is configured for vulnerability scanning on `main` and weekly schedules.

## Documentation

- README: project overview and setup instructions
- CODE_OF_CONDUCT.md: contributor expectations
- CONTRIBUTING.md: contribution process
- TASKS.md: A1 tasks and A2 vision
- Issue templates: bug report and feature request
- Wiki: this page can be copied into the GitHub wiki

## Workflow Notes

While getting the SonarCloud and Snyk CI pipelines working (fixing auth tokens, the quality gate, and mvnw permissions), a handful of commits were pushed straight to main instead of going through an issue, feature branch, and reviewed pull request. This was a mistake made early on while we were still nailing down the required workflow. From this point on, all contributions go through: open/approve an issue -> feature branch off main -> PR referencing the issue -> review by another team member -> squash and merge. See #6.

PR #14 was opened from a personal fork instead of a branch on this repo. GitHub does not pass repository secrets (`SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION`) to workflows triggered by pull requests from forks, so the SonarCloud check failed with an "Not authorized / empty project key" error even though the code itself was fine. All team members already have push access to this repo, so branches should be pushed here directly rather than from a fork — see the updated CONTRIBUTING.md.
