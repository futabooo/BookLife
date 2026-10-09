# BookLife
Unofficial Android application of bookmeter(https://bookmeter.com/)

|![](https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/header.png)|
|:-:|

## Preview
![](https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/1.gif)
![](https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/2.gif)

## ScreenShots
<img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-0.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-1.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-2.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-3.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-4.png" width="240">

<a href='https://play.google.com/store/apps/details?id=com.futabooo.android.booklife&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="92" width="240"/></a>

## How to build
Requirements: **JDK 21** (set `JAVA_HOME`), Android SDK (compileSdk 37; set `ANDROID_HOME` or `local.properties`).

```
$ ./gradlew :app:assembleDebug        # debug APK
$ ./gradlew :app:testDebugUnitTest    # unit + Robolectric Compose tests
$ ./gradlew :app:lintDebug
$ ./gradlew :app:bundleRelease        # release AAB (R8 minify + resource shrinking)
```

#### keystore.properties (optional, release signing)
Without it the release build is signed with the bundled `debug.keystore`, so CI and forks still build.
To sign with your own key:
```
$ cp keystore.properties.template keystore.properties
```

```txt:keystore.properties
storeFile=[your keystore filename]
storePassword=[your keystore password]
keyAlias=[your key alias]
keyPassword=[your key password]
```

#### google-services.json (optional, Firebase)
Create a Firebase project and put `google-services.json` in `app/`. It enables Crashlytics and Analytics.
Without it the Google Services / Crashlytics plugins are skipped and Firebase is a no-op.
(Fabric is no longer used; there is no `fabric.properties`.)

#### Open source licenses
The licenses screen is generated at build time by [AboutLibraries](https://github.com/mikepenz/AboutLibrariesGradlePlugin); no manual step is needed.

#### CI
GitHub Actions (`.github/workflows/android.yml`) builds the debug APK and runs the unit tests with JDK 21.

## Accounts
The app does not create accounts; it signs in to your existing bookmeter.com account. Account creation and deletion are handled by bookmeter.com. Credentials are sent only to bookmeter.com over HTTPS and are not stored on the device (only a session cookie is kept, and removed on sign-out). See the [privacy policy](public/privacy_policy.html) and the [Play compliance report](docs/play-compliance-report.md).

## Android skills
The official [android/skills](https://github.com/android/skills) used for the modernization (Navigation 3, edge-to-edge, R8, security and Play policy audits, ...) were installed with:
```
$ android skills add --all --project=.
```

## How to remote build with mainframer
See https://github.com/gojuno/mainframer/tree/development/samples/gradle-android

Japanese document [Androidのリモートビルドにmainframerを使ってみる - Qiita](https://qiita.com/futabooo/items/ed70efdd3929ebbfd161)

## Tech stack
- Kotlin, Jetpack Compose (Material 3), [Navigation 3](https://developer.android.com/guide/navigation/navigation-3)
- Hilt, Coroutines / Flow, DataStore, AndroidX Splash Screen
- [OkHttp](https://github.com/square/okhttp), [Retrofit](https://github.com/square/retrofit), kotlinx.serialization
- [jsoup](https://jsoup.org/) (HTML parsing of bookmeter.com)
- [Coil](https://github.com/coil-kt/coil)
- Google Code Scanner (ML Kit barcode scanning)
- Firebase Crashlytics / Analytics
- [AboutLibraries](https://github.com/mikepenz/AboutLibraries)
- [Timber](https://github.com/JakeWharton/timber)
- Tests: JUnit, Robolectric, Compose UI test, MockWebServer

## Licenses
https://github.com/futabooo/BookLife/blob/master/LICENSE
