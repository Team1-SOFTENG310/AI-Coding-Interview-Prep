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
  * Created Authenticator class and integrated it with the app (PR #34, #54, Issue #33)
  * Improved UI Design (PR #22, Issue #21)
  * Created draft UI appearance using stylesheets. (PR #10)
  * Created draft scenes (PR #9)
  * adjusted practice tab style and width (PR #5)
* **Other Contributions:**
  * Review on PR #62
  * Review on PR #17
  * Review on PR #16

### Gabriel Liu (@Gabeliu)
* **Code Contributions:**
  * Code editor syntax highlighting, voice input, auth fixes, and grading improvements - RichTextFX syntax highlighting, offline speech-to-text voice input on the Practice tab (Vosk, no OpenAI dependency), retry logic for flaky question generation, a stricter AI grading rubric, logged-in user indicator and log out, minimum window size (PR #57, closes #53 and #58)
  * Wire up sign up and log in to the Authenticator (PR #50)
  * Add LeetCode practice tab and redesign home/login screens (PR #52)
  * Remove stray editor lock file (PR #31, Issue #30)
  * Add AI-generated interview questions via OpenAI (PR #13, Issue #12)
  * Add initial JavaFX scaffold, Maven wrapper, documentation, and CI workflows (PR #3)
* **Other Contributions:**
  * Fix stale README, wiki wording, and fork-PR CI handling; close stale Issue #33, label Issue #26 (PR #59)
  * Review on PR #56
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
  * Review on PR #59
  * Review on PR #63

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
3. Copy `.env.example` to `.env` and set `OPENAI_API_KEY` - required for
   question generation and AI answer evaluation (voice input runs offline
   and doesn't need this).
4. Run the app with `./mvnw javafx:run`.
5. Run tests with `./mvnw test`.

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
- Wiki: this page is published on the [GitHub wiki](../../wiki) and mirrored here as `WIKI.md`

## Workflow Notes

While getting the SonarCloud and Snyk CI pipelines working (fixing auth tokens, the quality gate, and mvnw permissions), a handful of commits were pushed straight to main instead of going through an issue, feature branch, and reviewed pull request. This was a mistake made early on while we were still nailing down the required workflow. From this point on, all contributions go through: open/approve an issue -> feature branch off main -> PR referencing the issue -> review by another team member -> squash and merge. See #6.

PR #14 was opened from a personal fork instead of a branch on this repo. GitHub does not pass repository secrets (`SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION`) to workflows triggered by pull requests from forks, so the SonarCloud check failed with an "Not authorized / empty project key" error even though the code itself was fine. All team members already have push access to this repo, so branches should be pushed here directly rather than from a fork — see the updated CONTRIBUTING.md.

PR #25 was accepted by WhoWhatWhereAmI without a proper code review being completed first. Although the pull request was accepted with great quality, this did not follow our agreed review process because another team member should fully review the code before it is merged. This is identified as a workflow mistake, and future pull requests will require a proper review by another team member before being approved and merged.

PR #25 also was not merged with squash and merge, causing main branch consisting of many small commits. This also breaks our agreed review process. This is also identified as a workflow mistake for future pull requests to be aware, and use the squash and merge functionality to avoid overloading main branch. 

## Quality Tools & Vulnerability Analysis
A full scan of the project on `main` confirmed no open vulnerabilities:

![Snyk Clean Scan](images/snyk-clean-scan.png)

## Meeting Minutes

**Cadence:** In-person every Monday after lecture; online every Thursday at 7pm.

Each entry below should record: date and format (in-person/online), who attended,
topics discussed, decisions made (especially any change to how we manage the
project - workflow, labels, review process, scope), issues/PRs referenced, and
action items for the next meeting.

### Template


### DD-MM-YYYY (In-person / Online)
**Attendees:**<br>
**Topics discussed:** <br>
**Decisions made:** <br>
**Issues/PRs referenced:** <br>
**Action items:** <br>

### 24-08-2026 (In-person)
**Attendees:**Scott, Neia<br>
**Topics discussed:** <br>
**Decisions made:** <br>
**Issues/PRs referenced:** <br>
**Action items:** <br>

### 20-08-2026 (Online)
**Attendees:** Dylan, Scott, Shenol, Neia, Gabriel, Kenny, Dandan<br>
**Topics discussed:** Integrating Coding tab. adding voice input and fixing user authentication<br>
**Decisions made:** Voice input will run offline and will not need API key. User authentication will be done locally for now. coding tab current is plain, so we decided to style it up.<br>
**Issues/PRs referenced:** PR #28, PR #52<br>
**Action items:** Try and finalise the app and do the GitHub release and submit the repository link with the submission.

### 17-08-2026 (In-person)
**Attendees:** Dylan, Scott, Shenol, Neia, Gabriel, Kenny<br>
**Topics discussed:** API key working. update on UI. making A2 issues. separating Fxml files (scenes) to different files<br>
**Decisions made:** Decided to make custom A2 label for A2 issues related to part 2 of the project. checked on the current UI and gave feedback on how to improve it. made a scene for each tab in the app instead of everything in one java fle.<br>
**Issues/PRs referenced:** PR #9, PR #13, PR #16, Issue #12, Issue #15, PR #22, Issue #21, PR #25<br>
**Action items:** try and finalise the AI answers evaluation and question generation. try and improve the UI and its usability and finalise its controllers for javafx.

### 13-08-2026 ()
**Attendees:**<br>
**Topics discussed:**<br>
**Decisions made:**<br>
**Issues/PRs referenced:** <br>
**Action items:** No online meeting, everyone decided to use the time to work on their tasks as nothing much has happend.

### 10-08-2026 (In-person)
**Attendees:** Neia, Gabriel, Scott, Dandan, Kenny<br>
**Topics discussed:** Referenced PR #3 (set up PR). update on teams progress with the projects. talked to the lectuer about API key<br>
**Decisions made:** API key will take time so, AI question generation and AI answer evaluation can wait. Front-end can start without the AI components. We can use dummy data for now. <br>
**Issues/PRs referenced:** PR #3<br>
**Action items:** Work on UI, UI navigation. update documentation

### 06-08-2026 (Online)
**Attendees:** Dylan, Neia, Shenol, Gabriel, Scott, Dandan, Kenny<br>
**Topics discussed:** Whos was gonna do what in the project.<br>
**Decisions made:** made a google doc with each role written down to decide what roles to divide up between us.<br>
Kenny is PM and API setup, Scott and Neia working on front-end and user authentication, Dandan working on Data Management, Shenol chose to work on AI answer evaluation, Gabriel chose to do AI question generation, Dylan working on Code tab (Code editor, Coding questions). <br>
**Issues/PRs referenced:** none<br>
**Action items:** Git repo setup before next meeting and some progress made with assgined tasks.

### 03-08-2026 (In-person)
**Attendees:** Dylan, Neia, Shenol, Gabriel, Scott, Dandan, Kenny<br>
**Topics discussed:** Task breakdown, Overview of project. Talked about who wants to do what in the project.<br>
**Decisions made:** Gave people untill the next meeting to decided and finalise on what tasks they were going to do/contribute to.<br>
**Issues/PRs referenced:** none<br>
**Action items:** individual teamates decides what to do before next meeting.

<!-- Add one entry per meeting above this line, most recent first. -->

## Generative AI Tool Use

In line with the SOFTENG 310 assignment brief, team members disclose their
own use of generative AI tools below.

- **Gabriel Liu**: Used Claude for planning and guidance on new features,
  with some code written by the AI and reviewed before committing; used it
  to help debug issues that came up while getting the app running; some test
  cases were AI-generated; and used it for refactoring suggestions on
  existing code. All AI-assisted output was reviewed and tested before being
  committed.

- **Dandan Wu**: Used ChatGPT for:
  1: Analysing error messages and locating affected classes.
  2: Find xvfb solution to avoid SonarCloud running forever.
  3: Finding a method to fake Ai service without changing initial controller
  code.
  4: Generated follow up test cases from initially written test cases.
  5: Explanation of why unable to achieve particular branch for coverage.
  All AI-assisted output was reviewed and tested before committed.

- **Scott Wallace**: Used Copilot for assissting in planning integration of some features, and in troubleshooting issues.
  The primary use of AI was in assisting in refactoring the core SceneManager class and its implementations without breaking existing code.
  It was also used in troubleshooting to diagnose issues, and to generate roughly half of the test cases added by me.
  All AI-assisted output was reviewed and tested before committing.

- **Neia Tererei**: Used Claude for planning interface design and controller infrastructure; used it
  to construct CSS styling for fxml scenes; and used it for generating test cases on relevant
  controller classes. All AI-assisted output was reviewed and tested before being committed.

- **Shenol Peiris**: Used Gemini for initial brainstorming evaluation architecture, and Used it to help with test cases, for debugging issues. All AI-assisted output was reviewed and tested before being committed.

- **Dylan Liddle**: Used Claude for planning and integrating the coding questions tab and the related UI / answering panels. Used claude to generate test cases for relevant .java classes. All AI-assisted output was reviewed and tested before being committed.