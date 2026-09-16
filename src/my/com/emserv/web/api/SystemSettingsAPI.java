package my.com.emserv.web.api;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.org.scadabr.db.configuration.ConfigurationDB;

import com.serotonin.InvalidArgumentException;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.rt.maint.DataPurge;
import com.serotonin.mango.rt.maint.VersionCheck;
import com.serotonin.mango.rt.maint.work.EmailWorkItem;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.bean.PointHistoryCount;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.beans.IntegerPair;
import com.serotonin.mango.web.email.MangoEmailContent;
import com.serotonin.util.ColorUtils;
import com.serotonin.util.DirectoryInfo;
import com.serotonin.util.DirectoryUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.I18NUtils;
import com.serotonin.web.i18n.LocalizableMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for System Settings (/system_settings.shtm).
 * All endpoints require Administrator permissions.
 */
@RestController
@RequestMapping("/api/system-settings")
@Api(value = "System Settings API", tags = "System Settings & Configuration")
public class SystemSettingsAPI {
    private static final Log logger = LogFactory.getLog(SystemSettingsAPI.class);

    private boolean checkAdmin(User user) {
        if (user == null) {
            return false;
        }
        try {
            return Permissions.hasAdmin(user);
        } catch (Exception e) {
            return false;
        }
    }

    @ApiOperation(value = "Get all system settings organized by category", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSettings(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> settings = new HashMap<>();

        // Email Settings
        Map<String, Object> email = new HashMap<>();
        email.put("smtpHost", SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_HOST));
        email.put("smtpPort", SystemSettingsDao.getIntValue(SystemSettingsDao.EMAIL_SMTP_PORT));
        email.put("fromAddress", SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_FROM_ADDRESS));
        email.put("fromName", SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_FROM_NAME));
        email.put("authorization", SystemSettingsDao.getBooleanValue(SystemSettingsDao.EMAIL_AUTHORIZATION));
        email.put("smtpUsername", SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_USERNAME));
        email.put("smtpPassword", SystemSettingsDao.getValue(SystemSettingsDao.EMAIL_SMTP_PASSWORD));
        email.put("tls", SystemSettingsDao.getBooleanValue(SystemSettingsDao.EMAIL_TLS));
        email.put("contentType", SystemSettingsDao.getIntValue(SystemSettingsDao.EMAIL_CONTENT_TYPE));
        settings.put("email", email);

        // HTTP Proxy Settings
        Map<String, Object> http = new HashMap<>();
        http.put("useProxy", SystemSettingsDao.getBooleanValue(SystemSettingsDao.HTTP_CLIENT_USE_PROXY));
        http.put("proxyServer", SystemSettingsDao.getValue(SystemSettingsDao.HTTP_CLIENT_PROXY_SERVER));
        http.put("proxyPort", SystemSettingsDao.getIntValue(SystemSettingsDao.HTTP_CLIENT_PROXY_PORT));
        http.put("proxyUsername", SystemSettingsDao.getValue(SystemSettingsDao.HTTP_CLIENT_PROXY_USERNAME));
        http.put("proxyPassword", SystemSettingsDao.getValue(SystemSettingsDao.HTTP_CLIENT_PROXY_PASSWORD));
        settings.put("http", http);

        // Miscellaneous Settings
        Map<String, Object> misc = new HashMap<>();
        misc.put("eventPurgePeriodType", SystemSettingsDao.getIntValue(SystemSettingsDao.EVENT_PURGE_PERIOD_TYPE));
        misc.put("eventPurgePeriods", SystemSettingsDao.getIntValue(SystemSettingsDao.EVENT_PURGE_PERIODS));
        misc.put("reportPurgePeriodType", SystemSettingsDao.getIntValue(SystemSettingsDao.REPORT_PURGE_PERIOD_TYPE));
        misc.put("reportPurgePeriods", SystemSettingsDao.getIntValue(SystemSettingsDao.REPORT_PURGE_PERIODS));
        misc.put("uiPerformance", SystemSettingsDao.getIntValue(SystemSettingsDao.UI_PERFORAMANCE));
        misc.put("groveLogging", SystemSettingsDao.getBooleanValue(SystemSettingsDao.GROVE_LOGGING));
        misc.put("futureDateLimitPeriodType", SystemSettingsDao.getIntValue(SystemSettingsDao.FUTURE_DATE_LIMIT_PERIOD_TYPE));
        misc.put("futureDateLimitPeriods", SystemSettingsDao.getIntValue(SystemSettingsDao.FUTURE_DATE_LIMIT_PERIODS));
        settings.put("misc", misc);

        // Info & Language
        Map<String, Object> info = new HashMap<>();
        info.put("instanceDescription", SystemSettingsDao.getValue(SystemSettingsDao.INSTANCE_DESCRIPTION));
        info.put("newVersionNotificationLevel", SystemSettingsDao.getValue(SystemSettingsDao.NEW_VERSION_NOTIFICATION_LEVEL));
        info.put("language", SystemSettingsDao.getValue(SystemSettingsDao.LANGUAGE));
        settings.put("info", info);

        // Colours
        Map<String, Object> colours = new HashMap<>();
        colours.put("chartBackgroundColour", SystemSettingsDao.getValue(SystemSettingsDao.CHART_BACKGROUND_COLOUR));
        colours.put("plotBackgroundColour", SystemSettingsDao.getValue(SystemSettingsDao.PLOT_BACKGROUND_COLOUR));
        colours.put("plotGridlineColour", SystemSettingsDao.getValue(SystemSettingsDao.PLOT_GRIDLINE_COLOUR));
        settings.put("colours", colours);

        // Event Alarm Levels
        Map<String, Object> alarmLevels = new HashMap<>();
        ResourceBundle bundle = Common.getBundle();
        List<Map<String, Object>> systemEvents = new ArrayList<>();
        for (EventTypeVO et : SystemEventType.getSystemEventTypes()) {
            Map<String, Object> m = new HashMap<>();
            m.put("typeId", et.getTypeId());
            m.put("typeRef1", et.getTypeRef1());
            m.put("alarmLevel", et.getAlarmLevel());
            if (et.getDescription() != null && bundle != null) {
                m.put("description", et.getDescription().getLocalizedMessage(bundle));
                m.put("descriptionKey", et.getDescription().getKey());
            }
            systemEvents.add(m);
        }
        alarmLevels.put("systemEventTypes", systemEvents);

        List<Map<String, Object>> auditEvents = new ArrayList<>();
        for (EventTypeVO et : AuditEventType.getAuditEventTypes()) {
            Map<String, Object> m = new HashMap<>();
            m.put("typeId", et.getTypeId());
            m.put("typeRef1", et.getTypeRef1());
            m.put("alarmLevel", et.getAlarmLevel());
            if (et.getDescription() != null && bundle != null) {
                m.put("description", et.getDescription().getLocalizedMessage(bundle));
                m.put("descriptionKey", et.getDescription().getKey());
            }
            auditEvents.add(m);
        }
        alarmLevels.put("auditEventTypes", auditEvents);
        settings.put("alarmLevels", alarmLevels);

        return ResponseEntity.ok(settings);
    }

    @ApiOperation(value = "Get database size statistics and history point counts", response = Object.class)
    @RequestMapping(value = "/database-size", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getDatabaseSize(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> data = new HashMap<>();
        File dataDirectory = Common.ctx.getDatabaseAccess().getDataDirectory();
        long dbSize = 0;
        if (dataDirectory != null) {
            DirectoryInfo dbInfo = DirectoryUtils.getDirectorySize(dataDirectory);
            dbSize = dbInfo.getSize();
            data.put("databaseSize", DirectoryUtils.bytesDescription(dbSize));
        } else {
            data.put("databaseSize", "(unknown)");
        }

        DirectoryInfo fileDatainfo = DirectoryUtils.getDirectorySize(new File(Common.getFiledataPath()));
        long filedataSize = fileDatainfo.getSize();
        data.put("filedataCount", fileDatainfo.getCount());
        data.put("filedataSize", DirectoryUtils.bytesDescription(filedataSize));
        data.put("totalSize", DirectoryUtils.bytesDescription(dbSize + filedataSize));

        String dbType = Common.getEnvironmentProfile().getString("db.type", "derby");
        data.put("dbType", dbType);
        if ("mysql".equals(dbType)) {
            double size = new SystemSettingsDao().getDataBaseSize();
            data.put("databaseSize", size + " MB");
            data.put("totalSize", size + " MB");
        }

        List<PointHistoryCount> counts = new DataPointDao().getTopPointHistoryCounts();
        int sum = 0;
        List<Map<String, Object>> topPointsList = new ArrayList<>();
        if (counts != null) {
            for (PointHistoryCount c : counts) {
                sum += c.getCount();
                Map<String, Object> p = new HashMap<>();
                p.put("pointId", c.getPointId());
                p.put("pointName", c.getPointName());
                p.put("count", c.getCount());
                topPointsList.add(p);
            }
        }
        data.put("historyCount", sum);
        data.put("topPoints", topPointsList);
        data.put("eventCount", new EventDao().getEventCount());

        return ResponseEntity.ok(data);
    }

    @ApiOperation(value = "Update email SMTP configuration settings", response = Object.class)
    @RequestMapping(value = "/email", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateEmailSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SystemSettingsDao dao = new SystemSettingsDao();
        if (body.containsKey("smtpHost")) dao.setValue(SystemSettingsDao.EMAIL_SMTP_HOST, body.get("smtpHost").toString());
        if (body.containsKey("smtpPort")) dao.setIntValue(SystemSettingsDao.EMAIL_SMTP_PORT, ((Number) body.get("smtpPort")).intValue());
        if (body.containsKey("fromAddress")) dao.setValue(SystemSettingsDao.EMAIL_FROM_ADDRESS, body.get("fromAddress").toString());
        if (body.containsKey("fromName")) dao.setValue(SystemSettingsDao.EMAIL_FROM_NAME, body.get("fromName").toString());
        if (body.containsKey("authorization")) dao.setBooleanValue(SystemSettingsDao.EMAIL_AUTHORIZATION, Boolean.parseBoolean(body.get("authorization").toString()));
        if (body.containsKey("smtpUsername")) dao.setValue(SystemSettingsDao.EMAIL_SMTP_USERNAME, body.get("smtpUsername").toString());
        if (body.containsKey("smtpPassword")) dao.setValue(SystemSettingsDao.EMAIL_SMTP_PASSWORD, body.get("smtpPassword").toString());
        if (body.containsKey("tls")) dao.setBooleanValue(SystemSettingsDao.EMAIL_TLS, Boolean.parseBoolean(body.get("tls").toString()));
        if (body.containsKey("contentType")) dao.setIntValue(SystemSettingsDao.EMAIL_CONTENT_TYPE, ((Number) body.get("contentType")).intValue());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Email settings updated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Save email configuration and send a test email to the currently logged in user", response = Object.class)
    @RequestMapping(value = "/email/test", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!body.isEmpty()) {
            updateEmailSettings(body, request);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            ResourceBundle bundle = Common.getBundle();
            Map<String, Object> model = new HashMap<>();
            model.put("message", new LocalizableMessage("systemSettings.testEmail"));
            MangoEmailContent cnt = new MangoEmailContent("testEmail", model, bundle,
                    I18NUtils.getMessage(bundle, "ftl.testEmail"), Common.UTF8);
            EmailWorkItem.queueEmail(user.getEmail(), cnt);
            result.put("success", true);
            result.put("message", new LocalizableMessage("common.testEmailSent", user.getEmail()).getLocalizedMessage(bundle));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error sending test email", e);
            result.put("success", false);
            result.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @ApiOperation(value = "Update HTTP proxy configuration settings", response = Object.class)
    @RequestMapping(value = "/http", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateHttpSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SystemSettingsDao dao = new SystemSettingsDao();
        if (body.containsKey("useProxy")) dao.setBooleanValue(SystemSettingsDao.HTTP_CLIENT_USE_PROXY, Boolean.parseBoolean(body.get("useProxy").toString()));
        if (body.containsKey("proxyServer")) dao.setValue(SystemSettingsDao.HTTP_CLIENT_PROXY_SERVER, body.get("proxyServer").toString());
        if (body.containsKey("proxyPort")) dao.setIntValue(SystemSettingsDao.HTTP_CLIENT_PROXY_PORT, ((Number) body.get("proxyPort")).intValue());
        if (body.containsKey("proxyUsername")) dao.setValue(SystemSettingsDao.HTTP_CLIENT_PROXY_USERNAME, body.get("proxyUsername").toString());
        if (body.containsKey("proxyPassword")) dao.setValue(SystemSettingsDao.HTTP_CLIENT_PROXY_PASSWORD, body.get("proxyPassword").toString());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "HTTP settings updated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update miscellaneous settings (purge periods, performance, future limits)", response = Object.class)
    @RequestMapping(value = "/misc", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateMiscSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SystemSettingsDao dao = new SystemSettingsDao();
        if (body.containsKey("eventPurgePeriodType")) dao.setIntValue(SystemSettingsDao.EVENT_PURGE_PERIOD_TYPE, ((Number) body.get("eventPurgePeriodType")).intValue());
        if (body.containsKey("eventPurgePeriods")) dao.setIntValue(SystemSettingsDao.EVENT_PURGE_PERIODS, ((Number) body.get("eventPurgePeriods")).intValue());
        if (body.containsKey("reportPurgePeriodType")) dao.setIntValue(SystemSettingsDao.REPORT_PURGE_PERIOD_TYPE, ((Number) body.get("reportPurgePeriodType")).intValue());
        if (body.containsKey("reportPurgePeriods")) dao.setIntValue(SystemSettingsDao.REPORT_PURGE_PERIODS, ((Number) body.get("reportPurgePeriods")).intValue());
        if (body.containsKey("uiPerformance")) dao.setIntValue(SystemSettingsDao.UI_PERFORAMANCE, ((Number) body.get("uiPerformance")).intValue());
        if (body.containsKey("groveLogging")) dao.setBooleanValue(SystemSettingsDao.GROVE_LOGGING, Boolean.parseBoolean(body.get("groveLogging").toString()));
        if (body.containsKey("futureDateLimitPeriodType")) dao.setIntValue(SystemSettingsDao.FUTURE_DATE_LIMIT_PERIOD_TYPE, ((Number) body.get("futureDateLimitPeriodType")).intValue());
        if (body.containsKey("futureDateLimitPeriods")) dao.setIntValue(SystemSettingsDao.FUTURE_DATE_LIMIT_PERIODS, ((Number) body.get("futureDateLimitPeriods")).intValue());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Miscellaneous settings updated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update chart and plot color settings", response = Object.class)
    @RequestMapping(value = "/colours", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateColourSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String chartColour = body.get("chartBackgroundColour") != null ? body.get("chartBackgroundColour").toString() : null;
        String plotColour = body.get("plotBackgroundColour") != null ? body.get("plotBackgroundColour").toString() : null;
        String gridColour = body.get("plotGridlineColour") != null ? body.get("plotGridlineColour").toString() : null;

        Map<String, Object> errors = new HashMap<>();
        if (chartColour != null) {
            try { ColorUtils.toColor(chartColour); } catch (InvalidArgumentException e) { errors.put("chartBackgroundColour", "Invalid colour format"); }
        }
        if (plotColour != null) {
            try { ColorUtils.toColor(plotColour); } catch (InvalidArgumentException e) { errors.put("plotBackgroundColour", "Invalid colour format"); }
        }
        if (gridColour != null) {
            try { ColorUtils.toColor(gridColour); } catch (InvalidArgumentException e) { errors.put("plotGridlineColour", "Invalid colour format"); }
        }

        if (!errors.isEmpty()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        SystemSettingsDao dao = new SystemSettingsDao();
        if (chartColour != null) dao.setValue(SystemSettingsDao.CHART_BACKGROUND_COLOUR, chartColour);
        if (plotColour != null) dao.setValue(SystemSettingsDao.PLOT_BACKGROUND_COLOUR, plotColour);
        if (gridColour != null) dao.setValue(SystemSettingsDao.PLOT_GRIDLINE_COLOUR, gridColour);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Colour settings updated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update instance description and notification settings", response = Object.class)
    @RequestMapping(value = "/info", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateInfoSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SystemSettingsDao dao = new SystemSettingsDao();
        if (body.containsKey("newVersionNotificationLevel")) {
            dao.setValue(SystemSettingsDao.NEW_VERSION_NOTIFICATION_LEVEL, body.get("newVersionNotificationLevel").toString());
        }
        if (body.containsKey("instanceDescription")) {
            String desc = body.get("instanceDescription").toString();
            String escaped = desc.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&apos;");
            dao.setValue(SystemSettingsDao.INSTANCE_DESCRIPTION, escaped);
        }
        if (body.containsKey("language")) {
            String lang = body.get("language").toString();
            dao.setValue(SystemSettingsDao.LANGUAGE, lang);
            Common.setSystemLanguage(lang);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Info settings updated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update alarm levels for system and audit event types", response = Object.class)
    @RequestMapping(value = "/alarm-levels", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateAlarmLevels(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object sysObj = body.get("systemEventTypes");
        if (sysObj instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) sysObj;
            for (Map<String, Object> item : list) {
                if (item.get("typeRef1") != null && item.get("alarmLevel") != null) {
                    int id = ((Number) item.get("typeRef1")).intValue();
                    int level = ((Number) item.get("alarmLevel")).intValue();
                    SystemEventType.setEventTypeAlarmLevel(id, level);
                }
            }
        }

        Object auditObj = body.get("auditEventTypes");
        if (auditObj instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) auditObj;
            for (Map<String, Object> item : list) {
                if (item.get("typeRef1") != null && item.get("alarmLevel") != null) {
                    int id = ((Number) item.get("typeRef1")).intValue();
                    int level = ((Number) item.get("alarmLevel")).intValue();
                    AuditEventType.setEventTypeAlarmLevel(id, level);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Alarm levels updated");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Trigger manual purge of old events right now", response = Object.class)
    @RequestMapping(value = "/purge-events", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> purgeEventsNow(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPurge dataPurge = new DataPurge();
        dataPurge.execute(System.currentTimeMillis());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Event purge executed successfully");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Purge all historical point data values from the system", response = Object.class)
    @RequestMapping(value = "/purge-all-data", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> purgeAllData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long cnt = Common.ctx.getRuntimeManager().purgeDataPointValues();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("purgedCount", cnt);
        response.put("message", "Purge all data completed. Purged values count: " + cnt);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Restart the server", response = Object.class)
    @RequestMapping(value = "/restart", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> restartServer(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!checkAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();

        boolean restartable = "true".equals(System.getenv("SCADAMY_RESTARTABLE"));
        File batFile = new File("start.bat");
        File shFile = new File("start.sh");

        if (!restartable && !batFile.exists() && !shFile.exists()) {
            response.put("success", false);
            response.put("error", "Restart scripts (start.bat/start.sh) not detected. Please manually restart the server.");
            return ResponseEntity.ok(response);
        }

        response.put("success", true);
        response.put("message", "Restarting server in 2 seconds...");

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
            System.exit(0);
        }).start();

        return ResponseEntity.ok(response);
    }
}
