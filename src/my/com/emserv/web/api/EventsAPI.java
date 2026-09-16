package my.com.emserv.web.api;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.EventManager;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import my.com.emserv.web.ws.service.EventsWebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Api(value = "Events API", tags = "Events Operations")
public class EventsAPI {

    @ApiOperation(value = "Get active summary statistics (count and highest alarm level)")
    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSummary(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();
        int activeCount = 0;
        int criticalCount = 0;
        int highestLevel = 0;

        if (Common.ctx != null && Common.ctx.getEventManager() != null) {
            EventManager evMgr = Common.ctx.getEventManager();
            Integer countObj = evMgr.getActiveAlarmCountPerUser(user.getId());
            activeCount = countObj != null ? countObj : 0;
            highestLevel = evMgr.getHighestActiveEventLevelByUser(user.getId());

            // ScadaMY Modification: Calculate critical alarm count
            List<EventInstance> activeEvents = evMgr.getActiveEventsByUser(user.getId());
            if (activeEvents != null) {
                for (EventInstance e : activeEvents) {
                    if (e.getAlarmLevel() >= 3) {
                        criticalCount++;
                    }
                }
            }
        }

        response.put("activeAlarmCount", activeCount);
        response.put("criticalAlarmCount", criticalCount);
        response.put("highestAlarmLevel", highestLevel);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get list of real-time active events for current user from in-memory EventManager", response = Object.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getActiveEvents(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<EventInstance> data = null;
        if (Common.ctx != null && Common.ctx.getEventManager() != null) {
            data = Common.ctx.getEventManager().getActiveEventsByUser(user.getId());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (data != null) {
            for (EventInstance evt : data) {
                result.add(mapEventToMap(evt));
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get list of pending events for current user from persistent database (EventDao)", response = Object.class, responseContainer = "List")
    @RequestMapping(value = "/pending", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getPendingEvents(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<EventInstance> data = new EventDao().getPendingEvents(user.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        if (data != null) {
            for (EventInstance evt : data) {
                result.add(mapEventToMap(evt));
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Search historical and active alarms with filtering and pagination")
    @RequestMapping(value = "/search", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<Map<String, Object>> searchEvents(
            @RequestParam(required = false, defaultValue = "0") int eventId,
            @RequestParam(required = false) int[] sourceType,
            @RequestParam(required = false) String[] status,
            @RequestParam(required = false) int[] alarmLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "-1") long dateFrom,
            @RequestParam(required = false, defaultValue = "-1") long dateTo,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "50") int limit,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (sourceType == null) {
            sourceType = new int[0];
        }
        List<String> validStatus = new ArrayList<>();
        if (status != null) {
            for (String s : status) {
                if ("A".equalsIgnoreCase(s) || "R".equalsIgnoreCase(s) || "N".equalsIgnoreCase(s)) {
                    validStatus.add(s.toUpperCase());
                }
            }
        }
        status = validStatus.toArray(new String[0]);
        if (alarmLevel == null) {
            alarmLevel = new int[0];
        }

        String[] keywordArr = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            keywordArr = keyword.trim().split("\\s+");
        }

        int from = Math.max(0, offset);
        int to = limit <= 0 ? Integer.MAX_VALUE : (from + limit);

        EventDao eventDao = new EventDao();
        List<EventInstance> results = eventDao.search(
                eventId,
                sourceType,
                status,
                alarmLevel,
                keywordArr,
                dateFrom,
                dateTo,
                user.getId(),
                Common.getBundle(),
                from,
                to,
                new Date(0));

        List<Map<String, Object>> eventMaps = new ArrayList<>();
        if (results != null) {
            for (EventInstance evt : results) {
                eventMaps.add(mapEventToMap(evt));
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", eventDao.getSearchRowCount());
        response.put("offset", from);
        response.put("limit", limit);
        response.put("events", eventMaps);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> mapEventToMap(EventInstance evt) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", evt.getId());
        map.put("alarmLevel", evt.getAlarmLevel());
        map.put("activeTimestamp", evt.getActiveTimestamp());
        map.put("rtnApplicable", evt.isRtnApplicable());
        map.put("rtnTimestamp", evt.getRtnTimestamp());
        map.put("acknowledged", evt.isAcknowledged());
        map.put("ackUserId", evt.getAcknowledgedByUserId());
        map.put("ackUsername", evt.getAcknowledgedByUsername());
        map.put("ackTimestamp", evt.getAcknowledgedTimestamp());
        map.put("hasComments", evt.getEventComments() != null && !evt.getEventComments().isEmpty());

        if (evt.getMessage() != null && Common.getBundle() != null) {
            map.put("messageString", evt.getMessage().getLocalizedMessage(Common.getBundle()));
        } else if (evt.getMessage() != null) {
            map.put("messageString", evt.getMessage().getKey());
        } else {
            map.put("messageString", "");
        }

        if (evt.getEventType() != null) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("typeId", evt.getEventType().getEventSourceId());
            typeMap.put("dataPointId", evt.getEventType().getDataPointId());
            typeMap.put("duplicateHandling", evt.getEventType().getDuplicateHandling());
            map.put("eventType", typeMap);
        }

        return map;
    }

    @ApiOperation(value = "Get user comments for a specific event")
    @RequestMapping(value = "/{eventId}/comments", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getEventComments(@PathVariable int eventId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EventInstance evt = new EventDao().getEventInstance(eventId);
        if (evt == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (evt.getEventComments() != null) {
            for (UserComment c : evt.getEventComments()) {
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("userId", c.getUserId());
                cMap.put("username", c.getUsername());
                cMap.put("ts", c.getTs());
                cMap.put("comment", c.getComment());
                result.add(cMap);
            }
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Add a user comment to a specific event")
    @RequestMapping(value = "/{eventId}/comments", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addEventComment(@PathVariable int eventId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String text = body.get("comment");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UserComment comment = new UserComment();
        comment.setUserId(user.getId());
        comment.setTs(System.currentTimeMillis());
        comment.setComment(text);

        new EventDao().insertEventComment(eventId, comment);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("ts", comment.getTs());
        response.put("comment", text);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Acknowledge a specific event")
    @RequestMapping(value = "/{eventId}/acknowledge", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> acknowledgeEvent(@PathVariable int eventId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long time = System.currentTimeMillis();
        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            new EventDao().ackEvent(eventId, time, user.getId(),
                    EventInstance.AlternateAcknowledgementSources.MAINTENANCE_MODE);

            EventsWebSocketService service = EventsWebSocketService.getInstance();
            if (service != null) {
                List<String> usernames = Common.ctx.getEventManager().getUsernamesForEvent(eventId);
                service.notifyEventAcknowledged(eventId, user.getId(), user.getUsername(), time, usernames);
            }
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("eventId", eventId);
        response.put("ackTime", time);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Acknowledge all active events for the current user")
    @RequestMapping(value = "/acknowledge-all", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> acknowledgeAllEvents(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long time = System.currentTimeMillis();
        List<EventInstance> pendingEvents = new EventDao().getPendingEvents(user.getId());

        int count = 0;
        EventDao eventDao = new EventDao();
        EventsWebSocketService service = EventsWebSocketService.getInstance();

        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            if (pendingEvents != null) {
                for (EventInstance evt : pendingEvents) {
                    if (!evt.isAcknowledged()) {
                        eventDao.ackEvent(evt.getId(), time, user.getId(),
                                EventInstance.AlternateAcknowledgementSources.MAINTENANCE_MODE);
                        if (service != null) {
                            List<String> usernames = Common.ctx.getEventManager().getUsernamesForEvent(evt.getId());
                            service.notifyEventAcknowledged(evt.getId(), user.getId(), user.getUsername(), time, usernames);
                        }
                        count++;
                    }
                }
            }
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("acknowledgedCount", count);
        response.put("ackTime", time);
        return ResponseEntity.ok(response);
    }
}
