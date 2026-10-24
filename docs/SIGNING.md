# Signing and first migration

The installed Haier package is com.gathin.tvhome, code8 (2.7-haier-network.1), certificate SHA-256:
5f6835adc0b1ad58dac28764bcc4e85a94dabbb15e78ff093a5a57879bbc6884.
Its private key was not found and the owner has no known backup. An APK cannot supply the private key.
No unavailable upstream developer key is requested or used.

Today's DIFFERENT local test certificate is
b17f01c57f1e890363f1c90f01b5aa5ef434f52d5cdede95d2667497f46eb312.
It signs isolated emulator fixtures9→10→11, which CANNOT update the installed Haier APK.
The permanent production certificate is
0eb3e4397a0f1f2c05925c47da6bebeea0e757c41967642af051c774d72b36db.
Future production Stable and RC must share this permanent key.

Keys and a second protected local copy are outside Git. Password records use Windows DPAPI CurrentUser.
This is local protection, not independent disaster recovery. Before production adoption, create an external encrypted
key backup and a recoverable password-manager record. Never commit keys or include them in Actions artifacts.

## Real-TV migration remains blocked

Without the installed key, no valid Haier old→new signature lineage can be created. An emulator lineage from the test key
to the production key does NOT solve Haier migration. Ordinary updater metadata cannot authorize a new identity.
Keep the working television unchanged. Neither test fixtures nor production APKs should be installed on it now.

A future initial migration needs a separate reviewed plan:

1. Preserve current APKs, HOME, disabled packages and exportable settings privately. Non-debuggable apps with backup disabled
   do not provide guaranteed private-data recovery through ordinary ADB.
2. Prepare a usable HOME fallback and coordinated launcher/backend packages signed with the permanent key. Verify recovery.
   Enabling a factory launcher or changing HOME requires separate device permission.
3. Explain unexportable settings and obtain explicit approval before any uninstall or data loss. There is no automatic uninstall,
   package-name change, reset or signature-check bypass. Do not promise data preservation.
4. Migrate both applications only after approval: ByeByeDPI uses signature-protected IPC. Recheck all22 disabled packages and
   restore only verified, recoverable configuration.

If the genuine old key is later found, verify its fingerprint before considering same-key updates or Android11 rotation.
Until then, update/install/rotation experiments remain isolated on the emulator.
