# Lab: CI/CD for a Maven Java project with GitHub Actions

In this lab you will build a small but complete pipeline for a Java project:

* **Continuous Integration (CI):** every push and every pull request is built and unit tested automatically.
* **Continuous Delivery (CD):** when you tag a version, GitHub builds the jar and publishes it as a downloadable release.

You will also break the build on purpose, so you know what a failing pipeline looks like and how to read it.

**Estimated time:** 2.5 to 3 hours (exercises 1 to 6). Bonus exercises are optional.

---

## Contents

0. [Before you start](#0-before-you-start)
1. [Key concepts](#1-key-concepts)
2. [Exercise 1: Your first CI workflow](#exercise-1-your-first-ci-workflow)
3. [Exercise 2: Break the build](#exercise-2-break-the-build)
4. [Exercise 3: CI on pull requests and protecting `main`](#exercise-3-ci-on-pull-requests-and-protecting-main)
5. [Exercise 4: Keep the build output (artifacts)](#exercise-4-keep-the-build-output-artifacts)
6. [Exercise 5: Fix a bug test-first](#exercise-5-fix-a-bug-test-first)
7. [Exercise 6: Continuous Delivery with releases](#exercise-6-continuous-delivery-with-releases)
8. [Bonus exercises](#bonus-exercises)
9. [Troubleshooting](#troubleshooting)
10. [Cheat sheet](#cheat-sheet)

---

## 0. Before you start

### You need

* A GitHub account
* Git installed (`git --version`)
* JDK 25 or newer (`java -version`)
* An editor or IDE (IntelliJ IDEA, VS Code, Eclipse...)

You do **not** need Maven installed. The project includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), which downloads the right Maven version the first time you run it.

### Get your own copy of the project

Every student works in their **own** repository, because GitHub Actions runs per repository.

1. Open the course repository on GitHub.
2. Click **Use this template → Create a new repository**.
3. Owner: you. Name: for example `ci-cd-lab`. Visibility: **Public**.
   *Public repositories get free GitHub Actions minutes on standard runners, so this lab costs you nothing.*
4. Clone your new repository:

   ```bash
   git clone https://github.com/<your-username>/ci-cd-lab.git
   cd ci-cd-lab
   ```

> **No "Use this template" button?** Fork the repository instead, then open the **Actions** tab in your fork and click **I understand my workflows, go ahead and enable them**. Forks have Actions switched off by default.

### Build it locally first

A pipeline only automates what you can already do by hand. Run the build on your machine:

```bash
./mvnw verify          # macOS / Linux / Git Bash
mvnw.cmd verify        # Windows cmd / PowerShell
```

You should see something like:

```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 1
[INFO] BUILD SUCCESS
```

Then run the app:

```bash
java -jar target/ci-cd-demo-1.0.0-SNAPSHOT.jar
```

### Look around the project

```
.
├── pom.xml                         Maven build: Java 25, JUnit 5, runnable jar
├── mvnw, mvnw.cmd, .mvn/           Maven Wrapper
├── src/main/java/se/lab/cicd/
│   ├── App.java                    main() — prints a few results
│   ├── Calculator.java
│   └── TextUtils.java
└── src/test/java/se/lab/cicd/
    ├── CalculatorTest.java         5 unit tests
    └── TextUtilsTest.java          4 unit tests (1 is @Disabled — you will need it later)
```

**Question 0.1:** Which Maven lifecycle phase compiles the code, and which one runs the unit tests? What does `verify` include?

---

## 1. Key concepts

| Term | Meaning |
|---|---|
| **Workflow** | An automated process, defined in a YAML file in `.github/workflows/`. A repository can have many. |
| **Event** (trigger) | What starts a workflow: `push`, `pull_request`, a tag, a schedule, a manual click... Set with `on:`. |
| **Job** | A group of steps that runs on one fresh virtual machine. Jobs run in parallel unless you say otherwise. |
| **Runner** | The machine that runs a job. `ubuntu-latest` is a Linux VM hosted by GitHub, wiped after every job. |
| **Step** | One task in a job: either a shell command (`run:`) or a reusable **action** (`uses:`). |
| **Action** | A reusable building block, e.g. `actions/checkout` (gets your code) or `actions/setup-java` (installs a JDK). |
| **Artifact** | A file produced by a run that you want to keep, e.g. a jar or test reports. |

The pipeline you are about to build:

```
 git push ──► GitHub ──► runner VM:  checkout ─► install JDK ─► mvn verify (compile + test + package)
                                                                    │
                                                         ✅ green  or  ❌ red on the commit / PR
 git push tag v1.0.0 ──► runner VM:  ... ─► mvn verify ─► create GitHub Release with the jar
```

---

## Exercise 1: Your first CI workflow

**Goal:** Every push builds the project and runs the tests on GitHub.

1. In your local clone, create the folder and file `.github/workflows/ci.yml` (the path matters exactly):

   ```yaml
   name: CI

   on: [push, pull_request]

   jobs:
     build:
       runs-on: ubuntu-latest
       timeout-minutes: 10

       steps:
         - name: Check out code
           uses: actions/checkout@v5

         - name: Set up JDK 25
           uses: actions/setup-java@v5
           with:
             distribution: temurin
             java-version: '25'
             cache: maven

         - name: Build and run tests
           run: mvn -B verify
   ```

2. Commit and push:

   ```bash
   git add .github/workflows/ci.yml
   git commit -m "Add CI workflow"
   git push
   ```

3. On GitHub, open the **Actions** tab. Click the running workflow, then the `build` job, and expand each step to read its log.
4. When it has finished, go to the repository's front page. Next to the latest commit there is now a green check mark ✅.

**Questions**

* 1.1 What does each of the three steps do? What would happen if you removed `actions/checkout`?
* 1.2 The VM is brand new for every run. What does `cache: maven` save, and where in the log can you see it?
* 1.3 Why do we use `-B` (batch mode) in CI?
* 1.4 What is `timeout-minutes` protecting you against?

---

## Exercise 2: Break the build

**Goal:** See what a failing pipeline looks like and practise reading the log. This is the situation CI exists for.

1. Introduce a bug. In `Calculator.java`, change `add`:

   ```java
   public int add(int a, int b) {
       return a - b;   // oops
   }
   ```

2. Commit and push **without running the tests locally** (just this once):

   ```bash
   git commit -am "Refactor add"
   git push
   ```

3. Watch the **Actions** tab. The run turns red ❌.
4. Open the failed run and find:
   * which step failed,
   * which test failed,
   * the expected and the actual value.

   Tip: the line starting with `[ERROR] Failures:` is a good place to start. You may also get an e-mail from GitHub about the failed run.
5. Fix the bug, push again, and confirm the run is green.

**Questions**

* 2.1 Which test failed, and what message did it give?
* 2.2 Was the jar produced in the failed run? Why or why not?
* 2.3 In a team of ten developers, why is it better that this was caught by CI rather than by a colleague who pulled your code?

---

## Exercise 3: CI on pull requests and protecting `main`

**Goal:** Changes reach `main` through pull requests, and a pull request cannot be merged while the tests fail.

### 3a. Run CI on a pull request

1. Create a branch and add a test to `CalculatorTest.java`:

   ```bash
   git switch -c more-tests
   ```

   ```java
   @Test
   void multiplyingByZeroGivesZero() {
       assertEquals(0, calc.multiply(42, 0));
   }
   ```

2. Push the branch and open a pull request:

   ```bash
   git commit -am "Add multiply-by-zero test"
   git push -u origin more-tests
   ```

   GitHub shows a **Compare & pull request** button. Click it and create the PR.
3. Look at the bottom of the PR page: the CI check runs on the PR.

You may notice the workflow ran **twice**: once for the `push` to the branch and once for the `pull_request`. That wastes runner time. Change the trigger in `ci.yml` so that pushes only trigger CI on `main`, while pull requests still trigger it from any branch:

<details>
<summary>Hint</summary>

`push` can take a `branches:` filter. `pull_request` can stay as it is. You will need to expand `on:` from the short list form to the longer map form.

</details>

Commit the change on your branch and push. Check that the next push gives only one run.

### 3b. Protect `main`

1. Go to **Settings → Rules → Rulesets → New ruleset → New branch ruleset**.
2. Name: `protect-main`. Enforcement status: **Active**.
3. Target branches: **Add target → Include default branch**.
4. Enable **Require a pull request before merging** (set required approvals to 0 if you work alone).
5. Enable **Require status checks to pass**, click **Add checks** and pick `build`.
6. Save.

*(On older GitHub UIs this is **Settings → Branches → Add branch protection rule**.)*

### 3c. Try to merge a broken PR

1. On your `more-tests` branch, make a test fail on purpose (for example change the expected value to `1`), commit and push.
2. Look at the PR. The merge button is blocked while the `build` check is red.
3. Fix the test, push, wait for green, then **merge** the PR.
4. Locally: `git switch main && git pull`.

**Questions**

* 3.1 Why is a failing check on a PR better than a failing check on `main`?
* 3.2 Try `git push` directly to `main` now. What happens?

---

## Exercise 4: Keep the build output (artifacts)

**Goal:** Save the test reports and the jar from each run, so you can download them from GitHub.

Everything on the runner is deleted when the job ends. Use the action [`actions/upload-artifact`](https://github.com/actions/upload-artifact) to keep files.

Add two steps at the end of the `build` job:

1. Upload `target/surefire-reports/` as an artifact named `test-reports`. It should be uploaded **even when the tests fail**, because that is exactly when you want the reports.
2. Upload `target/*.jar` as an artifact named `app-jar`.

Keep storage use low with `retention-days: 5`.

<details>
<summary>Hint 1: the upload step</summary>

```yaml
- name: Upload ...
  uses: actions/upload-artifact@v5
  with:
    name: ...
    path: ...
    retention-days: 5
```

</details>

<details>
<summary>Hint 2: run a step even if an earlier one failed</summary>

By default a step is skipped when an earlier step fails. Look up `if: always()`.

</details>

Do this on a branch with a PR (your `main` is protected now). When the run is done, open it and scroll to **Artifacts** at the bottom of the summary page. Download `app-jar`, unzip it and run:

```bash
java -jar ci-cd-demo-1.0.0-SNAPSHOT.jar
```

**Questions**

* 4.1 What happens to the `app-jar` upload when the tests fail? Is that what you want?
* 4.2 Why is it a good idea to build the artifact once in CI rather than on a developer's laptop?

---

## Exercise 5: Fix a bug test-first

**Goal:** Use the pipeline the way a team does every day: a failing test describes the bug, the fix makes it pass, CI confirms it before merge.

The product owner reports: *"'Never odd or even' is a palindrome, but the app says it isn't."*

1. Create a branch `fix-palindrome`.
2. In `TextUtilsTest.java`, find the test `palindromeIgnoresSpacesAndPunctuation` and **remove the `@Disabled` line** (and the now-unused import if your IDE complains).
3. Commit, push, open a PR. CI goes red. Read in the log which assertion fails.
4. Fix `TextUtils.isPalindrome` so it ignores spaces and punctuation (and still ignores case).
5. Run `./mvnw verify` locally, then push. CI goes green. Merge the PR.

<details>
<summary>Hint</summary>

Remove every character that is not a letter or a digit before comparing. `String.replaceAll` with a regular expression is one way; a loop with `Character.isLetterOrDigit` is another.

</details>

**Questions**

* 5.1 Why did we write (enable) the test before the fix?
* 5.2 Why do teams sometimes use `@Disabled`, and why is it dangerous to leave tests disabled?

---

## Exercise 6: Continuous Delivery with releases

**Goal:** Pushing a version tag such as `v1.0.0` builds, tests and publishes the jar as a GitHub Release, automatically.

### Background

The `pom.xml` uses a *CI-friendly version*:

```xml
<version>${revision}</version>
...
<revision>1.0.0-SNAPSHOT</revision>
```

So the version can be overridden from the command line without editing the file. Try it locally:

```bash
./mvnw -B package -Drevision=9.9.9
ls target/*.jar           # ci-cd-demo-9.9.9.jar
```

### Task

Create a second workflow, `.github/workflows/release.yml`, that:

1. runs only when a tag starting with `v` is pushed,
2. checks out the code and sets up JDK 25 (same as CI),
3. builds and tests with the version taken from the tag (`v1.0.0` → `1.0.0`),
4. creates a GitHub Release for the tag and attaches the jar.

<details>
<summary>Hint 1: trigger on tags</summary>

```yaml
on:
  push:
    tags: [ 'v*' ]
```

</details>

<details>
<summary>Hint 2: get the version from the tag</summary>

GitHub sets the environment variable `GITHUB_REF_NAME` to the tag name (`v1.0.0`). In bash, `${GITHUB_REF_NAME#v}` removes the leading `v`.

</details>

<details>
<summary>Hint 3: create the release</summary>

The GitHub CLI `gh` is pre-installed on the runners:

```bash
gh release create "$GITHUB_REF_NAME" target/*.jar --title "$GITHUB_REF_NAME" --generate-notes
```

It needs a token in the environment variable `GH_TOKEN`; use `${{ github.token }}`. The token must be allowed to write to the repository, so add this at the top level of the workflow:

```yaml
permissions:
  contents: write
```

</details>

Get `release.yml` onto `main` through a PR. Then create and push a tag from an up-to-date `main`:

```bash
git switch main
git pull
git tag v1.0.0
git push origin v1.0.0
```

Watch the **Release** workflow in the Actions tab. When it is green, open **Releases** on the repository's front page, download `ci-cd-demo-1.0.0.jar` and run it.

Then make a small visible change (for example a new line printed in `App.java`), merge it through a PR, and release `v1.1.0`.

**Questions**

* 6.1 What is the difference between *Continuous Delivery* and *Continuous Deployment*? Which one did you just build?
* 6.2 Why does the release workflow run the tests again, even though CI already ran them on `main`?
* 6.3 What would happen if you pushed the tag `v1.0.0` a second time? (You can try: `git push origin v1.0.0` after deleting and re-creating it.)
* 6.4 Why is `permissions: contents: write` only in the release workflow and not in CI?

---

## Bonus exercises

**B1. Status badge.** Add a CI badge to `README.md`:

```markdown
![CI](https://github.com/<your-username>/<your-repo>/actions/workflows/ci.yml/badge.svg)
```

**B2. Build matrix.** Make CI build on both Java 25 and Java 26 using `strategy.matrix`. How many jobs run now? What happens to the required status check name `build`? *(Matrix builds multiply runner time; on private repositories that matters.)*

**B3. Don't build for docs.** Changing only `README.md` or `docs/` still runs CI. Use `paths-ignore` so documentation-only changes skip the build. What problem can this cause with a required status check?

**B4. Cancel outdated runs.** If you push three commits quickly, three runs start. Add a `concurrency` group with `cancel-in-progress: true` so only the newest one keeps running.

**B5. Manual runs.** Add `workflow_dispatch` to `on:` and start CI from the Actions tab with **Run workflow**.

**B6. Least privilege.** Add `permissions: contents: read` at the top of `ci.yml`. Why is this good practice?

---

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| Nothing appears in the Actions tab | The file is not in exactly `.github/workflows/` and/or does not end in `.yml`/`.yaml`; or Actions is disabled (forks: enable it on the Actions tab; or **Settings → Actions → General**). |
| "Invalid workflow file" | YAML indentation. Use spaces, never tabs. Every list item under `steps:` starts with `-`. |
| `./mvnw: Permission denied` (locally on macOS/Linux) | `chmod +x mvnw` (and `git update-index --chmod=+x mvnw` if you commit it). |
| CI fails with `release version 25 not supported` | `java-version` in `setup-java` is lower than 25. |
| `WARNING: A terminally deprecated method in sun.misc.Unsafe has been called` at the start of the build | Harmless. It comes from a library inside Maven itself running on Java 25, not from your code. |
| Release workflow fails with `HTTP 403` / `Resource not accessible by integration` | `permissions: contents: write` is missing, or **Settings → Actions → General → Workflow permissions** is restricted (the `permissions:` key in the workflow normally overrides that). |
| Pushing a tag did nothing | You created the tag but did not push it: `git push origin v1.0.0`. Or the tag does not match `v*`. |
| `release.yml` exists but the tag run does not start | The workflow file must exist in the commit the tag points to. Tag a commit that contains `release.yml`. |
| Workflow ran twice for one push | Both `push` and `pull_request` triggered; see exercise 3a. |

---

## Cheat sheet

```bash
# Build and test locally
./mvnw verify
./mvnw test -Dtest=CalculatorTest        # run one test class

# Branch, push, PR
git switch -c my-branch
git commit -am "message"
git push -u origin my-branch

# Release
git tag v1.2.0
git push origin v1.2.0

# Delete a tag (local and remote)
git tag -d v1.2.0
git push origin --delete v1.2.0
```

Useful documentation:

* GitHub Actions: <https://docs.github.com/actions>
* Workflow syntax: <https://docs.github.com/actions/reference/workflow-syntax-for-github-actions>
* Building Java with Maven: <https://docs.github.com/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-maven>
* `actions/setup-java`: <https://github.com/actions/setup-java>
