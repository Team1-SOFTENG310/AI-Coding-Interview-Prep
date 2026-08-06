# Starter Issue: Setup Sonar and Snyk

## Objective
Set up the development workflow for code quality and vulnerability analysis.

## Tasks
- Install and enable SonarLint in the IDE for continuous code analysis while writing code.
- Connect the GitHub repository to SonarCloud for main branch analysis.
- Add GitHub Actions workflows for SonarCloud and Snyk.
- Configure GitHub secrets:
  - `SONAR_PROJECT_KEY`
  - `SONAR_ORGANIZATION`
  - `SONAR_TOKEN`
  - `SNYK_TOKEN`
- Ensure the project builds with Maven and the JavaFX scaffold runs.

## Notes
- This issue is the starting point for compliance with the assignment requirement to use Sonar and Snyk.
- Keep this setup-only; do not add feature implementations yet.
