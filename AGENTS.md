# Carbon Launcher

Minimalist Android launcher built with Kotlin + Jetpack Compose. Single-activity, NavHost-driven, edge-to-edge.

## Build & Run

```bash
./gradlew assembleDebug          # debug build
./gradlew installDebug           # install on connected device/emulator
./gradlew clean assembleDebug    # clean build
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. No tests configured yet.

## Toolchain

- JDK 17 (source/target), AGP 8.7.3, Kotlin 2.0.21, Gradle 8.11.1 (wrapper)
- minSdk 26 (Android 8.0), targetSdk 35, compileSdk 35
- Compose BOM 2024.12.01, Material 3, Navigation Compose, coroutines 1.9.0
- Version catalog: `gradle/libs.versions.toml` (single source of truth for deps)

## Project structure

```
app/src/main/
├── AndroidManifest.xml
│   # Permissions: QUERY_ALL_PACKAGES, PACKAGE_USAGE_STATS, BIND_NOTIFICATION_LISTENER,
│   # WRITE_SETTINGS, ACCESS_NOTIFICATION_POLICY, BLUETOOTH_CONNECT, wifi/network state
│   # Components: MainActivity (HOME+LAUNCHER), LockDeviceAdminReceiver, NotificationBadgeService
├── java/com/carbon/launcher/
│   ├── MainActivity.kt
│   │   # Activity + NavHost (routes: home, settings, quick_settings, wallpaper, category_order)
│   │   # Holds ALL system state: permissions, battery, wifi, brightness, toggles, dock, wallpaper
│   │   # Defines launch/uninstall/openAppSettings + ~15 open*Settings() helpers
│   │   # Registers package-removed + battery BroadcastReceivers
│   │   # 36KB — candidate to split (see "Refactor candidates" below)
│   ├── data/
│   │   ├── AppRepository.kt        # PackageManager -> List<AppModel>; categorization + storage stats
│   │   ├── AppCategory.kt          # enum (16 categories, order field)
│   │   ├── AppModel                # data class in AppRepository.kt (packageName,label,icon,category,sizeMb,...)
│   │   ├── DockPref.kt             # SharedPreferences "carbon_dock"; MAX_DOCK_APPS=12
│   │   ├── CategoryOrderPref.kt    # SharedPreferences "carbon_categories"; orderMap()
│   │   ├── WallpaperPref.kt        # SharedPreferences "carbon_wallpaper"; 13 webp drawables
│   │   ├── DynamicColorPref.kt     # SharedPreferences "carbon_theme"; Material You toggle (API 31+)
│   │   ├── LockDeviceAdminReceiver.kt  # DeviceAdminReceiver for lockScreen()
│   │   └── NotificationBadgeService.kt # NotificationListenerService; StateFlow<Map<pkg,subtitle>>
│   └── ui/
│       ├── LauncherViewModel.kt    # AndroidViewModel; StateFlow<LauncherState{apps,isLoading,query}>
│       ├── theme/Theme.kt          # CarbonTheme(darkTheme); transparent system bars
│       ├── components/
│       │   ├── AppIcon.kt          # Drawable.toImageBitmap(), AppIcon, AppListRow, AppList (staggered fade-in)
│       │   └── ListItemRow.kt      # Reusable settings/info row (title/subtitle/leading/trailing)
│       ├── home/HomeScreen.kt
│       │   # ClockWidget (RAM/Storage/Temp), LetterBar (A-Z filter), CategoryFilter, AppList, DockRow
│       │   # AppInfoSheet (ModalBottomSheet: version/size/cache/data/category/installed/package)
│       │   # Uninstall flow (Confirming->Loading->Success AlertDialog state machine)
│       │   # FABs: quick settings, lock screen, settings (badge if missing permission)
│       │   # 36KB — candidate to split (see "Refactor candidates" below)
│       ├── drawer/AppDrawer.kt     # 4-col grid grouped by category (used? check HomeScreen routing)
│       ├── quicksettings/QuickSettingsScreen.kt  # wifi/bt/brightness/DnD/dark mode/airplane/location/lock
│       ├── settings/SettingsScreen.kt           # permission status rows + status + appearance + about
│       ├── settings/CategoryOrderScreen.kt      # reorder categories via up/down arrows
│       └── wallpaper/WallpaperPickerScreen.kt   # grid of 13 webp wallpapers
└── res/                            # values(strings/colors/themes), drawable(13 webp wallpapers), xml, mipmap
```

## Make it the default launcher

After installing, press Home and select **Carbon**. Manifest declares `CATEGORY_HOME` + `CATEGORY_DEFAULT`.

## Code conventions

- **State**: lifted to `MainActivity` as `mutableStateOf` fields, passed down as params to composables (no Hilt/DI). `LauncherViewModel` only owns the apps list.
- **Persistence**: `SharedPreferences` via `object` singletons in `data/` (`DockPref`, `CategoryOrderPref`, `WallpaperPref`). No DataStore/Room yet.
- **Composables**: stateless, params in, callbacks out. Route constants in `LauncherRoute` (private object in MainActivity).
- **Navigation**: Navigation Compose, slide transitions (320ms tween), `launchSingleTop = true`.
- **Categorization**: `AppRepository.packageRules` (keyword lists) + `ApplicationInfo.category` + game keywords. Order defined by `AppCategory.order` and user-reordered `CategoryOrderPref`.
- **Icons**: `Drawable.toImageBitmap()` extension (in AppIcon.kt) — handles BitmapDrawable + generic Drawable via Canvas.
- **No comments in code** unless necessary. Follow existing style.
- **Strings**: currently hardcoded English in composables (no `stringRes` except app_name). UI strings are NOT localized.

## Current state (implemented)

- [x] Dock customization (pin/unpin via long-press sheet, up to 12 apps, persists)
- [x] Persistent dock + category order + wallpaper (SharedPreferences)
- [x] App drawer animations (slide NavHost transitions, staggered row fade-in)
- [x] Long-press app: uninstall / app info sheet / clear cache+storage / dock pin
- [x] Settings screen (permission status, developer mode, app count, about)
- [x] Quick settings (brightness, DnD, bluetooth, dark mode, lock, wifi/bt/airplane/location settings)
- [x] Notification badges (NotificationListenerService -> subtitle in app list)
- [x] Lock screen (DeviceAdminReceiver)
- [x] Category filter + reorder + A-Z letter bar
- [x] Clock widget with RAM/Storage/Battery-temp mini widgets
- [x] Wallpaper picker (13 bundled webp)
- [x] Dynamic color / Material You (API 31+, toggle in Settings; fallback to default scheme)

## Refactor candidates (lower priority, help token efficiency)

- `MainActivity.kt` (792 lines): extract system-state + permission helpers into a dedicated controller/ViewModel. Currently every edit reads the whole file.
- `HomeScreen.kt` (869 lines): split `ClockWidget`, `LetterBar`, `CategoryFilter`, `DockRow`, `AppInfoSheet`, uninstall dialogs into separate files under `ui/home/`.
- `QuickSettingsScreen.kt` (683 lines): split toggle tiles from sliders/header.

## Roadmap (not yet started)

- [ ] Hide-from-drawer list
- [ ] DataStore migration (replace SharedPreferences)
- [ ] Localization (extract hardcoded strings to res/values/strings.xml)
- [ ] Tests (unit + UI; none configured)
