# Installing Milo Home

[Home](../../README.en.md) · [Русский](README.md) · [With an AI assistant](../AI-INSTALL.en.md)

## Select the right build first

**Production Standard and ByeByeDPI editions are available: Stable1.2.0 / RC1.3.0-rc.1.** Tags beginning with `test-` are for an isolated emulator/test device, not your working launcher. Select the edition and compatible signing identity; do not substitute a test APK.

1. Read the channel and notes in [Releases](https://github.com/anrhangels/milo-home/releases).
2. Choose **production / Stable** for normal use. RC is an explicitly chosen preview.
3. Choose Standard or byedpi. Examples use Standard: download `MiloHome-standard.apk`, `MiloHome-standard.apk.sha256` and `update.json` from the same release. Verify size, SHA-256 and signing identity.
4. If `com.gathin.tvhome` is installed, compare certificates and versions **before installing**. Updates need a compatible signing identity and a higher `versionCode`.

The current Haier signing key is unavailable, so the new APK cannot update it in place. Do not uninstall to bypass this. [Signing and migration](../SIGNING.md).

## Choose your system

| Windows | Linux | macOS |
| --- | --- | --- |
| [PowerShell and ADB](windows.en.md) | [Terminal and ADB](linux.en.md) | [Terminal and ADB](macos.en.md) |

Use official [Android SDK Platform-Tools](https://developer.android.com/tools/releases/platform-tools). Reuse a working suitable ADB rather than installing a duplicate. Android Studio is not required just to run ADB.

## Connect the TV

The computer and TV need network access to each other. Enable developer options and debugging on the TV.

- For **wireless debugging with pairing**, use the displayed pairing address/port with `adb pair`, then its connection address/port with `adb connect`. The ports may differ.
- Some older/vendor TVs use `5555` **only if network ADB is already listening there**. USB debugging alone does not guarantee this.
- For USB-only debugging use the manufacturer's supported device-mode connection and [official ADB guide](https://developer.android.com/tools/adb). A TV's USB socket may not support device mode.
- For `unauthorized`, approve the RSA prompt with the physical remote. For connection refusal, check address, port and debugging mode; do not scan ports or alter firmware.

Always specify the exact target with `-s`, especially when an emulator is connected too.

## Verify the APK and installed signing identity

A checksum verifies file integrity; it does **not replace** signature verification. Android SDK Build-Tools provides `apksigner` and `aapt`. On Windows use `apksigner.bat` / `aapt.exe`; on Linux/macOS use `apksigner` / `aapt`, by their actual full paths:

```text
apksigner verify --verbose --print-certs MiloHome-standard.apk
aapt dump badging MiloHome-standard.apk
```

Check package `com.gathin.tvhome`, versionCode, minSdk, ABI and certificate. Also compare the certificate with [SIGNING](../SIGNING.md), not just metadata beside the APK. The production certificate is `0eb3e4397a0f1f2c05925c47da6bebeea0e757c41967642af051c774d72b36db`.

To inspect an installed APK, obtain its path with `shell pm path com.gathin.tvhome`, download the reported base.apk using `adb -s SERIAL pull "ACTUAL_PATH" before-MiloHome-standard.apk`, then verify it with the same apksigner. SDK Build-Tools is a separate component, not part of standalone platform-tools. Use the [AI-assisted workflow](../AI-INSTALL.en.md) if needed; do not guess compatibility.

A saved old APK and HOME state are useful recovery records, **not a guaranteed private-data backup**. Backup may be disabled. Do not promise settings survive a key change or uninstall.

## First launch and HOME

First open the app from installed apps and test arrows, OK, Back, volume and settings with the remote. Keep the factory launcher available.

Select Milo Home using default-app/home settings or a Home chooser if the firmware provides one. Installation itself does not select HOME. If this UI is absent, investigate the specific model rather than disabling the original launcher.

For an understood device, the optional `adb -s SERIAL shell cmd package set-home-activity com.gathin.tvhome/.MainActivity` changes HOME. Record the previous component and confirm it is available first. Some firmware does not support it. An assistant must obtain approval for the concrete plan before using this command.

## ByeByeDPI, updates and recovery

- The launcher works without the paired backend. ByeByeDPI control needs the [modified backend with the coordinated signing identity](../BYEBYEDPI.md). Do not automatically replace an official backend; inspect its certificate and data separately.
- Verified Stable/RC updates use the standard Android installer. Compatible updates preserve data, but each vendor/installer scenario still needs testing.
- If a ByeByeDPI strategy prevents GitHub access, investigate it separately or decide manually whether to turn it off for the download. The launcher does not do so itself. [Test evidence](../TESTING.md).
- Select the old HOME in system settings if necessary. If you changed it with ADB, restore the **actual component from before-home.txt**, not a guessed package name.
- Older APK codes may be rejected. [Recovery](../ROLLBACK.md) rebuilds the known working implementation with a higher code and compatible signature, not `-d`, uninstall or reset.

## After installation

Verify version/HOME, compare disabled packages with the saved list, and test Wi-Fi and the remote. ByeByeDPI must retain the user's chosen state. Do not rerun debloat.

## Sources

[ADB documentation](https://developer.android.com/tools/adb) · [Official Platform-Tools](https://developer.android.com/tools/releases/platform-tools) · [Android signing](https://developer.android.com/studio/publish/app-signing).
