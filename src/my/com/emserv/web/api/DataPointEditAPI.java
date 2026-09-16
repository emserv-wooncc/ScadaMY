package my.com.emserv.web.api;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.view.chart.ImageChartRenderer;
import com.serotonin.mango.view.chart.ImageFlipbookRenderer;
import com.serotonin.mango.view.chart.StatisticsChartRenderer;
import com.serotonin.mango.view.chart.TableChartRenderer;
import com.serotonin.mango.view.text.AnalogRenderer;
import com.serotonin.mango.view.text.BinaryTextRenderer;
import com.serotonin.mango.view.text.MultistateRenderer;
import com.serotonin.mango.view.text.MultistateValue;
import com.serotonin.mango.view.text.NoneRenderer;
import com.serotonin.mango.view.text.PlainRenderer;
import com.serotonin.mango.view.text.RangeRenderer;
import com.serotonin.mango.view.text.RangeValue;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.view.text.TimeRenderer;
import com.serotonin.mango.vo.DataPointNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for Data Point Editing (/data_point_edit.shtm).
 * Provides full configuration, toggling, copying, purging, renderer updates, and event detector management.
 */
@RestController
@RequestMapping("/api/data-points")
@Api(value = "Data Points API", tags = "Data Points Management")
public class DataPointEditAPI {
    private static final Log logger = LogFactory.getLog(DataPointEditAPI.class);

    private boolean hasPermission(User user, DataPointVO dp) {
        if (user == null || dp == null) {
            return false;
        }
        try {
            return Permissions.hasDataSourcePermission(user, dp.getDataSourceId());
        } catch (PermissionException e) {
            return false;
        }
    }

    private void ensurePermission(User user, DataPointVO dp) throws PermissionException {
        if (user == null) {
            throw new PermissionException("Not authenticated", null);
        }
        if (dp == null) {
            throw new PermissionException("Data point not found", user);
        }
        Permissions.ensureDataSourcePermission(user, dp.getDataSourceId());
    }

    private void setLocatorDataTypeId(PointLocatorVO locator, int dataTypeId) {
        if (locator == null) return;
        try {
            java.lang.reflect.Method m = locator.getClass().getMethod("setDataTypeId", int.class);
            if (m != null) {
                m.invoke(locator, dataTypeId);
            }
        } catch (Exception e) {
            // Ignore if locator does not support setting data type
        }
    }

    private void setLocatorSettable(PointLocatorVO locator, boolean settable) {
        if (locator == null) return;
        try {
            java.lang.reflect.Method m = locator.getClass().getMethod("setSettable", boolean.class);
            if (m != null) {
                m.invoke(locator, settable);
            }
        } catch (Exception e) {
            // Ignore if locator does not support setting settable
        }
    }

    private DataPointVO findDataPoint(String idOrXid) {
        DataPointDao dao = new DataPointDao();
        if (idOrXid == null) {
            return null;
        }
        if (idOrXid.matches("-?\\d+")) {
            try {
                int id = Integer.parseInt(idOrXid);
                DataPointVO dp = dao.getDataPoint(id);
                if (dp != null) {
                    return dp;
                }
            } catch (NumberFormatException e) {
                // ignore and check by xid
            }
        }
        return dao.getDataPoint(idOrXid);
    }

    private Map<String, Object> mapEventDetector(PointEventDetectorVO ped) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", ped.getId());
        map.put("xid", ped.getXid());
        map.put("alias", ped.getAlias());
        map.put("detectorType", ped.getDetectorType());
        map.put("alarmLevel", ped.getAlarmLevel());
        map.put("limit", ped.getLimit());
        map.put("duration", ped.getDuration());
        map.put("durationType", ped.getDurationType());
        map.put("binaryState", ped.isBinaryState());
        map.put("multistateState", ped.getMultistateState());
        map.put("alphanumericState", ped.getAlphanumericState());
        map.put("changeCount", ped.getChangeCount());
        map.put("weight", ped.getWeight());
        if (ped.getDef() != null && Common.getBundle() != null) {
            map.put("detectorTypeDescription", LocalizableMessage.getMessage(Common.getBundle(), ped.getDef().getNameKey()));
        }
        return map;
    }

    private Map<String, Object> mapDataPointDetails(DataPointVO dp) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dp.getId());
        map.put("xid", dp.getXid());
        map.put("name", dp.getName());
        map.put("enabled", dp.isEnabled());
        map.put("dataSourceId", dp.getDataSourceId());
        map.put("dataSourceName", dp.getDataSourceName());
        map.put("dataSourceXid", dp.getDataSourceXid());
        map.put("dataSourceTypeId", dp.getDataSourceTypeId());
        map.put("deviceName", dp.getDeviceName());
        map.put("settable", dp.isSettable());
        map.put("defaultCacheSize", dp.getDefaultCacheSize());
        map.put("engineeringUnits", dp.getEngineeringUnits());
        map.put("chartColour", dp.getChartColour());
        map.put("comments", dp.getComments());

        PointLocatorVO locator = dp.getPointLocator();
        if (locator != null) {
            Map<String, Object> locatorMap = new HashMap<>();
            locatorMap.put("dataTypeId", locator.getDataTypeId());
            if (locator.getDataTypeMessage() != null && Common.getBundle() != null) {
                locatorMap.put("dataTypeDescription", locator.getDataTypeMessage().getLocalizedMessage(Common.getBundle()));
            }
            if (locator.getConfigurationDescription() != null && Common.getBundle() != null) {
                locatorMap.put("configurationDescription", locator.getConfigurationDescription().getLocalizedMessage(Common.getBundle()));
            }
            locatorMap.put("settable", locator.isSettable());
            locatorMap.put("relinquifiable", dp.isRelinquishable());

            if (locator instanceof com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO) {
                com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO vloc = (com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO) locator;
                locatorMap.put("changeTypeId", vloc.getChangeTypeId());
                try {
                    if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.ALTERNATE_BOOLEAN) {
                        locatorMap.put("startValue", vloc.getAlternateBooleanChange().getStartValue());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.BROWNIAN) {
                        locatorMap.put("startValue", vloc.getBrownianChange().getStartValue());
                        locatorMap.put("min", vloc.getBrownianChange().getMin());
                        locatorMap.put("max", vloc.getBrownianChange().getMax());
                        locatorMap.put("step", vloc.getBrownianChange().getMaxChange());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.INCREMENT_ANALOG) {
                        locatorMap.put("startValue", vloc.getIncrementAnalogChange().getStartValue());
                        locatorMap.put("min", vloc.getIncrementAnalogChange().getMin());
                        locatorMap.put("max", vloc.getIncrementAnalogChange().getMax());
                        locatorMap.put("step", vloc.getIncrementAnalogChange().getChange());
                        locatorMap.put("roll", vloc.getIncrementAnalogChange().isRoll());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.INCREMENT_MULTISTATE) {
                        locatorMap.put("startValue", vloc.getIncrementMultistateChange().getStartValue());
                        locatorMap.put("step", 1);
                        locatorMap.put("roll", vloc.getIncrementMultistateChange().isRoll());
                        locatorMap.put("values", vloc.getIncrementMultistateChange().getValues());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.NO_CHANGE) {
                        locatorMap.put("startValue", vloc.getNoChange().getStartValue());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.RANDOM_ANALOG) {
                        locatorMap.put("startValue", vloc.getRandomAnalogChange().getStartValue());
                        locatorMap.put("min", vloc.getRandomAnalogChange().getMin());
                        locatorMap.put("max", vloc.getRandomAnalogChange().getMax());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.RANDOM_BOOLEAN) {
                        locatorMap.put("startValue", vloc.getRandomBooleanChange().getStartValue());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.RANDOM_MULTISTATE) {
                        locatorMap.put("startValue", vloc.getRandomMultistateChange().getStartValue());
                        locatorMap.put("values", vloc.getRandomMultistateChange().getValues());
                    } else if (vloc.getChangeTypeId() == com.serotonin.mango.vo.dataSource.virtual.ChangeTypeVO.Types.ANALOG_ATTRACTOR) {
                        locatorMap.put("startValue", vloc.getAnalogAttractorChange().getStartValue());
                        locatorMap.put("step", vloc.getAnalogAttractorChange().getMaxChange());
                        locatorMap.put("volatility", vloc.getAnalogAttractorChange().getVolatility());
                        locatorMap.put("attractionPointId", vloc.getAnalogAttractorChange().getAttractionPointId());
                    }
                } catch (Exception e) {}
            } else if (locator instanceof com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO) {
                com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO mloc = (com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO) locator;
                locatorMap.put("script", mloc.getScript());
                locatorMap.put("updateEvent", mloc.getUpdateEvent());
                locatorMap.put("updateCronPattern", mloc.getUpdateCronPattern());
                locatorMap.put("executionDelaySeconds", mloc.getExecutionDelaySeconds());
                
                List<Map<String, Object>> ctxList = new ArrayList<>();
                DataPointDao dao = new DataPointDao();
                for (com.serotonin.db.IntValuePair pair : mloc.getContext()) {
                    Map<String, Object> ctxItem = new HashMap<>();
                    DataPointVO ctxDp = dao.getDataPoint(pair.getKey());
                    if (ctxDp != null) {
                        ctxItem.put("dataPointXid", ctxDp.getXid());
                    } else {
                        // fallback if point not found, but we should not send id as xid
                        ctxItem.put("dataPointId", pair.getKey()); 
                    }
                    ctxItem.put("varName", pair.getValue());
                    ctxList.add(ctxItem);
                }
                locatorMap.put("context", ctxList);
            } else {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(locator);
                    Map<String, Object> props = mapper.readValue(jsonStr, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                    locatorMap.putAll(props);
                } catch (Exception e) {
                    org.apache.commons.logging.LogFactory.getLog(DataPointEditAPI.class).error("Failed to serialize point locator properties for type " + locator.getClass().getSimpleName(), e);
                }
            }

            map.put("pointLocator", locatorMap);
        }

        Map<String, Object> loggingMap = new HashMap<>();
        loggingMap.put("loggingType", dp.getLoggingType());
        loggingMap.put("intervalLoggingPeriodType", dp.getIntervalLoggingPeriodType());
        loggingMap.put("intervalLoggingPeriod", dp.getIntervalLoggingPeriod());
        loggingMap.put("intervalLoggingType", dp.getIntervalLoggingType());
        loggingMap.put("tolerance", dp.getTolerance());
        loggingMap.put("discardExtremeValues", dp.isDiscardExtremeValues());
        loggingMap.put("discardLowLimit", dp.getDiscardLowLimit());
        loggingMap.put("discardHighLimit", dp.getDiscardHighLimit());
        map.put("logging", loggingMap);

        Map<String, Object> purgeMap = new HashMap<>();
        purgeMap.put("purgeType", dp.getPurgeType());
        purgeMap.put("purgePeriod", dp.getPurgePeriod());
        map.put("purge", purgeMap);

        if (dp.getTextRenderer() != null) {
            map.put("textRenderer", dp.getTextRenderer());
        }
        if (dp.getChartRenderer() != null) {
            map.put("chartRenderer", dp.getChartRenderer());
        }

        List<Map<String, Object>> detectorsList = new ArrayList<>();
        if (dp.getEventDetectors() != null) {
            for (PointEventDetectorVO ped : dp.getEventDetectors()) {
                detectorsList.add(mapEventDetector(ped));
            }
        }
        map.put("eventDetectors", detectorsList);

        return map;
    }

    @ApiOperation(value = "Get all accessible data points across data sources", response = List.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getAllDataPoints(
            @RequestParam(value = "dataSourceId", required = false) Integer dataSourceIdFilter,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointDao dao = new DataPointDao();
        List<DataPointVO> points;
        if (dataSourceIdFilter != null) {
            try {
                Permissions.ensureDataSourcePermission(user, dataSourceIdFilter);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            points = dao.getDataPoints(dataSourceIdFilter, DataPointNameComparator.instance);
        } else {
            points = dao.getDataPoints(DataPointNameComparator.instance, true);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (points != null) {
            for (DataPointVO dp : points) {
                if (hasPermission(user, dp)) {
                    result.add(mapDataPointDetails(dp));
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Create a new data point under a data source", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createDataPoint(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!payload.containsKey("dataSourceId") || payload.get("dataSourceId") == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Missing required field 'dataSourceId'");
            return ResponseEntity.badRequest().body(err);
        }

        int dataSourceId;
        try {
            dataSourceId = Integer.parseInt(payload.get("dataSourceId").toString());
        } catch (NumberFormatException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Invalid 'dataSourceId' format");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            Permissions.ensureDataSourcePermission(user, dataSourceId);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DataSourceVO<?> ds = new DataSourceDao().getDataSource(dataSourceId);
        if (ds == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Data source not found with ID: " + dataSourceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        String name = payload.get("name") != null ? payload.get("name").toString().trim() : "";
        if (name.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Data point name is required");
            return ResponseEntity.badRequest().body(err);
        }

        DataPointDao dpDao = new DataPointDao();
        String xid = payload.get("xid") != null ? payload.get("xid").toString().trim() : "";
        if (xid.isEmpty()) {
            xid = dpDao.generateUniqueXid();
        } else if (!dpDao.isXidUnique(xid, Common.NEW_ID)) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "XID already in use: " + xid);
            return ResponseEntity.badRequest().body(err);
        }

        DataPointVO dp = new DataPointVO();
        dp.setId(Common.NEW_ID);
        dp.setDataSourceId(ds.getId());
        dp.setName(name);
        dp.setXid(xid);
        dp.setEnabled(false);

        if (payload.containsKey("deviceName") && payload.get("deviceName") != null) {
            dp.setDeviceName(payload.get("deviceName").toString());
        } else {
            dp.setDeviceName(ds.getName());
        }

        if (payload.containsKey("settable")) {
            dp.setSettable(Boolean.TRUE.equals(payload.get("settable")));
        }

        if (payload.containsKey("engineeringUnits") && payload.get("engineeringUnits") instanceof Number) {
            dp.setEngineeringUnits(((Number) payload.get("engineeringUnits")).intValue());
        }

        PointLocatorVO locator = ds.createPointLocator();
        if (locator != null) {
            if (payload.containsKey("dataTypeId") && payload.get("dataTypeId") instanceof Number) {
                setLocatorDataTypeId(locator, ((Number) payload.get("dataTypeId")).intValue());
            }
            if (payload.containsKey("settable")) {
                boolean settable = Boolean.TRUE.equals(payload.get("settable"));
                dp.setSettable(settable);
                setLocatorSettable(locator, settable);
            }
            dp.setPointLocator(locator);
        }

        // Initialize default fields to prevent NullPointerException in RuntimeManager
        dp.setEventDetectors(new java.util.ArrayList<com.serotonin.mango.vo.event.PointEventDetectorVO>());
        dp.setComments(new java.util.ArrayList<com.serotonin.mango.vo.UserComment>());
        dp.defaultTextRenderer();

        updatePointLocatorFromPayload(dp, payload);

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(mapDataPointDetails(dp));
    }

    private void updatePointLocatorFromPayload(DataPointVO dp, Map<String, Object> payload) {
        if (payload.containsKey("pointLocator") && payload.get("pointLocator") instanceof Map) {
            Map<?, ?> locatorMap = (Map<?, ?>) payload.get("pointLocator");
            PointLocatorVO locator = dp.getPointLocator();
            if (locator instanceof com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO) {
                com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO vloc = (com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO) locator;
                if (locatorMap.containsKey("changeTypeId") && locatorMap.get("changeTypeId") instanceof Number) {
                    vloc.setChangeTypeId(((Number) locatorMap.get("changeTypeId")).intValue());
                }
                String startValue = locatorMap.containsKey("startValue") && locatorMap.get("startValue") != null ? locatorMap.get("startValue").toString() : "0";
                double min = locatorMap.containsKey("min") && locatorMap.get("min") instanceof Number ? ((Number) locatorMap.get("min")).doubleValue() : 0;
                double max = locatorMap.containsKey("max") && locatorMap.get("max") instanceof Number ? ((Number) locatorMap.get("max")).doubleValue() : 100;
                double step = locatorMap.containsKey("step") && locatorMap.get("step") instanceof Number ? ((Number) locatorMap.get("step")).doubleValue() : 1;
                boolean roll = locatorMap.containsKey("roll") && Boolean.TRUE.equals(locatorMap.get("roll"));
                double volatility = locatorMap.containsKey("volatility") && locatorMap.get("volatility") instanceof Number ? ((Number) locatorMap.get("volatility")).doubleValue() : 0;
                int attractionPointId = locatorMap.containsKey("attractionPointId") && locatorMap.get("attractionPointId") instanceof Number ? ((Number) locatorMap.get("attractionPointId")).intValue() : 0;
                
                int[] values = new int[0];
                if (locatorMap.containsKey("values") && locatorMap.get("values") instanceof List) {
                    List<?> list = (List<?>) locatorMap.get("values");
                    values = new int[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        values[i] = list.get(i) instanceof Number ? ((Number) list.get(i)).intValue() : 0;
                    }
                }
                
                vloc.getAlternateBooleanChange().setStartValue(startValue);
                vloc.getBrownianChange().setStartValue(startValue);
                vloc.getBrownianChange().setMin(min);
                vloc.getBrownianChange().setMax(max);
                vloc.getBrownianChange().setMaxChange(step);
                vloc.getIncrementAnalogChange().setStartValue(startValue);
                vloc.getIncrementAnalogChange().setMin(min);
                vloc.getIncrementAnalogChange().setMax(max);
                vloc.getIncrementAnalogChange().setChange(step);
                vloc.getIncrementAnalogChange().setRoll(roll);
                vloc.getIncrementMultistateChange().setStartValue(startValue);
                vloc.getIncrementMultistateChange().setRoll(roll);
                vloc.getIncrementMultistateChange().setValues(values);
                vloc.getNoChange().setStartValue(startValue);
                vloc.getRandomAnalogChange().setStartValue(startValue);
                vloc.getRandomAnalogChange().setMin(min);
                vloc.getRandomAnalogChange().setMax(max);
                vloc.getRandomBooleanChange().setStartValue(startValue);
                vloc.getRandomMultistateChange().setStartValue(startValue);
                vloc.getRandomMultistateChange().setValues(values);
                vloc.getAnalogAttractorChange().setStartValue(startValue);
                vloc.getAnalogAttractorChange().setMaxChange(step);
                vloc.getAnalogAttractorChange().setVolatility(volatility);
                vloc.getAnalogAttractorChange().setAttractionPointId(attractionPointId);
            } else if (locator instanceof com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO) {
                com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO mloc = (com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO) locator;
                if (locatorMap.containsKey("script")) {
                    mloc.setScript(locatorMap.get("script") != null ? locatorMap.get("script").toString() : "");
                }
                if (locatorMap.containsKey("updateEvent") && locatorMap.get("updateEvent") instanceof Number) {
                    mloc.setUpdateEvent(((Number) locatorMap.get("updateEvent")).intValue());
                }
                if (locatorMap.containsKey("updateCronPattern")) {
                    mloc.setUpdateCronPattern(locatorMap.get("updateCronPattern") != null ? locatorMap.get("updateCronPattern").toString() : "");
                }
                if (locatorMap.containsKey("executionDelaySeconds") && locatorMap.get("executionDelaySeconds") instanceof Number) {
                    mloc.setExecutionDelaySeconds(((Number) locatorMap.get("executionDelaySeconds")).intValue());
                }
                if (locatorMap.containsKey("context") && locatorMap.get("context") instanceof List) {
                    List<com.serotonin.db.IntValuePair> ctxList = new ArrayList<>();
                    for (Object obj : (List<?>) locatorMap.get("context")) {
                        if (obj instanceof Map) {
                            Map<?, ?> item = (Map<?, ?>) obj;
                            String xid = (String) item.get("dataPointXid");
                            String varName = (String) item.get("varName");
                            DataPointVO cdp = new DataPointDao().getDataPoint(xid);
                            if (cdp != null) {
                                ctxList.add(new com.serotonin.db.IntValuePair(cdp.getId(), varName));
                            }
                        }
                    }
                    mloc.setContext(ctxList);
                }
            } else if (locator != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String jsonStr = mapper.writeValueAsString(locatorMap);
                    mapper.readerForUpdating(locator).readValue(jsonStr);
                } catch (Exception e) {
                    org.apache.commons.logging.LogFactory.getLog(DataPointEditAPI.class).error("Failed to map point locator properties for type " + locator.getClass().getSimpleName(), e);
                }
            }
        }
    }


    @ApiOperation(value = "Get full configuration of a specific data point by numeric ID or XID", response = Object.class)
    @RequestMapping(value = "/{idOrXid}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getDataPointById(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mapDataPointDetails(dp));
    }

    @ApiOperation(value = "Update core settings and logging configuration of a data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}", method = { RequestMethod.PUT, RequestMethod.POST })
    public ResponseEntity<Map<String, Object>> updateDataPoint(@PathVariable("idOrXid") String idOrXid,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (payload.containsKey("name")) {
            dp.setName((String) payload.get("name"));
        }
        if (payload.containsKey("xid")) {
            String newXid = (String) payload.get("xid");
            if (!newXid.equals(dp.getXid()) && new DataPointDao().isXidUnique(newXid, dp.getId())) {
                dp.setXid(newXid);
            }
        }
        if (payload.containsKey("enabled")) {
            dp.setEnabled(Boolean.TRUE.equals(payload.get("enabled")));
        }
        if (payload.containsKey("defaultCacheSize") && payload.get("defaultCacheSize") instanceof Number) {
            dp.setDefaultCacheSize(((Number) payload.get("defaultCacheSize")).intValue());
        }
        if (payload.containsKey("engineeringUnits") && payload.get("engineeringUnits") instanceof Number) {
            dp.setEngineeringUnits(((Number) payload.get("engineeringUnits")).intValue());
        }
        if (payload.containsKey("chartColour")) {
            dp.setChartColour((String) payload.get("chartColour"));
        }

        if (payload.containsKey("logging") && payload.get("logging") instanceof Map) {
            Map<?, ?> logging = (Map<?, ?>) payload.get("logging");
            if (logging.containsKey("loggingType") && logging.get("loggingType") instanceof Number) {
                dp.setLoggingType(((Number) logging.get("loggingType")).intValue());
            }
            if (logging.containsKey("intervalLoggingPeriodType") && logging.get("intervalLoggingPeriodType") instanceof Number) {
                dp.setIntervalLoggingPeriodType(((Number) logging.get("intervalLoggingPeriodType")).intValue());
            }
            if (logging.containsKey("intervalLoggingPeriod") && logging.get("intervalLoggingPeriod") instanceof Number) {
                dp.setIntervalLoggingPeriod(((Number) logging.get("intervalLoggingPeriod")).intValue());
            }
            if (logging.containsKey("intervalLoggingType") && logging.get("intervalLoggingType") instanceof Number) {
                dp.setIntervalLoggingType(((Number) logging.get("intervalLoggingType")).intValue());
            }
            if (logging.containsKey("tolerance") && logging.get("tolerance") instanceof Number) {
                dp.setTolerance(((Number) logging.get("tolerance")).doubleValue());
            }
            if (logging.containsKey("discardExtremeValues")) {
                dp.setDiscardExtremeValues(Boolean.TRUE.equals(logging.get("discardExtremeValues")));
            }
            if (logging.containsKey("discardLowLimit") && logging.get("discardLowLimit") instanceof Number) {
                dp.setDiscardLowLimit(((Number) logging.get("discardLowLimit")).doubleValue());
            }
            if (logging.containsKey("discardHighLimit") && logging.get("discardHighLimit") instanceof Number) {
                dp.setDiscardHighLimit(((Number) logging.get("discardHighLimit")).doubleValue());
            }
        }

        if (payload.containsKey("purge") && payload.get("purge") instanceof Map) {
            Map<?, ?> purge = (Map<?, ?>) payload.get("purge");
            if (purge.containsKey("purgeType") && purge.get("purgeType") instanceof Number) {
                dp.setPurgeType(((Number) purge.get("purgeType")).intValue());
            }
            if (purge.containsKey("purgePeriod") && purge.get("purgePeriod") instanceof Number) {
                dp.setPurgePeriod(((Number) purge.get("purgePeriod")).intValue());
            }
        }

        if (payload.containsKey("deviceName") && payload.get("deviceName") != null) {
            dp.setDeviceName(payload.get("deviceName").toString());
        }
        if (payload.containsKey("settable")) {
            boolean settable = Boolean.TRUE.equals(payload.get("settable"));
            dp.setSettable(settable);
            setLocatorSettable(dp.getPointLocator(), settable);
        }
        if (payload.containsKey("dataTypeId") && payload.get("dataTypeId") instanceof Number) {
            setLocatorDataTypeId(dp.getPointLocator(), ((Number) payload.get("dataTypeId")).intValue());
        }

        updatePointLocatorFromPayload(dp, payload);

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }
        return ResponseEntity.ok(mapDataPointDetails(dp));
    }

    @ApiOperation(value = "Toggle enabled state of a data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/toggle", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> toggleDataPoint(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
        dp.setEnabled(!dp.isEnabled());
        runtimeManager.saveDataPoint(dp);

        Map<String, Object> result = new HashMap<>();
        result.put("id", dp.getId());
        result.put("xid", dp.getXid());
        result.put("enabled", dp.isEnabled());
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Explicitly enable or disable a data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/enable", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> enableDataPoint(@PathVariable("idOrXid") String idOrXid,
            @RequestParam("enabled") boolean enabled,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (dp.isEnabled() != enabled) {
            RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
            dp.setEnabled(enabled);
            runtimeManager.saveDataPoint(dp);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", dp.getId());
        result.put("xid", dp.getXid());
        result.put("enabled", dp.isEnabled());
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Restart a data point (disable then enable immediately)", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/restart", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> restartDataPoint(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RuntimeManager runtimeManager = Common.ctx.getRuntimeManager();
        dp.setEnabled(false);
        runtimeManager.saveDataPoint(dp);
        dp.setEnabled(true);
        runtimeManager.saveDataPoint(dp);

        Map<String, Object> result = new HashMap<>();
        result.put("id", dp.getId());
        result.put("xid", dp.getXid());
        result.put("enabled", dp.isEnabled());
        result.put("success", true);
        result.put("message", "Data point restarted successfully");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Copy a data point to the same or a target data source", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/copy", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> copyDataPoint(@PathVariable("idOrXid") String idOrXid,
            @RequestParam(value = "targetDataSourceId", required = false) Integer targetDataSourceId,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
            if (targetDataSourceId != null && targetDataSourceId != dp.getDataSourceId()) {
                Permissions.ensureDataSourcePermission(user, targetDataSourceId);
            }
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DataPointDao dataPointDao = new DataPointDao();
        DataPointVO copy = dp.copy();
        copy.setId(Common.NEW_ID);
        copy.setXid(dataPointDao.generateUniqueXid());
        ResourceBundle bundle = Common.getBundle();
        String prefix = bundle != null ? LocalizableMessage.getMessage(bundle, "common.copyPrefix", dp.getName()) : ("Copy of " + dp.getName());
        copy.setName(StringUtils.truncate(prefix, 40));
        if (targetDataSourceId != null) {
            copy.setDataSourceId(targetDataSourceId);
        }
        copy.setEnabled(false);
        if (copy.getComments() != null) {
            copy.getComments().clear();
        }

        if (copy.getEventDetectors() != null) {
            for (PointEventDetectorVO ped : copy.getEventDetectors()) {
                ped.setId(Common.NEW_ID);
                ped.njbSetDataPoint(copy);
            }
        }

        dataPointDao.saveDataPoint(copy);
        dataPointDao.copyPermissions(dp.getId(), copy.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("newPointId", copy.getId());
        result.put("newXid", copy.getXid());
        result.put("newName", copy.getName());
        result.put("message", "Data point copied successfully");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Permanently delete a data point and its history", response = Object.class)
    @RequestMapping(value = "/{idOrXid}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteDataPoint(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().deleteDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", dp.getId());
        result.put("xid", dp.getXid());
        result.put("message", "Data point deleted successfully");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Purge point values history for this data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/purge", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> purgeHistory(@PathVariable("idOrXid") String idOrXid,
            @RequestParam(value = "allData", required = false, defaultValue = "false") boolean allData,
            @RequestParam(value = "purgeType", required = false, defaultValue = "1") int purgeType,
            @RequestParam(value = "purgePeriod", required = false, defaultValue = "1") int purgePeriod,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        RuntimeManager rm = Common.ctx.getRuntimeManager();
        long count;
        if (allData) {
            count = rm.purgeDataPointValues(dp.getId());
        } else {
            count = rm.purgeDataPointValues(dp.getId(), purgeType, purgePeriod);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("purgedCount", count);
        result.put("message", "Purged " + count + " point values");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Clear runtime point cache values", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/clear-cache", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> clearCache(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DataPointRT rt = Common.ctx.getRuntimeManager().getDataPoint(dp.getId());
        if (rt != null) {
            rt.resetValues();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Point runtime cache reset");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Update text renderer of a data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/text-renderer", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setTextRenderer(@PathVariable("idOrXid") String idOrXid,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String type = (String) payload.get("type");
        if (type == null) {
            return ResponseEntity.badRequest().build();
        }

        TextRenderer renderer = null;
        if ("analog".equalsIgnoreCase(type)) {
            String format = (String) payload.get("format");
            String suffix = (String) payload.get("suffix");
            renderer = new AnalogRenderer(format != null ? format : "#.##", suffix != null ? suffix : "");
        } else if ("binary".equalsIgnoreCase(type)) {
            String zeroLabel = (String) payload.get("zeroLabel");
            String zeroColour = (String) payload.get("zeroColour");
            String oneLabel = (String) payload.get("oneLabel");
            String oneColour = (String) payload.get("oneColour");
            renderer = new BinaryTextRenderer(zeroLabel, zeroColour, oneLabel, oneColour);
        } else if ("multistate".equalsIgnoreCase(type)) {
            MultistateRenderer r = new MultistateRenderer();
            if (payload.get("values") instanceof List) {
                List<?> values = (List<?>) payload.get("values");
                for (Object item : values) {
                    if (item instanceof Map) {
                        Map<?, ?> m = (Map<?, ?>) item;
                        int key = m.get("key") instanceof Number ? ((Number) m.get("key")).intValue() : 0;
                        String text = (String) m.get("text");
                        String colour = (String) m.get("colour");
                        r.addMultistateValue(key, text, colour);
                    }
                }
            }
            renderer = r;
        } else if ("none".equalsIgnoreCase(type)) {
            renderer = new NoneRenderer();
        } else if ("plain".equalsIgnoreCase(type)) {
            String suffix = (String) payload.get("suffix");
            renderer = new PlainRenderer(suffix != null ? suffix : "");
        } else if ("range".equalsIgnoreCase(type)) {
            String format = (String) payload.get("format");
            RangeRenderer r = new RangeRenderer(format != null ? format : "#.##");
            if (payload.get("values") instanceof List) {
                List<?> values = (List<?>) payload.get("values");
                for (Object item : values) {
                    if (item instanceof Map) {
                        Map<?, ?> m = (Map<?, ?>) item;
                        double from = m.get("from") instanceof Number ? ((Number) m.get("from")).doubleValue() : 0;
                        double to = m.get("to") instanceof Number ? ((Number) m.get("to")).doubleValue() : 0;
                        String text = (String) m.get("text");
                        String colour = (String) m.get("colour");
                        r.addRangeValues(from, to, text, colour);
                    }
                }
            }
            renderer = r;
        } else if ("time".equalsIgnoreCase(type)) {
            String format = (String) payload.get("format");
            int exp = payload.get("conversionExponent") instanceof Number ? ((Number) payload.get("conversionExponent")).intValue() : 0;
            renderer = new TimeRenderer(format != null ? format : "", exp);
        }

        if (renderer != null) {
            dp.setTextRenderer(renderer);
            BackgroundContext.set(user);
            try {
                Common.ctx.getRuntimeManager().saveDataPoint(dp);
            } finally {
                BackgroundContext.remove();
            }
        }

        return ResponseEntity.ok(mapDataPointDetails(dp));
    }

    @ApiOperation(value = "Update chart renderer of a data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/chart-renderer", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setChartRenderer(@PathVariable("idOrXid") String idOrXid,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String type = (String) payload.get("type");
        if (type == null || "none".equalsIgnoreCase(type)) {
            dp.setChartRenderer(null);
        } else if ("table".equalsIgnoreCase(type)) {
            int limit = payload.get("limit") instanceof Number ? ((Number) payload.get("limit")).intValue() : 10;
            dp.setChartRenderer(new TableChartRenderer(limit));
        } else if ("image".equalsIgnoreCase(type)) {
            int timePeriod = payload.get("timePeriod") instanceof Number ? ((Number) payload.get("timePeriod")).intValue() : 1;
            int numberOfPeriods = payload.get("numberOfPeriods") instanceof Number ? ((Number) payload.get("numberOfPeriods")).intValue() : 1;
            dp.setChartRenderer(new ImageChartRenderer(timePeriod, numberOfPeriods));
        } else if ("statistics".equalsIgnoreCase(type)) {
            int timePeriod = payload.get("timePeriod") instanceof Number ? ((Number) payload.get("timePeriod")).intValue() : 1;
            int numberOfPeriods = payload.get("numberOfPeriods") instanceof Number ? ((Number) payload.get("numberOfPeriods")).intValue() : 1;
            boolean includeSum = Boolean.TRUE.equals(payload.get("includeSum"));
            dp.setChartRenderer(new StatisticsChartRenderer(timePeriod, numberOfPeriods, includeSum));
        } else if ("flipbook".equalsIgnoreCase(type)) {
            int limit = payload.get("limit") instanceof Number ? ((Number) payload.get("limit")).intValue() : 10;
            dp.setChartRenderer(new ImageFlipbookRenderer(limit));
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }
        return ResponseEntity.ok(mapDataPointDetails(dp));
    }

    @ApiOperation(value = "Get all event detectors for this data point", response = List.class)
    @RequestMapping(value = "/{idOrXid}/event-detectors", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getEventDetectors(@PathVariable("idOrXid") String idOrXid, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (dp.getEventDetectors() != null) {
            for (PointEventDetectorVO ped : dp.getEventDetectors()) {
                list.add(mapEventDetector(ped));
            }
        }
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Add a new event detector to this data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/event-detectors", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addEventDetector(@PathVariable("idOrXid") String idOrXid,
            @RequestParam("typeId") int typeId,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PointEventDetectorVO ped = new PointEventDetectorVO();
        ped.setXid(new DataPointDao().generateEventDetectorUniqueXid(dp.getId()));
        ped.setAlias("");
        ped.setDetectorType(typeId);

        if (typeId == PointEventDetectorVO.TYPE_STATE_CHANGE_COUNT) {
            ped.setChangeCount(2);
            ped.setDuration(1);
        } else if (typeId == PointEventDetectorVO.TYPE_NO_CHANGE || typeId == PointEventDetectorVO.TYPE_NO_UPDATE) {
            ped.setDuration(1);
        }

        int id = -1;
        synchronized (dp) {
            for (PointEventDetectorVO d : dp.getEventDetectors()) {
                if (d.getId() <= id) {
                    id = d.getId() - 1;
                }
            }
            ped.setId(id);
            ped.njbSetDataPoint(dp);
            dp.getEventDetectors().add(ped);
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }
        return ResponseEntity.ok(mapEventDetector(ped));
    }

    @ApiOperation(value = "Update an event detector on this data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/event-detectors/{pedId}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateEventDetector(@PathVariable("idOrXid") String idOrXid,
            @PathVariable("pedId") int pedId,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PointEventDetectorVO ped = null;
        for (PointEventDetectorVO d : dp.getEventDetectors()) {
            if (d.getId() == pedId) {
                ped = d;
                break;
            }
        }
        if (ped == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (payload.containsKey("xid")) {
            ped.setXid((String) payload.get("xid"));
        }
        if (payload.containsKey("alias")) {
            ped.setAlias((String) payload.get("alias"));
        }
        if (payload.containsKey("alarmLevel") && payload.get("alarmLevel") instanceof Number) {
            ped.setAlarmLevel(((Number) payload.get("alarmLevel")).intValue());
        }
        if (payload.containsKey("limit") && payload.get("limit") instanceof Number) {
            ped.setLimit(((Number) payload.get("limit")).doubleValue());
        }
        if (payload.containsKey("duration") && payload.get("duration") instanceof Number) {
            ped.setDuration(((Number) payload.get("duration")).intValue());
        }
        if (payload.containsKey("durationType") && payload.get("durationType") instanceof Number) {
            ped.setDurationType(((Number) payload.get("durationType")).intValue());
        }
        if (payload.containsKey("binaryState")) {
            ped.setBinaryState(Boolean.TRUE.equals(payload.get("binaryState")));
        }
        if (payload.containsKey("multistateState") && payload.get("multistateState") instanceof Number) {
            ped.setMultistateState(((Number) payload.get("multistateState")).intValue());
        }
        if (payload.containsKey("alphanumericState")) {
            ped.setAlphanumericState((String) payload.get("alphanumericState"));
        }
        if (payload.containsKey("changeCount") && payload.get("changeCount") instanceof Number) {
            ped.setChangeCount(((Number) payload.get("changeCount")).intValue());
        }
        if (payload.containsKey("weight") && payload.get("weight") instanceof Number) {
            ped.setWeight(((Number) payload.get("weight")).doubleValue());
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }
        return ResponseEntity.ok(mapEventDetector(ped));
    }

    @ApiOperation(value = "Delete an event detector from this data point", response = Object.class)
    @RequestMapping(value = "/{idOrXid}/event-detectors/{pedId}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteEventDetector(@PathVariable("idOrXid") String idOrXid,
            @PathVariable("pedId") int pedId,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointVO dp = findDataPoint(idOrXid);
        if (dp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            ensurePermission(user, dp);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PointEventDetectorVO ped = null;
        synchronized (dp) {
            for (PointEventDetectorVO d : dp.getEventDetectors()) {
                if (d.getId() == pedId) {
                    ped = d;
                    dp.getEventDetectors().remove(d);
                    break;
                }
            }
        }
        if (ped == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            Common.ctx.getRuntimeManager().saveDataPoint(dp);
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", pedId);
        result.put("message", "Event detector deleted successfully");
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Validate a meta point script", response = Object.class)
    @RequestMapping(value = "/validate-meta-script", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> validateMetaScript(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String script = payload.get("script") != null ? payload.get("script").toString() : "";
        int dataTypeId = payload.containsKey("dataTypeId") ? ((Number) payload.get("dataTypeId")).intValue() : 3;
        
        List<com.serotonin.db.IntValuePair> context = new ArrayList<>();
        if (payload.containsKey("context") && payload.get("context") instanceof List) {
            List<?> ctxList = (List<?>) payload.get("context");
            for (Object obj : ctxList) {
                if (obj instanceof Map) {
                    Map<?, ?> item = (Map<?, ?>) obj;
                    String xid = (String) item.get("dataPointXid");
                    String varName = (String) item.get("varName");
                    DataPointVO dp = new DataPointDao().getDataPoint(xid);
                    if (dp != null) {
                        context.add(new com.serotonin.db.IntValuePair(dp.getId(), varName));
                    }
                }
            }
        }

        com.serotonin.mango.web.dwr.DataSourceEditDwr dwr = new com.serotonin.mango.web.dwr.DataSourceEditDwr();
        com.serotonin.web.dwr.DwrResponseI18n validationResponse = dwr.validateScript(script, context, dataTypeId);

        Map<String, Object> result = new HashMap<>();
        
        // We always return HTTP success with the messages. The frontend will inspect the messages
        // to determine if the script validation passed or failed.
        result.put("success", true);
        if (validationResponse.getHasMessages()) {
            result.put("messages", validationResponse.getMessages());
        }
        
        return ResponseEntity.ok(result);
    }
}
