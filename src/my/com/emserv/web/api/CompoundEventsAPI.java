package my.com.emserv.web.api;

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
import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.event.CompoundEventDetectorVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.CompoundEventsDwr;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Compound Event Detectors (/compound_events.shtm).
 */
@RestController
@RequestMapping("/api/compound-events")
@Api(value = "Compound Events API", tags = "Compound Events Management")
public class CompoundEventsAPI {
    private static final Log logger = LogFactory.getLog(CompoundEventsAPI.class);

    @ApiOperation(value = "Get initialization context data including existing detectors, accessible data point event detectors, and scheduled events", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BackgroundContext.set(user);
        try {
            CompoundEventsDwr dwr = new CompoundEventsDwr();
            Map<String, Object> initData = dwr.getInitData();
            return ResponseEntity.ok(initData);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks data source permission when fetching compound events init data");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error retrieving compound events initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Get all compound event detectors in the system", response = CompoundEventDetectorVO.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<CompoundEventDetectorVO>> getCompoundEvents(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CompoundEventDetectorVO> list = new CompoundEventDetectorDao().getCompoundEventDetectors();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Get details of a specific compound event detector by ID (or new default structure for id -1)", response = CompoundEventDetectorVO.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<CompoundEventDetectorVO> getCompoundEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CompoundEventDetectorVO vo;
        if (id == Common.NEW_ID) {
            vo = new CompoundEventDetectorVO();
            vo.setXid(new CompoundEventDetectorDao().generateUniqueXid());
        } else {
            vo = new CompoundEventDetectorDao().getCompoundEventDetector(id);
            if (vo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.ok(vo);
    }

    @ApiOperation(value = "Create a new compound event detector", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createCompoundEvent(@RequestBody CompoundEventDetectorVO ced, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return parseAndSaveCompoundEvent(Common.NEW_ID, ced, user);
    }

    @ApiOperation(value = "Update an existing compound event detector by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateCompoundEvent(@PathVariable int id, @RequestBody CompoundEventDetectorVO ced, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CompoundEventDetectorDao dao = new CompoundEventDetectorDao();
        if (dao.getCompoundEventDetector(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveCompoundEvent(id, ced, user);
    }

    @ApiOperation(value = "Toggle or set disabled state of a compound event detector", response = Object.class)
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

        CompoundEventDetectorDao dao = new CompoundEventDetectorDao();
        CompoundEventDetectorVO ced = dao.getCompoundEventDetector(id);
        if (ced == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        boolean disabled = body.containsKey("disabled") ? Boolean.parseBoolean(body.get("disabled").toString()) : !ced.isDisabled();
        ced.setDisabled(disabled);

        BackgroundContext.set(user);
        try {
            boolean success = Common.ctx.getRuntimeManager().saveCompoundEventDetector(ced);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            response.put("disabled", ced.isDisabled());
            if (!success && !disabled) {
                response.put("warning", new LocalizableMessage("compoundDetectors.validation.initError").getLocalizedMessage(Common.getBundle()));
            }
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Delete a compound event detector by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteCompoundEvent(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to delete compound event ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CompoundEventDetectorDao dao = new CompoundEventDetectorDao();
        if (dao.getCompoundEventDetector(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            dao.deleteCompoundEventDetector(id);
            Common.ctx.getRuntimeManager().stopCompoundEventDetector(id);
            logger.info("Compound event detector deleted successfully: ID=" + id + " by user=" + user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Validate a boolean logic condition string", response = Object.class)
    @RequestMapping(value = "/validate-condition", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> validateCondition(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String condition = body.get("condition") != null ? body.get("condition").toString() : "";
        DwrResponseI18n validationResponse = new DwrResponseI18n();

        BackgroundContext.set(user);
        try {
            CompoundEventDetectorVO.validate(condition, validationResponse);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", !validationResponse.getHasMessages());
        if (validationResponse.getHasMessages()) {
            response.put("messages", validationResponse.getMessages());
            if (validationResponse.getData().get("range") != null) {
                response.put("range", true);
                response.put("from", validationResponse.getData().get("from"));
                response.put("to", validationResponse.getData().get("to"));
            }
        }
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> parseAndSaveCompoundEvent(int id, CompoundEventDetectorVO ced, User user) {
        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to save compound event");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (ced == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ced.setId(id);
        CompoundEventDetectorDao compoundEventDetectorDao = new CompoundEventDetectorDao();
        if (StringUtils.isEmpty(ced.getXid())) {
            ced.setXid(compoundEventDetectorDao.generateUniqueXid());
        }

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        if (!compoundEventDetectorDao.isXidUnique(ced.getXid(), id)) {
            validationResponse.addContextualMessage("xid", "validate.xidUsed");
        }

        BackgroundContext.set(user);
        try {
            ced.validate(validationResponse);

            if (validationResponse.getHasMessages()) {
                Map<String, Object> errorResp = new HashMap<>();
                errorResp.put("success", false);
                errorResp.put("messages", validationResponse.getMessages());
                if (validationResponse.getData().get("range") != null) {
                    errorResp.put("range", true);
                    errorResp.put("from", validationResponse.getData().get("from"));
                    errorResp.put("to", validationResponse.getData().get("to"));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
            }

            boolean success = Common.ctx.getRuntimeManager().saveCompoundEventDetector(ced);
            Map<String, Object> response = new HashMap<>();
            if (!success) {
                response.put("warning", new LocalizableMessage("compoundDetectors.validation.initError").getLocalizedMessage(Common.getBundle()));
            }
            response.put("success", true);
            response.put("compoundEvent", ced);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }
}

