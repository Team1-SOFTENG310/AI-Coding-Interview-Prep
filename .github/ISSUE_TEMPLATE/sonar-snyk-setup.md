name: Sonar/Snyk Setup
about: Starter issue for configuring SonarLint, SonarCloud, and Snyk in the repository.
title: Setup SonarCloud and Snyk
labels: setup, security, quality

## Objective
Set up the initial code quality and vulnerability scanning workflow for the project.

## Tasks
- Enable SonarLint in the IDE for continuous analysis while writing code.
- Connect the repository to SonarCloud for main branch analysis.
- Add GitHub Actions workflows for SonarCloud and Snyk.
- Configure GitHub secrets:
  - `SONAR_PROJECT_KEY`
  - `SONAR_ORGANIZATION`
  - `SONAR_TOKEN`
  - `SNYK_TOKEN`
- Confirm Maven builds successfully with the JavaFX scaffold.

## Notes
- This setup issue is the first step for the assignment and should be completed before any feature work.
- Do not implement application features in this issue.
