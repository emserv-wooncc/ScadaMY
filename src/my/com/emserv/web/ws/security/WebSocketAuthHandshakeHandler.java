package my.com.emserv.web.ws.security;

import com.serotonin.mango.vo.User;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class WebSocketAuthHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        User user = (User) attributes.get("user");
        if (user != null) {
            return new WebSocketPrincipal(user);
        }
        return null;
    }
}
