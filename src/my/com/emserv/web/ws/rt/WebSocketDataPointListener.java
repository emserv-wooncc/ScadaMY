package my.com.emserv.web.ws.rt;

import com.serotonin.mango.rt.dataImage.DataPointListener;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import my.com.emserv.web.ws.service.DataPointWebSocketService;

public class WebSocketDataPointListener implements DataPointListener {
    private final int pointId;

    public WebSocketDataPointListener(int pointId) {
        this.pointId = pointId;
    }

    @Override
    public void pointUpdated(PointValueTime newValue) {
        DataPointWebSocketService service = DataPointWebSocketService.getInstance();
        if (service != null) {
            service.notifyPointUpdated(pointId, newValue);
        }
    }

    @Override
    public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
        // Handled by pointUpdated
    }

    @Override
    public void pointSet(PointValueTime oldValue, PointValueTime newValue) {
        // Handled by pointUpdated
    }

    @Override
    public void pointBackdated(PointValueTime valueTime) {
        DataPointWebSocketService service = DataPointWebSocketService.getInstance();
        if (service != null) {
            service.notifyPointUpdated(pointId, valueTime);
        }
    }

    @Override
    public void pointInitialized() {
        DataPointWebSocketService service = DataPointWebSocketService.getInstance();
        if (service != null && com.serotonin.mango.Common.ctx != null && com.serotonin.mango.Common.ctx.getRuntimeManager() != null) {
            com.serotonin.mango.rt.dataImage.DataPointRT rt = com.serotonin.mango.Common.ctx.getRuntimeManager().getDataPoint(pointId);
            if (rt != null && rt.getPointValue() != null) {
                service.notifyPointUpdated(pointId, rt.getPointValue());
            }
        }
    }

    @Override
    public void pointTerminated() {
        if (com.serotonin.mango.Common.ctx != null && com.serotonin.mango.Common.ctx.getRuntimeManager() != null) {
            com.serotonin.mango.Common.ctx.getRuntimeManager().removeDataPointListener(pointId, this);
        }
        DataPointWebSocketService service = DataPointWebSocketService.getInstance();
        if (service != null) {
            service.notifyPointDisabled(pointId);
        }
    }
}
