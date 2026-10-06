# Workflow Notes

## Current workflow (Assignment 2 - Group 2)

Group 2 works from a fork of the main repository:

- **Fork (where branches go):** [`se310-fairshare/AI-Coding-Interview-Prep`](https://github.com/se310-fairshare/AI-Coding-Interview-Prep)
- **Upstream (where PRs go):** [`Team1-SOFTENG310/AI-Coding-Interview-Prep`](https://github.com/Team1-SOFTENG310/AI-Coding-Interview-Prep), branch `main`
  Steps for every contribution:

1. Open or pick up an issue on `se310-fairshare/AI-Coding-Interview-Prep`.
2. Create a new feature branch from the fork's `main` for that issue. 
3. Open a pull request from that branch into `Team1-SOFTENG310/AI-Coding-Interview-Prep:main`, referencing the issue.
4. Get a review from another Group 2 member.
5. Squash and merge.

Keep all development branches on the fork. Do not create feature branches in the upstream repository.

### A2 workflow mistakes

**TODO**: Add the mistakes we've made.

## Assignment 1 history (Group 1)

> **Note:** The workflow and mistakes below describe Assignment 1 (Group 1). They are kept as a record and do **not** describe the current workflow. In particular, the A1 rule against opening PRs from forks has been replaced by the A2 fork-based workflow above.

### A1 agreed workflow

All contributions went through: open/approve an issue -> feature branch off main -> PR referencing the issue -> review by another team member -> squash and merge. See #6.

### A1 workflow mistakes

#### Direct commits to main

While getting the SonarCloud and Snyk CI pipelines working (fixing auth tokens, the quality gate, and mvnw permissions), a handful of commits were pushed straight to main instead of going through an issue, feature branch, and reviewed pull request. This was a mistake made early on while we were still nailing down the required workflow. From that point on, all contributions went through: open/approve an issue -> feature branch off main -> PR referencing the issue -> review by another team member -> squash and merge. See #6.

#### PR from a fork (PR #14) - A1 only

PR #14 was opened from a personal fork instead of a branch on this repo. GitHub does not pass repository secrets (`SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION`) to workflows triggered by pull requests from forks, so the SonarCloud check failed with an "Not authorized / empty project key" error even though the code itself was fine. At the time, all Group 1 members had push access to this repo, so branches were meant to be pushed here directly rather than from a fork.

#### Merged without review (PR #25)

PR #25 was accepted by WhoWhatWhereAmI without a proper code review being completed first. Although the pull request was accepted with great quality, this did not follow our agreed review process because another team member should fully review the code before it is merged. This is identified as a workflow mistake, and future pull requests will require a proper review by another team member before being approved and merged.

#### Not squash-merged (PR #25)

PR #25 also was not merged with squash and merge, causing main branch consisting of many small commits. This also breaks our agreed review process. This is also identified as a workflow mistake for future pull requests to be aware, and use the squash and merge functionality to avoid overloading main branch.