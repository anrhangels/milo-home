# Protected ByeByeDPI integration

This fork retains the existing two-row network menu, not an extra add-on card. It controls a paired local modification of
[romanvht/ByeByeDPI](https://github.com/romanvht/ByeByeDPI), rather than assuming the official release has the same public API.
The backend uses a bound Messenger service protected by a signature permission and a calling-UID/package/signature check.
Service state comes from native startup/shutdown acknowledgements; manual OFF is preserved across launcher restarts.

Install the backend first, then the launcher, both with the coordinated signing identity. If an old official backend has a different
certificate, export settings and plan migration rather than uninstalling it automatically. The launcher updater does not update,
restart or reconfigure ByeByeDPI and does not create another autostart mechanism.

The backend integration patch is distributed separately under its GPLv3 licence in integrations/byebye-dpi/.
Apply it to the exact upstream commit recorded there and build with its submodules. Android's VPN permission remains an explicit system dialogue.
The initial permanent-key migration needs a coordinated backend transition as explained in SIGNING.md.
