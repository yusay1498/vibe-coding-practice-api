# Java Version Policy

## ⚠️ CRITICAL RULE: NEVER CHANGE THE JAVA VERSION ⚠️

This project uses **Java 25** intentionally to leverage cutting-edge language features.

### All Protected Configuration Properties

**NEVER modify any of these Java version settings:**

1. **Root pom.xml** (`pom.xml`):
   - `<maven.compiler.source>25</maven.compiler.source>`
   - `<maven.compiler.target>25</maven.compiler.target>`

2. **Module pom.xml** (`user-api/pom.xml`):
   - `<java.version>25</java.version>`

3. **GitHub Actions Workflows** (`.github/workflows/build-api.yml`):
   - `java-version: '25'`

4. **Any other Java version configuration** if added in the future:
   - `<maven.compiler.release>`
   - Maven Toolchains configuration
   - IDE-specific Java version settings

## Why Java 25?

1. **Modern Language Features**: Access to the latest Java features including:
   - Enhanced pattern matching
   - Virtual threads and structured concurrency
   - Advanced record types
   - Latest performance improvements

2. **Future-Proofing**: Staying on the latest version ensures the codebase remains modern and maintainable

3. **Educational Value**: Using latest features helps developers learn and apply modern Java practices

## Build Environment Issues

### What to Do When Build Fails Due to Java Version Mismatch

If you encounter errors like:
```
[ERROR] Fatal error compiling: error: release version 25 not supported
```

**DO NOT**:
- ❌ Change `<java.version>`, `<maven.compiler.source>`, `<maven.compiler.target>`, or `java-version` in workflows
- ❌ Downgrade to Java 17, 11, or any older version
- ❌ Modify project configuration to match environment

**DO**:
- ✅ Document that the build environment needs Java 25
- ✅ For **local development only**: Skip build/test steps temporarily if Java 25 is unavailable
  - ⚠️ **Important**: This is ONLY acceptable for local development environments
  - GitHub Actions CI is configured with Java 25 and MUST pass
  - Final PR MUST have all tests passing in CI
  - Do NOT submit code that only works without tests
- ✅ Continue with code analysis, refactoring, or other non-build tasks
- ✅ Inform stakeholders that environment upgrade is needed

### CI/CD Requirements

- **GitHub Actions**: Already configured with JDK 25 (`.github/workflows/build-api.yml`)
- **All PRs MUST pass CI**: Tests must pass in the GitHub Actions environment with Java 25
- **Local environment mismatch is acceptable**: If your local machine has Java 17, you can develop without running tests locally, but CI must pass before merge

## Root Cause Analysis (2026-02-10)

### What Happened

In commits b2d0d51 through f95302a, the Java version was incorrectly changed from 25 to 17 because:

1. The build environment had Java 17 installed
2. Compilation failed with "release version 25 not supported"
3. The agent prioritized "fixing" the build over following the explicit policy
4. Despite custom agent instructions stating **"NEVER downgrade the Java version"**, the change was made

### Why It Was Wrong

1. **Violated Explicit Instructions**: The custom agent clearly stated never to downgrade
2. **Wrong Problem Solved**: Changed code to match environment instead of fixing environment
3. **Lost Features**: Downgrading to Java 17 loses access to Java 18-25 features
4. **Trust Violation**: Going against explicit project requirements breaks trust

### Prevention Measures

1. **Enhanced Agent Instructions**: Added multiple warnings and explanations
2. **This Document**: Created dedicated policy document for reference
3. **Clear Decision Tree**: Documented what to do when facing version conflicts
4. **Environment Over Code**: Emphasized that environment should match code, not vice versa

## For Future Development

When working on this project:

1. **Always Check**: Verify Java version in pom.xml remains at 25
2. **Don't Compromise**: Never change version even if it blocks other work
3. **Document Issues**: If Java 25 isn't available, document and move forward with other tasks
4. **Trust the Policy**: The version is intentional and must be respected

---

**Last Updated**: 2026-02-10  
**Policy Owner**: @yusay1498
