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
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.web.dwr.UsersDwr;
import com.serotonin.web.dwr.DwrResponseI18n;

import br.org.scadabr.db.dao.UsersProfileDao;
import br.org.scadabr.vo.usersProfiles.UsersProfileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Users (/users.shtm).
 */
@RestController
@RequestMapping("/api/users")
@Api(value = "Users API", tags = "Users Management")
public class UsersAPI {
    private static final Log logger = LogFactory.getLog(UsersAPI.class);

    @ApiOperation(value = "Get initialization context data including users, user profiles, and data source permissions", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UsersDwr dwr = new UsersDwr();
            Map<String, Object> initData;
            BackgroundContext.set(user);
            try {
                initData = dwr.getInitData();
            } finally {
                BackgroundContext.remove();
            }
            return ResponseEntity.ok(initData);
        } catch (Exception e) {
            logger.error("Error retrieving users initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get current logged-in user details", response = User.class)
    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(user);
    }

    @ApiOperation(value = "Update home page URL for current user", response = Object.class)
    @RequestMapping(value = "/home-url", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateHomeUrl(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String homeUrl = body.get("homeUrl") != null ? body.get("homeUrl").toString() : "";
        
        BackgroundContext.set(user);
        try {
            Common.ctx.getUserCache().getUserDao().saveHomeUrl(user.getId(), homeUrl);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("homeUrl", homeUrl);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get all users in the system (Admin only)", response = User.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUsers(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<User> users = Common.ctx.getUserCache().getUserDao().getUsers();
        UsersProfileDao usersProfileDao = new UsersProfileDao();
        if (users != null) {
            for (User u : users) {
                UsersProfileVO profileVo = usersProfileDao.getUserProfileByUserId(u.getId());
                if (profileVo != null) {
                    u.setUserProfile(profileVo);
                }
            }
        }
        return ResponseEntity.ok(users);
    }

    @ApiOperation(value = "Get details of a specific user by ID (or new structure for id -1)", response = User.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user) && user.getId() != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User targetUser;
        if (id == Common.NEW_ID) {
            if (!Permissions.hasAdmin(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            targetUser = new User();
            targetUser.setDataSourcePermissions(new ArrayList<Integer>(0));
            targetUser.setDataPointPermissions(new ArrayList<DataPointAccess>(0));
        } else {
            targetUser = Common.ctx.getUserCache().getUser(id);
            if (targetUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            UsersProfileVO profileVo = new UsersProfileDao().getUserProfileByUserId(targetUser.getId());
            if (profileVo != null) {
                targetUser.setUserProfile(profileVo);
            }
        }

        return ResponseEntity.ok(targetUser);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Create a new user (Admin only)", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return parseAndSaveUser(Common.NEW_ID, body, user);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Update an existing user by ID (Admin or self)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user) && user.getId() != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (Common.ctx.getUserCache().getUser(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveUser(id, body, user);
    }

    @ApiOperation(value = "Delete a user by ID (Admin only, cannot delete self)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (user.getId() == id) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Cannot delete current logged-in user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        if (Common.ctx.getUserCache().getUser(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UsersDwr dwr = new UsersDwr();
        DwrResponseI18n dwrResp;
        BackgroundContext.set(user);
        try {
            dwrResp = dwr.deleteUser(id);
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

    @ApiOperation(value = "Send a test email to the specified address (Admin only)", response = Object.class)
    @RequestMapping(value = "/test-email", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String email = body.get("email") != null ? body.get("email").toString() : "";
        String username = body.get("username") != null ? body.get("username").toString() : user.getUsername();

        UsersDwr dwr = new UsersDwr();
        Map<String, Object> result;
        BackgroundContext.set(user);
        try {
            result = dwr.sendTestEmail(email, username);
        } finally {
            BackgroundContext.remove();
        }
        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> parseAndSaveUser(int id, Map<String, Object> body, User currentUser) {
        String username = body.get("username") != null ? body.get("username").toString() : "";
        String password = body.get("password") != null ? body.get("password").toString() : "";
        String email = body.get("email") != null ? body.get("email").toString() : "";
        String phone = body.get("phone") != null ? body.get("phone").toString() : "";
        boolean admin = body.get("admin") instanceof Boolean ? (Boolean) body.get("admin") : (body.get("admin") != null && Boolean.parseBoolean(body.get("admin").toString()));
        boolean disabled = body.get("disabled") instanceof Boolean ? (Boolean) body.get("disabled") : (body.get("disabled") != null && Boolean.parseBoolean(body.get("disabled").toString()));
        int receiveAlarmEmails = body.get("receiveAlarmEmails") instanceof Number ? ((Number) body.get("receiveAlarmEmails")).intValue() : 0;
        boolean receiveOwnAuditEvents = body.get("receiveOwnAuditEvents") instanceof Boolean ? (Boolean) body.get("receiveOwnAuditEvents") : (body.get("receiveOwnAuditEvents") != null && Boolean.parseBoolean(body.get("receiveOwnAuditEvents").toString()));
        int usersProfileId = body.get("usersProfileId") instanceof Number ? ((Number) body.get("usersProfileId")).intValue() : -1;

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

        UsersDwr dwr = new UsersDwr();
        DwrResponseI18n dwrResp;
        BackgroundContext.set(currentUser);
        try {
            if (Permissions.hasAdmin(currentUser)) {
                dwrResp = dwr.saveUserAdmin(id, username, password, email, phone, admin, disabled, receiveAlarmEmails, receiveOwnAuditEvents, dataSourcePermissions, dataPointPermissions, usersProfileId);
            } else {
                dwrResp = dwr.saveUser(id, password, email, phone, receiveAlarmEmails, receiveOwnAuditEvents, usersProfileId);
            }
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
