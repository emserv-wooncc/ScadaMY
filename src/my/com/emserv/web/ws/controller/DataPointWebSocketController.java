package my.com.emserv.web.ws.controller;

import my.com.emserv.web.ws.security.WebSocketPrincipal;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DataPointWebSocketController {

    // getInitialValue was removed; initial values are now pushed by WebSocketSubscribeListener


    @MessageMapping("/datapoint/{pointId}/set")
    public void setPointValue(@DestinationVariable int pointId, @Payload Map<String, Object> request, Principal principal) {
        if (principal == null || !(principal instanceof WebSocketPrincipal)) {
            throw new IllegalArgumentException("User principal is required to set data point value.");
        }
        User user = ((WebSocketPrincipal) principal).getUser();
        if (user == null || user.isDisabled()) {
            throw new IllegalArgumentException("Valid user is required to set data point value.");
        }

        Object valObj = request != null ? request.get("value") : null;
        if (valObj == null) {
            return;
        }

        DataPointVO point = new DataPointDao().getDataPoint(pointId);
        if (point == null) {
            return;
        }

        String valueStr = String.valueOf(valObj);
        MangoValue value = MangoValue.stringToValue(valueStr, point.getPointLocator().getDataTypeId());
        if (Common.ctx != null && Common.ctx.getRuntimeManager() != null) {
            com.serotonin.mango.util.BackgroundContext.set(user);
            try {
                Common.ctx.getRuntimeManager().setDataPointValue(pointId, value, user);
            } finally {
                com.serotonin.mango.util.BackgroundContext.remove();
            }
        }
    }
}
