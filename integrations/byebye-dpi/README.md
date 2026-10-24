# Paired ByeByeDPI backend

Upstream: https://github.com/romanvht/ByeByeDPI

Base commit: `4ca350c1831c405010cd7548c3d89ddbbe68f68a`.

The integration patch is GPLv3, retaining upstream copyright and licence.
Clone upstream recursively, checkout this commit, then apply `git apply tv-control.patch`.
Build using the upstream Gradle wrapper and SDK/NDK requirements. No compiled binaries or private keys are included.
Sign backend and launcher with the coordinated identity; see ../../docs/SIGNING.md.
The protected bound Messenger service checks calling package/UID/signature. VPN consent remains a system dialogue.

Changed files:
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/java/io/github/romanvht/byedpi/receiver/BootReceiver.kt
- app/src/main/java/io/github/romanvht/byedpi/services/ServiceManager.kt
- app/src/main/java/io/github/romanvht/byedpi/tv/ControlEvents.kt
- app/src/main/java/io/github/romanvht/byedpi/tv/ControlOperations.kt
- app/src/main/java/io/github/romanvht/byedpi/tv/ControlPolicy.kt
- app/src/main/java/io/github/romanvht/byedpi/tv/ControlSnapshot.kt
- app/src/main/java/io/github/romanvht/byedpi/tv/TvControlService.kt
- app/src/main/java/io/github/romanvht/byedpi/tv/VpnConsentActivity.kt
- app/src/main/res/layout/activity_file.xml
- app/src/main/res/values-en/strings.xml

## Included installer source

`source.zip` contains the complete corresponding patched backend source and native dependencies (GPLv3), without APKs, keys or build outputs. It is included inside GitHub's normal Source code archives, so no extra release assets are required.

Build with `python tools/milo/build_backend.py --out build/backend-source` from the repository root. Install Android platform 36, NDK 27.0.12077973 and CMake 3.22.1 first. The helper normalizes line endings, enables ARM32/ARM64/x86/x86_64 and sets backend version 1821 / 1.8.2-milo.1. It runs backend tests and produces an unsigned universal APK. Sign with the coordinated launcher key using `tools/milo/sign_backend.py`, then pass its absolute path in `MILO_BACKEND_APK` when building the ByeByeDPI launcher edition. Production Actions performs these steps automatically. Standard never includes the backend APK.

Source archive SHA-256: `0d60961a08aff6a90306b4d268dca77ad54f790abab96a0ebaa3ed5aac1551d1`.
