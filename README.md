[platformSite]: https://easydonate.ru/

[supportedVersions]: https://img.shields.io/badge/%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F%20%D0%B8%D0%B3%D1%80%D1%8B-1.8.X%20--%201.21.X-3BAF18?style=for-the-badge

[latestReleaseImg]: https://img.shields.io/github/v/release/EasyDonate/EasyPayments?color=3BAF18&label=%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F%20%D0%BF%D0%BB%D0%B0%D0%B3%D0%B8%D0%BD%D0%B0&logo=github&sort=semver&style=for-the-badge
[latestRelease]: https://github.com/EasyDonate/EasyPayments/releases/latest

[licenseImg]: https://img.shields.io/github/license/EasyDonate/EasyPayments?label=%D0%BB%D0%B8%D1%86%D0%B5%D0%BD%D0%B7%D0%B8%D1%8F&color=3BAF18&style=for-the-badge
[license]: https://github.com/EasyDonate/EasyPayments/blob/main/LICENSE

[documentationImg]: https://img.shields.io/badge/%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%D1%86%D0%B8%D1%8F-gitbook-3BAF18?style=for-the-badge
[documentation]: https://easypayments.easydonate.ru/

[forumTopicImg]: https://img.shields.io/badge/%D1%82%D0%B5%D0%BC%D0%B0%20%D0%BD%D0%B0%20%D1%84%D0%BE%D1%80%D1%83%D0%BC%D0%B5-easydonate-3BAF18?style=for-the-badge
[forumTopic]: https://forum.easydonate.ru/d/117-mc-easypayments-alternativnyy-sposob-vydachi-tovarov

[problemSolutionImg]: https://img.shields.io/badge/%D1%80%D0%B5%D1%88%D0%B5%D0%BD%D0%B8%D0%B5-%D0%BF%D1%80%D0%BE%D0%B1%D0%BB%D0%B5%D0%BC-3BAF18?style=for-the-badge
[problemSolution]: https://forum.easydonate.ru/d/117-mc-easypayments-alternativnyy-sposob-vydachi-tovarov/6

# EasyPayments
Официальная имплементация обработки платежей на платформе [EasyDonate][platformSite], разработанная<br>
в качестве альтернативы для алгоритма выдачи товаров с использованием протокола RCON.

![supportedVersions] [![latestReleaseImg]][latestRelease] [![licenseImg]][license]<br>
[![documentationImg]][documentation] [![forumTopicImg]][forumTopic] [![problemSolutionImg]][problemSolution]

## Ключевые преимущества
- Поддержка наиболее популярных версий игры.
- Полная поддержка Spigot, Paper, Folia (с 1.20.2) и их форков (не гарантируется).
- **Высокая производительность** и не менее **высокое быстродействие**:
  - Многопоточная обработка событий - обрабатываем команды параллельно!
  - Использование технологии LongPoll - обещаем минимальные задержки выдачи!
- Поддержка недоступных для плагина ранее функций платформы (подарки, функционал плагинов).
- Функция **быстрой настройки плагина** без редактирования конфига при помощи `/ep setup`.я
- **Корзина** для покупок ваших игроков, чтобы они забирали товары в удобное для них время.
- Перезагрузка всех компонентов плагина в реальном времени при помощи `/ep reload`.
- Локализация сообщений команд - вы можете изменить всё, что видят ваши игроки!
- **Быстрый фреймворк ORMLite** для работы с базами данных и поддержка самых популярных из них:
  - MySQL (MariaDB)
  - PostgreSQL
  - SQLite
  - H2
- Открытый исходный код, ведь мы ценим ваше доверие.

## Поддержка сторонних ядер
Поддержка на ядре `SSSpigot` не предоставляется ввиду невозможности работы плагина из-за некоторых технических причин! 
Также, любые ядра с поддержкой одновременно `Bukkit API` и `Forge`, вероятно, тоже не окажутся совместимыми. 
Остальные популярные ядра на данный момент не вызывали подобных ошибок. 
Посмотрите в сторону `Paper` и его известных форков, таких как `Purpur` и прочих...

### Сторонние несовместимые ядра
Несовместимость со следующими ядрами точно была подтверждена ранее.<br>
Возможно, в их новых сборках уже исправлена работа некоторых технических моментов.

| Название  | Версии игры | Выдаваемое Java исключение               |
|:---------:|:-----------:|:-----------------------------------------|
| SSSpigot  |   1.12.2    | `java.lang.IncompatibleClassChangeError` |
| CatServer |   1.12.2    | `java.lang.AbstractServerError`          |

### Как определить, что используемое Вами ядро не поддерживается?
Если при запуске плагина Вы видите `stack-trace`, содержащий строку вида:
```
Caused by: java.lang.IncompatibleClassChangeError: class 
ru.easydonate.easypayments.platform.spigot.nms.v1_16_R3.interceptor.InterceptedCommandListenerWrapper 
cannot inherit from final class net.minecraft.server.v1_16_R3.CommandListenerWrapper
```
... то используемое Вами серверное ядро было модифицировано с небольшим недочётом, заключающимся в том,
что необходимый для работы `EasyPayments` NMS-класс `CommandListenerWrapper` имеет модификатор `final`,
что не позволяет создать класс-наследник для данного класса. Такой класс-наследник необходим плагину
для корректного перехвата сообщений, отправляемых в ответ на выполненные им команды.

**В таком случае данное серверное ядро не поддерживается и не может быть поддержано нами в будущем!** 
Вам следует связаться с разработчиком данного ядра и сообщить ему об этой ошибке, чтобы плагин смог работать у Вас. 
В случае, если `stack-trace` при запуске не содержит такой строки, обратитесь в тех. поддержку платформы [EasyDonate](https://easydonate.ru).

## Установка
1. Создайте аккаунт на [платформе][platformSite] и изучите доступный функционал.
2. Скачайте JAR-файл актуальной версии плагина [отсюда][latestRelease].
3. Поместите скачанный файл в директорию `plugins` вашего сервера.
4. Найдите в панели управления **ключ доступа** и **ID сервера**, к которому будете подключать свой.
5. Запустите сервер и произведите быструю настройку, введя `ep setup` в консоли или `/ep setup` в игре.
6. По желанию вы можете изменить место хранения данных в разделе `database` в `config.yml`.
7. Готово, плагин уже ожидает товары для выдачи на данном сервере!

## Сборка из исходного кода
Плагин EasyPayments может быть собран из исходного кода, например, если Вы хотите внести какие-то изменения
в нем и использовать уже модифицированный плагин на сервере.

### Подготовка к сборке
Для осуществления сборки необходимо иметь:
- **Java 8** или новее.<br>
  В ходе сборки потребуются и другие версии (16, 17, 21) - Gradle установит их автоматически.
- Все jar-файлы **Spigot с внутренним кодом игры** (NMS) начиная с `1_8_R1`.<br>
  Сборка необходимых файлов может быть осуществлена автоматически или вручную (см. ниже).
  Рекомендуется в любом случае запустить автоматическую сборку для устранения возможных ошибок.

### Автоматическая сборка Spigot
После клонирования репозитория необходимые файлы Spigot могут быть собраны автоматически
при помощи задачи `setupSpigotJars` в корневом проекте Gradle.

Используйте исполняемые файлы _gradlew_ для вызова этой задачи:
```bash
# Windows
gradlew.bat setupSpigotJars

# Linux или Mac OS
./gradlew setupSpigotJars
```
Сборка происходит в одном потоке из-за ограничений Gradle, поэтому придется подождать...

### Последовательность действий
Для сборки плагина из исходного кода необходимо выполнить следующее:
1. Клонируйте этот репозиторий и переместитесь в его локальную директорию:
   ```bash
   git clone https://github.com/EasyDonate/EasyPayments.git
   cd EasyPayments
   ```
2. Рекомендуется запустить автоматическую сборку Spigot (см. выше).
3. Запустите сборку плагина:
   ```bash
   # Windows
   gradlew.bat clean build

   # Linux или Mac OS
   ./gradlew clean build
   ```
4. Получите готовый jar-файл плагина по пути `EasyPayments/build/EasyPayments-X.Y.Z.jar`.

Если столкнетесь с проблемами при сборке - создайте **issue** в этом репозитории.<br>
Помощь со сборкой из исходного кода осуществляется **только в issues**.

## License
This project is open-source and licensed under the [MIT license][license].
