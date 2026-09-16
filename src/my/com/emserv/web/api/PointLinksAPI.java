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
import com.serotonin.mango.db.dao.PointLinkDao;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.link.PointLinkVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.PointLinksDwr;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Point Links (/point_links.shtm).
 */
@RestController
@RequestMapping("/api/point-links")
@Api(value = "Point Links API", tags = "Point Links Management")
public class PointLinksAPI {
    private static final Log logger = LogFactory.getLog(PointLinksAPI.class);

    @ApiOperation(value = "Get initialization context data including accessible source points, settable target points, and existing point links", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            PointLinksDwr dwr = new PointLinksDwr();
            Map<String, Object> initData = dwr.init();
            return ResponseEntity.ok(initData);
        } catch (Exception e) {
            logger.error("Error retrieving point links initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get all point links in the system", response = PointLinkVO.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<PointLinkVO>> getPointLinks(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PointLinkVO> list = new PointLinkDao().getPointLinks();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Get details of a specific point link by ID (or new default structure for id -1)", response = PointLinkVO.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<PointLinkVO> getPointLink(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PointLinkVO vo;
        if (id == Common.NEW_ID) {
            vo = new PointLinkVO();
            vo.setXid(new PointLinkDao().generateUniqueXid());
        } else {
            vo = new PointLinkDao().getPointLink(id);
            if (vo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.ok(vo);
    }

    @ApiOperation(value = "Create a new point link", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPointLink(@RequestBody PointLinkVO vo, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return parseAndSavePointLink(Common.NEW_ID, vo, user);
    }

    @ApiOperation(value = "Update an existing point link by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updatePointLink(@PathVariable int id, @RequestBody PointLinkVO vo, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PointLinkDao dao = new PointLinkDao();
        if (dao.getPointLink(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSavePointLink(id, vo, user);
    }

    @ApiOperation(value = "Delete a point link by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deletePointLink(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to delete point link ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PointLinkDao dao = new PointLinkDao();
        if (dao.getPointLink(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Common.ctx.getRuntimeManager().deletePointLink(id);
        logger.info("Point link deleted successfully: ID=" + id + " by user=" + user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Validate a transformation script against source and target points", response = Object.class)
    @RequestMapping(value = "/validate-script", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> validateScript(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String script = body.get("script") != null ? body.get("script").toString() : "";
        int sourcePointId = body.get("sourcePointId") instanceof Number ? ((Number) body.get("sourcePointId")).intValue() : 0;
        int targetPointId = body.get("targetPointId") instanceof Number ? ((Number) body.get("targetPointId")).intValue() : 0;

        PointLinksDwr dwr = new PointLinksDwr();
        DwrResponseI18n validationResponse = dwr.validateScript(script, sourcePointId, targetPointId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", !validationResponse.getHasMessages());
        if (validationResponse.getHasMessages()) {
            response.put("messages", validationResponse.getMessages());
        }
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> parseAndSavePointLink(int id, PointLinkVO vo, User user) {
        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to save point link");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (vo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        vo.setId(id);
        PointLinkDao pointLinkDao = new PointLinkDao();
        if (StringUtils.isEmpty(vo.getXid())) {
            vo.setXid(pointLinkDao.generateUniqueXid());
        }

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        if (!pointLinkDao.isXidUnique(vo.getXid(), id)) {
            validationResponse.addContextualMessage("xid", "validate.xidUsed");
        }

        vo.validate(validationResponse);

        if (validationResponse.getHasMessages()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("messages", validationResponse.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        Common.ctx.getRuntimeManager().savePointLink(vo);
        logger.info("Point link saved successfully: ID=" + vo.getId() + ", xid=" + vo.getXid() + ", by user=" + user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("pointLink", vo);
        return ResponseEntity.ok(response);
    }
}
