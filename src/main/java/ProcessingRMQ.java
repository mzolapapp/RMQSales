import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ProcessingRMQ extends Thread {
    final static String queueName = "SalesOLAP";
    static String text;
    static long timer = System.currentTimeMillis() + 15000;

    ProcessingRMQ(String name) {
        super(name);
    }

    /**
     * Поток выполнет подключение к очереди в RMQ и записывает сообщения в виде файлов xml
     * Сообщения из очереди берутся по одному для синхронизации при инициализации нескольких потоков
     * сообщения из очереди не удаляются
     */

    @Override
    public void run() {
        System.out.println("Waiting for messages");

        Connection connection = null;
        try {
            connection = RMQConnect("172.25.1.79", "OLAP_user", "OLAP_user");
            Channel channel = connection.createChannel();

            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-dead-letter-exchange", "");
            args.put("x-dead-letter-routing-key", "SalesOLAP_error");

            channel.queueDeclare(queueName, true, false, false, args);
            channel.basicQos(1000);
            while (System.currentTimeMillis() < timer) {

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    text = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    write(text);
                };
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                });
            }
            channel.close();
            connection.close();


        } catch (TimeoutException ex) {
            Application.log.error("Таймаут соединения с RMQ ", ex.fillInStackTrace());
            System.out.println("Таймаут соединения с RMQ " + ex.fillInStackTrace());
        } catch (IOException ex) {
            Application.log.error("Ошибка соединения с RMQ ", ex.fillInStackTrace());
            ex.printStackTrace();
        }

    }

    /**
     * Метод принимает параметры для подключения к RMQ, порт подключения стандартный,
     * дополнительно Можно не прописывать
     *
     * @param host     - хост RMQ
     * @param username - логин
     * @param password - пароль
     * @return - Возвращает объект подключения к RMQ
     * @throws IOException
     * @throws TimeoutException
     */
    private static Connection RMQConnect(String host, String username, String password) throws
            IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        Connection connection = factory.newConnection();

        return connection;
    }

    /**
     * Метод создает файл по заданому пути и записывает туда сообщение из RMQ
     *
     * @param text - текст сообщения
     * @throws IOException
     */
    public static void write(String text) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(File.separator + "home" + File.separator + "rmqSales" + File.separator + "messages" + File.separator + UUID.randomUUID().toString() + ".xml");
            writer.write(text);
            writer.close();

        } catch (IOException ex) {
            Application.log.error("Ошибка записи файла ", ex.fillInStackTrace());
            System.out.println("Ошибка записи файла " + ex.fillInStackTrace());
        }
    }
}
