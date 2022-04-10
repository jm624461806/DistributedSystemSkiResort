package Servlet;

import Channels.ChannelsPool;
import Model.HttpMethod;
import Model.LiftRide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SkiersServlet extends HttpServlet {

    public final static String QUEUE_NAME = "messages";
    public final static String EXCHANGE_NAME = "exchange1";
    private ChannelsPool channelsPool = new ChannelsPool();
    private ServletsUtil servletsUtil = new ServletsUtil();
    private EventCountCircuitBreaker circuitBreaker = new EventCountCircuitBreaker(1500, 100, TimeUnit.MILLISECONDS, 800);
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processHttpMethod(request, response, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processHttpMethod(request, response, HttpMethod.POST);
    }

    private void processHttpMethod(HttpServletRequest req, HttpServletResponse res, HttpMethod method)
            throws IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        /*
        if(!circuitBreaker.incrementAndCheckState()) {
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            res.getWriter().write("Too many request sent per second");
            return;
        }*/

        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Missing Parameter");
            return;
        }

        String[] urlParts = urlPath.split("/");
        //System.out.println(urlParts[0]);
        if(!isValidUrl(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(urlPath);
            return;
        }

        // handle all the get methods
        if (method == HttpMethod.GET) {
            res.setStatus(HttpServletResponse.SC_OK);
            if(urlParts.length == 3) {
                res.getWriter().write("This is the total vertical for the skiers");
            } else {
                res.getWriter().write("This is the ski day vertical for the day");
            }
            return;
        }

        // handle the post method
        if (method == HttpMethod.POST) {

            String resortId = urlParts[1];
            String seasonId = urlParts[3];
            String dayId = urlParts[5];
            String skierId = urlParts[7];

            //check payload
            boolean payload = false;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = req.getReader();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                payload = true;
            } catch (Exception e) {
                payload = false;
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("no ski ride payload");
                return;
            }

            if(payload) {
                LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);

                liftRide.setResortID(resortId);
                liftRide.setSeasonID(seasonId);
                liftRide.setDayID(dayId);
                liftRide.setSkierID(skierId);

                //String resBody = "Response Msg ->  " + liftRide.toString();
                //System.out.println(resBody);

                sendMessageToQueue(liftRide);
                res.setStatus(HttpServletResponse.SC_CREATED);
                System.out.println(liftRide.toString());
                res.getWriter().write("success");
                //JsonObject jsonObject = new JsonObject();
                //servletsUtil.writeJsonObject(res, resBody, jsonObject);
            }

        }
    }

    private boolean isValidUrl(String[] urlParts) {
        if(urlParts.length == 3) {
            if(!urlParts[2].equals("vertical")) return false;
            return servletsUtil.isValidNumber(urlParts[1]);
        } else {
            if(urlParts.length != 8) return false;
            if(!urlParts[2].equals("seasons") || !urlParts[4].equals("days") || !urlParts[6].equals("skiers"))
                return false;
            return servletsUtil.isValidNumber(urlParts[1]) &&  servletsUtil.isValidNumber(urlParts[3])
                    && servletsUtil.isValidNumber(urlParts[5]) && servletsUtil.isValidNumber(urlParts[7]);
        }
    }

    private void sendMessageToQueue(LiftRide liftRide) {
        try {
            String liftRideMessage = gson.toJson(liftRide);
            Channel channel = channelsPool.getChannel();
            // ori can work
            /*
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null,
                    liftRideMessage.getBytes("UTF-8"));*/

            // direct solution
            /*
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueDeclare("skier", true, false, false, null);
            channel.queueBind("skier", EXCHANGE_NAME, "skier");
            channel.basicPublish(EXCHANGE_NAME, "skier", null, liftRideMessage.getBytes("UTF-8"));


            channel.queueDeclare("resort", true, false, false, null);
            channel.queueBind("resort", EXCHANGE_NAME, "resort");
            channel.basicPublish(EXCHANGE_NAME, "resort", null, liftRideMessage.getBytes("UTF-8"));*/

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            /*
            channel.queueDeclare("skier", true, false, false, null);
            channel.queueDeclare("resort", true, false, false, null);
            channel.queueBind("skier", EXCHANGE_NAME, "");
            channel.queueBind("resort", EXCHANGE_NAME, "");*/
            channel.basicPublish(EXCHANGE_NAME, "", null, liftRideMessage.getBytes("UTF-8"));

            channelsPool.returnChannel(channel);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}



