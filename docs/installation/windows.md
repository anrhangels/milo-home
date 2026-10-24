# Установка в Windows

[Общий порядок и совместимость](README.md) · [English](windows.en.md)

**Начинайте после проверки варианта и подписи по общей инструкции. Ниже показан Standard; для byedpi выберите его APK и проверьте парный backend отдельно.**

## 1. Подготовьте ADB

[Скачайте официальный Platform-Tools](https://developer.android.com/tools/releases/platform-tools) для вашей системы и распакуйте ZIP. Откройте терминал **в папке platform-tools**. Команды ниже не требуют добавлять ADB в системный PATH.

```powershell
.\adb.exe version
```

Для подключения по Wi-Fi USB-драйвер не нужен. При USB-подключении драйвер зависит от производителя ТВ. Не устанавливайте случайный универсальный драйвер.

## 2. Подключитесь к телевизору

Замените `TV_IP:ADB_PORT` фактическим адресом из настроек ТВ. Если используется сопряжение, **сначала** выполните `.\adb.exe pair TV_IP:PAIR_PORT` с кодом с экрана. Не подставляйте порт сопряжения в команду подключения.

```powershell
$serial = "TV_IP:ADB_PORT"
.\adb.exe connect "$serial"
.\adb.exe devices -l
.\adb.exe -s "$serial" get-state
```

Подтвердите запрос отладки на экране ТВ. Продолжайте только при состоянии `device`. Если прошивка предоставляет лишь старый сетевой ADB на5555, замените порт на5555 только после проверки, что он включён.

## 3. Сохраните исходное состояние

```powershell
.\adb.exe -s "$serial" shell pm list packages -d > before-disabled.txt
.\adb.exe -s "$serial" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME > before-home.txt
.\adb.exe -s "$serial" shell dumpsys package com.gathin.tvhome > before-package.txt
```

Файлы сохраните локально. Если лаунчер уже установлен, выполните сравнение сертификатов и кодов из [общей инструкции](README.md#проверка-apk-и-старой-подписи). При несовместимой подписи остановитесь; не удаляйте приложение.

## 4. Проверьте скачанный файл

Поместите совместимый production APK и его `.sha256` из одного релиза в текущую папку. Эта проверка не заменяет apksigner.

```powershell
$expected = ((Get-Content .\MiloHome-standard.apk.sha256 -Raw) -split '\s+')[0].ToLowerInvariant()
$actual = (Get-FileHash .\MiloHome-standard.apk -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actual -ne $expected) { throw 'SHA-256 mismatch: stop installation' }
```

## 5. Установите и запустите

Только после успешных проверок и выбора правильного устройства:

```powershell
.\adb.exe -s "$serial" install -r .\MiloHome-standard.apk
if ($LASTEXITCODE -ne 0) { throw "Installation failed: stop here" }
.\adb.exe -s "$serial" shell am start -n com.gathin.tvhome/.MainActivity
```

При ошибке установки **не выполняйте следующую команду запуска**: сначала разберите ошибку. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` означает несовместимость подписи; `VERSION_DOWNGRADE` — уменьшение кода версии. Не добавляйте `-d`, не удаляйте приложение и не очищайте его данные.

Для управления ByeByeDPI парный backend устанавливают отдельно **до лаунчера**, только после проверки его подписи и данных. Здесь он не скачивается и не заменяется автоматически.

## 6. Проверьте пульт и сохранение состояния

Выберите HOME и проверьте возврат по [общей инструкции](README.md#первый-запуск-и-home). Заводской лаунчер не отключайте. После установки:

```powershell
.\adb.exe -s "$serial" shell pm list packages -d > after-disabled.txt
.\adb.exe -s "$serial" shell dumpsys package com.gathin.tvhome > after-package.txt
Compare-Object (Get-Content before-disabled.txt) (Get-Content after-disabled.txt)
```

Список должен совпасть. Проверьте Wi-Fi, состояние ByeByeDPI, Home, стрелки, OK, Назад и громкость. В конце можно отключить только это ADB-соединение: `.\adb.exe disconnect "$serial"`.

[Восстановление](../ROLLBACK.md) · [Проблемы с подписью](../SIGNING.md) · [На главную](../../README.md).
