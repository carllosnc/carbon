# Carbon Launcher

Minimalist Android launcher built with Kotlin + Jetpack Compose.

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Toolchain

- JDK 17 (source/target), AGP 8.7.3, Kotlin 2.0.21, Gradle 8.11.1 (via wrapper)
- minSdk 26 (Android 8.0), targetSdk 35, compileSdk 35
- Compose BOM 2024.12.01, Material 3, Navigation Compose

## Project structure

```
app/src/main/
├── AndroidManifest.xml          # HOME category intent filter -> launcher
├── java/com/carbon/launcher/
│   ├── MainActivity.kt          # Edge-to-edge, Home <-> Drawer transitions
│   ├── data/AppRepository.kt    # PackageManager -> installed apps list
│   └── ui/
│       ├── LauncherViewModel.kt # State: apps, isLoading, query
│       ├── theme/Theme.kt       # Material3 dark/light color schemes
│       ├── home/HomeScreen.kt   # Clock, dock (5 apps), swipe-up to drawer
│       ├── drawer/AppDrawer.kt  # Search field + 4-col grid
│       └── components/AppIcon.kt# Drawable -> ImageBitmap, AppIcon, AppGrid
└── res/                         # strings, themes, adaptive icon
```

## Make it the default launcher

After installing, press Home on the device and select **Carbon** as the default launcher. The manifest declares `CATEGORY_HOME` + `CATEGORY_DEFAULT`.

## Next steps (MVP roadmap)

- [ ] Dock customization (pin/unpin apps)
- [ ] Persistent dock + home-screen layout (DataStore)
- [ ] App drawer animations (slide instead of fade)
- [ ] Long-press app: uninstall / app info
- [ ] Hide-from-drawer list
- [ ] Wallpaper tinting / dynamic color (API 31+)
- [ ] Settings screen
