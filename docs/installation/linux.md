# Установка в Linux

[Общий порядок и совместимость](README.md) · [English](linux.en.md)

**Начинайте после проверки варианта и подписи по общей инструкции. Ниже показан Standard; для byedpi выберите его APK и проверьте парный backend отдельно.**

## 1. Подготовьте ADB

[Скачайте официальный Platform-Tools](https://developer.android.com/tools/releases/platform-tools) для вашей системы и распакуйте ZIP. Откройте терминал **в папке platform-tools**. Команды ниже не требуют добавлять ADB в системный PATH.

```bash
./adb version
```

При запуске ошибка «Permission denied» может относиться к файлу: проверьте `ls -l ./adb` и при необходимости дайте ему право запуска `chmod u+x ./adb`. Для USB могут понадобиться правила udev производителя; сетевое подключение их не требует. Не запускайте ADB через sudo ради обхода доступа.

## 2. Подключитесь к телевизору

Замените `TV_IP:ADB_PORT` фактическим адресом из настроек ТВ. Если используется сопряжение, **сначала** выполните `./adb pair TV_IP:PAIR_PORT` с кодом с экрана. Не подставляйте порт сопряжения в команду подключения.

```bash
serial="TV_IP:ADB_PORT"
./adb connect "$serial"
./adb devices -l
./adb -s "$serial" get-state
```

Подтвердите запрос отладки на экране ТВ. Продолжайте только при состоянии `device`. Если прошивка предоставляет лишь старый сетевой ADB на5555, замените порт на5555 только после проверки, что он включён.

## 3. Сохраните исходное состояние

```bash
./adb -s "$serial" shell pm list packages -d > before-disabled.txt
./adb -s "$serial" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME > before-home.txt
./adb -s "$serial" shell dumpsys package com.gathin.tvhome > before-package.txt
```

Файлы сохраните локально. Если лаунчер уже установлен, выполните сравнение сертификатов и кодов из [общей инструкции](README.md#проверка-apk-и-старой-подписи). При несовместимой подписи остановитесь; не удаляйте приложение.

## 4. Проверьте скачанный файл

Поместите совместимый production APK и его `.sha256` из одного релиза в текущую папку. Эта проверка не заменяет apksigner.

```bash
sha256sum --check MiloHome-standard.apk.sha256
```

## 5. Установите и запустите

Только после успешных проверок и выбора правильного устройства:

```bash
./adb -s "$serial" install -r ./MiloHome-standard.apk &&
  ./adb -s "$serial" shell am start -n com.gathin.tvhome/.MainActivity
```

При ошибке установки **не выполняйте следующую команду запуска**: сначала разберите ошибку. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` означает несовместимость подписи; `VERSION_DOWNGRADE` — уменьшение кода версии. Не добавляйте `-d`, не удаляйте приложение и не очищайте его данные.

Для управления ByeByeDPI парный backend устанавливают отдельно **до лаунчера**, только после проверки его подписи и данных. Здесь он не скачивается и не заменяется автоматически.

## 6. Проверьте пульт и сохранение состояния

Выберите HOME и проверьте возврат по [общей инструкции](README.md#первый-запуск-и-home). Заводской лаунчер не отключайте. После установки:

```bash
./adb -s "$serial" shell pm list packages -d > after-disabled.txt
./adb -s "$serial" shell dumpsys package com.gathin.tvhome > after-package.txt
diff -u before-disabled.txt after-disabled.txt
```

Список должен совпасть. Проверьте Wi-Fi, состояние ByeByeDPI, Home, стрелки, OK, Назад и громкость. В конце можно отключить только это ADB-соединение: `./adb disconnect "$serial"`.

[Восстановление](../ROLLBACK.md) · [Проблемы с подписью](../SIGNING.md) · [На главную](../../README.md).
