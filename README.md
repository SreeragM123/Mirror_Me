# MirrorMe Android app

A beginner-friendly Android app written in Java using AndroidX CameraX. It shows a live front-camera preview in mirrored and unmirrored orientations and lets the user choose whether to save a captured photo to Pictures/MirrorMe.

## Requirements
- Android Studio
- JDK 17 (Android Studio's bundled JDK is usually suitable)
- Android SDK Platform 35
- Internet access for the first Gradle sync
- Android phone with a front camera (recommended for testing)

## Open and run
1. Extract this project folder.
2. In Android Studio choose **Open** and select the `MirrorMe` root folder (the one containing `settings.gradle.kts`).
3. Let Gradle sync and download dependencies.
4. Connect an Android phone with USB debugging enabled, or create an emulator.
5. Press Run and grant camera permission.

## Features
- Live front-camera preview.
- Mirror View / Others' View toggle.
- Take Photo button.
- After capture, choose Save or Discard.
- Saved photos go to `Pictures/MirrorMe` using Android MediaStore.

## Notes
- This is source code, not a prebuilt APK. It has not been compiled or device-tested in this environment.
- The live preview flip and the saved image orientation are separate. This starter app saves the normal unmirrored camera image; it does not yet provide an option to save a mirrored copy.
- `compileSdk` and `targetSdk` are set to 35. Android Studio may offer newer SDKs, but API 35 is sufficient for this project.
