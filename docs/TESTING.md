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

## Actual published delivery: passed on 2026-10-24

Owner-approved public fork: https://github.com/anrhangels/milo-home (default branch milo-main).

| Evidence | Result |
| --- | --- |
| [CI](https://github.com/anrhangels/milo-home/actions/runs/40106068402) | Unit tests, lint and optimized assembly passed |
| [Stable publishing](https://github.com/anrhangels/milo-home/actions/runs/40106068264) | Signed code10 published |
| [RC publishing](https://github.com/anrhangels/milo-home/actions/runs/40106190790) | Signed code11 published as prerelease |
| [Stable release](https://github.com/anrhangels/milo-home/releases/tag/test-v1.0.0) | APK + SHA-256 + metadata |
| [RC release](https://github.com/anrhangels/milo-home/releases/tag/test-v1.1.0-rc.1) | APK + SHA-256 + metadata |

Both releases use the same isolated test certificate and track=test. Neither is a production migration release.
PublishedReleaseTest, explicitly opted in with publishedReleases=true, cleared the emulator cache, discovered both channels
using the actual unauthenticated REST response, clicked Stable/Yes, downloaded from the real asset URL and passed the app's verifier.
No transport/metadata substitution was used. The device's cached Stable SHA-256 matched the published value:
`bc2a5603b6b9f7347e4930f018204013f566c8b0e1547c540aaf5877fc09c2f9`.

After standard installation to code10, the ordinary optimized launcher UI offered RC, downloaded it after Yes and opened the installer.
The independent test module verified the cached APK's package/code/certificate and emitted SHA-256:
`32df912d16b8611edf75f453a599158bbb82f2606e8e36a8f9b281e1f2defd96`, matching the real RC metadata.
Standard installation reached code11; preferences, columns and HOME checks passed. UID stayed10136 throughout this fresh online run.
The update button then disappeared because neither channel offered a higher compatible version. Disabled-package lists matched.
ByeByeDPI manual OFF survived the updates, and ON control successfully started its native foreground service afterwards.

The initial GitHub jobs failed because sdkmanager was not on PATH; both workflows now resolve its explicit SDK path.
One immediate post-install test raced Android package replacement; waiting for InstallSuccess and rerunning the independent
instrumentation passed. Do not start instrumentation merely because dumpsys already reports the new versionCode.

### Network limitation

The emulator's fresh/default ByeByeDPI VPN profile caused TLS handshakes to GitHub to close, including a simple upstream API read.
The actual online delivery tests above therefore ran with ByeByeDPI manually OFF. Earlier same-key local-fixture updates
proved a running backend's native PID survives ordinary launcher updates, but that does not prove GitHub connectivity with every DPI strategy.
The updater does not disable VPN, select a different network, weaken TLS or bypass certificate verification automatically.
Choose/test a suitable backend strategy separately. Nothing was changed on the real television.

To repeat: publish same-key test10/11 through the workflow, install isolated instrumented bootstrap9, run
PublishedReleaseTest with -e publishedReleases true, then confirm the standard installer. On optimized code10,
select RC through the ordinary UI, run UpdateChecks#testDownloadedRc and compare its emitted digest with published metadata;
confirm installation, wait for InstallSuccess, then run testPersistentData. Never install these fixtures on the existing Haier.

## Physical tests still pending

On Haier, only read-only state comparisons have been performed: all22 disabled packages and HOME remain unchanged.
No updater has been installed there. The missing old private key blocks an in-place transition; see SIGNING.md.
Physical installation, initial migration, any HOME fallback change and potential private-data loss require separate approval.
