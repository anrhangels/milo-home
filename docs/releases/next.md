# Milo Home 0.0.9

Легче в фоне, удобнее каждый день. Привычный главный экран сохранён.

- Графика и цвета приложений кэшируются совместно; каталог обновляется по изменившимся пакетам.
- Скрытый лаунчер и погашенный экран больше не держат активными визуальные задачи. Минутные задачи видимых сцен объединены.
- Прозрачность дока, часы и время заставки применяются без пересоздания главного экрана.
- Библиотека запоминает позицию. Добавлены фильтр по первой букве, «Показать все» и собственные названия карточек через удержание OK.
- Новые приложения не переставляют прежние автоматически заполненные места дока. При перемещении показывается номер места.
- Ошибка запуска объясняет причину и предлагает убрать карточку, не удаляя приложение.
- Обновления показывают нехватку места, продолжают прерванную загрузку при поддержке сервера и восстанавливают готовый проверенный APK без повторного скачивания.

Проверенная Preview прошла установку на Android TV 11 через настоящий GitHub Release. HOME и состояние системных пакетов сохранились; владелец проверил пульт и меню. Автоматически прошли 261 JVM-тест, 17 проверок инструментов и семь Android-тестов. Процент снижения CPU/RAM не заявляется: для такого сравнения нужны одинаковые условия измерения.

**Milo-Home.apk** — обычная версия. **Milo-Home-ByeByeDPI.apk** — со встроенным установщиком ByeByeDPI. Обновляйте свой вариант поверх установленного приложения, без удаления данных.

[Установка](https://github.com/anrhangels/milo-home/blob/milo-main/docs/START.ru.md) · [Проверки и ограничения](https://github.com/anrhangels/milo-home/blob/milo-main/docs/releases/0.0.9-preview.md) · [Будущие стили Milo](https://github.com/anrhangels/milo-home/blob/milo-main/docs/ADDONS-CONCEPT.md)

---

Lighter in the background, easier to use. The familiar Home layout is preserved.

- Shared artwork/colour caches and package-scoped catalogue refreshes.
- Hidden/screen-off visual work suspension and shared minute scheduling.
- Dock opacity, clock options and idle delay without recreating Home.
- Library position retention, first-letter filtering, Show all and custom shortcut names.
- Stable automatically filled dock positions, a move destination and actionable launch errors.
- Storage feedback, validated Range resume and recovery of a fully verified cached APK.

The Preview implementation passed a real GitHub Release upgrade on Android TV 11, preserving HOME and system-package state; the owner checked remote controls and menus. Validation includes 261 JVM tests, 17 tooling tests and seven Android tests. No device-specific CPU/RAM percentage improvement is claimed.

Choose Milo-Home.apk or Milo-Home-ByeByeDPI.apk to match your existing edition. Update in place without uninstalling.
