# Сборка Milo Home

[Документация](README.md) · [English README](../README.en.md)

## Инструменты

JDK17, Android SDK platform34, Build-Tools35.0.0. Wrapper фиксирует Gradle8.9, Android Gradle Plugin8.5.2. SDK задаётся через ANDROID_HOME или локальный launcher/local.properties, который не коммитится.

## Два варианта

```sh
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease -PmiloVariant=standard
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease -PmiloVariant=byedpi
```

Результат каждой команды: app/build/outputs/apk/release/app-release.apk. Скопируйте его отдельно перед сборкой другого варианта. По умолчанию собирается Standard. -PmiloVersionCode и -PmiloVersionName переопределяют version.properties; -PmiloUpdateRepo задаёт репозиторий Releases.

## Подпись и тестовый канал

Без переменных подписи используется локальный debug-ключ; такая сборка не является официальным релизом и может быть несовместима с установленной. Production использует MILO_KEYSTORE, MILO_STORE_PASSWORD, MILO_KEY_ALIAS, MILO_KEY_PASSWORD из защищённого окружения. Stable и RC подписываются одним постоянным ключом.

Для изолированных испытаний добавьте -PmiloUpdateTrack=test. -PmiloInstrumented допустим только с track=test: включает инструменты тестирования и отключает минимизацию. Публикатор отвергает debuggable APK. Не устанавливайте тестовые сборки на рабочий телевизор ради обхода подписи.

MILO_TEST_ASSETS может указывать на внешний каталог APK-фикстур. AndroidX-тесты запускаются в эмуляторе; tools/device-tests — отдельный framework-only инструмент проверки данных после замены оптимизированного APK. Во всех ADB-командах нужен явный -s.

## Выпуск и оформление

CI проверяет оба варианта. tools/milo/release.py проверяет настоящий APK, включая variant, и создаёт метаданные. Ключи не попадают в артефакты. Подробности: [каналы](RELEASE_CHANNELS.md) и [подпись](SIGNING.md).

tools/branding — только экспорт документации; Node/Sharp не входят в Android-приложение. Изменение изображений не должно менять тему или раскладку в продукте. [Снимки](screenshots/README.md).

## Bundled VPN installer

The ByeByeDPI edition now requires `MILO_BACKEND_APK` pointing to the signed universal backend built from `integrations/byebye-dpi/source.zip`. See that directory’s README. Standard needs no backend. Backend and launcher must share a signing certificate. Never ship a test-signed backend inside a production-signed launcher.
