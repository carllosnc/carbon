# Carbon Launcher

Carbon is a minimalist Android launcher built with Kotlin and Jetpack Compose. It focuses on a dense, fast home experience with categorized apps, a customizable dock, quick settings, wallpaper selection, and a settings area for permissions and launcher organization.

## Highlights

- Home screen with live device stats, category filters, letter filters, app list, and dock.
- Customizable dock with pinned apps and add/remove animations.
- App details bottom sheet with version, size, cache, data, category, system-app status, install date, package name, and actions.
- Quick Settings screen with controls for dark mode, Do Not Disturb, lock screen, Wi-Fi, vibration, brightness, airplane mode, location, Bluetooth, battery, and volume.
- Battery card supports charging detection and an animated charging treatment.
- Settings screen with permission/status checks and subpages.
- Category ordering screen with up/down controls and local persistence.
- Wallpaper picker using bundled abstract wallpapers.
- iOS-style page transitions via Navigation Compose.
- Material 3 UI with support for dark/light theme behavior.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- SharedPreferences for local launcher preferences
- Gradle wrapper

## Requirements

- JDK 17
- Android Studio or Android SDK command-line tools
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.11.1 via wrapper
- minSdk 26
- targetSdk 35
- compileSdk 35

## Build

From the project root:

```bash
./gradlew assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install

Install on a connected device or emulator:

```bash
./gradlew installDebug
```

Or install a built APK manually:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Make Carbon the Default Launcher

After installing, press the device Home button and choose **Carbon** as the default launcher.

The app declares the launcher/home intent filters in `AndroidManifest.xml`:

- `android.intent.category.LAUNCHER`
- `android.intent.category.HOME`
- `android.intent.category.DEFAULT`

## Permissions

Some features require Android permissions or settings access:

- Usage access: used to show cache and data sizes for installed apps.
- Notification access: used for notification badges in the app list.
- Default launcher: required for Carbon to handle the Home action.
- Device admin: required for lock screen support.
- Write system settings: used for brightness controls.
- Notification policy access: used for Do Not Disturb controls.
- Wi-Fi/network state: used to display the current network name.
- Bluetooth connect: used to read/toggle Bluetooth state where Android allows it.
- Set wallpaper: used by the wallpaper picker.

Android restricts some system toggles, such as airplane mode and location, for third-party apps. When direct control is not allowed, Carbon opens the relevant system settings screen.

## Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/carbon/launcher/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── AppCategory.kt
│   │   ├── AppRepository.kt
│   │   ├── CategoryOrderPref.kt
│   │   ├── DockPref.kt
│   │   ├── LockDeviceAdminReceiver.kt
│   │   ├── NotificationBadgeService.kt
│   │   └── WallpaperPref.kt
│   └── ui/
│       ├── LauncherViewModel.kt
│       ├── components/
│       ├── home/
│       ├── quicksettings/
│       ├── settings/
│       ├── theme/
│       └── wallpaper/
└── res/
    ├── drawable/
    ├── mipmap-*/
    ├── values/
    └── xml/
```

## Local Preferences

Carbon stores user customization locally:

- Dock apps: `DockPref`
- Wallpaper selection: `WallpaperPref`
- Category ordering: `CategoryOrderPref`

These preferences are stored with Android `SharedPreferences`.

## Development Notes

Useful commands:

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew clean assembleDebug
```

If the launcher is already installed and set as default, reinstalling the debug APK keeps the same package name: `com.carbon.launcher`.

## Status

Carbon is in active development. Current focus areas include launcher customization, system controls, settings subpages, and polish for motion and layout behavior.
