# AI Coding Interview Prep Wiki

## Project Overview

AI Coding Interview Prep is an AI-supported interview preparation tool that helps students and job seekers practise coding and software engineering questions.

## Team

- Team Name: Team 1
- Members: Scott Wallace, Gabriel Liu, Dylan Liddle, Neia Tererei, Kenny Geng, Dandan Wu, Shenol Peiris
- Course: University of Auckland SOFTENG 310

## Contributions

### Scott Wallace (@Swal289)
* **Code Contributions:**
  * Improved UI Design (PR #22, Issue #21)
  * Created draft UI appearance using stylesheets. (PR #10)
  * Created draft scenes (PR #9)
  * adjusted practice tab style and width (PR #5)
* **Other Contributions:**
  * Review on PR #17
  * Review on PR #16

### Gabriel Liu (@Gabeliu)
* **Code Contributions:**
  * Remove stray editor lock file (PR #31, Issue #30)
  * Add AI-generated interview questions via OpenAI (PR #13, Issue #12)
  * Add initial JavaFX scaffold, Maven wrapper, documentation, and CI workflows (PR #3)
* **Other Contributions:**
  * Stop contributors from forking; guard SonarCloud against fork PRs (PR #17, Issue #18)
  * Document early direct-to-main commits as a workflow mistake (PR #7, Issue #6)
  * Revise README with project details and description (PR #1)
  * Review on PR #28

### Dylan Liddle (@DL-Eng-Acc)
* **Code Contributions:**
  * Add Coding tab: Code editor, AI evaluation (PR #28)
* **Other Contributions:**

### Neia Tererei (@rbntres)
* **Code Contributions:**
  * UI tab controllers (PR #25)
* **Other Contributions:**
  * Review on PR #31
  * Review on PR #7
  * Review on PR #3
  * Review on PR #22
  * Review on PR #10
  * Review on PR #5
  * Review on PR #32

### Kenny Geng (@imke11)
* **Code Contributions:**
  * Oversight of the whole project as the project manager
* **Other Contributions:**
  * 

### Dandan Wu (@WhoWhatWhereAmI)
* **Code Contributions:**
  * Test/UI navigation (PR #32)
* **Other Contributions:**
  * Review on PR #25

### Shenol Peiris (@nikopeiris)
* **Code Contributions:**
  * Implement AI answer evaluation module and test suite (PR #16, Issue #15)
* **Other Contributions:**
  * Correcting and editing documentation (PR #49, Issues #45, #46, #47)
  * Review on PR #13
  * Review on PR #1

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

PR #25 was accepted by WhoWhatWhereAmI without a proper code review being completed first. Although the pull request was accepted with great quality, this did not follow our agreed review process because another team member should fully review the code before it is merged. This is identified as a workflow mistake, and future pull requests will require a proper review by another team member before being approved and merged.

PR #25 also was not merged with squash and merge, causing main branch consisting of many small commits. This also breaks our agreed review process. This is also identified as a workflow mistake for future pull requests to be aware, and use the squash and merge functionality to avoid overloading main branch. 

## Quality Tools & Vulnerability Analysis
A full scan of the project on `main` confirmed no open vulnerabilities:

![Snyk Clean Scan](images/snyk-clean-scan.png)
