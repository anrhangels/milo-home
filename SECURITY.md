# Security

Do not post private signing keys, credentials, personal network names, device addresses or unredacted logs in public issues.
Report reproducible security defects privately through GitHub private vulnerability reporting when enabled;
otherwise ask the maintainer for a private contact without publishing the exploit or secrets.

Trust boundary: HTTPS GitHub Releases metadata can announce a candidate, but cannot authorize a different signing key.
The APK is cryptographically verified with apksig and must match the current installed certificate, expected SHA-256,
package, versionCode, min Android API and device ABI. Key changes require a separately reviewed initial migration.
No token, private key or publisher secret is shipped in the client. FileProvider grants read access only to verified update files.

Updates use Android's standard installer and its source-permission/confirmation UI. There is no root, silent installer,
Accessibility automation, package-enabling/disabling logic, debloat script execution or HOME takeover in the updater.
The test instrumentation can replace an internal HTTP connection factory inside its own signed test process;
there is no exported production test endpoint, service or setter. Instrumented APKs are rejected by the release publisher.

Production/RC share one permanent signing key; test releases use a separate isolated environment.
GitHub environments and their signing secrets must be configured by the repository owner before publishing.
Keep an offline recovery copy of the key/password in addition to the local protected backup.
