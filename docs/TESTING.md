# Проверки Milo Home

## Новый экран настроек (ветка разработки)

- Два варианта: unit tests, Android Lint и сборка release; 243 JVM-теста, включая режимы обновления и прозрачность материалов дока.
- На отдельном Android 11 / x86_64 эмуляторе проверены: переход из разделов к управлению пультом, выбор колонок и прозрачности, сохранение после перезапуска, стиль и яркость часов.
- JobScheduler регистрирует периодические задания на 6 и 24 часа. Режим «Только вручную» отменяет задание; этот выбор сохраняется после перезапуска. Проверены системные START/STOP и завершение через jobFinished при запущенных поверх Milo настройках Android.
- Проверка политики покрывает запрет автоматической сети в ручном режиме, интервалы 6/24ч, минутное ограничение ручных запросов, часовой backoff, блокировку сервера и изменение часов.
- Проверены скрытие Wi-Fi при сохранении входа в сеть из панели, отмена/подтверждение сброса оформления без сброса режима обновлений, переход в перестановку приложений и отсутствие прежнего меню удержания OK.
- Интервалы целиком в 6/24 часа не ожидались: проверены параметры задания Android, принудительный тестовый запуск на эмуляторе и функция интервалов. Реальная доступность времени запуска зависит от Android и прошивки.
- Физический телевизор не изменялся. Тестовые APK подписаны тестовым ключом и не предназначены для установки поверх релиза.

Повторить основные проверки: открыть Settings → Launcher settings; выбрать 5 колонок и 50% дока; вернуться на Home, открыть настройки снова; в Updates выбрать каждый режим и проверить задание 60417 через Android JobScheduler. После проверки вернуть Manual only и убедиться, что задание отменено. Делать это на отдельном тестовом эмуляторе.


## OTA 0.0.4 → 0.0.5 на телевизоре

- Haier Android 11 / ARM32: установленная 0.0.4 получила настоящий GitHub Release, показала зелёную кнопку, загрузила и проверила APK. Владелец подтвердил установку стандартным Android installer; версия стала 0.0.5 / code106. `adb install` для перехода не применялся.
- UID, время первоначальной установки, каталог данных, HOME и внешний конфиг сохранены. Все 22 отключённых пакета сохранили состояние. Backend не переустанавливался.
- VPN владелец выключил вручную; непрерывность активного VPN при обновлении в этом прогоне не проверялась.
- На установленной 0.0.5 поток обновлятора появился после ручной проверки и исчез после простоя (контроль через 18 секунд).
- 236 JVM-тестов лаунчера, 16 тестов инструментов, Android Lint и Actions прошли. Проверка очистки APK — автоматические тесты; промежуточные проценты реальной загрузки отдельно не фиксировались.


## Встроенный установщик 0.0.4

- 234 JVM-теста лаунчера, 10 тестов backend и 16 тестов инструментов публикации/исходников: без ошибок.
- Оба варианта проходят Android Lint; Standard не содержит вложенного backend.
- Отдельный Android 11 / x86_64 эмулятор: установка с нуля через Wi-Fi → системный установщик, возврат в меню и управление через защищённый IPC.
- Отмена начального диалога не устанавливает backend. Отмена разрешения VPN оставляет его выключенным; подтверждение включает VPN, последующее выключение работает.
- Инструментальный тест принимает подлинный вложенный APK и отклоняет изменённые байты до установки.
- Разрешение TV_CONTROL автоматически выдано Android после установки backend вслед за лаунчером; переустановка лаунчера не потребовалась.
- ARM32/ARM64/x86/x86_64 собраны и проверяются в APK; реальная установка в этом прогоне испытана только на x86_64 эмуляторе. Совместимость всех прошивок этим не подтверждена.
- Скачанный из GitHub production APK также установлен с нуля при отсутствии активной сети: полный путь через системное разрешение установки, установщик Android и возврат в меню с состоянием «Выключен» прошёл. Проверены внешняя и вложенная production-подписи, package/version и все четыре ABI.
- Физический телевизор не изменялся.

Ниже сохранены результаты прежних прогонов; их номера версий относятся к ранней истории проекта.


Дата:24.10.2026. Новые сборки проверены на Android11/API30 x86_64 эмуляторе. Haier Android11/ARM32 сохраняет прежнюю совместимую установку.

## Автоматические проверки

- 233 JVM-теста лаунчера для каждого варианта; 11 тестов подготовки релизов.
- Android Lint без ошибок, оптимизированные APK Standard и byedpi; CI проверяет оба.
- Отдельные тесты фильтрации edition: Standard не получает byedpi/legacy; каналы отбираются внутри своего варианта.
- Публикатор сверяет настоящее содержимое APK, подпись, код, имя, variant и наличие/отсутствие TV_CONTROL.
- Четыре скачанных из GitHub APK проверены по размеру, SHA-256, сертификату и варианту. Проверка каждого APK как другого варианта отвергнута.

## Реальные релизы

| Результат | Workflow |
| --- | --- |
| Standard Stable1.2.0, код102 | [Успешно](https://github.com/anrhangels/milo-home/actions/runs/40110041926) |
| ByeByeDPI Stable1.2.0, код102 | [Успешно](https://github.com/anrhangels/milo-home/actions/runs/40110045045) |
| Standard RC1.3.0-rc.1, код103 | [Успешно](https://github.com/anrhangels/milo-home/actions/runs/40110216008) |
| ByeByeDPI RC1.3.0-rc.1, код103 | [Успешно](https://github.com/anrhangels/milo-home/actions/runs/40110218387) |

Все четыре лаунчера собраны GitHub Actions и подписаны постоянным ключом0eb3… Из release скачивались APK, а не локальные подмены. Одно скачивание CLI оборвалось; файл не прошёл разбор Android, повторно скачан и полностью проверен до успешной установки.

## Standard

Установлен настоящий Standard Stable102. Framework-only проверка подтвердила variant=standard, отсутствие TV_CONTROL и иконку. Кнопка Wi-Fi открыла com.android.settings/.Settings$WifiSettings2Activity, без меню ByeByeDPI.

Лаунчер сам прочитал настоящий Release, предложил RC103 и после «Да» скачал APK. Стандартный установщик Android установил его. Проверки подтвердили variant=standard, код103, прежние данные/sentinel и HOME. После предоставления разрешения источника и возвращения потребовался повторный выбор/загрузка: кэш в памяти не обещается после пересоздания процесса.

## ByeByeDPI

Установлены парный non-debuggable release-backend и настоящий byedpi Stable102. Проверены variant=byedpi, TV_CONTROL и рабочее состояние «Выключен» через защищённый IPC. После системного VPN-согласия эмулятора native-сервис стал foreground.

Установка настоящего byedpi RC103 поверх Stable102 через ADB сохранила данные, HOME и работающий native PID7548. Это проверка обновления готовым APK, а не отдельная повторная проверка загрузки RC через UI в этом варианте. Сам онлайн-путь и установщик пройдены на Standard; общая реализация одинакова, фильтр variant проверен отдельно.

Парные backend armeabi-v7a/x86_64 собраны локально в release, подписаны тем же ключом, отладка выключена. Исходники с native-зависимостями и лицензиями приложены к релизам. ARM32 backend на физическом Haier не заменялся.

## Сеть и пределы доказательств

Свежий профиль ByeByeDPI на эмуляторе обрывал TLS к GitHub. Онлайн-загрузка проверена при ручном OFF. Работа на другой стратегии и на конкретном операторе не гарантируется; лаунчер не обходит VPN и не ослабляет TLS.

Ранее на тестовых9→10→11 также были проверены офлайн-отклик, Да/Нет, реальный круговой прогресс, отказ от несовместимых/повреждённых APK и ротация тестового ключа. Эмуляторная ротация не решает отсутствие настоящего ключа Haier.

## Скриншоты

Свежие haier-home-fresh.png и haier-media-fresh.png — прямой screencap работающего телевизора, только менялось выделение пультом. Никакое изображение приложения не дорисовано. На ТВ остаётся версия8. byedpi-network.png и standard-rc-menu.png — новые опубликованные сборки на эмуляторе. [Происхождение](screenshots/README.md).

## Что ещё проверять на ТВ

До установки новой подписи нужен согласованный [план миграции](SIGNING.md). После разрешённой установки нужно проверить физический пульт, HOME, Wi-Fi, backend и совпадение списка отключённых пакетов. На реальном Haier новые APK не устанавливались, пакеты и системные настройки не менялись.

## Воспроизводимость

[Сборка](BUILDING.md) описывает miloVariant, miloUpdateTrack и инструменты. tools/device-tests проверяет variant/permission, сохранение данных и HOME после обновления. Используйте отдельный эмулятор с явным -s, устанавливайте backend перед byedpi-лаунчером и ждите InstallSuccess до запуска инструментальных тестов. Не подменяйте рабочую установку ТВ ради фикстур.

## Milo Home 0.0.8 — simple bug report

On an isolated Android 11 emulator, both Standard and ByeByeDPI editions passed the path Launcher settings → More → Report a problem → Show QR code → Return to settings. Back and reopening the screen were also checked. The displayed version and edition matched the installed APK. A QR reader decoded the actual full-screen capture to the expected GitHub issue URL in both editions. No issue was submitted during testing.

To reproduce: install a test build on a disposable emulator, disconnect its network, open the report screen, check the six displayed device facts and generate the QR. Scan it on a connected phone; verify the destination is `github.com/anrhangels/milo-home/issues/new`, the title and device facts are filled in, and submission still requires the user's GitHub login and action. Do not publish test reports to the project.

`BugReportTest` covers sanitisation, unavailable metrics, the fixed field allowlist and destination, maximum-length Unicode input, and QR round-trip decoding. No new Android permissions, background service or reporting server were added. These checks do not claim physical-TV or phone-camera validation for this release.


## 0.0.9 Preview

[Coverage, limits and reproduction](releases/0.0.9-preview.md) include actual APK-upgrade checks and lifecycle, shared-artwork, library and resumable-download instrumentation. Preview acceptance on Android TV 11 is complete; Stable uses the same implementation under a new monotonic version code.
