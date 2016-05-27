package me.stuntguy3000.java.ledhub.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.stuntguy3000.java.ledhub.LEDHub;
import me.stuntguy3000.java.ledhub.object.LEDService;
import me.stuntguy3000.java.ledhub.object.LEDServiceAction;

/**
 * @author stuntguy3000
 */
@WebServlet(name = "ServiceServlet", urlPatterns = "/service")
public class ServiceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String serviceName = req.getParameter("service");
        String actionName = req.getParameter("action");

        if (serviceName != null) {
            LEDService ledService = LEDHub.getInstance().getServiceHandler().getService(serviceName);
            LEDServiceAction action = ledService.getServiceActions().get(actionName);

            LEDHub.getInstance().getServiceHandler().addToServiceQueue(action);
            return;
        }

        resp.sendError(404);
    }
}
