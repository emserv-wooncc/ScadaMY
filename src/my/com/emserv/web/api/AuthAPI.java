package my.com.emserv.web.api;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.lang.reflect.Method;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.mvc.form.LoginForm;
import com.serotonin.util.StringUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/auth")
@Api(value = "Authentication API", tags = "Authentication Operations")
public class AuthAPI {
    private static final Log logger = LogFactory.getLog(AuthAPI.class);

    @Autowired
    private ApplicationContext applicationContext;

    @ApiOperation(value = "Check current login status")
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> status(HttpServletRequest request) {
        User user = Common.getUser(request);
        Map<String, Object> response = new HashMap<>();
        if (applicationContext != null) {
            try {
                Class<?> evClass = applicationContext.containsBean("eventsAPI") ?
                        applicationContext.getType("eventsAPI") : applicationContext.getType("eventsController");
                Map<String, Object> evDiag = new HashMap<>();
                evDiag.put("className", evClass != null ? evClass.getName() : "null");
                if (evClass != null) {
                    evDiag.put("hasController", AnnotatedElementUtils.hasAnnotation(evClass, Controller.class));
                    evDiag.put("hasRequestMapping", AnnotatedElementUtils.hasAnnotation(evClass, RequestMapping.class));
                    List<String> methodMappings = new java.util.ArrayList<>();
                    for (Method m : evClass.getDeclaredMethods()) {
                        RequestMapping rm = AnnotatedElementUtils.findMergedAnnotation(m, RequestMapping.class);
                        if (rm != null) {
                            methodMappings.add(m.getName() + " -> " + java.util.Arrays.toString(rm.value()));
                        }
                    }
                    evDiag.put("methods", methodMappings);
                }
                response.put("evDiag", evDiag);

                Map<String, RequestMappingHandlerMapping> rmhms = applicationContext
                        .getBeansOfType(RequestMappingHandlerMapping.class);
                Map<String, List<String>> handlersByBean = new HashMap<>();
                for (Map.Entry<String, RequestMappingHandlerMapping> entry : rmhms.entrySet()) {
                    List<String> allHandlers = new java.util.ArrayList<>();
                    entry.getValue().getHandlerMethods().forEach((info, method) -> {
                        if (method.getBeanType().getName().contains("Events")
                                || method.getBeanType().getName().contains("Auth")) {
                            allHandlers.add(info.toString() + " -> " + method.toString());
                        }
                    });
                    handlersByBean.put(entry.getKey(), allHandlers);
                }
                evDiag.put("handlersByBean", handlersByBean);
            } catch (Exception ex) {
                response.put("evDiagError", ex.toString());
            }
        }

        if (user != null) {
            response.put("loggedIn", true);
            response.put("username", user.getUsername());
            response.put("userId", user.getId());
            response.put("isAdmin", user.isAdmin());
            response.put("hasDataSourcePermission", Permissions.hasDataSourcePermission(user));

            String homeUrl = user.getHomeUrl();
            if (StringUtils.isEmpty(homeUrl)) {
                homeUrl = "watch_list.shtm";
            }
            response.put("homeUrl", homeUrl);
        } else {
            response.put("loggedIn", false);
        }
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Login with credentials")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginForm loginForm, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        logger.trace(" REST Login called! ");

        if (loginForm == null || StringUtils.isEmpty(loginForm.getUsername())
                || StringUtils.isEmpty(loginForm.getPassword())) {
            response.put("success", false);
            response.put("error", "Username and password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate credentials against UserDao using hashed password
        User user = new UserDao().getUser(loginForm.getUsername());
        if (user != null) {
            logger.trace(" User: " + user.getUsername() + ", with hash: " + user.getPassword());
        } else {
            logger.trace(" User not found: " + loginForm.getUsername());
        }

        String passwordHash = Common.encrypt(loginForm.getPassword());
        logger.trace(" Against hash: " + passwordHash);

        if (user == null || !passwordHash.equals(user.getPassword()) || user.isDisabled()) {
            response.put("success", false);
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Update permissions & record login
        if (Common.ctx != null && Common.ctx.getUserCache() != null) {
            user = Common.ctx.getUserCache().getUpdatedPermissions(user);
            if (Common.ctx.getUserCache().getUserDao() != null) {
                Common.ctx.getUserCache().getUserDao().recordLogin(user.getId());
            }
        } else {
            new UserDao().recordLogin(user.getId());
        }

        // Establish session
        Common.setUser(request, user);

        // Return user info & redirect URL for Svelte
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("isAdmin", user.isAdmin());
        response.put("hasDataSourcePermission", Permissions.hasDataSourcePermission(user));

        String homeUrl = user.getHomeUrl();
        if (StringUtils.isEmpty(homeUrl)) {
            homeUrl = "watch_list.shtm";
        }
        response.put("homeUrl", homeUrl);

        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Logout current user")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
