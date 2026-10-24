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
