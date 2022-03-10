package Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Consumer {
    private final static String QUEUE_NAME = "messages";
    private final static Integer THREADS = 10;
    private final static String HOST_IP = "34.210.159.67";
    private final static int PORT = 5672;
    private final static String USER_NAME = "test";
    private final static String USER_PASSWORD = "test";

    public static void main(String[] args) throws Exception {
        Map<String, List<LiftRide>> map = new ConcurrentHashMap<>();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(HOST_IP);
        connectionFactory.setPort(PORT);
        connectionFactory.setUsername(USER_NAME);
        connectionFactory.setPassword(USER_PASSWORD);
        Connection connection = connectionFactory.newConnection();

        Runnable runnable = () -> {
            Channel channel;
            try {
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.basicQos(1);
                DeliverCallback deliverCallback =  (tag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = new GsonBuilder().setPrettyPrinting().create().fromJson(message, LiftRide.class);
                    String skierId = liftRide.getSkierID();

                    if (!map.containsKey(skierId)) {
                        map.put(skierId, new CopyOnWriteArrayList<>());
                    }

                    map.get(skierId).add(liftRide);
                    //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println("message -> " + message);
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, tag -> {});

            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }

    }
}
