package ru.cdecl.pub.iota.servlets;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.servlet.FiberHttpServlet;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jvnet.hk2.annotations.Service;
import ru.cdecl.pub.iota.services.AccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            makeErrorResponse(resp, RESP_STATUS_NOT_AUTHORIZED, "{}");
            return;
        }

        UserProfile profile = accountService.getUserProfile(userId);
        final String email = profile.getEmail();
        final String login = profile.getLogin();
        if (!"".equals(email) || "".equals(login)) {
            makeErrorResponse(resp, RESP_STATUS_FORBIDDEN, "Can not get user");
            return;
        }

        final JSONObject object = new JSONObject();
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
            makeErrorResponse(resp, RESP_STATUS_NOT_AUTHORIZED, "{}");
            return;
        }
        final JSONObject obj = makeJSONFromRequest(req);

        if (!obj.has("login") || !obj.has("email") || !obj.has("password")) {
            resp.setStatus(RESP_STATUS_SERVER_ERROR); //TODO: change status code (a.petrukhin).
            resp.getWriter().write("Invalid JSON Request");
            return;
        }

        String login = obj.get("login").toString();
        UserProfile profile = accountService.getUserProfile(login);
        //String profileLogin = profile.getLogin(); //TODO implement when ready(a.petrukhin).
        if (!(login.equals("test"))) {
            final JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", RESP_STATUS_FORBIDDEN);
            jsonResponse.put("message", "Чужой юзер");
            makeErrorResponse(resp, RESP_STATUS_FORBIDDEN, jsonResponse.toString());
            return;
        }

        resp.setStatus(RESP_STATUS_OK);
        resp.getWriter().write("test");
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
            return null;
        }
    }

    private void makeErrorResponse(HttpServletResponse resp, int respCode, String message) throws IOException, ServletException {
        resp.setStatus(respCode);
        resp.getWriter().write(message);
    }

    private JSONObject makeJSONFromRequest(HttpServletRequest req) throws IOException {
        final JSONTokener tokener = new JSONTokener(req.getInputStream());
        return new JSONObject(tokener);
    }

    private static final int RESP_STATUS_OK = 200;
    private static final int RESP_STATUS_NOT_AUTHORIZED = 401;
    private static final int RESP_STATUS_FORBIDDEN = 403;
    private static final int RESP_STATUS_SERVER_ERROR = 500;
}
