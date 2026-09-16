package my.com.emserv.web.ws.service;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.EventInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventsWebSocketService {

    private static EventsWebSocketService instance;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public EventsWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        EventsWebSocketService.instance = this;
    }

    public static EventsWebSocketService getInstance() {
        return instance;
    }

    /**
     * Broadcasts when an event is newly raised in the system.
     */
    public void notifyEventRaised(EventInstance evt, List<String> usernames) {
        if (messagingTemplate == null || evt == null || usernames == null || usernames.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", evt.getId());
        payload.put("alarmLevel", evt.getAlarmLevel());
        payload.put("message", evt.getMessage() != null && Common.getBundle() != null ? 
                evt.getMessage().getLocalizedMessage(Common.getBundle()) : 
                (evt.getMessage() != null ? evt.getMessage().getKey() : ""));
        payload.put("activeTimestamp", evt.getActiveTimestamp());
        payload.put("rtnApplicable", evt.isRtnApplicable());
        payload.put("acknowledged", false);
        payload.put("status", "ACTIVE");

        if (evt.getEventType() != null && evt.getEventType().getDataPointId() > 0) {
            payload.put("dataPointId", evt.getEventType().getDataPointId());
        }

        try {
            for (String username : usernames) {
                messagingTemplate.convertAndSendToUser(username, "/topic/events/new", payload);
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting new event " + evt.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Broadcasts when an event is acknowledged by any operator or maintenance mode.
     */
    public void notifyEventAcknowledged(int eventId, int ackByUserId, String ackByUsername, long ackTime, List<String> usernames) {
        if (messagingTemplate == null || usernames == null || usernames.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", eventId);
        payload.put("acknowledged", true);
        payload.put("ackByUserId", ackByUserId);
        payload.put("ackByUsername", ackByUsername);
        payload.put("ackTime", ackTime);
        payload.put("status", "ACKNOWLEDGED");

        try {
            for (String username : usernames) {
                messagingTemplate.convertAndSendToUser(username, "/topic/events/update", payload);
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting event ack update for " + eventId + ": " + e.getMessage());
        }
    }

    /**
     * Broadcasts when an event returns to normal (RTN).
     */
    public void notifyEventReturnToNormal(EventInstance evt, long time, int cause, List<String> usernames) {
        if (messagingTemplate == null || evt == null || usernames == null || usernames.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", evt.getId());
        payload.put("rtnTimestamp", time);
        payload.put("rtnCause", cause);
        payload.put("status", "RETURN_TO_NORMAL");

        if (evt.getEventType() != null && evt.getEventType().getDataPointId() > 0) {
            payload.put("dataPointId", evt.getEventType().getDataPointId());
        }

        try {
            for (String username : usernames) {
                messagingTemplate.convertAndSendToUser(username, "/topic/events/update", payload);
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting RTN update for event " + evt.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Broadcasts when the highest active alarm level changes for a specific user.
     */
    public void notifyMaxAlarmLevelChanged(String username, int oldLevel, int newLevel) {
        if (messagingTemplate == null || username == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("oldLevel", oldLevel);
        payload.put("newLevel", newLevel);
        payload.put("timestamp", System.currentTimeMillis());

        try {
            messagingTemplate.convertAndSendToUser(username, "/topic/events/maxLevel", payload);
        } catch (Exception e) {
            System.err.println("Error broadcasting max alarm level change for user " + username + ": " + e.getMessage());
        }
    }
}
