# Milo Home releases

Current version: **Milo Home 0.0.3**. [Download](https://github.com/anrhangels/milo-home/releases/tag/v0.0.3).

One public release contains two alternatives:
- `Milo-Home.apk`: recommended for most people.
- `Milo-Home-ByeByeDPI.apk`: optional integration for an already installed compatible paired backend; see [setup](BYEBYEDPI.md).

Install only one launcher APK. GitHub shows the SHA-256 beneath each APK. Updater metadata is embedded in a hidden release-body comment, not uploaded as an extra asset.

## Maintainers

Dispatch **Publish Milo Home** with the version name and a strictly increasing internal code. Actions tests, lints, signs and verifies both editions, then publishes one release titled `Milo Home X.Y.Z`, tagged `vX.Y.Z`. Preview is not published automatically.

Public version 0.0.3 uses Android code 104, above previous 102/103 builds. Future codes must continue increasing. Both editions use the existing permanent production certificate.

Metadata retains a top-level Standard record for older clients and separate records in `editions.standard` and `editions.byedpi`. Each edition retains its own asset, hash, size and APK verification. Early separate-edition clients, particularly ByeByeDPI, may need one manual download of 0.0.3. New clients select their edition from the shared release.

Previous experimental releases are removed from the public release list. Git history and tags remain. Historical evidence remains in [testing](TESTING.md); RC support in code is retained for future testing, but no Preview is published now.
