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

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.DataPointAccess;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.web.dwr.DwrResponseI18n;

import br.org.scadabr.db.dao.UsersProfileDao;
import br.org.scadabr.vo.permission.ViewAccess;
import br.org.scadabr.vo.permission.WatchListAccess;
import br.org.scadabr.vo.usersProfiles.UsersProfileVO;
import br.org.scadabr.web.dwr.UsersProfilesDwr;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing User Profiles (/usersProfiles.shtm).
 */
@RestController
@RequestMapping("/api/user-profiles")
@Api(value = "User Profiles API", tags = "User Profiles Management")
public class UserProfilesAPI {
    private static final Log logger = LogFactory.getLog(UserProfilesAPI.class);

    @ApiOperation(value = "Get initialization context data including user profiles, data sources, watchlists, and views", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            UsersProfilesDwr dwr = new UsersProfilesDwr();
            Map<String, Object> initData = dwr.getInitData();
            return ResponseEntity.ok(initData);
        } catch (Exception e) {
            logger.error("Error retrieving user profiles initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get all user profiles in the system (Admin only)", response = UsersProfileVO.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<UsersProfileVO>> getUserProfiles(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UsersProfileVO> profiles = new UsersProfileDao().getUsersProfiles();
        return ResponseEntity.ok(profiles);
    }

    @ApiOperation(value = "Get details of a specific user profile by ID (or new structure for id -1)", response = UsersProfileVO.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<UsersProfileVO> getUserProfile(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UsersProfileVO vo;
        if (id == Common.NEW_ID) {
            vo = new UsersProfileVO();
        } else {
            vo = new UsersProfileDao().getUserProfileById(id);
            if (vo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.ok(vo);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Create a new user profile (Admin only)", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createUserProfile(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return parseAndSaveUserProfile(Common.NEW_ID, body, user);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Update an existing user profile by ID (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateUserProfile(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (new UsersProfileDao().getUserProfileById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveUserProfile(id, body, user);
    }

    @ApiOperation(value = "Delete a user profile by ID (Admin only)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteUserProfile(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (new UsersProfileDao().getUserProfileById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UsersProfilesDwr dwr = new UsersProfilesDwr();
        DwrResponseI18n dwrResp;
        BackgroundContext.set(user);
        try {
            dwrResp = dwr.deleteUsersProfile(id);
        } finally {
            BackgroundContext.remove();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", !dwrResp.getHasMessages());
        if (dwrResp.getHasMessages()) {
            response.put("messages", dwrResp.getMessages());
        } else {
            response.put("id", id);
        }
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> parseAndSaveUserProfile(int id, Map<String, Object> body, User currentUser) {
        String name = body.get("name") != null ? body.get("name").toString() : "";

        List<Integer> dataSourcePermissions = new ArrayList<>();
        if (body.get("dataSourcePermissions") instanceof List) {
            for (Object obj : (List<?>) body.get("dataSourcePermissions")) {
                if (obj instanceof Number) {
                    dataSourcePermissions.add(((Number) obj).intValue());
                }
            }
        }

        List<DataPointAccess> dataPointPermissions = new ArrayList<>();
        if (body.get("dataPointPermissions") instanceof List) {
            for (Object obj : (List<?>) body.get("dataPointPermissions")) {
                if (obj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    int dataPointId = map.get("dataPointId") instanceof Number ? ((Number) map.get("dataPointId")).intValue() : 0;
                    int permission = map.get("permission") instanceof Number ? ((Number) map.get("permission")).intValue() : 0;
                    DataPointAccess dpa = new DataPointAccess();
                    dpa.setDataPointId(dataPointId);
                    dpa.setPermission(permission);
                    dataPointPermissions.add(dpa);
                }
            }
        }

        List<WatchListAccess> watchlistPermissions = new ArrayList<>();
        if (body.get("watchlistPermissions") instanceof List) {
            for (Object obj : (List<?>) body.get("watchlistPermissions")) {
                if (obj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    int watchlistId = map.get("watchlistId") instanceof Number ? ((Number) map.get("watchlistId")).intValue() : (map.get("id") instanceof Number ? ((Number) map.get("id")).intValue() : 0);
                    int permission = map.get("permission") instanceof Number ? ((Number) map.get("permission")).intValue() : 0;
                    WatchListAccess wla = new WatchListAccess(watchlistId, permission);
                    watchlistPermissions.add(wla);
                }
            }
        }

        List<ViewAccess> viewsPermissions = new ArrayList<>();
        if (body.get("viewsPermissions") instanceof List) {
            for (Object obj : (List<?>) body.get("viewsPermissions")) {
                if (obj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    int viewId = map.get("viewId") instanceof Number ? ((Number) map.get("viewId")).intValue() : (map.get("id") instanceof Number ? ((Number) map.get("id")).intValue() : 0);
                    int permission = map.get("permission") instanceof Number ? ((Number) map.get("permission")).intValue() : 0;
                    ViewAccess va = new ViewAccess(viewId, permission);
                    viewsPermissions.add(va);
                }
            }
        }

        UsersProfilesDwr dwr = new UsersProfilesDwr();
        DwrResponseI18n dwrResp;
        BackgroundContext.set(currentUser);
        try {
            dwrResp = dwr.saveUserAdmin(id, name, dataSourcePermissions, dataPointPermissions, watchlistPermissions, viewsPermissions);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", !dwrResp.getHasMessages());
        if (dwrResp.getHasMessages()) {
            response.put("messages", dwrResp.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            response.put("data", dwrResp.getData());
            return ResponseEntity.ok(response);
        }
    }
}
