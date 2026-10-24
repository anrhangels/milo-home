# Contributing to Milo Home

This is an independent fork of CleanYourTV, not the upstream review queue. Keep patches focused,
preserve original attribution and include tests for security-critical updater changes.
JDK17 / SDK34 / Build Tools35.0.0; in `launcher/` run `./gradlew testReleaseUnitTest lintRelease assembleRelease`.
Run `python -m unittest discover -s tools/milo -p 'test_*.py' -v` and `python tools/milo/verify_public_tree.py`.

Do not commit keystores, APKs, personal device identifiers, private snapshots, tokens or local SDK paths.
Do not add polling, device package-management commands, forced/silent installs or code that changes HOME.
Use test-track metadata and isolated emulators; never test a new signing identity first on a user's only Home.
Actual screenshots must come from the running app; clearly label test fixtures and emulator/device scope.

Open a pull request with the behaviour change, tests and limitations. Release workflows run only by explicit dispatch
from the default branch; PRs never receive release secrets. Original upstream contribution guidance is retained
as `docs/UPSTREAM-CONTRIBUTING.md`. Existing upstream workflows are archived under `.github/upstream-workflows/`.
