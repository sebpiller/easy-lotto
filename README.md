# Easy Loto

Made with Love for my Mother, a wonderful woman who loves lotos but cannot play them anymore the traditional way
for medical reasons.

I hope you will like this little helper app and that it will help you to continue to have quality time with us all.

I love you Mom!

<img alt="Loto" src="app/src/androidTest/assets/loto_grid.webp" width="200"/>


## Run in waydroid

To run this app in waydroid, follow these steps:

1. Install waydroid on your device.
2. Clone this repository.
3. Open the project in Android Studio.
4. Build and run the app on waydroid.

```shell
waydroid session start
```

```shell
waydroid app install app/build/outputs/apk/debug/app-debug.apk
waydroid app launch ch.sebpiller.easy.loto
```

## Release

```shell
keytool -genkeypair -v -keystore ~/spich/spich-playstore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias easyloto -storepass $KEYPASS -keypass $KEYPASS
````

---


### Easy Loto — Android camera app module

This project contains a modern, 100% client‑side Android app ready for Google Play Store submission. It is built with:

- Kotlin
- Jetpack Compose (Material 3)
- CameraX (Camera2 implementation)

The app requests camera permission at runtime, shows a live viewfinder, lets the user take a photo, and saves images to
the device:

- Android 10+ (API 29+): images are saved to MediaStore Pictures/EasyLoto without storage permissions.
- Android 7.0–9 (API 24–28): images are saved to the app’s external files directory (Pictures/EasyLoto), no legacy
  storage permission required.

#### Project structure

- `app/` — Android application module
    - `src/main/java/com/easy/loto/MainActivity.kt` — Compose UI + CameraX logic
    - `src/main/AndroidManifest.xml` — app manifest (camera permission, launcher activity)
    - `src/main/res/values/` — resources (`strings.xml`, `styles.xml`)
    - `proguard-rules.pro` — R8/ProGuard config

#### Requirements

- Android Studio Jellyfish+ (or newer)
- Android Gradle Plugin 8.6+
- JDK 17
- An Android device with a camera (emulator support depends on emulator camera passthrough)

#### Run locally

1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Select a physical device (recommended) or an emulator with camera support.
4. Run the `app` configuration. Grant the camera permission when prompted.

Tap “Take photo” to capture an image. On API 29+, photos are visible in the system gallery under Pictures/EasyLoto. On
API 24–28, they are stored in the app’s external files directory.



#### Play Store readiness notes

- This module is a client‑only app with no backend.
- App signing, Play Console configuration, icons/branding, privacy policy, and store listing are not included and must
  be provided by you.
- Minimum SDK is 24, target SDK 35.

#### Tech choices

- CameraX with `LifecycleCameraController` simplifies binding to lifecycle and capturing images.
- Compose UI + `AndroidView(PreviewView)` to render the camera preview.
- MediaStore on API 29+ for scoped storage compliance.
