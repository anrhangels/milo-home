# Credits and third-party notices

- Original launcher and core design: **Gathin23**, https://github.com/Gathin23/cleanyourtv .
  Original `LICENSE` is unchanged: MIT, Copyright (c) 2026 Gathin.
- Milo Home project and continued development: **anrhangels**, https://github.com/anrhangels .
  Developed and maintained by anrhangels, with development assistance from **OpenAI Codex**.
  Codex is an AI development tool. No fictional account, e-mail address or human co-author is attributed to it.
- AndroidX RecyclerView 1.3.2, DynamicAnimation 1.0.0 and transitive AndroidX components: Apache License 2.0,
  https://android.googlesource.com/platform/frameworks/support/ .
- Android apksig 8.5.2: Apache License 2.0, https://android.googlesource.com/platform/tools/apksig/ .
  Used to cryptographically verify downloaded APKs rather than trusting only a manifest hash or certificate label.
- Font resources: original upstream font licence in `launcher/FONT-LICENSE-OFL.txt` is preserved.
- Separate backend: romanvht/ByeByeDPI, GPLv3, https://github.com/romanvht/ByeByeDPI .
  Its native dependencies and their licences remain in that project. The launcher communicates by protected IPC
  and does not embed ByeDPI/heV native source. The integration patch directory includes the backend's GPLv3 licence.

Milo Home is not an official product of Gathin23, OpenAI, GitHub, Haier or Google.
- Mascot artwork supplied by the project owner. The crowned ghost adaptation was created using OpenAI image generation.
  Documentation covers combine fresh ADB captures of the running TV and actual emulator captures; application UI was not generated.
  Branding export tools are documentation-only and add no Android runtime dependency.

## QR code generation

Report QR codes use [ZXing Core 3.5.3](https://github.com/zxing/zxing), Apache License 2.0. Only offline QR encoding is included in the app; no scanner or camera integration. [License](docs/licenses/zxing-LICENSE.txt).
