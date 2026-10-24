# Milo Home 0.0.5

Небольшое обновление без изменения привычного интерфейса.

- После установки обновления Milo убирает свой устаревший установочный файл.
- После успешной установки ByeByeDPI удаляется временная распакованная копия установщика.
- Поток проверки обновлений освобождается после простоя; постоянной фоновой службы по-прежнему нет.
- Ручная проверка обновлений точнее сообщает о завершении запроса.

Данные приложений, темы и системные кэши не очищаются. Обычный APK не содержит VPN; вариант с ByeByeDPI сохраняет встроенный установщик.

[Установка и помощь](https://github.com/anrhangels/milo-home/blob/milo-main/docs/START.ru.md)

---

A small maintenance update with the familiar interface unchanged.

- Milo removes its obsolete update APK after the update is installed.
- The temporary extracted ByeByeDPI installer is removed after successful installation.
- The update-check worker thread exits after being idle; there is still no persistent background service.
- Manual update checks report completion more accurately.

App data, themes and system caches are not cleared. The regular APK has no VPN; the ByeByeDPI edition keeps its bundled installer.

[Installation and help](https://github.com/anrhangels/milo-home/blob/milo-main/docs/START.md)
