package ResortMicroservice;

import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import redis.clients.jedis.*;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class resortConsumer {
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
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.basicQos(1);*/

                // direct
                /*
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                channel.queueDeclare("resort", true, false, false, null);
                channel.queueBind("resort", EXCHANGE_NAME, "resort");*/
                String queueName = "resort1";
                channel.queueDeclare(queueName, false, false, false, null);
                channel.queueBind(queueName, EXCHANGE_NAME, "");
                channel.basicQos(1);

                DeliverCallback deliverCallback =  (tag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = new GsonBuilder().setPrettyPrinting().create().fromJson(message, LiftRide.class);
                    //print
                    //System.out.println("message -> " + message);
                    Jedis jedis = null;
                    try {
                        jedis = pool.getResource();
                        addInfoToRedis(jedis, liftRide);
                        //System.out.println("resort message -> " + message);
                    } catch (JedisException e) {
                        e.printStackTrace();
                        if (null != jedis) {
                            jedis.close();
                            jedis = null;
                        }
                    }
                    finally {
                        if(jedis != null) jedis.close();
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

    private static void addInfoToRedis(Jedis jedis, LiftRide liftRide) {
        // “How many unique skiers visited resort X on day N?”
        jedis.sadd("day:" + liftRide.getDayID(), liftRide.getSkierID());

        //“How many rides on lift N happened on day N?”
        String key1 = "day:" + liftRide.getDayID() + "+LiftId:" + liftRide.getLiftID();
        if(jedis.exists(key1)) {
            String curr = jedis.get(key1);
            int currInt = Integer.parseInt(curr);
            jedis.set(key1, String.valueOf(currInt + 1));
        } else {
            jedis.set(key1, "1");
        }

        //“On day N, show me how many lift rides took place in each hour of the ski day”
        String hour = String.valueOf(7 + Integer.valueOf(liftRide.getTime()) / 60);
        String key2 = "day:" + liftRide.getDayID() + "+" + "hour:" + hour;
        if(jedis.exists(key2)) {
            String curr = jedis.get(key2);
            int currInt = Integer.parseInt(curr);
            jedis.set(key2, String.valueOf(currInt + 1));
        } else {
            jedis.set(key2, "1");
        }
    }
}
