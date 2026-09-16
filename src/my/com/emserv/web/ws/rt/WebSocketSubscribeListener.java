package my.com.emserv.web.ws.rt;

import my.com.emserv.web.ws.service.DataPointWebSocketService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.DataPointRT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketSubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private final Pattern pattern = Pattern.compile("^/topic/datapoint/(\\d+)/value$");

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        
        if (destination != null) {
            Matcher matcher = pattern.matcher(destination);
            if (matcher.matches()) {
                int pointId = Integer.parseInt(matcher.group(1));
                
                if (Common.ctx != null && Common.ctx.getRuntimeManager() != null) {
                    DataPointRT rt = Common.ctx.getRuntimeManager().getDataPoint(pointId);
                    DataPointWebSocketService service = DataPointWebSocketService.getInstance();
                    
                    if (service != null) {
                        if (rt == null) {
                            // Point is disabled, broadcast disabled status so the new subscriber gets it immediately
                            service.notifyPointDisabled(pointId);
                        } else {
                            // Point is active, broadcast latest value so the new subscriber gets it immediately
                            service.notifyPointUpdated(pointId, rt.getPointValue());
                        }
                    }
                }
            }
        }
    }
}
