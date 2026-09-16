package my.com.emserv.web.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.org.scadabr.db.dao.ScriptDao;
import br.org.scadabr.rt.scripting.ScriptRT;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import br.org.scadabr.vo.scripting.ContextualizedScriptVO;
import br.org.scadabr.vo.scripting.ScriptVO;

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.web.dwr.DwrResponseI18n;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing and executing Scripts (/scripting.shtm).
 */
@RestController
@RequestMapping("/api/scripts")
@Api(value = "Scripts API", tags = "Scripting Operations")
public class ScriptsAPI {
    private static final Log logger = LogFactory.getLog(ScriptsAPI.class);

    @ApiOperation(value = "Get initialization context including accessible data points and system objects", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BackgroundContext.set(user);
        try {
            DataPointDao dataPointDao = new DataPointDao();
            List<DataPointVO> allPoints = dataPointDao.getDataPoints(null, false);
            List<Map<String, Object>> pointsList = new ArrayList<>();
            for (DataPointVO dp : allPoints) {
                if (Permissions.hasDataPointReadPermission(user, dp)) {
                    Map<String, Object> pt = new HashMap<>();
                    pt.put("id", dp.getId());
                    pt.put("xid", dp.getXid());
                    pt.put("name", dp.getExtendedName() != null ? dp.getExtendedName() : dp.getName());
                    pt.put("dataType", dp.getPointLocator().getDataTypeId());
                    pt.put("settable", dp.getPointLocator().isSettable());
                    pointsList.add(pt);
                }
            }

            List<Map<String, Object>> objectsList = new ArrayList<>();
            for (ScriptContextObject.Type type : ScriptContextObject.Type.values()) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("id", type.getId());
                obj.put("name", type.name());
                obj.put("key", type.getKey());
                obj.put("defaultVarName", type == ScriptContextObject.Type.DATASOURCE_COMMANDS ? "dsCommands" : "dpCommands");
                obj.put("help", type.getHelp());
                objectsList.add(obj);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("dataPoints", pointsList);
            response.put("availableObjects", objectsList);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "List all configured scripts", response = Object.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> listScripts(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ScriptVO<?>> scripts = new ScriptDao().getScripts();
        List<Map<String, Object>> result = new ArrayList<>();
        if (scripts != null) {
            for (ScriptVO<?> s : scripts) {
                result.add(mapScriptToMap(s));
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get details of a specific script by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getScript(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ScriptVO<?> script = new ScriptDao().getScript(id);
        if (script == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(mapScriptToMap(script));
    }

    @ApiOperation(value = "Create a new script", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createScript(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return parseAndSaveScript(Common.NEW_ID, body, user);
    }

    @ApiOperation(value = "Update an existing script by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateScript(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ScriptVO<?> existing = new ScriptDao().getScript(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return parseAndSaveScript(id, body, user);
    }

    @ApiOperation(value = "Delete a script by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteScript(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ScriptVO<?> existing = new ScriptDao().getScript(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            new ScriptDao().deleteScript(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Execute a script by ID immediately", response = Object.class)
    @RequestMapping(value = "/{id}/execute", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> executeScript(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ScriptVO<?> script = new ScriptDao().getScript(id);
        if (script == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            Map<String, Object> response = new HashMap<>();
            try {
                ScriptRT rt = script.createScriptRT();
                rt.execute();
                response.put("success", true);
                response.put("message", "Script executed successfully");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error executing script id: " + id, e);
                response.put("success", false);
                response.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Execute a script by XID immediately", response = Object.class)
    @RequestMapping(value = "/execute-xid/{xid}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> executeScriptByXid(@PathVariable String xid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ScriptVO<?> script = new ScriptDao().getScript(xid);
        if (script == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            Map<String, Object> response = new HashMap<>();
            try {
                ScriptRT rt = script.createScriptRT();
                rt.execute();
                response.put("success", true);
                response.put("message", "Script executed successfully");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error executing script xid: " + xid, e);
                response.put("success", false);
                response.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } finally {
            BackgroundContext.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> parseAndSaveScript(int id, Map<String, Object> body, User user) {
        ContextualizedScriptVO vo = new ContextualizedScriptVO();
        vo.setId(id);

        String xid = body.get("xid") != null ? body.get("xid").toString().trim() : null;
        if (xid == null || xid.isEmpty()) {
            if (id != Common.NEW_ID) {
                ScriptVO<?> existing = new ScriptDao().getScript(id);
                if (existing != null) {
                    xid = existing.getXid();
                }
            }
            if (xid == null || xid.isEmpty()) {
                xid = new ScriptDao().generateUniqueXid();
            }
        }
        vo.setXid(xid);

        String name = body.get("name") != null ? body.get("name").toString().trim() : "";
        vo.setName(name);

        String scriptCode = body.get("script") != null ? body.get("script").toString() : "";
        vo.setScript(scriptCode);

        vo.setUserId(user.getId());

        List<IntValuePair> pointsOnContext = new ArrayList<>();
        Object pointsObj = body.get("pointsOnContext");
        if (pointsObj instanceof List) {
            List<Map<String, Object>> pointsList = (List<Map<String, Object>>) pointsObj;
            for (Map<String, Object> ptMap : pointsList) {
                String varName = ptMap.get("varName") != null ? ptMap.get("varName").toString().trim() : null;
                if (varName == null || varName.isEmpty()) {
                    continue;
                }
                int dpId = -1;
                if (ptMap.get("dataPointId") != null && ptMap.get("dataPointId") instanceof Number) {
                    dpId = ((Number) ptMap.get("dataPointId")).intValue();
                } else if (ptMap.get("dataPointId") != null && ptMap.get("dataPointId") instanceof String) {
                    try {
                        dpId = Integer.parseInt(ptMap.get("dataPointId").toString());
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                if (dpId <= 0 && ptMap.get("dataPointXid") != null) {
                    DataPointVO dpvo = new DataPointDao().getDataPoint(ptMap.get("dataPointXid").toString().trim());
                    if (dpvo != null) {
                        dpId = dpvo.getId();
                    }
                }
                if (dpId > 0) {
                    pointsOnContext.add(new IntValuePair(dpId, varName));
                }
            }
        }
        vo.setPointsOnContext(pointsOnContext);

        List<IntValuePair> objectsOnContext = new ArrayList<>();
        Object objectsObj = body.get("objectsOnContext");
        if (objectsObj instanceof List) {
            List<Map<String, Object>> objectsList = (List<Map<String, Object>>) objectsObj;
            for (Map<String, Object> objMap : objectsList) {
                String varName = objMap.get("varName") != null ? objMap.get("varName").toString().trim() : null;
                if (varName == null || varName.isEmpty()) {
                    continue;
                }
                int objectId = -1;
                if (objMap.get("objectId") != null && objMap.get("objectId") instanceof Number) {
                    objectId = ((Number) objMap.get("objectId")).intValue();
                } else if (objMap.get("objectId") != null && objMap.get("objectId") instanceof String) {
                    try {
                        objectId = Integer.parseInt(objMap.get("objectId").toString());
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                if (objectId <= 0 && objMap.get("objectType") != null) {
                    ScriptContextObject.Type type = ScriptContextObject.Type.valueOfIgnoreCase(objMap.get("objectType").toString().trim());
                    if (type != null) {
                        objectId = type.getId();
                    }
                }
                if (objectId > 0) {
                    objectsOnContext.add(new IntValuePair(objectId, varName));
                }
            }
        }
        vo.setObjectsOnContext(objectsOnContext);

        DwrResponseI18n validationResponse = new DwrResponseI18n();
        vo.validate(validationResponse);

        if (validationResponse.getHasMessages()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("messages", validationResponse.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResp);
        }

        BackgroundContext.set(user);
        try {
            new ScriptDao().saveScript(vo);
            return ResponseEntity.ok(mapScriptToMap(vo));
        } finally {
            BackgroundContext.remove();
        }
    }

    private Map<String, Object> mapScriptToMap(ScriptVO<?> s) {
        Map<String, Object> map = new HashMap<>();
        if (s == null) {
            return map;
        }

        map.put("id", s.getId());
        map.put("xid", s.getXid());
        map.put("name", s.getName());
        map.put("script", s.getScript());
        map.put("userId", s.getUserId());

        if (s instanceof ContextualizedScriptVO) {
            ContextualizedScriptVO cvo = (ContextualizedScriptVO) s;
            List<Map<String, Object>> pointList = new ArrayList<>();
            if (cvo.getPointsOnContext() != null) {
                DataPointDao dpDao = new DataPointDao();
                for (IntValuePair p : cvo.getPointsOnContext()) {
                    DataPointVO dp = dpDao.getDataPoint(p.getKey());
                    Map<String, Object> pt = new HashMap<>();
                    pt.put("varName", p.getValue());
                    pt.put("dataPointId", p.getKey());
                    pt.put("dataPointXid", dp != null ? dp.getXid() : null);
                    pt.put("dataPointName", dp != null ? dp.getName() : null);
                    pointList.add(pt);
                }
            }
            map.put("pointsOnContext", pointList);

            List<Map<String, Object>> objectsList = new ArrayList<>();
            if (cvo.getObjectsOnContext() != null) {
                for (IntValuePair p : cvo.getObjectsOnContext()) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("varName", p.getValue());
                    obj.put("objectId", p.getKey());
                    ScriptContextObject.Type type = ScriptContextObject.Type.valueOf(p.getKey());
                    obj.put("objectType", type != null ? type.name() : null);
                    objectsList.add(obj);
                }
            }
            map.put("objectsOnContext", objectsList);
        }

        return map;
    }
}
