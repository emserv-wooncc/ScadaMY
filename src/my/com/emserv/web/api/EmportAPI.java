package my.com.emserv.web.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.EmportDwr;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.mango.util.BackgroundContext;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for system Export/Import (/emport.shtm).
 */
@RestController
@RequestMapping("/api/emport")
@Api(value = "Emport API", tags = "System Export/Import Management")
public class EmportAPI {
    private static final Log logger = LogFactory.getLog(EmportAPI.class);

    @ApiOperation(value = "Export system configuration data as JSON (Admin only)", response = String.class)
    @RequestMapping(value = "/export", method = RequestMethod.POST, produces = "application/json")
    public void exportSystemData(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        User user = Common.getUser(request);
        if (user == null) {
            response.setStatus(401);
            return;
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            response.setStatus(403);
            return;
        }

        if (params == null) {
            params = new HashMap<>();
        }

        int prettyIndent = getIntParam(params, "prettyIndent", 3);
        boolean graphicalViews = getBoolParam(params, "graphicalViews", true);
        boolean eventHandlers = getBoolParam(params, "eventHandlers", true);
        boolean dataSources = getBoolParam(params, "dataSources", true);
        boolean dataPoints = getBoolParam(params, "dataPoints", true);
        boolean scheduledEvents = getBoolParam(params, "scheduledEvents", true);
        boolean compoundEventDetectors = getBoolParam(params, "compoundEventDetectors", true);
        boolean pointLinks = getBoolParam(params, "pointLinks", true);
        boolean users = getBoolParam(params, "users", true);
        boolean pointHierarchy = getBoolParam(params, "pointHierarchy", true);
        boolean mailingLists = getBoolParam(params, "mailingLists", true);
        boolean publishers = getBoolParam(params, "publishers", true);
        boolean watchLists = getBoolParam(params, "watchLists", true);
        boolean maintenanceEvents = getBoolParam(params, "maintenanceEvents", true);
        boolean scripts = getBoolParam(params, "scripts", true);
        boolean pointValues = getBoolParam(params, "pointValues", false);
        int maxPointValues = getIntParam(params, "maxPointValues", 100);
        boolean systemSettings = getBoolParam(params, "systemSettings", true);
        boolean usersProfiles = getBoolParam(params, "usersProfiles", true);

        try {
            BackgroundContext.set(user);
            String jsonExport = EmportDwr.createExportJSON(prettyIndent, graphicalViews, eventHandlers, dataSources,
                    dataPoints, scheduledEvents, compoundEventDetectors, pointLinks, users, pointHierarchy, mailingLists,
                    publishers, watchLists, maintenanceEvents, scripts, pointValues, maxPointValues, systemSettings, usersProfiles);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonExport);
        } catch (Exception e) {
            logger.error("Error generating system JSON export", e);
            response.setStatus(500);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Export system configuration data as ZIP (Admin only)")
    @RequestMapping(value = "/export-zip", method = RequestMethod.GET)
    public void exportSystemDataZip(HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        User user = Common.getUser(request);
        if (user == null) {
            response.setStatus(401);
            return;
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            response.setStatus(403);
            return;
        }

        br.org.scadabr.vo.exporter.ZIPProjectManager exporter = new br.org.scadabr.vo.exporter.ZIPProjectManager();
        try {
            BackgroundContext.set(user);
            exporter.exportProject(request, response);
        } catch (Exception e) {
            logger.error("Error generating system ZIP export", e);
            response.setStatus(500);
        } finally {
            BackgroundContext.remove();
        }
    }

    private void stopRunningDataSources() {
        java.util.List<com.serotonin.mango.vo.dataSource.DataSourceVO<?>> dataSources = new com.serotonin.mango.db.dao.DataSourceDao().getDataSources();
        com.serotonin.mango.rt.RuntimeManager rtm = com.serotonin.mango.Common.ctx.getRuntimeManager();
        for (com.serotonin.mango.vo.dataSource.DataSourceVO<?> dataSourceVO : dataSources) {
            if (dataSourceVO.isEnabled())
                rtm.stopDataSource(dataSourceVO.getId());
        }
    }

    @ApiOperation(value = "Initiate background import of system configuration ZIP (Admin only)", response = Object.class)
    @RequestMapping(value = "/import-zip", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> importSystemDataZip(@org.springframework.web.bind.annotation.RequestParam("importFile") org.springframework.web.multipart.MultipartFile multipartFile, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (multipartFile.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Import file is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        br.org.scadabr.vo.exporter.ZIPProjectManager importer = new br.org.scadabr.vo.exporter.ZIPProjectManager();
        try {
            BackgroundContext.set(user);
            importer.setupToImportProject(multipartFile);
            
            stopRunningDataSources();
            new com.serotonin.mango.db.dao.SystemSettingsDao().resetDataBase();
            
            importer.importProject();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("importStarted", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error importing system ZIP", e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Initiate background import of system configuration JSON (Admin only)", response = Object.class)
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> importSystemData(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String data = body.get("data") != null ? body.get("data").toString() : "";
        if (data.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Import data payload is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        DwrResponseI18n dwrResp;
        
        try {
            BackgroundContext.set(user);
            dwrResp = EmportDwr.importDataImpl(data, com.serotonin.mango.Common.getBundle(), user);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", !dwrResp.getHasMessages());
        if (dwrResp.getHasMessages()) {
            response.put("messages", dwrResp.getMessages());
        }
        if (dwrResp.getData() != null) {
            response.putAll(dwrResp.getData());
        }
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Check progress of active background import task (Admin only)", response = Object.class)
    @RequestMapping(value = "/import/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getImportStatus(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        EmportDwr dwr = new EmportDwr();
        DwrResponseI18n dwrResp;
        
        try {
            BackgroundContext.set(user);
            dwrResp = dwr.importUpdate();
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        if (dwrResp.getMessages() != null && !dwrResp.getMessages().isEmpty()) {
            response.put("messages", dwrResp.getMessages());
        }
        if (dwrResp.getData() != null) {
            response.putAll(dwrResp.getData());
        }
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Cancel an ongoing background import task (Admin only)", response = Object.class)
    @RequestMapping(value = "/import/cancel", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> cancelImport(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!Permissions.hasAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        EmportDwr dwr = new EmportDwr();
        
        try {
            BackgroundContext.set(user);
            dwr.importCancel();
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Import cancelled if active");
        return ResponseEntity.ok(response);
    }

    private int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        if (params.get(key) instanceof Number) {
            return ((Number) params.get(key)).intValue();
        }
        if (params.get(key) != null) {
            try {
                return Integer.parseInt(params.get(key).toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private boolean getBoolParam(Map<String, Object> params, String key, boolean defaultValue) {
        if (params.get(key) instanceof Boolean) {
            return (Boolean) params.get(key);
        }
        if (params.get(key) != null) {
            return Boolean.parseBoolean(params.get(key).toString());
        }
        return defaultValue;
    }
}
