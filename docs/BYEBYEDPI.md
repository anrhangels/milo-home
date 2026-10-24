# ByeByeDPI в Milo Home

Вариант **Milo-Home-ByeByeDPI.apk** включает совместимый установщик VPN. В обычном **Milo-Home.apk** его нет.

## Первый запуск

1. Установите вариант лаунчера с ByeByeDPI.
2. Откройте **Wi-Fi → Установить ByeByeDPI**.
3. Подтвердите установку в Android. Если потребуется, разрешите Milo устанавливать приложения в системных настройках и вернитесь назад.
4. После установки снова откройте Wi-Fi и включите ByeByeDPI.
5. Подтвердите системный запрос VPN на экране ТВ.

Файл уже внутри APK: интернет для этого шага не нужен. VPN работает как отдельное приложение Android, но искать второй файл не требуется. Не подтверждайте запросы от неизвестных приложений.

## Если ByeByeDPI уже установлен

Совместимая версия сразу управляется из Wi-Fi. При другой цифровой подписи приложение не удаляется и его данные не очищаются: сначала нужен [план перехода](SIGNING.md). Если пользователь отменил установку или разрешение VPN, ничего не включается автоматически.

## Ограничения

Установщик содержит ARM32, ARM64, x86 и x86_64. Возможности установки и VPN зависят от прошивки. Некоторым сайтам может мешать выбранная стратегия ByeDPI; настройка стратегии — отдельный выбор пользователя. Milo не обещает доступ ко всем сервисам и не отключает VPN автоматически ради обновлений.

## Для разработчиков

[Полные исходники backend, происхождение и сборка](../integrations/byebye-dpi/README.md). Отдельный backend лицензирован по GPLv3; код лаунчера сохраняет MIT. Launcher и backend подписываются согласованным ключом. Перед установкой проверяются криптографическая подпись, package, версия, API и ABI. Взаимодействие — защищённый bound Messenger service, без фонового опроса лаунчером. Подтверждение установки и VPN остаётся за пользователем.

---

## English — first setup

The **Milo-Home-ByeByeDPI.apk** download includes the compatible VPN installer. The regular **Milo-Home.apk** does not.

1. Install the ByeByeDPI edition of Milo.
2. Open **Wi-Fi → Install ByeByeDPI**.
3. Confirm Android installation. If prompted, allow Milo to install apps in Android settings and return.
4. Open Wi-Fi again and turn on ByeByeDPI.
5. Approve Android’s VPN request on your TV.

No extra download is required. Android still installs the VPN as a separate app; it is not silently installed or enabled. A conflicting existing signing identity is never resolved by deleting the app. See [migration](SIGNING.en.md).

The bundled backend supports ARM32, ARM64, x86 and x86_64; installation and VPN availability depend on device firmware. ByeDPI strategies can affect connectivity and may require user configuration. [Corresponding GPL source and build instructions](../integrations/byebye-dpi/README.md).
