# Milo Home 0.0.9-rc.1

Предварительная версия: оптимизация и удобство без смены привычного главного экрана. Для обычного использования пока остаётся стабильная 0.0.8.

- Общий ограниченный кэш графики и цветов приложений; каталог обновляется по изменившимся пакетам.
- Усилена остановка эффектов скрытого лаунчера и при выключенном экране. Минутные задачи видимых сцен используют общий планировщик.
- Прозрачность дока, настройки часов и время заставки применяются без пересоздания главного экрана.
- Библиотека запоминает выделение и прокрутку; добавлены фильтр по первой букве, «Показать все» и переименование карточки через её действия.
- Новые приложения не вытесняют прежние автоматически заполненные места дока. Перемещение карточки показывает номер места.
- Понятные ошибки запуска предлагают убрать только карточку, без удаления приложения.
- Загрузка обновления проверяет свободное место, сохраняет прерванные данные и продолжает загрузку той же версии, если сервер поддерживает Range. Проверка полного APK перед установкой сохранена.
- Добавлены тесты сохранения настроек при обновлении APK, скрытого экрана, кэша и продолжения загрузки.

**Файлы:** Milo-Home.apk — обычная версия; Milo-Home-ByeByeDPI.apk — с ByeByeDPI. Обновляйте свой вариант.

На физическом телевизоре эта версия ещё не проверена. Перед стабильным выпуском нужны испытания пульта, HOME, VPN, библиотеки, заставки и обновления по воздуху. Улучшение расхода памяти/CPU на конкретном ТВ пока не измерено.

[Подробности и ограничения проверки](https://github.com/anrhangels/milo-home/blob/milo-main/docs/releases/0.0.9-preview.md) · [Концепция стилей](https://github.com/anrhangels/milo-home/blob/milo-main/docs/ADDONS-CONCEPT.md)

---

Preview release: performance and usability improvements without replacing the familiar Home layout. Stable 0.0.8 remains the recommended everyday release while TV validation is pending.

- Bounded shared artwork/colour caches and package-scoped catalogue updates.
- Hidden/screen-off visual work suspension and shared minute-aligned scene scheduling.
- Dock opacity, clock preferences and idle delay apply without recreating Home.
- Library selection/scroll retention, first-letter filtering, Show all and custom shortcut names.
- Stable automatically filled dock positions and a visible move destination.
- Actionable launch failures offer shortcut removal, without uninstalling apps.
- Storage checks, interrupted-download retention and validated HTTP Range resume for the same APK. Full APK integrity/signature checks remain mandatory.
- Upgrade, lifecycle, cache and resumed-transfer regression tests.

Choose Milo-Home.apk or Milo-Home-ByeByeDPI.apk to match your installed edition. Physical-TV validation is pending; no device-specific CPU/RAM improvement is claimed yet.
