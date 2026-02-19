# GitHub Actions CI/CD Workflows

This directory contains the CI/CD workflows for the AI Finance Manager project.

## Workflows Overview

### 1. CI Pipeline (`ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches
- Manual workflow dispatch

**Jobs:**
- **Backend Build & Test**: Builds the Spring Boot application, runs unit tests, integration tests, and generates code coverage reports
- **Frontend Build & Test**: Builds the React application, runs unit tests with coverage, and creates production build
- **E2E Tests**: Runs Cypress end-to-end tests with both backend and frontend running
- **Build Summary**: Provides a comprehensive summary of all test results

**Artifacts Generated:**
- Backend test results (`target/surefire-reports/`)
- Backend coverage report (`target/site/jacoco/`)
- Frontend build (`frontend/build/`)
- Frontend test coverage (`frontend/coverage/`)
- Cypress screenshots (on failure)
- Cypress videos

### 2. Pull Request Checks (`pr-checks.yml`)

**Triggers:**
- Pull request opened, synchronized, or reopened

**Jobs:**
- **PR Validation**: Validates PR title follows conventional commits format, checks for merge conflicts
- **Code Quality Check**: Runs checkstyle (Maven) and ESLint (frontend) if configured, identifies TODO/FIXME comments
- **Security Scan**: Runs Trivy vulnerability scanner, checks for hardcoded secrets

### 3. Dependency Check (`dependency-check.yml`)

**Triggers:**
- Weekly schedule (Monday at 8 AM UTC)
- Manual workflow dispatch
- Push to `main` with changes to `pom.xml`, `package.json`, or `package-lock.json`

**Jobs:**
- **Dependency Audit**: 
  - Checks for Maven dependency and plugin updates
  - Runs npm audit for security vulnerabilities
  - Lists outdated npm packages
- **OWASP Dependency Check**: Scans for known vulnerabilities in dependencies (fails on CVSS >= 7)

**Artifacts Generated:**
- Maven dependency reports
- NPM dependency reports
- OWASP dependency check report

### 4. Build and Release (`release.yml`)

**Triggers:**
- Push tags matching `v*.*.*` pattern (e.g., v1.0.0)
- Manual workflow dispatch with version input

**Jobs:**
- **Build Release Artifacts**: 
  - Builds production-ready backend JAR
  - Creates optimized frontend production bundle
  - Packages complete release with deployment guide
  - Creates GitHub release with artifacts

**Artifacts Generated:**
- Backend JAR: `ai-finance-manager-{version}.jar`
- Frontend bundle: `frontend-{version}.tar.gz`
- Complete release package: `ai-finance-manager-{version}-release.tar.gz`
- DEPLOYMENT.md guide

## Setup Instructions

### Prerequisites

1. **Repository Secrets** (if needed for deployment):
   - Go to Settings → Secrets and variables → Actions
   - Add any required secrets (e.g., deployment tokens)

2. **Branch Protection** (recommended):
   - Go to Settings → Branches
   - Add rule for `main` branch:
     - Require status checks to pass before merging
     - Require pull request reviews before merging
     - Require CI workflow to pass

### Local Testing

Before pushing, you can test the builds locally:

```bash
# Backend tests
mvn clean test
mvn verify

# Frontend tests
cd frontend
npm test
npm run build

# E2E tests
./test-integration.sh  # or your equivalent script
```

## Workflow Usage

### Running CI on Feature Branches

```bash
# Create a feature branch
git checkout -b feature/new-feature

# Make changes and commit
git add .
git commit -m "feat: add new feature"

# Push to trigger CI
git push origin feature/new-feature

# Create PR to trigger all checks
```

### Creating a Release

```bash
# Tag the release
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Or use manual workflow dispatch from GitHub Actions UI
```

### Manual Workflow Execution

1. Go to Actions tab in GitHub
2. Select the workflow you want to run
3. Click "Run workflow"
4. Fill in any required inputs
5. Click "Run workflow" button

## Troubleshooting

### CI Failures

1. **Backend Tests Failing:**
   - Check the backend test results artifact
   - Review the logs for specific test failures
   - Ensure database schema is up to date

2. **Frontend Tests Failing:**
   - Check the frontend test coverage artifact
   - Review component test failures
   - Ensure all dependencies are properly installed

3. **E2E Tests Failing:**
   - Check Cypress screenshots for visual debugging
   - Review Cypress videos to see test execution
   - Ensure backend and frontend are properly started

### Cache Issues

If you encounter cache-related problems:

1. Go to Actions tab
2. Click "Caches" in the left sidebar
3. Delete relevant caches
4. Re-run the workflow

## Best Practices

1. **Commit Messages**: Follow conventional commits format
   - `feat:` for new features
   - `fix:` for bug fixes
   - `docs:` for documentation
   - `test:` for test changes
   - `chore:` for maintenance tasks

2. **Pull Requests**: 
   - Keep PRs focused and small
   - Ensure all CI checks pass before requesting review
   - Address reviewer comments and re-run CI

3. **Testing**: 
   - Write tests for new features
   - Maintain good test coverage (aim for >80%)
   - Run tests locally before pushing

4. **Dependencies**: 
   - Regularly check for updates
   - Review security advisories
   - Update dependencies in separate PRs

## Workflow Badges

Add these badges to your README.md:

```markdown
![CI Pipeline](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml/badge.svg)
![PR Checks](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/pr-checks.yml/badge.svg)
![Dependency Check](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/dependency-check.yml/badge.svg)
```

## Maintenance

### Updating Workflows

1. Edit workflow files in `.github/workflows/`
2. Test changes in a feature branch
3. Review the workflow run results
4. Merge to main once validated

### Monitoring

- Regularly review workflow runs for failures
- Check dependency update reports weekly
- Monitor security scan results
- Review and address TODO/FIXME comments

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Documentation](https://maven.apache.org/guides/)
- [NPM Documentation](https://docs.npmjs.com/)
- [Cypress Documentation](https://docs.cypress.io/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)

