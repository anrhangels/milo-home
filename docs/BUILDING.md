# Building

Requirements: JDK17, SDK platform34, Build Tools35.0.0. The checked-in Gradle wrapper pins Gradle8.9 and its distribution hash;
AGP is 8.5.2. Existing native Java UI, RecyclerView and DynamicAnimation are preserved. Runtime addition: apksig8.5.2.
JSON/JUnit/AndroidX test runner dependencies are test-only. Minimum Android remains API26, target34.

Create `launcher/local.properties` with your SDK path (ignored by Git) or set ANDROID_HOME.

```sh
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

Without signing environment variables, local builds use the machine's debug key and are **not production releases**.
Production signing uses MILO_KEYSTORE, MILO_STORE_PASSWORD, MILO_KEY_ALIAS, MILO_KEY_PASSWORD, provided through protected
environment/CI secrets, never committed Gradle properties. Version defaults come from version.properties and can be overridden
by `-PmiloVersionCode=... -PmiloVersionName=...`. Public repository: `-PmiloUpdateRepo=anrhangels/milo-home`.

Runtime test track: `-PmiloUpdateTrack=test`. The isolated emulator fixtures are bootstrap9, Stable10 and RC11 with one
new local test/debug key, different from the installed Haier key. Prepared production100 uses a permanent key. Do not mix their installation
tracks or treat certificate differences as an error to bypass.

To run isolated instrumentation, provide signed Stable10 and RC11 fixtures as `MiloHome.apk` / `MiloHome-rc.apk` in a directory
outside Git, set MILO_TEST_ASSETS to that directory, then build with `-PmiloInstrumented -PmiloUpdateTrack=test` and
`assembleRelease assembleReleaseAndroidTest`. This enables debuggability only for the local test build; publisher validation rejects it.
Always pass the emulator serial explicitly. Never run an installation command without a serial while a real TV is connected.

`tools/milo/release.py` validates channel/version/code, signer and the actual APK and generates update.json, checksum and release notes.
Actions pins dependencies by commit SHA. Signing keys are never Actions artifacts. Upstream workflows are retained for reference
under `.github/upstream-workflows/` and the fork uses its own CI/release workflow.
