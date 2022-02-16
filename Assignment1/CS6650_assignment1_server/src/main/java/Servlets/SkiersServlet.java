package Servlets;

import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "Servlets.SkiersServlet", value = "/Servlets.SkiersServlet")
public class SkiersServlet extends HttpServlet {
    private ServletsUtil servletsUtil = new ServletsUtil();

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
            res.setStatus(HttpServletResponse.SC_CREATED);
            String resortId = urlParts[1];
            String seasonId = urlParts[3];
            String dayId = urlParts[5];
            String skierId = urlParts[7];
            String resBody = "Response Msg -> resortId: " + resortId
                    + " seasonId: " + seasonId
                    + " dayId: " + dayId
                    + " skierId: " + skierId;

            JsonObject jsonObject = new JsonObject();
            servletsUtil.writeJsonObject(res, resBody, jsonObject);
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

}
