package ru.cdecl.pub.iota.servlets;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.servlet.FiberHttpServlet;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import ru.cdecl.pub.iota.services.AccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import ru.cdecl.pub.iota.models.UserProfile;

@Service
@Singleton
@WebServlet(asyncSupported = true)
public final class ConcreteUserServlet extends FiberHttpServlet {

    @Inject
    AccountService accountService;

    @Override
    @Suspendable
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Long userId = getUserIdFromHttpRequest(req);
        if (userId == null) {
            makeInvalidResponse(resp);
            return;
        }

        UserProfile profile = accountService.getUserProfile(userId);
        //String email = profile.getEmail();
        //String login = profile.getLogin();
        //if (email.equals("") || login.equals("")) {
        //    makeInvalidResponse(resp);
        //    return;
        //} //TODO uncomment when implemented(a.petrukhin).

        JSONObject object = new JSONObject();
        object.put("id", userId);
        //object.put("login", login);
        //object.put("email", email);

        resp.getWriter().write(object.toString());
    }

    @Override
    @Suspendable
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Long userId = getUserIdFromHttpRequest(req);
        if (userId == null) {
            makeInvalidResponse(resp);
            return;
        }
        JSONObject obj = makeJSONFromRequest(req);
        resp.setStatus(200);
        resp.getWriter().write(obj.toString());
    }

    @Override
    @Suspendable
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Long userId = getUserIdFromHttpRequest(req);
        //
        super.doDelete(req, resp);
    }

    @Nullable
    private static Long getUserIdFromHttpRequest(HttpServletRequest req) {
        final String requestUri = req.getRequestURI();
        try {
            return Long.parseLong(requestUri.substring(requestUri.lastIndexOf('/') + 1));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("Can not convert userID from String to Long in getUserIdFromHttpRequest in ConcreteUserServlet");
            return null;
        }
    }

    private void makeInvalidResponse(HttpServletResponse resp) throws IOException, ServletException {
        resp.setStatus(401);
        resp.getWriter().write("{}");
    }

    private JSONObject makeJSONFromRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }

        System.out.println(sb.toString());
        return new JSONObject(sb.toString());
    }
}
