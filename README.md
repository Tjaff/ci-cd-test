# CI/CD lab: GitHub Actions for a Maven Java project

A small Java 25 / Maven / JUnit 5 project used to learn how to build a CI/CD pipeline with GitHub Actions.

**Start here: [docs/LAB-GUIDE.md](docs/LAB-GUIDE.md)**

In the lab you will:

1. Build and unit test the project automatically on every push.
2. Break the build on purpose and learn to read a failing run.
3. Run CI on pull requests and block merging while tests fail.
4. Save the jar and test reports as build artifacts.
5. Fix a bug test-first, verified by CI.
6. Publish a versioned GitHub Release automatically when you push a tag.

## Run it locally

```bash
./mvnw verify                                   # compile, test, package
java -jar target/ci-cd-demo-1.0.0-SNAPSHOT.jar  # run the app
```

Requires JDK 25 or newer. Maven is not required; the Maven Wrapper downloads it.
