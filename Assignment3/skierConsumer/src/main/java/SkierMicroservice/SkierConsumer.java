package SkierMicroservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import redis.clients.jedis.*;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class SkierConsumer {
    private static final String EXCHANGE_NAME = "exchange1";
    private final static String QUEUE_NAME = "messages";
    private final static Integer THREADS = 150;
    private final static String HOST_IP = "54.187.87.189";
    private final static String SKIER_REDIS_IP = "localhost";
    private final static int PORT = 5672;
    private final static String USER_NAME = "test";
    private final static String USER_PASSWORD = "test";

    private static JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

    static {
        jedisPoolConfig.setMaxTotal(150);
    }

    public static void main(String[] args) throws Exception {

        JedisPool pool = new JedisPool(jedisPoolConfig, SKIER_REDIS_IP, 6379);
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
                /*
                //channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                channel.queueDeclare("skier", true, false, false, null);
                //channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                channel.queueBind("skier", EXCHANGE_NAME, "skier");
                //channel.basicQos(1);

               // String queueName = channel.queueDeclare().getQueue();*/

                /*
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                channel.queueDeclare("skier", true, false, false, null);
                channel.queueBind("skier", EXCHANGE_NAME, "skier");*/

                String queueName = "skier1";
                channel.queueDeclare(queueName, false, false, false, null);
                channel.queueBind(queueName, EXCHANGE_NAME, "");
                //channel.basicQos(1);

                DeliverCallback deliverCallback =  (tag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = new GsonBuilder().setPrettyPrinting().create().fromJson(message, LiftRide.class);


                    Jedis jedis = null;
                    try {
                        jedis = pool.getResource();
                        addLiftRideToRedis(jedis, liftRide);
                        //System.out.println("skier message -> " + message);
                    }catch (JedisException e) {
                        e.printStackTrace();
                        if (null != jedis) {
                           jedis.close();
                           jedis = null;
                        }
                    } finally {
                        if(jedis != null)  jedis.close();
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }

                };
                channel.basicConsume(queueName, false, deliverCallback, tag -> {});

            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }

    }

    private static void addLiftRideToRedis(Jedis jedis, LiftRide liftRide) {
        // for skier N, how many days have they skied this season?”
        jedis.sadd(liftRide.getSkierID(), liftRide.getDayID());

        //“For skier N, what are the vertical totals for each ski day?” (calculate vertical as liftID*10)
        String key1 = liftRide.getSkierID() + "+" + liftRide.getDayID() + "verticals";
        int vertical = Integer.valueOf(liftRide.getLiftID()) * 10;
        if(jedis.exists(key1)) {
            String curr = jedis.get(key1);
            int currInt = Integer.parseInt(curr);
            jedis.set(key1, String.valueOf(currInt + vertical));
        } else {
            jedis.set(key1, String.valueOf(vertical));
        }

        //“For skier N, show me the lifts they rode on each ski day”
        String key2 = liftRide.getSkierID() + "+" + liftRide.getDayID() + "lifts";
        jedis.sadd(key2, liftRide.getLiftID());
    }
}
