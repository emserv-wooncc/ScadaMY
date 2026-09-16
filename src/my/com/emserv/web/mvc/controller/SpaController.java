package my.com.emserv.web.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller to handle SvelteKit Single Page Application (SPA) client-side routing.
 * When a request arrives under /app/ without a file extension (e.g. /app/dashboard or /app/datapoints/123),
 * this controller forwards the request to /app/index.html so SvelteKit's client router can handle the view.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
        "/app",
        "/app/",
        "/app/{path:[^\\.]*}",
        "/app/**/{path:[^\\.]*}"
    }, method = RequestMethod.GET)
    public String forwardSpa() {
        return "forward:/app/index.html";
    }
}
