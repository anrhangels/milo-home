<p align="center"><a href="README.md">Русский</a> · <strong>English</strong></p>

![Milo Home — your TV, less noise, more home](docs/brand/hero-en.webp)

<h1 align="center">Milo Home</h1>
<p align="center">A calmer home screen for Android TV.<br>Your apps, your remote, and updates on your terms.</p>

<p align="center">
<a href="docs/installation/README.en.md"><strong>Install</strong></a> ·
<a href="docs/AI-INSTALL.en.md"><strong>Install with an AI assistant</strong></a> ·
<a href="https://github.com/anrhangels/milo-home/releases">Releases</a> ·
<a href="#screenshots">Screenshots</a>
</p>

<p align="center">
<a href="https://github.com/anrhangels/milo-home/actions/workflows/ci.yml"><img alt="Build checks" src="https://github.com/anrhangels/milo-home/actions/workflows/ci.yml/badge.svg?branch=milo-main"></a>
<img alt="Minimum Android version: 8" src="https://img.shields.io/badge/Android-8%2B-a8aff4?labelColor=101321">
<a href="LICENSE"><img alt="MIT licence" src="https://img.shields.io/badge/Code-MIT-ebc17e?labelColor=101321"></a>
</p>

## Less noise. More you.

Milo Home is an independent fork of **Gathin23**'s [CleanYourTV](https://github.com/Gathin23/cleanyourtv). It keeps the native Java interface, themes and remote navigation, adding a practical network menu and verified GitHub updates.

| Your home screen | Network within reach | Updates with a choice |
| --- | --- | --- |
| Large app tiles, a clock, wallpapers and D-pad navigation. No launcher advertising feed. | The Wi-Fi button opens network settings and control of the paired ByeByeDPI build. | Stable or RC, confirmation before downloading and a circular indicator based on actual received bytes. |

There is no persistent updater service. Launch-time checks are asynchronous, with a 24-hour success cache. Downloads are checked for SHA-256, cryptographic signature, package identity, version and compatibility before opening Android's standard installer.

<a id="screenshots"></a>

## The interface, in use

The cover uses the owner's actual Haier 32 Smart TV BX photograph in the project's new visual frame. The photographed interface has not been redrawn.

![Version selection and a genuine GitHub RC](docs/brand/updates-en.webp)

*Left: a menu demonstration with labelled local fixture metadata. Right: a genuine published GitHub RC. Both are actual Android 11 emulator captures.*

<details>
<summary><strong>Original captures and image provenance</strong></summary>

- [Haier photo](docs/screenshots/haier-user-photo.webp) · [Home](docs/screenshots/home.png)
- [Channels](docs/screenshots/update-channels.png) · [Confirmation](docs/screenshots/update-confirm.png) · [Progress](docs/screenshots/update-progress.png)
- [Published RC](docs/screenshots/online-rc-menu.png) · [Android installer](docs/screenshots/online-rc-installer.png)
- [Capture provenance](docs/screenshots/README.md) · [Mascot and branding](docs/brand/README.md)

</details>

## Choose how to install

| With an AI assistant | On your own |
| --- | --- |
| [Copyable prompt and workflow](docs/AI-INSTALL.en.md). The assistant inspects the device, APK and signing identity before preparing a concrete plan. | [Windows](docs/installation/windows.en.md) · [Linux](docs/installation/linux.en.md) · [macOS](docs/installation/macos.en.md). Official ADB, file verification, state records and first launch. |

> **Only isolated test Stable10 and RC11 builds are currently published.** No production release is available yet. These APKs cannot update the existing Haier launcher in place: its old private signing key was not found. [Signing and migration status →](docs/SIGNING.md)

The [installation overview](docs/installation/README.en.md) covers compatibility, HOME selection and returning to the original screen. A new launcher does not require removing the factory launcher. Installation itself does not select HOME or disable system packages.

### Using ByeByeDPI

The network menu requires the **paired modified backend**, signed with the coordinated identity. The stock official ByeByeDPI APK does not expose this protected interface. Install the backend before the launcher; Android VPN consent remains an on-screen confirmation. [Integration and source patch →](docs/BYEBYEDPI.md)

## What has been verified

- **231 launcher tests**, nine publisher tests, Android Lint and GitHub Actions assembly.
- Actual **Actions → Release → download → APK verification → Android installer**, Stable10 → RC11 on an Android11 emulator.
- App data, UID and HOME preserved during ordinary same-key updates.
- The earlier network-menu build works on Haier / Android11 / ARM32. The new updater has not been installed on the physical TV.

[Evidence and limitations](docs/TESTING.md) · [CI](https://github.com/anrhangels/milo-home/actions) · [Test releases](https://github.com/anrhangels/milo-home/releases)

The fresh/default ByeByeDPI emulator profile interrupted GitHub TLS. Actual online delivery was tested with manual OFF; the launcher never disables VPN automatically. Android8+ is the build minimum, not certification for every vendor firmware.

## Documentation

| Get started | Build and release |
| --- | --- |
| [Installation](docs/installation/README.en.md) · [AI-assisted installation](docs/AI-INSTALL.en.md) | [Building](docs/BUILDING.md) · [Contributing](CONTRIBUTING.md) |
| [Updating](docs/UPDATING.md) · [Recovery](docs/ROLLBACK.md) | [Channels and Actions](docs/RELEASE_CHANNELS.md) · [Signing](docs/SIGNING.md) |
| [ByeByeDPI](docs/BYEBYEDPI.md) · [Testing](docs/TESTING.md) | [Changelog](CHANGELOG.md) · [Security](SECURITY.md) |

---

**Original project:** [Gathin23 / CleanYourTV](https://github.com/Gathin23/cleanyourtv). **Fork development and maintenance:** [anrhangels](https://github.com/anrhangels), with assistance from **OpenAI Codex**. The mascot was supplied by the project owner; the new visual adaptation uses OpenAI image generation.

The original [MIT licence](LICENSE) and attribution are preserved. The separate ByeByeDPI patch is GPLv3. [Credits and licences](CREDITS.md). This is an independent project, not an official CleanYourTV release.
