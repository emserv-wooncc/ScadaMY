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

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Watch Lists (/watch_list.shtm replacement).
 */
@RestController
@RequestMapping("/api/watch-list")
@Api(value = "Watch List API", tags = "Watch List Management")
public class WatchListAPI {
    private static final Log logger = LogFactory.getLog(WatchListAPI.class);

    @ApiOperation(value = "Get all accessible watch lists for the current user", response = List.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getWatchLists(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WatchListDao watchListDao = new WatchListDao();
        List<WatchList> watchLists = watchListDao.getWatchLists(user.getId(), user.getUserProfile());
        watchListDao.populateWatchlistData(watchLists);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WatchList wl : watchLists) {
            result.add(formatWatchList(wl, user));
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get the user's selected watch list ID", response = Map.class)
    @RequestMapping(value = "/selected", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSelectedWatchList(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Fetch fresh from DB so cross-machine updates reflect instantly on refresh
        User freshUser = new com.serotonin.mango.db.dao.UserDao().getUser(user.getId());
        if (freshUser != null) {
            user.setSelectedWatchList(freshUser.getSelectedWatchList());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("selectedWatchListId", user.getSelectedWatchList());
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update the user's selected watch list", response = Map.class)
    @RequestMapping(value = "/selected/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setSelectedWatchList(@PathVariable("id") int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WatchListDao watchListDao = new WatchListDao();
        WatchList wl = watchListDao.getWatchList(id);
        if (wl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        user.setSelectedWatchList(id);
        watchListDao.saveSelectedWatchList(user.getId(), id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("selectedWatchListId", id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get a specific watch list by ID", response = Map.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getWatchList(@PathVariable("id") int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WatchListDao watchListDao = new WatchListDao();
        WatchList wl = watchListDao.getWatchList(id);
        if (wl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureWatchListPermission(user, wl);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(formatWatchList(wl, user));
    }

    @ApiOperation(value = "Create a new watch list", response = Map.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createWatchList(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String name = payload.get("name") != null ? payload.get("name").toString().trim() : "New Watch List";
        if (name.isEmpty()) {
            name = "New Watch List";
        }

        WatchList wl = new WatchList();
        wl.setName(name);

        WatchListDao watchListDao = new WatchListDao();
        wl = watchListDao.createNewWatchList(wl, user.getId());
        watchListDao.saveWatchList(wl);

        logger.info("Created new watch list id=" + wl.getId() + ", name='" + wl.getName() + "' by user=" + user.getUsername());
        return ResponseEntity.ok(formatWatchList(wl, user));
    }

    @ApiOperation(value = "Update a watch list name or points", response = Map.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateWatchList(@PathVariable("id") int id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WatchListDao watchListDao = new WatchListDao();
        WatchList wl = watchListDao.getWatchList(id);
        if (wl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureWatchListEditPermission(user, wl);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (payload.containsKey("name") && payload.get("name") != null) {
            wl.setName(payload.get("name").toString().trim());
        }

        if (payload.containsKey("pointIds") && payload.get("pointIds") instanceof List) {
            List<?> pointIdObjects = (List<?>) payload.get("pointIds");
            List<DataPointVO> newPoints = new ArrayList<>();
            DataPointDao dataPointDao = new DataPointDao();
            for (Object pidObj : pointIdObjects) {
                try {
                    int pid = Integer.parseInt(pidObj.toString());
                    DataPointVO dp = dataPointDao.getDataPoint(pid);
                    if (dp != null && Permissions.hasDataPointReadPermission(user, dp)) {
                        newPoints.add(dp);
                    }
                } catch (Exception ex) {
                    logger.warn("Invalid point ID in watch list update: " + pidObj);
                }
            }
            wl.getPointList().clear();
            wl.getPointList().addAll(newPoints);
        }

        watchListDao.saveWatchList(wl);
        logger.info("Updated watch list id=" + wl.getId() + " by user=" + user.getUsername());
        return ResponseEntity.ok(formatWatchList(wl, user));
    }

    @ApiOperation(value = "Delete a watch list", response = Map.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteWatchList(@PathVariable("id") int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WatchListDao watchListDao = new WatchListDao();
        WatchList wl = watchListDao.getWatchList(id);
        if (wl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureWatchListEditPermission(user, wl);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        watchListDao.deleteWatchList(id);
        logger.info("Deleted watch list id=" + id + " by user=" + user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deletedId", id);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> formatWatchList(WatchList wl, User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", wl.getId());
        map.put("xid", wl.getXid());
        map.put("name", wl.getName());
        map.put("userId", wl.getUserId());

        List<Map<String, Object>> points = new ArrayList<>();
        if (wl.getPointList() != null) {
            for (DataPointVO dp : wl.getPointList()) {
                if (dp != null && Permissions.hasDataPointReadPermission(user, dp)) {
                    Map<String, Object> dpMap = new HashMap<>();
                    dpMap.put("id", dp.getId());
                    dpMap.put("xid", dp.getXid());
                    dpMap.put("name", dp.getName());
                    dpMap.put("extendedName", dp.getExtendedName());
                    dpMap.put("settable", dp.getPointLocator() != null && dp.getPointLocator().isSettable());
                    dpMap.put("dataTypeId", dp.getPointLocator() != null ? dp.getPointLocator().getDataTypeId() : 1);
                    String eu = "";
                    if (dp.getTextRenderer() != null && dp.getTextRenderer().getMetaText() != null && !dp.getTextRenderer().getMetaText().trim().isEmpty()) {
                        eu = dp.getTextRenderer().getMetaText();
                    } else if (dp.getEngineeringUnits() != com.serotonin.mango.vo.DataPointVO.ENGINEERING_UNITS_DEFAULT) {
                        eu = new com.serotonin.bacnet4j.type.enumerated.EngineeringUnits(dp.getEngineeringUnits()).toString();
                    }
                    dpMap.put("engineeringUnits", eu);

                    com.serotonin.mango.view.chart.ChartRenderer cr = dp.getChartRenderer();
                    if (cr != null) {
                        dpMap.put("chartRendererType", cr.getTypeName());
                        if (cr instanceof com.serotonin.mango.view.chart.TableChartRenderer) {
                            dpMap.put("chartLimit", ((com.serotonin.mango.view.chart.TableChartRenderer)cr).getLimit());
                        } else if (cr instanceof com.serotonin.mango.view.chart.TimePeriodChartRenderer) {
                            com.serotonin.mango.view.chart.TimePeriodChartRenderer tpcr = (com.serotonin.mango.view.chart.TimePeriodChartRenderer) cr;
                            dpMap.put("chartTimePeriod", tpcr.getTimePeriod());
                            dpMap.put("chartNumberOfPeriods", tpcr.getNumberOfPeriods());
                        }
                    } else {
                        dpMap.put("chartRendererType", "NONE");
                    }

                    points.add(dpMap);
                }
            }
        }
        map.put("points", points);
        return map;
    }
    @ApiOperation(value = "Get chart data for a data point based on its configured Chart Renderer", response = Map.class)
    @RequestMapping(value = "/point/{pointId}/chart-data", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getChartData(@PathVariable("pointId") int pointId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        com.serotonin.mango.db.dao.DataPointDao dataPointDao = new com.serotonin.mango.db.dao.DataPointDao();
        DataPointVO dp = dataPointDao.getDataPoint(pointId);
        if (dp == null) {
            return ResponseEntity.notFound().build();
        }

        com.serotonin.mango.view.chart.ChartRenderer cr = dp.getChartRenderer();
        Map<String, Object> response = new HashMap<>();
        response.put("pointId", pointId);

        if (cr == null) {
            response.put("type", "NONE");
            return ResponseEntity.ok(response);
        }

        com.serotonin.mango.db.dao.PointValueDao pointValueDao = new com.serotonin.mango.db.dao.PointValueDao();
        response.put("type", cr.getTypeName());

        if (cr instanceof com.serotonin.mango.view.chart.TableChartRenderer) {
            int limit = ((com.serotonin.mango.view.chart.TableChartRenderer) cr).getLimit();
            response.put("periodDescription", "LAST " + limit + " POINTS");
            List<com.serotonin.mango.rt.dataImage.PointValueTime> pvts = pointValueDao.getLatestPointValues(pointId, limit);
            List<Map<String, Object>> values = new ArrayList<>();
            for (com.serotonin.mango.rt.dataImage.PointValueTime pvt : pvts) {
                Map<String, Object> valMap = new HashMap<>();
                valMap.put("timestamp", pvt.getTime());
                valMap.put("value", pvt.getValue().getObjectValue() != null ? pvt.getValue().getObjectValue() : pvt.getValue().getStringValue());
                valMap.put("renderedValue", dp.getTextRenderer() != null ? dp.getTextRenderer().getText(pvt.getValue(), com.serotonin.mango.view.text.TextRenderer.HINT_FULL) : pvt.getValue().getStringValue());
                values.add(valMap);
            }
            response.put("data", values);
        } else if (cr instanceof com.serotonin.mango.view.chart.TimePeriodChartRenderer) {
            com.serotonin.mango.view.chart.TimePeriodChartRenderer tpcr = (com.serotonin.mango.view.chart.TimePeriodChartRenderer) cr;
            long from = tpcr.getStartTime();
            long to = System.currentTimeMillis();
            
            String periodName = com.serotonin.mango.Common.TIME_PERIOD_CODES.getCode(tpcr.getTimePeriod());
            response.put("periodDescription", "LAST " + tpcr.getNumberOfPeriods() + " " + (periodName != null ? periodName : ""));
            
            if (cr instanceof com.serotonin.mango.view.chart.StatisticsChartRenderer) {
                com.serotonin.mango.rt.dataImage.PointValueFacade facade = new com.serotonin.mango.rt.dataImage.PointValueFacade(pointId);
                List<com.serotonin.mango.rt.dataImage.PointValueTime> pvts = facade.getPointValues(from);
                
                com.serotonin.mango.rt.dataImage.PointValueTime startValue = null;
                if (pvts.size() == 0 || pvts.get(0).getTime() > from) {
                    com.serotonin.mango.rt.dataImage.PointValueTime before = facade.getPointValueBefore(from);
                    if (before != null) startValue = new com.serotonin.mango.rt.dataImage.PointValueTime(before.getValue(), from);
                }

                Map<String, Object> stats = new HashMap<>();
                int dataTypeId = dp.getPointLocator().getDataTypeId();
                
                if (startValue != null || pvts.size() > 0) {
                    if (dataTypeId == com.serotonin.mango.DataTypes.NUMERIC) {
                        com.serotonin.mango.view.stats.AnalogStatistics as = new com.serotonin.mango.view.stats.AnalogStatistics(startValue, pvts, from, from + tpcr.getDuration());
                        stats.put("minimum", as.getMinimum());
                        stats.put("minTime", as.getMinTime());
                        stats.put("maximum", as.getMaximum());
                        stats.put("maxTime", as.getMaxTime());
                        stats.put("average", as.getAverage());
                        stats.put("sum", as.getSum());
                        stats.put("count", as.getCount());
                    } else if (dataTypeId == com.serotonin.mango.DataTypes.BINARY || dataTypeId == com.serotonin.mango.DataTypes.MULTISTATE) {
                        com.serotonin.mango.view.stats.StartsAndRuntimeList sar = new com.serotonin.mango.view.stats.StartsAndRuntimeList(startValue, pvts, from, from + tpcr.getDuration());
                        stats.put("startsAndRuntimes", sar.getData());
                    }
                }
                response.put("stats", stats);
            } else {
                // ImageChartRenderer - just return raw points for frontend drawing
                List<com.serotonin.mango.rt.dataImage.PointValueTime> pvts = pointValueDao.getPointValuesBetween(pointId, from, to);
                List<Map<String, Object>> values = new ArrayList<>();
                for (com.serotonin.mango.rt.dataImage.PointValueTime pvt : pvts) {
                    Map<String, Object> valMap = new HashMap<>();
                    valMap.put("timestamp", pvt.getTime());
                    valMap.put("value", pvt.getValue().getObjectValue() != null ? pvt.getValue().getObjectValue() : pvt.getValue().getStringValue());
                    valMap.put("renderedValue", dp.getTextRenderer() != null ? dp.getTextRenderer().getText(pvt.getValue(), com.serotonin.mango.view.text.TextRenderer.HINT_FULL) : pvt.getValue().getStringValue());
                    values.add(valMap);
                }
                response.put("data", values);
            }
        }

        return ResponseEntity.ok(response);
    }
}
