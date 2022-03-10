package Servlets;

import Channels.ChannelsPool;
import Model.HttpMethod;
import Model.LiftRide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "Servlets.SkiersServlet", value = "/Servlets.SkiersServlet")
public class SkiersServlet extends HttpServlet {
    public final static String QUEUE_NAME = "messages";

    private ChannelsPool channelsPool = new ChannelsPool();
    private ServletsUtil servletsUtil = new ServletsUtil();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        processHttpMethod(req, res, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        processHttpMethod(req, res, HttpMethod.POST);
    }

    private void processHttpMethod(HttpServletRequest req, HttpServletResponse res, HttpMethod method)
            throws IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

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
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null,
                    liftRideMessage.getBytes("UTF-8"));
            channelsPool.returnChannel(channel);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }



}
