package my.com.emserv.web.ws.controller;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.User;
import my.com.emserv.web.ws.security.WebSocketPrincipal;
import my.com.emserv.web.ws.service.EventsWebSocketService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EventsWebSocketController {

    @SubscribeMapping("/events/summary")
    public Map<String, Object> getInitialSummary(Principal principal) {
        Map<String, Object> payload = new HashMap<>();
        
        int activeCount = 0;
        int highestLevel = 0;

        if (principal instanceof WebSocketPrincipal) {
            User user = ((WebSocketPrincipal) principal).getUser();
            if (user != null && Common.ctx != null && Common.ctx.getEventManager() != null) {
                EventManager evMgr = Common.ctx.getEventManager();
                Integer countObj = evMgr.getActiveAlarmCountPerUser(user.getId());
                activeCount = countObj != null ? countObj : 0;
                highestLevel = evMgr.getHighestActiveEventLevelByUser(user.getId());
            }
        }

        payload.put("activeAlarmCount", activeCount);
        payload.put("highestAlarmLevel", highestLevel);
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }

    @SubscribeMapping("/events/pending")
    public Map<String, Object> getInitialPendingList(Principal principal) {
        Map<String, Object> payload = new HashMap<>();
        
        if (principal instanceof WebSocketPrincipal) {
            User user = ((WebSocketPrincipal) principal).getUser();
            if (user != null && Common.ctx != null && Common.ctx.getEventManager() != null) {
                List<EventInstance> userEvents = Common.ctx.getEventManager().getActiveEventsByUser(user.getId());
                payload.put("events", userEvents);
                payload.put("count", userEvents.size());
                payload.put("timestamp", System.currentTimeMillis());
                return payload;
            }
        }

        payload.put("events", null);
        payload.put("count", 0);
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }

    @MessageMapping("/events/{eventId}/ack")
    public void acknowledgeEventOverWs(@DestinationVariable int eventId, @Payload(required = false) Map<String, Object> request, Principal principal) {
        if (principal == null || !(principal instanceof WebSocketPrincipal)) {
            throw new IllegalArgumentException("User principal is required to acknowledge event over WebSocket.");
        }
        User user = ((WebSocketPrincipal) principal).getUser();
        if (user == null || user.isDisabled()) {
            throw new IllegalArgumentException("Valid user is required to acknowledge event.");
        }

        long time = System.currentTimeMillis();
        new EventDao().ackEvent(eventId, time, user.getId(), EventInstance.AlternateAcknowledgementSources.MAINTENANCE_MODE);

        EventsWebSocketService service = EventsWebSocketService.getInstance();
        if (service != null) {
            List<String> usernames = Common.ctx.getEventManager().getUsernamesForEvent(eventId);
            service.notifyEventAcknowledged(eventId, user.getId(), user.getUsername(), time, usernames);
        }
    }
}
