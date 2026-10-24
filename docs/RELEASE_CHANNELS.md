# Варианты и каналы Milo Home

**Stable — для обычного использования. Preview — новые возможности, возможны ошибки.** В текущих технических тегах и установленных версиях Preview называется RC; это тот же предварительный канал, а не дополнительная третья версия. Метки существующих релизов не переименовываются.

Есть два варианта одного приложения:

| Вариант | Stable | RC |
| --- | --- | --- |
| Standard: системные настройки Wi-Fi, без управления ByeByeDPI | [1.2.0](https://github.com/anrhangels/milo-home/releases/tag/standard-v1.2.0) | [1.3.0-rc.1](https://github.com/anrhangels/milo-home/releases/tag/standard-v1.3.0-rc.1) |
| ByeByeDPI: меню сети с парным backend | [1.2.0](https://github.com/anrhangels/milo-home/releases/tag/byedpi-v1.2.0) | [1.3.0-rc.1](https://github.com/anrhangels/milo-home/releases/tag/byedpi-v1.3.0-rc.1) |

Stable102, RC103; новые официальные APK подписаны постоянным ключом. RC — prerelease, а не обязательное автоматическое обновление. Одновременно эти варианты не устанавливаются: applicationId одинаков. Встроенный обновлятор предлагает только свой вариант и совместимую подпись. Ручной переход между вариантами требует проверки подписи и неуменьшающегося versionCode.

## Что скачивать

MiloHome-standard.apk или MiloHome-byedpi.apk и соответствующий .sha256; update.json описывает конкретный файл. В релизах byedpi отдельно приложены парные backend APK для armeabi-v7a и x86_64, контрольные суммы и архив исходников GPL. Backend собран локально из опубликованной модификации; четыре APK лаунчера собраны GitHub Actions. Backend не встроен в APK лаунчера и не ставится автоматически.

## Версии и метаданные

Теги: standard-vX.Y.Z / byedpi-vX.Y.Z, для RC — суффикс -rc.N. Track=test добавляет префикс test-; эти сборки production-клиент игнорирует. Старые тестовые релизы без variant считаются legacy byedpi, не Standard.

versionCode возрастает через оба канала внутри варианта. Metadata schema1 содержит variant, track, channel, package, version, размер, SHA-256 и сертификат. Блок milo-update:v1 в теле REST Release позволяет проверить кандидатов без HTML-парсинга. По размеру и хешу проверяется настоящий release-asset, затем подпись и manifest APK.

## Выпуск

Workflow Publish Milo Home запускается вручную из milo-main с variant, channel, track, version_code, version_name. Окружения production-releases / test-releases содержат MILO_KEYSTORE_B64, MILO_STORE_PASSWORD, MILO_KEY_ALIAS, MILO_KEY_PASSWORD и публичную переменную MILO_SIGNER_SHA256. Прямые push и PR не публикуют релиз.

Каждая публикация повторно проверяет монотонность версии и подпись; ключ удаляется из временного каталога runner. CI собирает оба варианта без секретов. Ключи и пароли не являются артефактами. По умолчанию Latest указывает на Standard Stable; у GitHub один общий Latest, поэтому второй Stable выбирайте по таблице выше.

Старый ключ установленного Haier не найден; наличие новых релизов не делает его текущую установку совместимой. [Миграция](SIGNING.md) · [Испытания](TESTING.md).
