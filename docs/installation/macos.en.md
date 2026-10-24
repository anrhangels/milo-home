# Installing on macOS

[Overview and compatibility](README.en.md) · [Русский](macos.md)

**Complete the overview’s edition/signature checks first. Commands below use Standard; for byedpi choose its APK and check the paired backend separately.**

## 1. Prepare ADB

[Download official Platform-Tools](https://developer.android.com/tools/releases/platform-tools) for your system, extract the ZIP and open a terminal **inside platform-tools**. These commands do not require a system PATH change.

```bash
./adb version
```

If macOS displays a security warning, verify that the ZIP came from Android Developers. Do not disable Gatekeeper or recursively remove protection attributes. Use a normal user Terminal, without sudo.

## 2. Connect the TV

Replace `TV_IP:ADB_PORT` with the actual connection address shown by the TV. For pairing, **first** run `./adb pair TV_IP:PAIR_PORT` using its displayed code. Do not confuse the pairing port with the connection port.

```bash
serial="TV_IP:ADB_PORT"
./adb connect "$serial"
./adb devices -l
./adb -s "$serial" get-state
```

Approve the debugging prompt on the TV. Proceed only with state `device`. Use port5555 only if this firmware actually provides an enabled legacy network ADB listener there.

## 3. Record the starting state

```bash
./adb -s "$serial" shell pm list packages -d > before-disabled.txt
./adb -s "$serial" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME > before-home.txt
./adb -s "$serial" shell dumpsys package com.gathin.tvhome > before-package.txt
```

Keep these records locally. If the package exists, compare signing identities and version codes using the [overview](README.en.md#verify-the-apk-and-installed-signing-identity). Stop on an incompatible certificate; do not uninstall.

## 4. Verify the download

Place the compatible production APK and its `.sha256` from the same release in the current directory. This check does not replace apksigner.

```bash
shasum -a 256 --check MiloHome-standard.apk.sha256
```

## 5. Install and launch

Only after successful checks and selecting the correct device:

```bash
./adb -s "$serial" install -r ./MiloHome-standard.apk &&
  ./adb -s "$serial" shell am start -n com.gathin.tvhome/.MainActivity
```

**Do not run the launch command if installation fails.** Investigate first. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indicates a signing mismatch; `VERSION_DOWNGRADE` means a lower code. Do not add `-d`, uninstall or clear app data.

The paired ByeByeDPI backend is installed separately **before the launcher**, after its own signing/data checks. This guide does not download or replace it automatically.

## 6. Check the remote and preserved state

Select HOME and verify recovery using the [overview](README.en.md#first-launch-and-home). Keep the factory launcher available. After installation:

```bash
./adb -s "$serial" shell pm list packages -d > after-disabled.txt
./adb -s "$serial" shell dumpsys package com.gathin.tvhome > after-package.txt
diff -u before-disabled.txt after-disabled.txt
```

The lists should match. Check Wi-Fi, ByeByeDPI state, Home, arrows, OK, Back and volume. Afterwards you may disconnect just this ADB connection: `./adb disconnect "$serial"`.

[Recovery](../ROLLBACK.md) · [Signing issues](../SIGNING.en.md) · [Home](../../README.en.md).
