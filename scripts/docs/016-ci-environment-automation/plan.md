# Implementation Plan: CI Environment Automation

This plan outlines the specific steps to implement the automated pre-build rewriting step within the GitHub Actions workflows.

## 🛠️ Step-by-Step Implementation

### Step 1: Design the Inline Python Rewriter Script
The rewriter script will load the `Platform.kt` file, perform a string replacement, and save it. By using Python, we ensure cross-platform compatibility across Linux (Ubuntu) and macOS runners without `sed` formatting headaches:

```bash
python3 -c "
with open('FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/Platform.kt', 'r') as f:
    text = f.read()
text = text.replace('val CURRENT_ENVIRONMENT = AppEnvironment.LOCAL', 'val CURRENT_ENVIRONMENT = AppEnvironment.AUTO')
with open('FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/Platform.kt', 'w') as f:
    f.write(text)
"
```

### Step 2: Update `release.yml`
1. Edit `.github/workflows/release.yml`.
2. Insert the Python rewriting step before the Android build (`assembleDebug`) and iOS build (`linkReleaseFrameworkIosArm64`).

### Step 3: Update `ci.yml`
1. Edit `.github/workflows/ci.yml`.
2. Insert the same Python rewriting step right before compiling the Kotlin Multiplatform shared library.

---

## 🧪 Verification Strategy

### 1. Build Verification
- Verify that both the continuous integration workflow (`ci.yml`) and the release compilation workflow (`release.yml`) build successfully without any compilation errors.
