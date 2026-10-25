# Milo styles — concept, not a 0.0.9 feature

A style is one small local package, not an application that runs in the background. The first implementation should cover appearance only; executable plugins and a store are separate future decisions.

## For the owner

1. Open **Launcher settings → Appearance → Styles**.
2. Choose a built-in style or import one `.milo` file using Android's file picker.
3. Preview it with the owner's existing apps, then select **Apply**. **Undo** restores the previous appearance.

A style card shows its real preview, name, author and compatibility. Wallpapers belong to the style; select one image or a collection. Keep the current app order, hidden shortcuts, VPN and updater policy when switching styles. A permanent **Default Milo** option provides recovery. Installation needs no ADB, root or extra application.

## For creators

A `.milo` file is a ZIP containing `style.json`, an optional preview and local wallpaper images. The versioned schema inherits Milo defaults. Initially expose palette, dock opacity, supported clock styles, named wallpaper collections and a few tested placements of the clock/dock. Prefer known layout presets over arbitrary screen scripts.

Unknown optional fields fall back to defaults; unsupported required schema versions are explained before import. Validate paths, image dimensions, expanded size and file count before extracting. Import to a temporary private folder and activate atomically. A style cannot execute shell commands, install APKs, control packages, read accounts or load remote code. Creators may distribute packages independently; a catalogue can be added later without becoming necessary for local styles.

Suggested starting budgets: 20 MiB expanded per style, 30 images, decoded wallpapers fitted to the display. These are design proposals to benchmark on 1 GB TVs, not implemented limits.

---

## Концепция на русском

**«Стили Milo» — один файл с оформлением.** Открыл раздел стилей, выбрал или импортировал файл, посмотрел предпросмотр и применил. Рядом всегда доступны «Отменить» и «Стандартный Milo».

Внутри — цвета, оформление дока, вид часов и наборы обоев. Можно переключать отдельные обои или коллекции. Несколько проверенных вариантов расположения часов и дока дадут заметный кастом, сохранив удобное управление пультом.

Приложения, их порядок, VPN и режим обновлений не меняются при смене стиля. Автору достаточно подготовить изображения и небольшой файл настроек на основе примера. Устанавливать дополнительный APK пользователю не понадобится.

Сначала стоит реализовать локальные стили. Магазин — следующий этап, когда появятся формат, проверка совместимости и реальные пакеты от авторов. Исполняемые плагины, виджеты и собственные источники данных нужно проектировать отдельно: это другая нагрузка, разрешения и жизненный цикл.

Это концепция для обсуждения после испытаний 0.0.9, а не обещание уже работающего магазина.
