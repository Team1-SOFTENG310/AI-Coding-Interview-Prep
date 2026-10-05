# Workflow Notes

## Agreed workflow

All contributions go through: open/approve an issue -> feature branch off main -> PR referencing the issue -> review by another team member -> squash and merge. See #6.

## Workflow mistakes

### Direct commits to main

While getting the SonarCloud and Snyk CI pipelines working (fixing auth tokens, the quality gate, and mvnw permissions), a handful of commits were pushed straight to main instead of going through an issue, feature branch, and reviewed pull request. This was a mistake made early on while we were still nailing down the required workflow. From this point on, all contributions go through: open/approve an issue -> feature branch off main -> PR referencing the issue -> review by another team member -> squash and merge. See #6.

### PR from a fork (PR #14)

PR #14 was opened from a personal fork instead of a branch on this repo. GitHub does not pass repository secrets (`SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION`) to workflows triggered by pull requests from forks, so the SonarCloud check failed with an "Not authorized / empty project key" error even though the code itself was fine. All team members already have push access to this repo, so branches should be pushed here directly rather than from a fork — see the updated CONTRIBUTING.md.

### Merged without review (PR #25)

PR #25 was accepted by WhoWhatWhereAmI without a proper code review being completed first. Although the pull request was accepted with great quality, this did not follow our agreed review process because another team member should fully review the code before it is merged. This is identified as a workflow mistake, and future pull requests will require a proper review by another team member before being approved and merged.

### Not squash-merged (PR #25)

PR #25 also was not merged with squash and merge, causing main branch consisting of many small commits. This also breaks our agreed review process. This is also identified as a workflow mistake for future pull requests to be aware, and use the squash and merge functionality to avoid overloading main branch.