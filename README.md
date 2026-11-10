# PlayKit

Reusable Play in-app review and in-app update stack for Android apps.

## Installation

Add JitPack to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency:

```kotlin
dependencies {
    implementation("com.github.umirtech:PlayKit:v1.0.0")
}
```

## Usage

```kotlin
PlayKit.init(this)                          // Application.onCreate
PlayKit.onSuccessfulRun()                   // after a successful run

PlayKit.review.promptPending               // collect while the app is resumed
PlayKit.review.consumePrompt()             // or launchReview(activity)
PlayKit.update.check(applyCooldown = true) // Activity.onResume (automatic)
```

Both features are best-effort and no-op on side-loads/emulators.

See the source for the full API.
