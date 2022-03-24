[platformSite]: https://easydonate.ru/

[supportedVersions]: https://img.shields.io/badge/%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F%20%D0%B8%D0%B3%D1%80%D1%8B-1.8.X%20--%201.18.X-3BAF18?style=for-the-badge

[latestReleaseImg]: https://img.shields.io/github/v/release/EasyDonate/EasyPayments?color=3BAF18&label=%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F%20%D0%BF%D0%BB%D0%B0%D0%B3%D0%B8%D0%BD%D0%B0&logo=github&sort=semver&style=for-the-badge
[latestRelease]: https://github.com/EasyDonate/EasyPayments/releases/latest

[licenseImg]: https://img.shields.io/github/license/EasyDonate/EasyPayments?label=%D0%BB%D0%B8%D1%86%D0%B5%D0%BD%D0%B7%D0%B8%D1%8F&color=3BAF18&style=for-the-badge
[license]: https://github.com/EasyDonate/EasyPayments/blob/main/LICENSE

[documentationImg]: https://img.shields.io/badge/%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F-gitbook-3BAF18?style=for-the-badge
[documentation]: https://docs.easypayments.easydonate.ru/

[forumTopicImg]: https://img.shields.io/badge/%D1%82%D0%B5%D0%BC%D0%B0%20%D0%BD%D0%B0%20%D1%84%D0%BE%D1%80%D1%83%D0%BC%D0%B5-easydonate-3BAF18?style=for-the-badge
[forumTopic]: https://forum.easydonate.ru/d/117-mc-easypayments-alternativnyy-sposob-vydachi-tovarov

[problemSolutionImg]: https://img.shields.io/badge/%D1%80%D0%B5%D1%88%D0%B5%D0%BD%D0%B8%D0%B5-%D0%BF%D1%80%D0%BE%D0%B1%D0%BB%D0%B5%D0%BC-3BAF18?style=for-the-badge
[problemSolution]: https://forum.easydonate.ru/d/117-mc-easypayments-alternativnyy-sposob-vydachi-tovarov/6

# EasyPayments
Официальная имплементация обработки платежей на платформе [EasyDonate][platformSite], разработанная<br>
в качестве альтернативы для алгоритма выдачи товаров с использованием протокола RCON.

![supportedVersions] [![latestReleaseImg]][latestRelease] [![licenseImg]][license]<br>
[![documentationImg]][documentation] [![forumTopicImg]][forumTopic] [![problemSolutionImg]][problemSolution]

## Поддержка сторонних ядер
Поддержка на ядре `SSSpigot` не предоставляется ввиду невозможности работы плагина из-за некоторых технических причин!<br>
Остальные популярные ядра на данный момент не вызывали подобных ошибок.<br>
Посмотрите в сторону `Paper` и его известных форков, таких как `Purpur` и прочих...

## Ключевые преимущества
- Поддержка наиболее популярных версий игры.
- **Высокая производительность** и не менее **высокое быстродействие**:
  - Многопоточная обработка событий - обрабатываем команды параллельно!
  - Использование технологии LongPoll - обещаем минимальные задержки выдачи!
- Поддержка недоступных для плагина ранее функций платформы (подарки, функционал плагинов).
- Функция **быстрой настройки плагина** без редактирования конфига при помощи `/ep setup`.
- **Корзина** для покупок ваших игроков, чтобы они забирали товары в удобное для них время.
- Перезагрузка всех компонентов плагина в реальном времени при помощи `/ep reload`.
- Локализация сообщений команд - вы можете изменить всё, что выводится игроку!
- **Быстрый фреймворк ORMLite** для работы с базами данных и поддержка самых популярных из них:
  - MySQL (MariaDB)
  - PostgreSQL
  - SQLite
  - H2
- Открытый исходный код, ведь мы ценим ваше доверие.

## Установка
1. Создайте аккаунт на [платформе][platformSite] и разберитесь с её возможностями.
2. Скачайте JAR-файл актуальной версии плагина [отсюда][latestRelease].
3. Поместите скачанный файл в директорию `plugins` вашего сервера.
4. Найдите в панели управления **ключ доступа** и **ID сервера**, к которому будете подключать свой.
5. Запустите сервер и произведите быструю настройку, введя `ep setup` в консоли или `/ep setup` в игре.
6. По желанию вы можете изменить место хранения данных в разделе `database` в `config.yml`.
7. Готово, плагин уже ожидает товары для выдачи на данном сервере!

## License
This project is open-source and licensed under the [MIT license][license].
