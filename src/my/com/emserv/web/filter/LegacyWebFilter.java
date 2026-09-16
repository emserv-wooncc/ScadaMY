package my.com.emserv.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LegacyWebFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (Boolean.getBoolean("scadamy.disableLegacyWeb")) {
            HttpServletRequest req = (HttpServletRequest) request;
            String path = req.getRequestURI().substring(req.getContextPath().length());

            // Redirect root and standard login pages to /app/
            if (path.equals("/") || path.equals("") || path.equals("/index.jsp") || path.equals("/login.htm") || path.equals("/home.jsp")) {
                HttpServletResponse res = (HttpServletResponse) response;
                res.sendRedirect(req.getContextPath() + "/app/");
                return;
            }

            // Check if the path is a legacy file extension and NOT in the /app/ folder
            if (!path.startsWith("/app/")) {
                if (path.endsWith(".shtm") || path.endsWith(".jsp") || path.endsWith(".htm") || path.endsWith(".html")) {
                    HttpServletResponse res = (HttpServletResponse) response;
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Legacy web access is disabled.");
                    return;
                }
            }
        }

        // Proceed to next filter
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
