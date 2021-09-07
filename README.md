# RMQSales
Скрипт принимает сообщения из очереди RMQ SalesOLAP и сохраняет их на сервере в папке messages<br>
Логируется на уровне ошибок<br>
Запускается через crontab  командой java -jar /home/rmqSales/RMQSales.jar каждые 2 минуты
