package my.com.emserv.web.mvc.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;

public class ApiSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Allow public and authentication endpoints without checking login
        String uri = request.getRequestURI();
        if (uri.contains("/api/public/") || uri.contains("/api/auth/")) {
            return true;
        }

        User user = Common.getUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication required to access API\"}");
            return false;
        }

        // Store user in request attribute for easy controller access
        request.setAttribute("currentUser", user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // no op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) throws Exception {
        // no op
    }
}
