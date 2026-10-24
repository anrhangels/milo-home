# Milo visual assets

The mascot comes from the owner's crowned ghost, adapted with OpenAI image generation. The transparent mascot is used on documentation covers; the installed app icon keeps its existing badge.

- `milo-mascot.png`: transparent mascot, no square background.
- `milo-simple-en.webp` / `milo-simple-ru.webp`: minimal bilingual covers.
- `milo-interface.webp`: two real screen captures in simple frames.
- `install-steps-en-v2.webp` / `install-steps-ru-v2.webp`: instructional diagrams, not Android screenshots.
- `milo-icon.png`: existing app icon source.
- `social-preview.png`: previous prepared social card, not a claim that repository settings use it.

Run `node tools/branding/simple.cjs` with the dependencies in `tools/branding/package.json` to reproduce the documentation graphics. This does not change app resources. The old `render.cjs` entry point forwards to the same renderer.

Screenshots are not AI-generated or retouched. Their provenance and version are recorded in [screenshots](../screenshots/README.md). Decorative frames stay outside the screen; the source UI is unchanged.
