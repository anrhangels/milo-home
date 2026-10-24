# Update testing and evidence

## Executed locally

- 231 JVM launcher tests, zero failures/errors; Android lint zero errors; optimized release assembly successful.
- Nine publisher unit tests (versions, channels, metadata).
- Real HTTPS GitHub REST release read against the upstream public repository on Android11.
- Android instrumentation: offline startup keeps the main thread responsive and update button hidden.
- UI fixture: green button right of Settings, Stable/RC selection, confirmation No prevents download,
  streamed signed APK bytes show bounded real progress and pass the production verifier.
- Android Settings unknown-source permission and standard Package Installer: bootstrap9→Stable10→RC11.
- Independent framework-only runner verifies preferences, columns setting and HOME after both installs.
  Package UID remained identical; ByeByeDPI foreground native PID remained identical across both updates.
- Isolated test-key→permanent-key v3 rotation of both launcher and backend accepted on API30;
  preferences, UID, HOME, matching signatures and signature permission checked.
  This proof does not apply to the installed Haier certificate whose private key is unavailable.
- Real-APK publisher checks: correct production accepted; wrong certificate, code, name, debuggable build
  and tampered APK rejected. Backend source patch passes git apply --check against its recorded base.
- Public-tree privacy scan and git diff --check passed.

The first post-RC instrumentation attempt raced package replacement and reported process termination;
after installation settled the independent check passed. The original AndroidX test APK could not run
against the minimized target because a shared Kotlin dependency had been removed by R8. The independent
runner under tools/device-tests avoids target dependency sharing; production launcher dependencies were not expanded.

## Reproduce isolated tests

Use a dedicated API30 emulator, always specify its serial, and never substitute a physical device.
Do not remove a real installation to run fixtures. Fixture APKs are external files, excluded from Git.

1. Build same-key, track=test bootstrap9, Stable10, RC11 as in BUILDING.md. Save Stable/RC APKs outside Git.
2. Set MILO_TEST_ASSETS to that directory containing MiloHome.apk (Stable10) and MiloHome-rc.apk (RC11).
3. Build bootstrap9 with miloInstrumented and assembleReleaseAndroidTest. Install only on the emulator.
4. Run LiveGitHubTest, OfflineUpdaterTest and UpdaterUiTest individually using AndroidJUnitRunner.
   The UI test injects local metadata/transport inside the signed test process; it is not GitHub delivery evidence.
5. Use InstallerGateTest to open Android Settings, grant permission through that UI, then open the installer.
   Confirm Install normally. Wait for package code10 and installation completion.
6. Build tools/device-tests with the launcher Gradle wrapper and the same test signing key. Install its separate APK.
   Run org.milohome.devicechecks.UpdateChecks#testPersistentData via android.test.InstrumentationTestRunner.
7. Run UpdateChecks#testStageRc, confirm the standard installer, wait for code11, then rerun testPersistentData.
   This stages a verified local RC fixture; it is separate from the GitHub download path.
8. Record HOME, UID, backend service/PID and disabled packages before/after. Use a valid old→new lineage only for keys
   you possess; rotate both paired emulator packages, then run testRotatedPair. Never apply this test lineage to Haier.
9. Run tools/milo/check_apk_fixtures.py with actual APK/tool paths and public certificate fingerprints.

## Pending owner-approved online and physical tests

Repository creation, first push, environment secrets and workflow runs require publication approval.
Publish test Stable10 and RC11 through Actions; install bootstrap9 only on the isolated emulator, then test actual
REST detection, asset download, checksum and standard install from those Releases. Keep test metadata separate from production.
These Actions→Release→download steps have NOT passed yet; no public fork/release URL is claimed.

On Haier, only read-only state comparisons have been performed: all22 disabled packages and HOME remain unchanged.
No updater has been installed there. The missing old private key blocks an in-place transition; see SIGNING.md.
Physical installation, initial migration, any HOME fallback change and potential private-data loss require separate approval.
