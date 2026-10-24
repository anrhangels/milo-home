# Release channels and publisher

Stable: tag `v1.0.0`, GitHub Release with prerelease=false. RC: tag `v1.1.0-rc.1`, prerelease=true.
Drafts are never client updates. Both channels use the same production keystore. Every successive installable code must grow,
including RC→Stable (e.g. Stable100 → RC101 → Stable102); a lower Stable is not a downgrade option.

The test distribution uses tags `test-v1.0.0` / `test-v1.1.0-rc.1` and track=test metadata. It is a separate test configuration,
not a third user-facing channel. It still has exactly Stable and RC. Production-track apps ignore test metadata.

Release entrypoint: **Publish Milo Home** / workflow_dispatch on the default branch, channel + track + version_code + version_name.
It is not triggered by arbitrary pushes. PR/push CI compiles/tests/lints and uploads a test-only artifact without release secrets.
Release publishing serializes jobs, validates versions before and immediately before publication, signs and verifies the actual APK,
then uploads MiloHome.apk, MiloHome.apk.sha256 and update.json with generated notes. RC is marked as prerelease.

Create GitHub environments `test-releases` and `production-releases`. Each needs secrets MILO_KEYSTORE_B64,
MILO_STORE_PASSWORD, MILO_KEY_ALIAS, MILO_KEY_PASSWORD and public environment variable MILO_SIGNER_SHA256.
Stable/RC within one environment must share the same key. Recommended: restrict environments to the default branch and require owner approval.
The isolated emulator test environment uses its own test key. The current Haier key is unavailable;
neither test nor production APKs can update it in place. See SIGNING.md.

Metadata schema1 contains applicationId, versionCode/versionName, channel, track, tag, minSdk, size, SHA-256, signer SHA-256,
asset name and notes. The same JSON is included in a `milo-update:v1` comment block in the REST release body,
so the client can inspect both channels with bounded API calls without HTML scraping or separate metadata downloads per release.
The asset URL must match the configured repository/tag/name. APK signature verification remains the final trust anchor.

Before first publication, repository creation, secret upload, Actions execution and real releases are explicitly pending owner approval.
Do not label local fixture tests as a passed GitHub Actions→Release delivery test.
