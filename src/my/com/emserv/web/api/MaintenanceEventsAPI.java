package my.com.emserv.web.api;

import java.util.ArrayList;
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
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.MaintenanceEventDao;
import com.serotonin.mango.rt.event.maintenance.MaintenanceEventRT;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.MaintenanceEventVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Maintenance Events (/maintenance_events.shtm).
 */
@RestController
@RequestMapping("/api/maintenance-events")
@Api(value = "Maintenance Events API", tags = "Maintenance Events Management")
public class MaintenanceEventsAPI {
    private static final Log logger = LogFactory.getLog(MaintenanceEventsAPI.class);

    @ApiOperation(value = "Get all maintenance events and available data sources (Admin only)", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getMaintenanceEvents(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error checking admin permission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            // Data sources map for quick lookup
            Map<Integer, String> dsNameMap = new HashMap<>();
            List<Map<String, Object>> dataSources = new ArrayList<>();
            for (DataSourceVO<?> ds : new DataSourceDao().getDataSources()) {
                dsNameMap.put(ds.getId(), ds.getName());
                Map<String, Object> dsMap = new HashMap<>();
                dsMap.put("id", ds.getId());
                dsMap.put("name", ds.getName());
                dataSources.add(dsMap);
            }

            List<MaintenanceEventVO> events = new MaintenanceEventDao().getMaintenanceEvents();
            List<Map<String, Object>> enrichedEvents = new ArrayList<>();
            for (MaintenanceEventVO me : events) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", me.getId());
                item.put("xid", me.getXid());
                item.put("dataSourceId", me.getDataSourceId());
                item.put("dataSourceName", dsNameMap.getOrDefault(me.getDataSourceId(), me.getDataSourceName()));
                item.put("alias", me.getAlias());
                item.put("alarmLevel", me.getAlarmLevel());
                item.put("scheduleType", me.getScheduleType());
                item.put("disabled", me.isDisabled());
                item.put("activeYear", me.getActiveYear());
                item.put("activeMonth", me.getActiveMonth());
                item.put("activeDay", me.getActiveDay());
                item.put("activeHour", me.getActiveHour());
                item.put("activeMinute", me.getActiveMinute());
                item.put("activeSecond", me.getActiveSecond());
                item.put("activeCron", me.getActiveCron());
                item.put("inactiveYear", me.getInactiveYear());
                item.put("inactiveMonth", me.getInactiveMonth());
                item.put("inactiveDay", me.getInactiveDay());
                item.put("inactiveHour", me.getInactiveHour());
                item.put("inactiveMinute", me.getInactiveMinute());
                item.put("inactiveSecond", me.getInactiveSecond());
                item.put("inactiveCron", me.getInactiveCron());

                // Check live active status
                MaintenanceEventRT rt = Common.ctx.getRuntimeManager().getRunningMaintenanceEvent(me.getId());
                item.put("active", rt != null && rt.isEventActive());

                enrichedEvents.add(item);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("events", enrichedEvents);
            result.put("dataSources", dataSources);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error retrieving maintenance events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get details of a specific maintenance event by ID along with its live active status (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getMaintenanceEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MaintenanceEventVO me;
        boolean activated = false;
        if (id == Common.NEW_ID) {
            DateTime dt = new DateTime();
            me = new MaintenanceEventVO();
            me.setXid(new MaintenanceEventDao().generateUniqueXid());
            me.setActiveYear(dt.getYear());
            me.setInactiveYear(dt.getYear());
            me.setActiveMonth(dt.getMonthOfYear());
            me.setInactiveMonth(dt.getMonthOfYear());
        } else {
            me = new MaintenanceEventDao().getMaintenanceEvent(id);
            if (me == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            MaintenanceEventRT rt = Common.ctx.getRuntimeManager().getRunningMaintenanceEvent(me.getId());
            if (rt != null) {
                activated = rt.isEventActive();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("me", me);
        response.put("activated", activated);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Create a new maintenance event (Admin only)", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createMaintenanceEvent(@RequestBody MaintenanceEventVO me, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return parseAndSaveMaintenanceEvent(Common.NEW_ID, me, user);
    }

    @ApiOperation(value = "Update an existing maintenance event by ID (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateMaintenanceEvent(@PathVariable int id, @RequestBody MaintenanceEventVO me, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (new MaintenanceEventDao().getMaintenanceEvent(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveMaintenanceEvent(id, me, user);
    }

    @ApiOperation(value = "Toggle disabled status of a maintenance event (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}/disabled", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setDisabled(@PathVariable int id, @RequestBody Map<String, Boolean> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MaintenanceEventVO me = new MaintenanceEventDao().getMaintenanceEvent(id);
        if (me == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Boolean disabled = body.get("disabled");
        if (disabled == null) {
            return ResponseEntity.badRequest().build();
        }

        me.setDisabled(disabled);
        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveMaintenanceEvent(me);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("disabled", me.isDisabled());
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Delete a maintenance event by ID (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteMaintenanceEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (new MaintenanceEventDao().getMaintenanceEvent(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().deleteMaintenanceEvent(id);
            logger.info("Maintenance event deleted successfully: ID=" + id + " by user=" + user.getUsername());
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Toggle the active status of a running maintenance event (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}/toggle", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> toggleMaintenanceEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MaintenanceEventRT rt = Common.ctx.getRuntimeManager().getRunningMaintenanceEvent(id);
        if (rt == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Maintenance event is currently disabled or not running.");
            return ResponseEntity.badRequest().body(response);
        }

        BackgroundContext.set(user);
        boolean activated;
        try {
            activated = rt.toggle();
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        Map<String, Object> data = new HashMap<>();
        data.put("activated", activated);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> parseAndSaveMaintenanceEvent(int id, MaintenanceEventVO me, User user) {
        if (me == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        me.setId(id);
        MaintenanceEventDao maintenanceEventDao = new MaintenanceEventDao();
        if (StringUtils.isEmpty(me.getXid())) {
            me.setXid(maintenanceEventDao.generateUniqueXid());
        }

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        if (!maintenanceEventDao.isXidUnique(me.getXid(), id)) {
            validationResponse.addContextualMessage("xid", "validate.xidUsed");
        }

        me.validate(validationResponse);

        if (validationResponse.getHasMessages()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("messages", validationResponse.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveMaintenanceEvent(me);
            logger.info("Maintenance event saved successfully: ID=" + me.getId() + ", alias=" + me.getAlias() + ", by user=" + user.getUsername());
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("maintenanceEvent", me);
        return ResponseEntity.ok(response);
    }
}
