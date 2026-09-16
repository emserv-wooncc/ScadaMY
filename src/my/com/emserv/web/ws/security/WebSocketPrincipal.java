package my.com.emserv.web.ws.security;

import com.serotonin.mango.vo.User;
import java.security.Principal;

public class WebSocketPrincipal implements Principal {
    private final User user;

    public WebSocketPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return user != null ? user.getUsername() : "anonymous";
    }
}
