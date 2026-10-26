# Milo Home 0.1.0

Проверенное обновление 0.1.0. Привычный главный экран и порядок приложений сохранены.

- Заставка: мягкое свечение или чёрный фон, три размера часов, предпросмотр перед выходом.
- «Уменьшить эффекты»: одна команда отключает анимации, дополнительные эффекты и свечение часов. Все опции можно настроить отдельно.
- Исправлено повторное открытие панели после перехода в системные настройки; улучшен возврат выделения после меню сети.
- Исправлен найденный на ТВ сбой: ползунок громкости обновляется и когда кнопки обрабатывает сама прошивка. Без постоянного опроса.
- Панель быстрых настроек и часы теперь соблюдают выбор отключённых анимаций.
- Обработчики кнопок пульта не перехватывают команды при выключенном экране.
- Меню библиотеки скрывает сведения о приложении, если прошивка не предоставляет этот экран.
- Обновлена совместимость системных настроек. Добавлены переводы основной части исходного интерфейса на датский, корейский и европейский португальский; новые пункты Milo используют английский при отсутствии перевода.
- Оптимизации 0.0.9 и проверка подписи обновлений сохранены.

Preview прошла испытания на Android TV 11. Проверены заставка и сохранение настроек, панель и переход к настройкам Android, громкость, библиотека, сеть, сон/пробуждение и физический пульт. Сохранены HOME и состояние системных пакетов. Прошли 264 JVM-теста для каждого варианта, 17 проверок выпуска и девять Android-тестов. Снижение общей нагрузки Android в процентах не заявляется.

**Milo-Home.apk** — обычная версия. **Milo-Home-ByeByeDPI.apk** — со встроенным установщиком ByeByeDPI. Устанавливайте свой вариант поверх прежнего, без удаления данных.

---

# Milo Home 0.1.0

The familiar Home screen and app order are preserved.

- Clock screensaver: ambient glow or black background, three clock sizes and an appearance preview.
- Reduce effects applies animation, extra-effect and black-clock preferences in one confirmation. Each option remains independently adjustable.
- Fixed panel reopening after visiting Android settings and improved network-menu focus restoration.
- Fixed a physical-TV finding: the volume slider follows system-handled volume keys through visible-only audio events, without polling.
- Quick settings and clock transitions respect disabled animations.
- Remote helpers pass keys through while the display is off.
- App info is offered only when the firmware provides it; vendor settings resolution is retained.
- Danish, Korean and European Portuguese upstream interface translations added. Untranslated Milo-specific options fall back to English.
- All 0.0.9 optimizations and APK update verification retained.

The Preview passed real Android TV11 tests and owner remote-control acceptance. Clock preferences, panel reopening, system-handled volume, library, network and sleep/wake were checked; HOME and system-package state were preserved. Both editions passed 264 JVM tests and Android Lint, plus17 tooling tests and nine emulator instrumentation tests. No percentage reduction in overall Android load is claimed.

[Test results and limits](https://github.com/anrhangels/milo-home/blob/milo-main/docs/releases/0.1.0-testing.md)
