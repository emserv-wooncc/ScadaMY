package my.com.emserv.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.event.EventHandlerVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.EventHandlersDwr;
import com.serotonin.mango.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Event Handlers (/event_handlers.shtm).
 * All endpoints enforce user data source and event type permissions.
 */
@RestController
@RequestMapping("/api/event-handlers")
@Api(value = "Event Handlers API", tags = "Event Handlers Management")
public class EventHandlersAPI {
    private static final Log logger = LogFactory.getLog(EventHandlersAPI.class);

    @ApiOperation(value = "Get initialization context data including all event sources, detectors, system/audit events and their handlers", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BackgroundContext.set(user);
        try {
            EventHandlersDwr dwr = new EventHandlersDwr();
            Map<String, Object> initData = dwr.getInitData();

            // Ensure allPoints has a complete list of readable data points
            if (initData.get("allPoints") == null || ((List<?>) initData.get("allPoints")).isEmpty()) {
                List<DataPointVO> dps = new DataPointDao().getDataPoints(DataPointExtendedNameComparator.instance, false);
                List<Map<String, Object>> allPointsList = new ArrayList<>();
                for (DataPointVO dp : dps) {
                    if (Permissions.hasDataPointReadPermission(user, dp)) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("id", dp.getId());
                        p.put("name", dp.getExtendedName());
                        p.put("settable", dp.getPointLocator().isSettable());
                        p.put("dataType", dp.getPointLocator().getDataTypeId());
                        allPointsList.add(p);
                    }
                }
                initData.put("allPoints", allPointsList);
            }

            return ResponseEntity.ok(initData);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks data source permission when fetching init data");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error retrieving event handlers initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Get all event handlers in the system", response = Object.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getEventHandlers(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        EventDao eventDao = new EventDao();
        List<EventHandlerVO> handlers = eventDao.getEventHandlers();
        List<Map<String, Object>> result = new ArrayList<>();
        if (handlers != null) {
            for (EventHandlerVO h : handlers) {
                result.add(mapEventHandlerToMap(h));
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get details of a specific event handler by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getEventHandler(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EventDao eventDao = new EventDao();
        EventHandlerVO handler = eventDao.getEventHandler(id);
        if (handler == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            EventType type = eventDao.getEventHandlerType(id);
            if (type != null) {
                Permissions.ensureEventTypePermission(user, type);
            }
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission for event handler ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mapEventHandlerToMap(handler));
    }

    @ApiOperation(value = "Create a new event handler", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createEventHandler(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return parseAndSaveEventHandler(Common.NEW_ID, body, user);
    }

    @ApiOperation(value = "Update an existing event handler by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateEventHandler(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EventDao eventDao = new EventDao();
        EventHandlerVO existing = eventDao.getEventHandler(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveEventHandler(id, body, user);
    }

    @ApiOperation(value = "Delete an event handler by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteEventHandler(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EventDao eventDao = new EventDao();
        EventHandlerVO existing = eventDao.getEventHandler(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            EventType type = eventDao.getEventHandlerType(id);
            if (type != null) {
                Permissions.ensureEventTypePermission(user, type);
            }
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to delete event handler ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BackgroundContext.set(user);
        try {
            eventDao.deleteEventHandler(id);
            logger.info("Event handler deleted successfully: ID=" + id + " by user=" + user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Test a process command on the server", response = Object.class)
    @RequestMapping(value = "/test-process-command", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> testProcessCommand(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String command = body.get("command") != null ? body.get("command").toString() : "";
        if (StringUtils.isEmpty(command)) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Command cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        LocalizableMessage msg = new EventHandlersDwr().testProcessCommand(command);
        Map<String, Object> response = new HashMap<>();
        response.put("success", msg != null && !"common.default".equals(msg.getKey()));
        response.put("message", msg != null ? msg.getLocalizedMessage(Common.getBundle()) : "");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Generate sample set value content for testing set point handler snippet", response = Object.class)
    @RequestMapping(value = "/test-set-value", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> testSetValueContent(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int pointId = body.get("pointId") instanceof Number ? ((Number) body.get("pointId")).intValue() : 0;
        String valueStr = body.get("valueStr") != null ? body.get("valueStr").toString() : "";
        String idSuffix = body.get("idSuffix") != null ? body.get("idSuffix").toString() : "";

        BackgroundContext.set(user);
        try {
            String content = new EventHandlersDwr().createSetValueContent(pointId, valueStr, idSuffix);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", content);
            return ResponseEntity.ok(response);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.warn("Error generating set value content for point ID: " + pointId, e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    private ResponseEntity<Map<String, Object>> parseAndSaveEventHandler(int id, Map<String, Object> body, User user) {
        EventDao eventDao = new EventDao();
        EventHandlerVO vo;

        if (id == Common.NEW_ID) {
            vo = new EventHandlerVO();
            vo.setId(Common.NEW_ID);
        } else {
            vo = eventDao.getEventHandler(id);
            if (vo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        int eventSourceId = 0;
        int eventTypeRef1 = 0;
        int eventTypeRef2 = 0;

        if (body.containsKey("eventSourceId")) {
            eventSourceId = body.get("eventSourceId") instanceof Number ? ((Number) body.get("eventSourceId")).intValue() : 0;
        } else if (body.containsKey("eventTypeId")) {
            eventSourceId = body.get("eventTypeId") instanceof Number ? ((Number) body.get("eventTypeId")).intValue() : 0;
        } else if (id != Common.NEW_ID) {
            EventType et = eventDao.getEventHandlerType(id);
            if (et != null) {
                eventSourceId = et.getEventSourceId();
                eventTypeRef1 = et.getReferenceId1();
                eventTypeRef2 = et.getReferenceId2();
            }
        }

        if (body.containsKey("eventTypeRef1")) {
            eventTypeRef1 = body.get("eventTypeRef1") instanceof Number ? ((Number) body.get("eventTypeRef1")).intValue() : 0;
        }
        if (body.containsKey("eventTypeRef2")) {
            eventTypeRef2 = body.get("eventTypeRef2") instanceof Number ? ((Number) body.get("eventTypeRef2")).intValue() : 0;
        }

        EventTypeVO typeVO = new EventTypeVO(eventSourceId, eventTypeRef1, eventTypeRef2);
        try {
            if (eventSourceId > 0 || eventTypeRef1 > 0 || eventTypeRef2 > 0) {
                Permissions.ensureEventTypePermission(user, typeVO);
            } else if (id != Common.NEW_ID) {
                EventType et = eventDao.getEventHandlerType(id);
                if (et != null) {
                    Permissions.ensureEventTypePermission(user, et);
                }
            } else {
                Permissions.ensureDataSourcePermission(user);
            }
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission for event handler operation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (body.get("xid") != null && !body.get("xid").toString().trim().isEmpty()) {
            vo.setXid(body.get("xid").toString().trim());
        } else if (vo.getXid() == null || vo.getXid().isEmpty()) {
            vo.setXid(eventDao.generateUniqueXid());
        }

        if (body.get("alias") != null) {
            vo.setAlias(body.get("alias").toString().trim());
        }

        if (body.get("handlerType") instanceof Number) {
            vo.setHandlerType(((Number) body.get("handlerType")).intValue());
        } else if (id == Common.NEW_ID) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("error", "handlerType is required when creating a new event handler");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        if (body.get("disabled") instanceof Boolean) {
            vo.setDisabled((Boolean) body.get("disabled"));
        } else if (body.get("disabled") != null) {
            vo.setDisabled(Boolean.parseBoolean(body.get("disabled").toString()));
        }

        switch (vo.getHandlerType()) {
            case EventHandlerVO.TYPE_SET_POINT:
                if (body.get("targetPointId") instanceof Number) {
                    vo.setTargetPointId(((Number) body.get("targetPointId")).intValue());
                }
                if (body.get("activeAction") instanceof Number) {
                    vo.setActiveAction(((Number) body.get("activeAction")).intValue());
                }
                if (body.get("activeValueToSet") != null) {
                    vo.setActiveValueToSet(body.get("activeValueToSet").toString());
                }
                if (body.get("activePointId") instanceof Number) {
                    vo.setActivePointId(((Number) body.get("activePointId")).intValue());
                }
                if (body.get("inactiveAction") instanceof Number) {
                    vo.setInactiveAction(((Number) body.get("inactiveAction")).intValue());
                }
                if (body.get("inactiveValueToSet") != null) {
                    vo.setInactiveValueToSet(body.get("inactiveValueToSet").toString());
                }
                if (body.get("inactivePointId") instanceof Number) {
                    vo.setInactivePointId(((Number) body.get("inactivePointId")).intValue());
                }
                break;
            case EventHandlerVO.TYPE_EMAIL:
                if (body.get("activeRecipients") != null) {
                    vo.setActiveRecipients(parseRecipientsList(body.get("activeRecipients")));
                } else if (vo.getActiveRecipients() == null) {
                    vo.setActiveRecipients(new ArrayList<RecipientListEntryBean>());
                }
                if (body.get("sendEscalation") instanceof Boolean) {
                    vo.setSendEscalation((Boolean) body.get("sendEscalation"));
                } else if (body.get("sendEscalation") != null) {
                    vo.setSendEscalation(Boolean.parseBoolean(body.get("sendEscalation").toString()));
                }
                if (body.get("escalationDelayType") instanceof Number) {
                    vo.setEscalationDelayType(((Number) body.get("escalationDelayType")).intValue());
                }
                if (body.get("escalationDelay") instanceof Number) {
                    vo.setEscalationDelay(((Number) body.get("escalationDelay")).intValue());
                }
                if (body.get("escalationRecipients") != null) {
                    vo.setEscalationRecipients(parseRecipientsList(body.get("escalationRecipients")));
                } else if (vo.getEscalationRecipients() == null) {
                    vo.setEscalationRecipients(new ArrayList<RecipientListEntryBean>());
                }
                if (body.get("sendInactive") instanceof Boolean) {
                    vo.setSendInactive((Boolean) body.get("sendInactive"));
                } else if (body.get("sendInactive") != null) {
                    vo.setSendInactive(Boolean.parseBoolean(body.get("sendInactive").toString()));
                }
                if (body.get("inactiveOverride") instanceof Boolean) {
                    vo.setInactiveOverride((Boolean) body.get("inactiveOverride"));
                } else if (body.get("inactiveOverride") != null) {
                    vo.setInactiveOverride(Boolean.parseBoolean(body.get("inactiveOverride").toString()));
                }
                if (body.get("inactiveRecipients") != null) {
                    vo.setInactiveRecipients(parseRecipientsList(body.get("inactiveRecipients")));
                } else if (vo.getInactiveRecipients() == null) {
                    vo.setInactiveRecipients(new ArrayList<RecipientListEntryBean>());
                }
                break;
            case EventHandlerVO.TYPE_PROCESS:
                if (body.get("activeProcessCommand") != null) {
                    vo.setActiveProcessCommand(body.get("activeProcessCommand").toString());
                }
                if (body.get("inactiveProcessCommand") != null) {
                    vo.setInactiveProcessCommand(body.get("inactiveProcessCommand").toString());
                }
                break;
            case EventHandlerVO.TYPE_SCRIPT:
                if (body.get("activeScriptCommand") instanceof Number) {
                    vo.setActiveScriptCommand(((Number) body.get("activeScriptCommand")).intValue());
                }
                if (body.get("inactiveScriptCommand") instanceof Number) {
                    vo.setInactiveScriptCommand(((Number) body.get("inactiveScriptCommand")).intValue());
                }
                break;
        }

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        vo.validate(validationResponse);

        if (validationResponse.getHasMessages()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("messages", validationResponse.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        BackgroundContext.set(user);
        try {
            vo = eventDao.saveEventHandler(typeVO, vo);
            logger.info("Event handler saved successfully: ID=" + vo.getId() + ", alias=" + vo.getAlias() + ", by user=" + user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("handler", mapEventHandlerToMap(vo));
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    private Map<String, Object> mapEventHandlerToMap(EventHandlerVO h) {
        Map<String, Object> map = new HashMap<>();
        if (h == null) {
            return map;
        }

        map.put("id", h.getId());
        map.put("xid", h.getXid());
        map.put("alias", h.getAlias());
        map.put("handlerType", h.getHandlerType());
        map.put("disabled", h.isDisabled());

        EventDao eventDao = new EventDao();
        EventType et = eventDao.getEventHandlerType(h.getId());
        if (et != null) {
            map.put("eventSourceId", et.getEventSourceId());
            map.put("eventTypeRef1", et.getReferenceId1());
            map.put("eventTypeRef2", et.getReferenceId2());
        }

        switch (h.getHandlerType()) {
            case EventHandlerVO.TYPE_SET_POINT:
                map.put("targetPointId", h.getTargetPointId());
                map.put("activeAction", h.getActiveAction());
                map.put("activeValueToSet", h.getActiveValueToSet());
                map.put("activePointId", h.getActivePointId());
                map.put("inactiveAction", h.getInactiveAction());
                map.put("inactiveValueToSet", h.getInactiveValueToSet());
                map.put("inactivePointId", h.getInactivePointId());
                break;
            case EventHandlerVO.TYPE_EMAIL:
                map.put("activeRecipients", mapRecipientsList(h.getActiveRecipients()));
                map.put("sendEscalation", h.isSendEscalation());
                map.put("escalationDelayType", h.getEscalationDelayType());
                map.put("escalationDelay", h.getEscalationDelay());
                map.put("escalationRecipients", mapRecipientsList(h.getEscalationRecipients()));
                map.put("sendInactive", h.isSendInactive());
                map.put("inactiveOverride", h.isInactiveOverride());
                map.put("inactiveRecipients", mapRecipientsList(h.getInactiveRecipients()));
                break;
            case EventHandlerVO.TYPE_PROCESS:
                map.put("activeProcessCommand", h.getActiveProcessCommand());
                map.put("inactiveProcessCommand", h.getInactiveProcessCommand());
                break;
            case EventHandlerVO.TYPE_SCRIPT:
                map.put("activeScriptCommand", h.getActiveScriptCommand());
                map.put("inactiveScriptCommand", h.getInactiveScriptCommand());
                break;
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private List<RecipientListEntryBean> parseRecipientsList(Object obj) {
        List<RecipientListEntryBean> list = new ArrayList<>();
        if (obj instanceof List) {
            List<Map<String, Object>> mapList = (List<Map<String, Object>>) obj;
            for (Map<String, Object> map : mapList) {
                RecipientListEntryBean bean = new RecipientListEntryBean();
                if (map.get("recipientType") instanceof Number) {
                    bean.setRecipientType(((Number) map.get("recipientType")).intValue());
                }
                if (map.get("referenceId") instanceof Number) {
                    bean.setReferenceId(((Number) map.get("referenceId")).intValue());
                }
                if (map.get("referenceAddress") != null) {
                    bean.setReferenceAddress(map.get("referenceAddress").toString().trim());
                }
                list.add(bean);
            }
        }
        return list;
    }

    private List<Map<String, Object>> mapRecipientsList(List<RecipientListEntryBean> beans) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (beans != null) {
            for (RecipientListEntryBean r : beans) {
                Map<String, Object> map = new HashMap<>();
                map.put("recipientType", r.getRecipientType());
                map.put("referenceId", r.getReferenceId());
                map.put("referenceAddress", r.getReferenceAddress());
                list.add(map);
            }
        }
        return list;
    }
}
