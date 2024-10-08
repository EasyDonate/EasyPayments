# Поддержка сторонних платформ
Поддержка на ядре `SSSpigot` не предоставляется ввиду невозможности работы плагина из-за некоторых технических причин! 
Также, любые ядра с поддержкой одновременно `Bukkit API` и `Forge`, вероятно, тоже не окажутся совместимыми. 
Остальные популярные ядра на данный момент не вызывали подобных ошибок. 
Посмотрите в сторону `Paper` и его известных форков, таких как `Purpur` и прочих...

## Сторонние несовместимые ядра
Несовместимость со следующими ядрами точно была подтверждена ранее.<br>
Возможно, в их новых сборках уже исправлена работа некоторых технических моментов.

| Название  | Версии игры | Выдаваемое Java исключение               |
|:---------:|:-----------:|:-----------------------------------------|
| SSSpigot  |   1.12.2    | `java.lang.IncompatibleClassChangeError` |
| CatServer |   1.12.2    | `java.lang.AbstractServerError`          |

## Как определить, что используемое Вами ядро не поддерживается?
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
