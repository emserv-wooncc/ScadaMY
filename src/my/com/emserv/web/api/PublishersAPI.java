package my.com.emserv.web.api;

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

import com.serotonin.db.KeyValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.mango.web.dwr.PublisherEditDwr;
import com.serotonin.mango.web.dwr.PublisherListDwr;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Publishers (/publishers.shtm).
 */
@RestController
@RequestMapping("/api/publishers")
@Api(value = "Publishers API", tags = "Publishers Management")
public class PublishersAPI {
    private static final Log logger = LogFactory.getLog(PublishersAPI.class);

    @ApiOperation(value = "Get initialization context data including publisher types and existing publishers", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
            PublisherListDwr dwr = new PublisherListDwr();
            DwrResponseI18n dwrResp = dwr.init();
            return ResponseEntity.ok(dwrResp.getData());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error retrieving publishers initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get all publishers in the system", response = PublisherVO.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<PublisherVO<? extends PublishedPointVO>>> getPublishers(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<PublisherVO<? extends PublishedPointVO>> list = new PublisherDao().getPublishers();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Get details of a specific publisher by ID", response = PublisherVO.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<PublisherVO<? extends PublishedPointVO>> getPublisher(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PublisherVO<? extends PublishedPointVO> pub = new PublisherDao().getPublisher(id);
        if (pub == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(pub);
    }

    @ApiOperation(value = "Create a new publisher", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPublisher(@RequestBody PublisherVO<? extends PublishedPointVO> pub, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return parseAndSavePublisher(Common.NEW_ID, pub, user);
    }

    @ApiOperation(value = "Update an existing publisher by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updatePublisher(@PathVariable int id, @RequestBody PublisherVO<? extends PublishedPointVO> pub, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PublisherDao dao = new PublisherDao();
        if (dao.getPublisher(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSavePublisher(id, pub, user);
    }

    @ApiOperation(value = "Delete a publisher by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deletePublisher(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to delete publisher ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PublisherDao dao = new PublisherDao();
        if (dao.getPublisher(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Common.ctx.getRuntimeManager().deletePublisher(id);
        logger.info("Publisher deleted successfully: ID=" + id + " by user=" + user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Toggle the enabled status of a publisher", response = Object.class)
    @RequestMapping(value = "/{id}/toggle", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> togglePublisher(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PublisherListDwr dwr = new PublisherListDwr();
        DwrResponseI18n dwrResp = dwr.togglePublisher(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", !dwrResp.getHasMessages());
        if (dwrResp.getHasMessages()) {
            response.put("messages", dwrResp.getMessages());
        } else {
            response.put("data", dwrResp.getData());
        }
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Initiate HTTP sender testing utility", response = Object.class)
    @RequestMapping(value = "/test-http-sender", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> testHttpSender(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String url = body.get("url") != null ? body.get("url").toString() : "";
        boolean usePost = body.get("usePost") instanceof Boolean ? (Boolean) body.get("usePost") : (body.get("usePost") != null && Boolean.parseBoolean(body.get("usePost").toString()));

        List<KeyValuePair> staticHeaders = parseKeyValuePairs(body.get("staticHeaders"));
        List<KeyValuePair> staticParameters = parseKeyValuePairs(body.get("staticParameters"));

        PublisherEditDwr dwr = new PublisherEditDwr();
        dwr.httpSenderTest(url, usePost, staticHeaders, staticParameters);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "HTTP sender test initiated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get the latest result of the ongoing HTTP sender test", response = Object.class)
    @RequestMapping(value = "/test-http-sender/result", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getHttpSenderTestResult(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PublisherEditDwr dwr = new PublisherEditDwr();
        String result = dwr.httpSenderTestUpdate();

        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private List<KeyValuePair> parseKeyValuePairs(Object obj) {
        List<KeyValuePair> list = new ArrayList<>();
        if (obj instanceof List) {
            for (Object item : (List<?>) obj) {
                if (item instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) item;
                    String key = map.get("key") != null ? map.get("key").toString() : "";
                    String value = map.get("value") != null ? map.get("value").toString() : "";
                    list.add(new KeyValuePair(key, value));
                }
            }
        }
        return list;
    }

    private ResponseEntity<Map<String, Object>> parseAndSavePublisher(int id, PublisherVO<? extends PublishedPointVO> pub, User user) {
        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to save publisher");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (pub == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        pub.setId(id);
        PublisherDao publisherDao = new PublisherDao();
        if (StringUtils.isEmpty(pub.getXid())) {
            pub.setXid(publisherDao.generateUniqueXid());
        }

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        if (!publisherDao.isXidUnique(pub.getXid(), id)) {
            validationResponse.addContextualMessage("xid", "validate.xidUsed");
        }

        pub.validate(validationResponse);

        if (validationResponse.getHasMessages()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("messages", validationResponse.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        Common.ctx.getRuntimeManager().savePublisher(pub);
        logger.info("Publisher saved successfully: ID=" + pub.getId() + ", name=" + pub.getName() + ", by user=" + user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("publisher", pub);
        return ResponseEntity.ok(response);
    }
}
