# Make something with Milo

[Home](../README.md) · [Contributing](../CONTRIBUTING.md)

Milo is open source. Start small: fix a bug, improve a translation or build a TV app with a Milo module card.

## Choose a path

- **Build the launcher:** [build instructions](BUILDING.md), then [architecture](ARCHITECTURE.md).
- **Add a module card:** [the supported card contract](specs/card.md). A separate Android TV app declares a tile; the owner decides whether to show it. This is not an arbitrary code-loading plugin system.
- **Improve a language:** edit the matching `values-*` resources under `launcher/app/src/main/res`. New Milo-specific screens need translations too; the existence of a locale folder does not prove complete coverage.
- **Improve the guides:** [26-language quick start](installation/languages.md). Translations are community-review drafts; report unclear steps.
- **Change update or release behaviour:** read [channels](RELEASE_CHANNELS.md), [signing](SIGNING.en.md) and [testing](TESTING.md) first.

## Our upstream connection

Milo has its own releases and priorities. CleanYourTV updates are reviewed, not merged or published automatically. Compare changes, select useful fixes, preserve attribution and licenses, test both editions, then publish a Milo release. A newer upstream commit is not itself a reason to update users.

The original Git history and upstream remote remain available. Do not delete licenses, signing checks or tests to make the repository appear smaller.
