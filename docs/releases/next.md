# Milo Home 0.1.1

Обновление с выбранными исправлениями CleanYourTV 3.1. Привычный интерфейс Milo сохранён.

- Исправлено удержание назначенных кнопок пульта: действие запускается после удержания, а два обработчика не запускают его повторно.
- Поддержаны отдельные экраны приложений в назначениях кнопок и нестандартные коды кнопок на главном экране.
- Подсветка и название карточки восстанавливаются после прерванного появления и повторного Home.
- Иконки лучше помещаются в компактных карточках; смена обоев не пропускает минутный интервал из-за небольшого смещения таймера.
- Улучшено открытие входов на Android 9 и настроек на прошивках без стандартного обработчика.
- Добавлен основной шведский перевод, обновлены испанский, итальянский и французский. Список языков Android 13+ дополнен переводами, добавленными ранее; новые пункты Milo без перевода используют английский.

Проверки: 272 JVM-теста для каждого варианта, Android Lint, 17 тестов выпуска и 11 Android-сценариев на отдельном эмуляторе. Исправления для конкретных прошивок не означают испытания на всех моделях. На физическом телевизоре эта версия в данном прогоне не устанавливалась.

**Milo-Home.apk** — обычная версия. **Milo-Home-ByeByeDPI.apk** — вариант со встроенным установщиком ByeByeDPI. Выбирайте свой вариант и устанавливайте поверх прежней версии.

[Проверки и происхождение изменений](https://github.com/anrhangels/milo-home/blob/milo-main/docs/releases/0.1.1-testing.md)

---

# Milo Home 0.1.1

Selected CleanYourTV 3.1 fixes, adapted to preserve Milo's interface and independent update system.

- Mapped long presses fire on time; duplicate delivery through key helpers launches once.
- Explicit activity targets and vendor keycodes on Home are supported.
- Interrupted same-card shelf/glow fades recover after returning Home.
- Compact-card icons scale to fit; wallpaper rotation tolerates small minute-timer drift.
- Deferred input opening for Android 9 and an AOSP TV settings fallback for firmware missing standard handlers.
- Swedish base-interface translation, refreshed Spanish/Italian/French, and registration of all available base languages for Android 13+. Untranslated Milo options fall back to English.

Validation: 272 JVM tests per edition, Android Lint, 17 tooling tests and 11 Android emulator scenarios. Firmware-specific fixes are not a claim of physical validation on every model. This version was not installed on the physical TV in this run.

Choose Milo-Home.apk or Milo-Home-ByeByeDPI.apk to match the installed edition and update in place.
