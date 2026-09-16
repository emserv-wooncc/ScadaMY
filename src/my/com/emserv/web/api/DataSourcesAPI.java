package my.com.emserv.web.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonValue;
import com.serotonin.json.JsonWriter;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.web.dwr.DwrResponseI18n;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.SystemSettingsDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.vo.DataPointNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.web.i18n.I18NUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for Data Sources (/data_sources.shtm).
 * All endpoints enforce user data source access permissions.
 */
@RestController
@RequestMapping("/api/data-sources")
@Api(value = "Data Sources API", tags = "Data Sources Management")
public class DataSourcesAPI {
    private static final Log logger = LogFactory.getLog(DataSourcesAPI.class);

    private boolean hasPermission(User user, int dataSourceId) {
        if (user == null) {
            return false;
        }
        try {
            return Permissions.hasDataSourcePermission(user, dataSourceId);
        } catch (PermissionException e) {
            return false;
        }
    }

    private void ensurePermission(User user, int dataSourceId) throws PermissionException {
        if (user == null) {
            throw new PermissionException("Not authenticated", null);
        }
        Permissions.ensureDataSourcePermission(user, dataSourceId);
    }

    private DataSourceVO<?> findDataSource(String idOrXid) {
        DataSourceDao dao = new DataSourceDao();
        if (idOrXid == null) {
            return null;
        }
        if (idOrXid.matches("-?\\d+")) {
            try {
                int id = Integer.parseInt(idOrXid);
                DataSourceVO<?> ds = dao.getDataSource(id);
                if (ds != null) {
                    return ds;
                }
            } catch (NumberFormatException e) {
                // ignore and check by xid
            }
        }
        return dao.getDataSource(idOrXid);
    }

    private Map<String, Object> mapDataPoint(DataPointVO dp) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dp.getId());
        map.put("xid", dp.getXid());
        map.put("name", dp.getName());
        map.put("enabled", dp.isEnabled());
        map.put("dataSourceId", dp.getDataSourceId());
        map.put("deviceName", dp.getDeviceName());
        map.put("settable", dp.isSettable());
        if (dp.getPointLocator() != null) {
            map.put("dataTypeId", dp.getPointLocator().getDataTypeId());
        }
        map.put("chartColour", dp.getChartColour());
        map.put("engineeringUnits", dp.getEngineeringUnits());
        return map;
    }

    private Map<String, Object> mapDataSource(DataSourceVO<?> ds, ResourceBundle bundle, boolean includePoints) {
        Map<String, Object> map = new HashMap<>();
        try {
            com.serotonin.json.JsonWriter writer = new com.serotonin.json.JsonWriter();
            String json = writer.write(ds);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            map = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            logger.error("Error serializing data source", e);
        }

        map.put("id", ds.getId());
        map.put("xid", ds.getXid());
        map.put("name", ds.getName());
        map.put("enabled", ds.isEnabled());
        if (ds.getType() != null) {
            map.put("type", ds.getType().name());
            map.put("typeId", ds.getType().getId());
            map.put("typeKey", ds.getType().getKey());
            if (bundle != null) {
                map.put("typeDescription", I18NUtils.getMessage(bundle, ds.getType().getKey()));
            }
        }
        if (ds.getConnectionDescription() != null && bundle != null) {
            map.put("connectionDescription", ds.getConnectionDescription().getLocalizedMessage(bundle));
        }

        List<DataPointVO> points = new DataPointDao().getDataPoints(ds.getId(), DataPointNameComparator.instance);
        map.put("pointCount", points != null ? points.size() : 0);

        if (includePoints && points != null) {
            List<Map<String, Object>> pointsList = new ArrayList<>();
            for (DataPointVO dp : points) {
                pointsList.add(mapDataPoint(dp));
            }
            map.put("points", pointsList);
        }
        return map;
    }

    @ApiOperation(value = "Get all accessible data sources with optional points list", response = List.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getDataSources(
            @RequestParam(value = "includePoints", required = false, defaultValue = "false") boolean includePoints,
            @RequestParam(value = "type", required = false) String typeFilter,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResourceBundle bundle = Common.getBundle();
        List<DataSourceVO<?>> all = Common.ctx.getRuntimeManager().getDataSources();
        List<Map<String, Object>> result = new ArrayList<>();

        for (DataSourceVO<?> ds : all) {
            if (hasPermission(user, ds.getId())) {
                if (typeFilter != null && !typeFilter.isEmpty()) {
                    if (ds.getType() == null || !ds.getType().name().equalsIgnoreCase(typeFilter)) {
                        continue;
                    }
                }
                result.add(mapDataSource(ds, bundle, includePoints));
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get all supported data source types available for creation", response = List.class)
    @RequestMapping(value = "/types", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getDataSourceTypes(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResourceBundle bundle = Common.getBundle();
        List<Map<String, Object>> types = new ArrayList<>();

        for (DataSourceVO.Type type : DataSourceVO.Type.values()) {
            boolean display = SystemSettingsDao.getBooleanValue(type.name() + SystemSettingsDao.DATASOURCE_DISPLAY_SUFFIX, type.isDisplay());
            if (display || user.isAdmin()) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", type.getId());
                t.put("name", type.name());
                t.put("key", type.getKey());
                if (bundle != null) {
                    t.put("description", I18NUtils.getMessage(bundle, type.getKey()));
                }
                types.add(t);
            }
        }

        return ResponseEntity.ok(types);
    }

    @ApiOperation(value = "Get details of a specific data source by numeric ID or XID, with optional points list", response = Object.class)
    @RequestMapping(value = "/{idOrXid}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getDataSourceById(
            @PathVariable("idOrXid") String idOrXid, 
            @RequestParam(value = "includePoints", required = false, defaultValue = "false") boolean includePoints,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!hasPermission(user, ds.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ResourceBundle bundle = Common.getBundle();
        return ResponseEntity.ok(mapDataSource(ds, bundle, includePoints));
    }

    @ApiOperation(value = "Get all data points belonging to a specific data source", response = List.class)
    @RequestMapping(value = "/{idOrXid}/points", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getDataSourcePoints(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!hasPermission(user, ds.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DataPointVO> points = new DataPointDao().getDataPoints(ds.getId(), DataPointNameComparator.instance);
        List<Map<String, Object>> list = new ArrayList<>();
        if (points != null) {
            for (DataPointVO dp : points) {
                list.add(mapDataPoint(dp));
            }
        }

        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Toggle the enabled status of a data source", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/toggle", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> toggleDataSource(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, ds.getId());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
        DataSourceVO<?> runtimeDs = runtimeManager.getDataSource(ds.getId());
        if (runtimeDs == null) {
            runtimeDs = ds;
        }

        runtimeDs.setEnabled(!runtimeDs.isEnabled());
        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            runtimeManager.saveDataSource(runtimeDs);
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", runtimeDs.getId());
        result.put("xid", runtimeDs.getXid());
        result.put("enabled", runtimeDs.isEnabled());
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Explicitly set the enabled state of a data source", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/enable", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> enableDataSource(@PathVariable("idOrXid") String idOrXid,
            @RequestParam("enabled") boolean enabled,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, ds.getId());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
        DataSourceVO<?> runtimeDs = runtimeManager.getDataSource(ds.getId());
        if (runtimeDs == null) {
            runtimeDs = ds;
        }

        if (runtimeDs.isEnabled() != enabled) {
            runtimeDs.setEnabled(enabled);
            com.serotonin.mango.util.BackgroundContext.set(user);
            try {
                runtimeManager.saveDataSource(runtimeDs);
            } finally {
                com.serotonin.mango.util.BackgroundContext.remove();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", runtimeDs.getId());
        result.put("xid", runtimeDs.getXid());
        result.put("enabled", runtimeDs.isEnabled());
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Copy an existing data source along with its configuration", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/copy", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> copyDataSource(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, ds.getId());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        int newId;
        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            newId = new DataSourceDao().copyDataSource(ds.getId(), Common.getBundle());
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }
        Common.ctx.getUserCache().getUserDao().populateUserPermissions(user);

        DataSourceVO<?> newDs = new DataSourceDao().getDataSource(newId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("newDataSourceId", newId);
        if (newDs != null) {
            result.put("newXid", newDs.getXid());
            result.put("newName", newDs.getName());
        }
        result.put("message", "Data source copied successfully");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Delete a data source permanently", response = Object.class)
    @RequestMapping(value = "/{idOrXid}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteDataSource(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, ds.getId());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().deleteDataSource(ds.getId());
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", ds.getId());
        result.put("xid", ds.getXid());
        result.put("message", "Data source deleted successfully");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Toggle the enabled status of a specific data point under this data source", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/points/{pointIdOrXid}/toggle", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> toggleDataPoint(@PathVariable("idOrXid") String idOrXid,
            @PathVariable("pointIdOrXid") String pointIdOrXid,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, ds.getId());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DataPointDao pointDao = new DataPointDao();
        DataPointVO point = null;
        if (pointIdOrXid.matches("-?\\d+")) {
            try {
                int pid = Integer.parseInt(pointIdOrXid);
                point = pointDao.getDataPoint(pid);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (point == null) {
            point = pointDao.getDataPoint(pointIdOrXid);
        }

        if (point == null || point.getDataSourceId() != ds.getId()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
        point.setEnabled(!point.isEnabled());
        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            runtimeManager.saveDataPoint(point);
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", point.getId());
        result.put("xid", point.getXid());
        result.put("enabled", point.isEnabled());
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Enable all data points under this data source", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/points/enable-all", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> enableAllPoints(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, ds.getId());
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DataPointVO> points = new DataPointDao().getDataPoints(ds.getId(), DataPointNameComparator.instance);
        int enabledCount = 0;
        RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
        com.serotonin.mango.util.BackgroundContext.set(user);
        try {
            if (points != null) {
                for (DataPointVO dp : points) {
                    if (!dp.isEnabled()) {
                        dp.setEnabled(true);
                        runtimeManager.saveDataPoint(dp);
                        enabledCount++;
                    }
                }
            }
        } finally {
            com.serotonin.mango.util.BackgroundContext.remove();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("enabledCount", enabledCount);
        result.put("message", "Enabled " + enabledCount + " previously disabled data points");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Create a new data source from a JSON representation", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createDataSource(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> result = new HashMap<>();
        try {
            JsonWriter writer = new JsonWriter();
            String jsonPayload = writer.write(body);
            
            JsonReader reader = new JsonReader(jsonPayload);
            JsonValue value = reader.inflate();
            if (!(value instanceof JsonObject)) {
                result.put("success", false);
                result.put("message", "Invalid JSON payload, expected an object");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            JsonObject jo = value.toJsonObject();
            String typeStr = jo.getString("type");
            if (typeStr == null || typeStr.isEmpty()) {
                result.put("success", false);
                result.put("message", "Missing required property 'type'");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            DataSourceVO.Type type = DataSourceVO.Type.valueOfIgnoreCase(typeStr);
            if (type == null) {
                result.put("success", false);
                result.put("message", "Unknown data source type: " + typeStr);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            DataSourceVO<?> ds = type.createDataSourceVO();
            
            String xid = jo.getString("xid");
            if (xid == null || xid.isEmpty()) {
                xid = DataSourceVO.generateXid();
                ds.setXid(xid);
            } else {
                DataSourceDao dao = new DataSourceDao();
                if (!dao.isXidUnique(xid, -1)) {
                    result.put("success", false);
                    result.put("message", "XID already in use: " + xid);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }
                ds.setXid(xid);
            }

            reader.populateObject(ds, jo);

            DwrResponseI18n validationResponse = new DwrResponseI18n();
            ds.validate(validationResponse);

            if (validationResponse.getHasMessages()) {
                result.put("success", false);
                result.put("messages", validationResponse.getMessages());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            com.serotonin.mango.util.BackgroundContext.set(user);
            try {
                Common.ctx.getRuntimeManager().saveDataSource(ds);
            } finally {
                com.serotonin.mango.util.BackgroundContext.remove();
            }
            
            result.put("success", true);
            result.put("id", ds.getId());
            result.put("xid", ds.getXid());
            result.put("name", ds.getName());
            result.put("message", "Data source created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (LocalizableJsonException e) {
            result.put("success", false);
            result.put("message", e.getMsg().getLocalizedMessage(Common.getBundle()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (JsonException | ClassCastException e) {
            result.put("success", false);
            result.put("message", "JSON parse error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            logger.error("Error creating data source", e);
            result.put("success", false);
            result.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @ApiOperation(value = "Update an existing data source from a JSON representation", response = Object.class)
    @RequestMapping(value = "/{idOrXid}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateDataSource(@PathVariable("idOrXid") String idOrXid, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataSourceVO<?> ds = findDataSource(idOrXid);
        if (ds == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        if (!hasPermission(user, ds.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> result = new HashMap<>();
        try {
            JsonWriter writer = new JsonWriter();
            String jsonPayload = writer.write(body);
            
            JsonReader reader = new JsonReader(jsonPayload);
            JsonValue value = reader.inflate();
            if (!(value instanceof JsonObject)) {
                result.put("success", false);
                result.put("message", "Invalid JSON payload, expected an object");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            JsonObject jo = value.toJsonObject();
            
            String newXid = jo.getString("xid");
            if (newXid != null && !newXid.isEmpty() && !newXid.equals(ds.getXid())) {
                DataSourceDao dao = new DataSourceDao();
                if (!dao.isXidUnique(newXid, ds.getId())) {
                    result.put("success", false);
                    result.put("message", "XID already in use: " + newXid);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }
                ds.setXid(newXid);
            }

            reader.populateObject(ds, jo);

            DwrResponseI18n validationResponse = new DwrResponseI18n();
            ds.validate(validationResponse);

            if (validationResponse.getHasMessages()) {
                result.put("success", false);
                result.put("messages", validationResponse.getMessages());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            com.serotonin.mango.util.BackgroundContext.set(user);
            try {
                Common.ctx.getRuntimeManager().saveDataSource(ds);
            } finally {
                com.serotonin.mango.util.BackgroundContext.remove();
            }
            
            result.put("success", true);
            result.put("id", ds.getId());
            result.put("xid", ds.getXid());
            result.put("name", ds.getName());
            result.put("message", "Data source updated successfully");

            return ResponseEntity.ok(result);

        } catch (LocalizableJsonException e) {
            result.put("success", false);
            result.put("message", e.getMsg().getLocalizedMessage(Common.getBundle()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (JsonException | ClassCastException e) {
            result.put("success", false);
            result.put("message", "JSON parse error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            logger.error("Error updating data source", e);
            result.put("success", false);
            result.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
