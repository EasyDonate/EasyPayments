# EasyPayments

Официальная имплементация обработки платежей на платформе [EasyDonate](https://easydonate.ru/) 
как альтернатива обработке через протокол RCON.

<div align="center">
  <a href="https://docs.easypayments.easydonate.ru/">Документация</a>
  <b>|</b>
  <a href="https://forum.easydonate.ru/d/117-mc-easypayments-alternativnyy-sposob-vydachi-tovarov">Тема на форуме</a>
  <b>|</b>
  <a href="https://forum.easydonate.ru/d/117-mc-easypayments-alternativnyy-sposob-vydachi-tovarov/6">Решение проблем</a>
</div>

## Расшифровка названий модулей
Ввиду того, что мы работаем с NMS (код игры), нам пришлось разделить плагин на два модуля:
- Alcor - модуль, работающий на версиях игры от **1.17** и новее, требует **Java 16** или новее.
- Sirius - модуль, работающий на версиях игры от **1.8** до **1.16.5**, требует **Java 8** или новее.

## Сборка из исходников
1) Клонируйте репозиторий: `git clone https://github.com/SoKnight/EasyPayments.git easypayments`
2) Переместитесь в локальную директорию: `cd easypayments`
3) Соберите проект используя Maven:
    - Модуль _Alcor_: `mvn -pl alcor -am clean package` (укажите `JAVA_HOME` до JDK 16 или новее)
    - Модуль _Sirius_: `mvn -pl sirius -am clean package` (укажите `JAVA_HOME` до JDK 8 или новее)
4) Заберите готовые файлы JAR и установите на сервер.
