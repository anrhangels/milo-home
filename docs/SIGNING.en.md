# Moving to Milo Home

[Installation](installation/README.en.md) · [Русский](SIGNING.md) · [Recovery](ROLLBACK.md)

This guide applies to Android TV and Google TV devices from different manufacturers. Firmware support for ADB and HOME selection varies; discover the fallback launcher on your own device.

## Choose the right route

| Installed state | Route | Data |
| --- | --- | --- |
| No `com.gathin.tvhome` package | Follow the installation guide; keep the factory launcher | Other apps remain installed |
| Same package, compatible signature | A: in-place update | App data is retained by the update |
| Changing Standard / ByeByeDPI | B: manual edition switch | Compatible signing permits an in-place update |
| Same package, incompatible certificate without a verified signing lineage | C: initial reinstall | Uninstall deletes private app data unless separately exported through a working method |

A matching app name is not proof of compatibility. A file SHA-256 checksum and a signing certificate SHA-256 fingerprint are different checks.

## Read-only preparation

Download the chosen APK from [Releases](https://github.com/anrhangels/milo-home/releases) and verify its file checksum using the OS installation guide. Android SDK Build-Tools supplies `apksigner` and `aapt` separately from Platform-Tools:

```text
apksigner verify --print-certs MiloHome-standard.apk
aapt dump badging MiloHome-standard.apk
```

For ByeByeDPI use `MiloHome-byedpi.apk`. The official production certificate for both editions and Stable/RC is:

`0eb3e4397a0f1f2c05925c47da6bebeea0e757c41967642af051c774d72b36db`

Check package `com.gathin.tvhome`, versionCode, minimum SDK and supported ABIs against the device. Do not trust a new certificate just because nearby metadata lists it.

Replace `SERIAL` with the device identifier. Use `./adb.exe`, `./adb` or a full executable path as appropriate:

```text
adb devices -l
adb -s SERIAL shell getprop ro.build.version.sdk
adb -s SERIAL shell getprop ro.product.cpu.abilist
adb -s SERIAL shell am get-current-user
adb -s SERIAL shell pm list users
adb -s SERIAL shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s SERIAL shell pm list packages -d
adb -s SERIAL shell dumpsys package com.gathin.tvhome
adb -s SERIAL shell pm path com.gathin.tvhome
```

Save the outputs locally, including HOME, user, disabled packages and app version. Confirm an unauthorized debugging request on the device; stop if the connection fails.

Create a local `old-apks` folder. Copy each path returned after `package:`:

```text
adb -s SERIAL pull APK_PATH old-apks/base.apk
apksigner verify --print-certs old-apks/base.apk
```

Save every split APK with a distinct filename when multiple paths appear. Compare the full certificate fingerprint, not the short `signatures` field in a system dump.

**An APK copy is not an app-data backup.** Record settings and app order manually or with screenshots. Milo Home currently has no universal settings export. Ordinary ADB does not guarantee access to a release app's private storage; `adb backup`, permission failures and an APK copy do not prove data recovery. Promise restoration only when a working export method has actually been verified.

## A. Compatible update

Use the same package, compatible signing and a higher versionCode. The built-in updater also keeps the current edition.

```text
adb -s SERIAL install -r MiloHome-standard.apk
```

Continue only after `Success`. Stop on `INSTALL_FAILED_UPDATE_INCOMPATIBLE` or `VERSION_DOWNGRADE`; do not add `-d` or automatically uninstall. Recheck the certificate and version, then perform the final checks below.

## B. Changing editions

Standard and ByeByeDPI share a package and cannot coexist. With the same certificate and a versionCode no lower than installed, manually install the other edition using `install -r`. The updater never switches editions itself.

RC code 103 cannot ordinarily update to Stable code 102: wait for a Stable with a higher code. Compare actual codes, not only version labels.

Standard does not remove the separate ByeByeDPI backend or stop its VPN. The ByeByeDPI edition needs the compatible paired backend; verify its APK and signing separately. A backend signing conflict must not trigger an unannounced uninstall.

## C. Incompatible signing: initial reinstall

Without a verified supported signing lineage, an in-place update is unavailable. The practical route is to save available records, prepare a working fallback HOME, uninstall the old app and install the new one. **Uninstall deletes private app data. Permission to install an APK is not consent to this loss.**

The following commands are intended for an ordinary user-installed APK with a single Android user. System/updated-system apps, shared UID, multiple profiles and administrator restrictions require a separate device-specific assessment. Do not guess with `--user 0`, `-k`, root or a factory reset.

### C1. Prepare a fallback HOME first

```text
adb -s SERIAL shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Select another installed, functioning launcher in device settings. Where supported and separately authorized:

```text
adb -s SERIAL shell cmd package set-home-activity FALLBACK_COMPONENT
```

Use a component discovered on this device. If these commands are unsupported, use manufacturer settings. Physically press Home and verify the fallback actually opens and ADB remains available. A Home remapping helper must not redirect back to the old app.

Enabling a previously disabled fallback is a separate authorized change; record the exception to the disabled-package list. If none is available, first prepare a trusted alternative launcher or manufacturer recovery route. **Do not uninstall before the fallback has passed this check.**

### C2. Readiness gate

- The new APK passes checksum, package, signing, version, SDK and ABI checks.
- The old APK and every split are saved with a known reinstall method.
- The fallback works with the physical remote and ADB is connected.
- Restorable settings and unrecoverable data are clearly listed.
- The owner explicitly agrees to uninstall and the stated data loss.
- No unresolved system, profile or administrative restrictions remain.

### C3. Reinstall

**Only after C1–C2**, on the agreed device:

```text
adb -s SERIAL uninstall com.gathin.tvhome
```

After `Success`:

```text
adb -s SERIAL install MiloHome-standard.apk
```

Use the matching APK and separately verified backend for ByeByeDPI. An incompatible existing backend needs its own data/migration agreement. Stop on any error and leave the fallback functioning.

### C4. Test before selecting HOME

Open Milo Home from the app list. Test directions, OK, Back, settings and Wi-Fi. Manually restore only settings actually recorded. Then select Milo Home as the default in device settings and check physical Home. Keep the fallback accessible.

### C5. Return to the old app if needed

First switch to the tested fallback. If the new package is absent, reinstall the saved old APK. If an incompatible new package is already installed, returning requires **separately authorized removal of that new installation**, losing its data.

For a single APK:

```text
adb -s SERIAL install old-apks/base.apk
```

For splits, use `adb install-multiple` with the complete saved APK set. The saved APK contains its old signature, so reinstalling does not require the old private key. **It does not restore deleted private data.** Select the old HOME again only after testing it.

## Final checks

Compare version, certificate, edition, HOME and disabled packages with the initial records. Disabled packages must match except for explicitly approved fallback changes. Test remote buttons, held volume, Wi-Fi, settings and Home; for ByeByeDPI also check backend connectivity and VPN state. Migration does not require unrelated app, system-package or network changes.

## Future publisher key rotation

Keep the permanent key outside Git, pass it through Secrets and maintain an independent protected backup of both key and password. Updating across certificates requires a supported, verified signing lineage and compatible Android/firmware. Do not assume an arbitrary new certificate works on Android 11. The updater does not authorize another key just because JSON announces it.

Sources: [Android app signing](https://developer.android.com/studio/publish/app-signing), [apksigner](https://developer.android.com/tools/apksigner), [ADB](https://developer.android.com/tools/adb).
