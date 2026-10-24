# Recovery and rollback

Keep the current signed APK and device snapshot privately. Do not restore system packages, change networks or delete app data as part of an updater rollback.
Android normally rejects an older versionCode; `adb install -r -d` is not a dependable recovery plan for a non-debuggable launcher.

The known working ByeByeDPI network-menu baseline is preserved as Git commit `710a49aa2e20ad72d3b7e3c5c174262386edbea9`.
`tools/milo/build_recovery.py` rebuilds it with an explicitly higher code and the chosen signing identity, without installing anything:

```sh
python tools/milo/build_recovery.py --code 12 --after-code 11 --name 2.7-recovery.12 --out recovery.apk
```

For a permanent-key build100, use a matching permanent key and a code greater than every installed Stable/RC, e.g.101 only if100 is the highest installed code.
Pass signing secrets through the environment as in BUILDING.md. Verify the recovery certificate against the installed certificate before installing.
Rebuilding the previous implementation as a forward version preserves its package/data model while removing the updater; it does not restore a lost private key.

HOME and the disabled-package snapshot must be checked before/after an approved recovery. No automatic fallback to uninstall, root, factory reset,
package enable/disable or a different applicationId exists. Selecting the original factory Home is a separate user-approved system operation.
If initial key migration has not been proven, do not apply a new-key recovery APK to the old-key device.
