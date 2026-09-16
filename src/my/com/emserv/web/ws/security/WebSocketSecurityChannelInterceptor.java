package my.com.emserv.web.ws.security;

import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.Permissions;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketSecurityChannelInterceptor extends ChannelInterceptorAdapter {

    private static final Pattern DATAPOINT_READ_PATTERN = Pattern.compile("^/(topic|app)/datapoint/(\\d+)(/.*)?$");
    private static final Pattern DATAPOINT_SET_PATTERN = Pattern.compile("^/(topic|app)/datapoint/(\\d+)/set(/.*)?$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        User user = getUser(accessor);

        if (command == StompCommand.CONNECT) {
            if (user == null || user.isDisabled()) {
                throw new IllegalArgumentException("WebSocket access denied: User not authenticated or account is disabled.");
            }
        } else if (command == StompCommand.SUBSCRIBE) {
            if (user == null || user.isDisabled()) {
                throw new IllegalArgumentException("SUBSCRIBE access denied: User not authenticated.");
            }
            String destination = accessor.getDestination();
            if (destination != null) {
                checkSubscriptionPermission(user, destination);
            }
        } else if (command == StompCommand.SEND) {
            if (user == null || user.isDisabled()) {
                throw new IllegalArgumentException("SEND access denied: User not authenticated.");
            }
            String destination = accessor.getDestination();
            if (destination != null) {
                checkSendPermission(user, destination);
            }
        }

        return message;
    }

    private User getUser(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof WebSocketPrincipal) {
            return ((WebSocketPrincipal) principal).getUser();
        }
        if (accessor.getSessionAttributes() != null) {
            return (User) accessor.getSessionAttributes().get("user");
        }
        return null;
    }

    private void checkSubscriptionPermission(User user, String destination) {
        Matcher matcher = DATAPOINT_READ_PATTERN.matcher(destination);
        if (matcher.matches()) {
            int pointId = Integer.parseInt(matcher.group(2));
            DataPointVO point = new DataPointDao().getDataPoint(pointId);
            if (point == null) {
                throw new IllegalArgumentException("SUBSCRIBE access denied: Data point " + pointId + " does not exist.");
            }
            if (!Permissions.hasDataPointReadPermission(user, point)) {
                throw new IllegalArgumentException("SUBSCRIBE access denied: User '" + user.getUsername() + "' lacks read permission for Data Point " + pointId + " (" + point.getName() + ").");
            }
        }
    }

    private void checkSendPermission(User user, String destination) {
        Matcher matcher = DATAPOINT_SET_PATTERN.matcher(destination);
        if (matcher.matches()) {
            int pointId = Integer.parseInt(matcher.group(2));
            DataPointVO point = new DataPointDao().getDataPoint(pointId);
            if (point == null) {
                throw new IllegalArgumentException("SEND access denied: Data point " + pointId + " does not exist.");
            }
            if (!Permissions.hasDataPointSetPermission(user, point)) {
                throw new IllegalArgumentException("SEND access denied: User '" + user.getUsername() + "' lacks set permission for Data Point " + pointId + " (" + point.getName() + ").");
            }
        }
    }
}
