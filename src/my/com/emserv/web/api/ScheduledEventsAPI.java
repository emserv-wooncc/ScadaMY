package my.com.emserv.web.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.ScheduledEventDao;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.event.ScheduledEventVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Scheduled Events (/scheduled_events.shtm).
 */
@RestController
@RequestMapping("/api/scheduled-events")
@Api(value = "Scheduled Events API", tags = "Scheduled Events Management")
public class ScheduledEventsAPI {
    private static final Log logger = LogFactory.getLog(ScheduledEventsAPI.class);

    @ApiOperation(value = "Get all scheduled events in the system", response = ScheduledEventVO.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<ScheduledEventVO>> getScheduledEvents(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ScheduledEventVO> list = new ScheduledEventDao().getScheduledEvents();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Get details of a specific scheduled event by ID (or new default structure for id -1)", response = ScheduledEventVO.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ScheduledEventVO> getScheduledEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ScheduledEventVO se;
        if (id == Common.NEW_ID) {
            DateTime dt = new DateTime();
            se = new ScheduledEventVO();
            se.setXid(new ScheduledEventDao().generateUniqueXid());
            se.setActiveYear(dt.getYear());
            se.setInactiveYear(dt.getYear());
            se.setActiveMonth(dt.getMonthOfYear());
            se.setInactiveMonth(dt.getMonthOfYear());
        } else {
            se = new ScheduledEventDao().getScheduledEvent(id);
            if (se == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.ok(se);
    }

    @ApiOperation(value = "Create a new scheduled event", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createScheduledEvent(@RequestBody ScheduledEventVO se, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return parseAndSaveScheduledEvent(Common.NEW_ID, se, user);
    }

    @ApiOperation(value = "Update an existing scheduled event by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateScheduledEvent(@PathVariable int id, @RequestBody ScheduledEventVO se, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ScheduledEventDao dao = new ScheduledEventDao();
        if (dao.getScheduledEvent(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveScheduledEvent(id, se, user);
    }

    @ApiOperation(value = "Toggle or set disabled state of a scheduled event", response = Object.class)
    @RequestMapping(value = "/{id}/disabled", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setDisabled(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ScheduledEventDao dao = new ScheduledEventDao();
        ScheduledEventVO se = dao.getScheduledEvent(id);
        if (se == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        boolean disabled = body.containsKey("disabled") ? Boolean.parseBoolean(body.get("disabled").toString()) : !se.isDisabled();
        se.setDisabled(disabled);

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveScheduledEvent(se);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            response.put("disabled", disabled);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Delete a scheduled event by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteScheduledEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to delete scheduled event ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ScheduledEventDao dao = new ScheduledEventDao();
        if (dao.getScheduledEvent(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            dao.deleteScheduledEvent(id);
            Common.ctx.getRuntimeManager().stopSimpleEventDetector(ScheduledEventVO.getEventDetectorKey(id));
            logger.info("Scheduled event deleted successfully: ID=" + id + " by user=" + user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    private ResponseEntity<Map<String, Object>> parseAndSaveScheduledEvent(int id, ScheduledEventVO se, User user) {
        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to save scheduled event");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (se == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        se.setId(id);
        ScheduledEventDao scheduledEventDao = new ScheduledEventDao();
        if (StringUtils.isEmpty(se.getXid())) {
            se.setXid(scheduledEventDao.generateUniqueXid());
        }

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        if (!scheduledEventDao.isXidUnique(se.getXid(), id)) {
            validationResponse.addContextualMessage("xid", "validate.xidUsed");
        }

        se.validate(validationResponse);

        if (validationResponse.getHasMessages()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("messages", validationResponse.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveScheduledEvent(se);
            logger.info("Scheduled event saved successfully: ID=" + se.getId() + ", alias=" + se.getAlias() + ", by user=" + user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scheduledEvent", se);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }
}
