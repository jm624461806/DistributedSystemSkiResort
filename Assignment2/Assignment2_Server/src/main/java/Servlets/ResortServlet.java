package Servlets;

import Model.HttpMethod;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Servlets.ResortServlet", value = "/Servlets.ResortServlet")
public class ResortServlet extends HttpServlet {
    ServletsUtil servletsUtil = new ServletsUtil();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processHttpMethod(req, res, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processHttpMethod(req, res, HttpMethod.POST);
    }

    private void processHttpMethod(HttpServletRequest req, HttpServletResponse res, HttpMethod method)
            throws IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("This is the list of the resorts");
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
                res.getWriter().write("This is the list of seasons for the specified resort");
            } else {
                res.getWriter().write("This is the number of unique skiers");
            }
            return;
        }

        // handle the post method
        if (method == HttpMethod.POST) {
            res.setStatus(HttpServletResponse.SC_CREATED);
            String resortId = urlParts[1];
            String resBody = "Response Message -> resortId: " + resortId;

            JsonObject jsonObject = new JsonObject();
            servletsUtil.writeJsonObject(res, resBody, jsonObject);
        }
    }


    private boolean isValidUrl(String[] urlParts) {
        if(!urlParts[2].equals("seasons")) return false;
        if(urlParts.length != 3 && urlParts.length != 7) return false;
        if(urlParts.length == 3) {
            return servletsUtil.isValidNumber(urlParts[1]);
        } else {
            if(!urlParts[4].equals("day") || !urlParts[6].equals("skiers")) return false;
            return servletsUtil.isValidNumber(urlParts[1]) &&
                    servletsUtil.isValidNumber(urlParts[3]) && servletsUtil.isValidNumber(urlParts[5]);
        }
    }

}
