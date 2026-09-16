package my.com.emserv.web.ws.service;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DataPointWebSocketService {

    private static DataPointWebSocketService instance;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public DataPointWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        DataPointWebSocketService.instance = this;
    }

    public static DataPointWebSocketService getInstance() {
        return instance;
    }

    public void notifyPointUpdated(int pointId, PointValueTime pvt) {
        if (messagingTemplate == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("pointId", pointId);

        MangoValue value = pvt != null ? pvt.getValue() : null;
        if (value != null) {
            payload.put("value", value.getObjectValue() != null ? value.getObjectValue() : value.getStringValue());
            
            if (com.serotonin.mango.Common.ctx != null && com.serotonin.mango.Common.ctx.getRuntimeManager() != null) {
                com.serotonin.mango.rt.dataImage.DataPointRT rt = com.serotonin.mango.Common.ctx.getRuntimeManager().getDataPoint(pointId);
                if (rt != null) {
                    Object disconnected = rt.getAttribute(com.serotonin.mango.rt.dataSource.DataSourceRT.ATTR_DISCONNECTED_KEY);
                    payload.put("disconnected", disconnected != null ? disconnected : false);
                    
                    Object unreliable = rt.getAttribute(com.serotonin.mango.rt.dataSource.DataSourceRT.ATTR_UNRELIABLE_KEY);
                    payload.put("unreliable", unreliable != null ? unreliable : false);
                    
                    if (rt.getVO() != null) {
                        if (rt.getVO().getTextRenderer() != null) {
                            payload.put("renderedValue", rt.getVO().getTextRenderer().getText(pvt, com.serotonin.mango.view.text.TextRenderer.HINT_FULL));
                            String eu = "";
                            if (rt.getVO().getTextRenderer().getMetaText() != null && !rt.getVO().getTextRenderer().getMetaText().trim().isEmpty()) {
                                eu = rt.getVO().getTextRenderer().getMetaText();
                            } else if (rt.getVO().getEngineeringUnits() != com.serotonin.mango.vo.DataPointVO.ENGINEERING_UNITS_DEFAULT) {
                                eu = new com.serotonin.bacnet4j.type.enumerated.EngineeringUnits(rt.getVO().getEngineeringUnits()).toString();
                            }
                            payload.put("engineeringUnits", eu);
                        } else {
                            payload.put("renderedValue", null);
                            payload.put("engineeringUnits", "");
                        }
                    }
                }
            }
        } else {
            payload.put("value", null);
            payload.put("renderedValue", null);
            payload.put("engineeringUnits", "");
            payload.put("disconnected", false);
            payload.put("unreliable", false);
        }
        
        int highestSeverity = 0;
        try {
            java.util.List<com.serotonin.mango.rt.event.EventInstance> events = new com.serotonin.mango.db.dao.EventDao().getPendingEventsForDataPoint(pointId, 1);
            if (events != null) {
                for (com.serotonin.mango.rt.event.EventInstance e : events) {
                    if (e.isActive() && e.getAlarmLevel() > highestSeverity) {
                        highestSeverity = e.getAlarmLevel();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        payload.put("severity", highestSeverity);
        
        payload.put("timestamp", pvt != null ? pvt.getTime() : System.currentTimeMillis());

        String destination = "/topic/datapoint/" + pointId + "/value";
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            System.err.println("Error broadcasting point update for point " + pointId + ": " + e.getMessage());
        }
    }

    public void notifyPointDisabled(int pointId) {
        if (messagingTemplate == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("pointId", pointId);
        payload.put("disabled", true);
        payload.put("status", "Disabled");
        payload.put("renderedValue", "Disabled");
        payload.put("severity", 0);
        payload.put("timestamp", System.currentTimeMillis());

        String destination = "/topic/datapoint/" + pointId + "/value";
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            System.err.println("Error broadcasting disabled status for point " + pointId + ": " + e.getMessage());
        }
    }
}
