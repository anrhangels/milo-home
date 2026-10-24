# Updating Milo Home

At launch, a bounded worker checks unauthenticated public GitHub Releases. Successful results are cached for 24 hours;
failed automatic checks back off for an hour and respect bounded GitHub rate-limit reset/Retry-After responses.
Return to Home does not start a new request each time. Use launcher settings → **Check for updates** for a manual check.

The green pill to the right of Settings is hidden if no compatible newer Stable or RC exists. Open it, choose a build,
read the version/channel/size/notes and confirm **Yes**. **No** returns to channel selection without downloading.
RC never installs automatically. Version ordering uses Android versionCode across both channels.

The ring reflects received bytes relative to the release's validated expected size. A release without a known size
is not offered, rather than showing a fabricated percentage. At completion the client validates the exact size/hash,
APK signature, installed certificate, package, versionName/versionCode, Android API and native ABI.
Only then is Android's ordinary installer opened. If source permission is missing, enable it in Android's own
settings screen, return Home and select the green pill again. The app does not grant itself permission or confirm installation.

Closing only the popup leaves the transfer running while Home remains visible. Leaving the launcher cancels an active
transfer/verification; partial bytes are removed and no installer starts in the background. A verified APK can remain
in the private cache until Android clears it. No separate download service or periodic worker is installed.

For fresh installations of the protected ByeByeDPI integration, install the paired backend first and Milo Home second.
For normal same-key updates do not uninstall or change applicationId. Android preserves app data/UID, but the actual
vendor/installer scenario must still be verified. The updater never changes HOME, system packages or network settings.

Test distribution is a distinct build configuration (`miloUpdateTrack=test`); production clients ignore it.
Do not install CI artifacts or instrumented builds as a permanent Home. Read SIGNING.md before a first-key migration.
