# CI/CD Quick Start Guide

This guide will help you get started with the CI/CD pipelines configured for this project.

## 🚀 Quick Start

### Step 1: Enable GitHub Actions

1. Go to your repository on GitHub
2. Click on the "Actions" tab
3. If prompted, click "I understand my workflows, go ahead and enable them"

### Step 2: First Push

```bash
# Make sure you're on the main or develop branch
git checkout main

# Push your code
git push origin main
```

The CI pipeline will automatically trigger and run all tests!

### Step 3: Monitor Your Build

1. Go to the "Actions" tab in your GitHub repository
2. Click on the latest workflow run
3. Watch the progress of each job:
   - ✅ Backend Build & Test
   - ✅ Frontend Build & Test
   - ✅ E2E Tests
   - ✅ Build Summary

## 📋 What Gets Tested

### Backend (Java/Spring Boot)
- **Build**: Maven clean install
- **Unit Tests**: All JUnit tests in `src/test`
- **Integration Tests**: Spring Boot integration tests
- **Code Coverage**: JaCoCo reports (minimum 80% recommended)

### Frontend (React)
- **Build**: Production build with optimizations
- **Unit Tests**: Jest + React Testing Library
- **Code Coverage**: Jest coverage reports
- **Linting**: ESLint checks (if configured)

### End-to-End
- **Cypress Tests**: Full application flow testing
- **Screenshots**: Captured on test failures
- **Videos**: Recorded for all test runs

## 🔄 Development Workflow

### Creating a Feature

```bash
# 1. Create a feature branch
git checkout -b feature/my-new-feature

# 2. Make your changes
# ... edit files ...

# 3. Run tests locally (recommended)
mvn test                    # Backend tests
cd frontend && npm test     # Frontend tests

# 4. Commit with conventional commits format
git add .
git commit -m "feat: add new feature description"

# 5. Push your branch
git push origin feature/my-new-feature
```

### Creating a Pull Request

1. Go to your repository on GitHub
2. Click "Pull requests" → "New pull request"
3. Select your feature branch
4. Click "Create pull request"
5. Fill in the PR template

**The following checks will run automatically:**
- ✅ CI Pipeline (full test suite)
- ✅ PR Validation (title format, conflicts)
- ✅ Code Quality (linting, style checks)
- ✅ Security Scan (vulnerability detection)

### Merging to Main

Once all checks pass and the PR is approved:

```bash
# Option 1: Merge via GitHub UI (recommended)
# Click "Squash and merge" or "Merge pull request"

# Option 2: Merge locally
git checkout main
git merge feature/my-new-feature
git push origin main
```

## 🏷️ Creating a Release

### Automatic Release (Recommended)

```bash
# 1. Ensure main branch is stable and all tests pass

# 2. Create and push a version tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 3. GitHub Actions will automatically:
#    - Build release artifacts
#    - Create a GitHub Release
#    - Attach JAR and frontend bundles
```

### Manual Release

1. Go to Actions → "Build and Release"
2. Click "Run workflow"
3. Enter the version (e.g., v1.0.0)
4. Click "Run workflow"

## 📊 Viewing Results

### Test Results

After a workflow completes:

1. Click on the workflow run
2. Scroll down to "Artifacts"
3. Download:
   - `backend-test-results`: JUnit test reports
   - `backend-coverage-report`: JaCoCo coverage HTML
   - `frontend-test-results`: Jest coverage reports
   - `cypress-videos`: E2E test recordings

### Coverage Reports

**Backend Coverage:**
1. Download `backend-coverage-report` artifact
2. Extract and open `index.html` in a browser
3. Navigate through package/class coverage

**Frontend Coverage:**
1. Download `frontend-test-results` artifact
2. Extract and open `lcov-report/index.html` in a browser

## 🔍 Debugging Failed Builds

### Backend Test Failures

```bash
# Run tests locally with verbose output
mvn test -X

# Run a specific test
mvn test -Dtest=YourTestClass

# Check test reports
cat target/surefire-reports/TEST-*.xml
```

### Frontend Test Failures

```bash
cd frontend

# Run tests in watch mode
npm test

# Run tests with coverage
npm run test:coverage

# Run specific test file
npm test -- YourComponent.test.js
```

### E2E Test Failures

```bash
cd frontend

# Open Cypress interactive mode
npm run cypress:open

# Run specific test
npm run cypress:run -- --spec "cypress/e2e/your-test.cy.js"
```

**Check artifacts:**
- Download `cypress-screenshots` for visual debugging
- Download `cypress-videos` to watch test execution

## 🛡️ Security & Quality

### Weekly Dependency Checks

Every Monday, the system automatically:
- Checks for outdated Maven dependencies
- Audits npm packages for vulnerabilities
- Runs OWASP dependency check
- Creates reports in artifacts

**Action Required:**
1. Review the workflow run
2. Check artifacts for update recommendations
3. Create PR to update dependencies if needed

### Pull Request Security Scans

Every PR automatically runs:
- Trivy vulnerability scanner
- Secret detection
- Dependency audits

**If vulnerabilities are found:**
1. Review the security scan results
2. Update affected dependencies
3. Re-run the checks

## 📈 Best Practices

### ✅ Do's

- ✅ Write tests for new features
- ✅ Run tests locally before pushing
- ✅ Use conventional commit messages
- ✅ Keep PRs small and focused
- ✅ Review coverage reports regularly
- ✅ Update dependencies monthly

### ❌ Don'ts

- ❌ Don't push directly to main (use PRs)
- ❌ Don't merge PRs with failing tests
- ❌ Don't ignore security warnings
- ❌ Don't commit secrets or credentials
- ❌ Don't skip code reviews

## 🔧 Configuration

### Environment Variables (CI)

Add secrets in: Settings → Secrets and variables → Actions

Common secrets needed:
- `GITHUB_TOKEN` (automatically provided)
- Add others as needed for deployment

### Branch Protection Rules

Recommended settings for `main` branch:

1. Go to Settings → Branches
2. Add rule for `main`:
   - ✅ Require pull request reviews (1 approval)
   - ✅ Require status checks to pass
   - ✅ Require branches to be up to date
   - ✅ Include administrators
   - Select required checks:
     - Backend Build & Test
     - Frontend Build & Test
     - E2E Tests

## 📚 Additional Resources

- [Main CI Workflow](.github/workflows/ci.yml)
- [PR Checks Workflow](.github/workflows/pr-checks.yml)
- [Dependency Check Workflow](.github/workflows/dependency-check.yml)
- [Release Workflow](.github/workflows/release.yml)
- [Detailed Workflow Documentation](.github/workflows/README.md)

## 🆘 Getting Help

### Common Issues

**Issue: "Maven build fails"**
- Check Java version (should be 21)
- Clear local Maven cache: `rm -rf ~/.m2/repository`
- Re-run: `mvn clean install`

**Issue: "npm install fails"**
- Delete `node_modules` and `package-lock.json`
- Re-run: `npm install`

**Issue: "E2E tests timeout"**
- Check if backend is running on port 8080
- Check if frontend is running on port 3000
- Increase timeout in cypress config

**Issue: "Workflow not triggering"**
- Check branch name matches trigger conditions
- Ensure Actions are enabled in repository settings
- Check workflow syntax with `yamllint`

### Support

If you encounter issues:
1. Check workflow logs in GitHub Actions
2. Review this guide and workflow documentation
3. Check individual test reports in artifacts
4. Review the detailed logs for specific errors

## 🎉 Success Indicators

Your CI/CD is working correctly when:
- ✅ All workflow jobs complete successfully
- ✅ Test coverage is maintained (>80%)
- ✅ No security vulnerabilities reported
- ✅ Dependencies are up to date
- ✅ Releases are created automatically
- ✅ Artifacts are properly generated

Happy coding! 🚀

