package my.com.emserv.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.serotonin.InvalidArgumentException;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.MailingListDao;
import com.serotonin.mango.db.dao.ReportDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.maint.work.ReportWorkItem;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.vo.report.EventCsvStreamer;
import com.serotonin.mango.vo.report.ReportChartCreator;
import com.serotonin.mango.vo.report.ReportChartCreator.PointStatistics;
import com.serotonin.mango.vo.report.ReportCsvStreamer;
import com.serotonin.mango.vo.report.ReportInstance;
import com.serotonin.mango.vo.report.ReportJob;
import com.serotonin.mango.vo.report.ReportPointVO;
import com.serotonin.mango.vo.report.ReportUserComment;
import com.serotonin.mango.vo.report.ReportVO;
import com.serotonin.mango.vo.report.UserCommentCsvStreamer;
import com.serotonin.mango.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.util.ColorUtils;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Reports and Report Instances (/reports.shtm).
 * All endpoints enforce user report access permissions.
 */
@RestController
@RequestMapping("/api/reports")
@Api(value = "Reports API", tags = "Reports Management")
public class ReportsAPI {
    private static final Log logger = LogFactory.getLog(ReportsAPI.class);

    @ApiOperation(value = "Get report initialization context data including templates, instances, readable points, mailing lists, and users", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        List<ReportVO> reports = reportDao.getReports(user.getId());
        List<Map<String, Object>> reportsList = new ArrayList<>();
        if (reports != null) {
            for (ReportVO report : reports) {
                reportsList.add(mapReportToMap(report));
            }
        }

        List<ReportInstance> instances = reportDao.getReportInstances(user.getId());
        ResourceBundle bundle = Common.getBundle();
        List<Map<String, Object>> instancesList = new ArrayList<>();
        if (instances != null) {
            for (ReportInstance i : instances) {
                i.setBundle(bundle);
                instancesList.add(mapReportInstanceToMap(i));
            }
        }

        // Readable points
        List<DataPointVO> allPoints = new DataPointDao().getDataPoints(DataPointExtendedNameComparator.instance, false);
        List<Map<String, Object>> pointsList = new ArrayList<>();
        if (allPoints != null) {
            for (DataPointVO dp : allPoints) {
                if (Permissions.hasDataPointReadPermission(user, dp)) {
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("id", dp.getId());
                    pMap.put("xid", dp.getXid());
                    pMap.put("name", dp.getName());
                    pMap.put("deviceName", dp.getDeviceName());
                    pMap.put("extendedName", dp.getExtendedName());
                    pMap.put("dataTypeId", dp.getPointLocator().getDataTypeId());
                    pMap.put("dataTypeMessage", dp.getDataTypeMessage() != null ? dp.getDataTypeMessage().getKey() : null);
                    pointsList.add(pMap);
                }
            }
        }

        // Mailing lists
        List<MailingList> mailingLists = new MailingListDao().getMailingLists();
        List<Map<String, Object>> mailingListsData = new ArrayList<>();
        if (mailingLists != null) {
            for (MailingList ml : mailingLists) {
                Map<String, Object> mlMap = new HashMap<>();
                mlMap.put("id", ml.getId());
                mlMap.put("xid", ml.getXid());
                mlMap.put("name", ml.getName());
                mailingListsData.add(mlMap);
            }
        }

        // Users
        List<User> users = new UserDao().getUsers();
        List<Map<String, Object>> usersData = new ArrayList<>();
        if (users != null) {
            for (User u : users) {
                if (!u.isDisabled()) {
                    Map<String, Object> uMap = new HashMap<>();
                    uMap.put("id", u.getId());
                    uMap.put("username", u.getUsername());
                    uMap.put("email", u.getEmail());
                    uMap.put("admin", u.isAdmin());
                    usersData.add(uMap);
                }
            }
        }

        // Watchlists
        WatchListDao watchListDao = new WatchListDao();
        List<WatchList> watchlists = user.isAdmin() ? watchListDao.getWatchLists() : watchListDao.getWatchLists(user.getId(), user.getUserProfile());
        List<Map<String, Object>> watchlistsData = new ArrayList<>();
        if (watchlists != null) {
            watchListDao.populateWatchlistData(watchlists);
            for (WatchList wl : watchlists) {
                Map<String, Object> wlMap = new HashMap<>();
                wlMap.put("id", wl.getId());
                wlMap.put("name", wl.getName());
                List<Integer> ptIds = new ArrayList<>();
                if (wl.getPointList() != null) {
                    for (DataPointVO dp : wl.getPointList()) {
                        ptIds.add(dp.getId());
                    }
                }
                wlMap.put("pointIds", ptIds);
                watchlistsData.add(wlMap);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reports", reportsList);
        result.put("instances", instancesList);
        result.put("points", pointsList);
        result.put("mailingLists", mailingListsData);
        result.put("users", usersData);
        result.put("watchlists", watchlistsData);

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get all reports accessible by the current user along with report instances", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getReports(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        List<ReportVO> reports = reportDao.getReports(user.getId());
        List<Map<String, Object>> reportsList = new ArrayList<>();
        if (reports != null) {
            for (ReportVO report : reports) {
                reportsList.add(mapReportToMap(report));
            }
        }

        List<ReportInstance> instances = reportDao.getReportInstances(user.getId());
        ResourceBundle bundle = Common.getBundle();
        List<Map<String, Object>> instancesList = new ArrayList<>();
        if (instances != null) {
            for (ReportInstance i : instances) {
                i.setBundle(bundle);
                instancesList.add(mapReportInstanceToMap(i));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reports", reportsList);
        result.put("instances", instancesList);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get a specific report template by ID (or use -1 for a new report template)", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable int id,
            @RequestParam(value = "copy", required = false, defaultValue = "false") boolean copy,
            HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportVO report;
        if (id == Common.NEW_ID) {
            report = new ReportVO();
            report.setName(Common.getMessage("common.newName"));
        } else {
            ReportDao reportDao = new ReportDao();
            report = reportDao.getReport(id);
            if (report == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            try {
                Permissions.ensureReportPermission(user, report);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (copy) {
                report.setId(Common.NEW_ID);
                report.setName(LocalizableMessage.getMessage(Common.getBundle(), "common.copyPrefix", report.getName()));
            }
        }

        return ResponseEntity.ok(mapReportToMap(report));
    }

    @ApiOperation(value = "Create or update a report template", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveReport(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int id = Common.NEW_ID;
        if (body.get("id") instanceof Number) {
            id = ((Number) body.get("id")).intValue();
        } else if (body.get("id") != null) {
            try {
                id = Integer.parseInt(body.get("id").toString());
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse report ID from body: " + body.get("id"), e);
            }
        }

        return parseAndSaveReport(id, body, user);
    }

    @ApiOperation(value = "Update a report template by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateReport(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportVO existing = reportDao.getReport(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportPermission(user, existing);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return parseAndSaveReport(id, body, user);
    }

    @ApiOperation(value = "Delete a report template by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportVO report = reportDao.getReport(id);
        if (report == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportPermission(user, report);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BackgroundContext.set(user);
        try {
            ReportJob.unscheduleReportJob(report);
            reportDao.deleteReport(id);
        } finally {
            BackgroundContext.remove();
        }
        logger.info("Report deleted and unscheduled: ID=" + id + ", by user=" + user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Run a report template immediately by ID", response = Object.class)
    @RequestMapping(value = "/{id}/run", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> runReportById(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportVO report = reportDao.getReport(id);
        if (report == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportPermission(user, report);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BackgroundContext.set(user);
        try {
            // Sanitize period types in case the report was saved with legacy or invalid period types
            boolean modified = false;
            int prevPt = normalizePeriodType(report.getPreviousPeriodType());
            if (prevPt != report.getPreviousPeriodType()) {
                report.setPreviousPeriodType(prevPt);
                modified = true;
            }
            int pastPt = normalizePeriodType(report.getPastPeriodType());
            if (pastPt != report.getPastPeriodType()) {
                report.setPastPeriodType(pastPt);
                modified = true;
            }
            int schedPt = normalizeSchedulePeriod(report.getSchedulePeriod());
            if (schedPt != report.getSchedulePeriod()) {
                report.setSchedulePeriod(schedPt);
                modified = true;
            }
            if (modified) {
                reportDao.saveReport(report);
            }

            ReportWorkItem.queueReport(report);
            logger.info("Report execution queued successfully: ID=" + id + ", by user=" + user.getUsername());
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Report execution queued successfully");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Run an ad-hoc report immediately from provided configuration without saving", response = Object.class)
    @RequestMapping(value = "/run-ad-hoc", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> runAdHocReport(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportVO report = new ReportVO();
        report.setId(Common.NEW_ID);
        report.setUserId(user.getId());

        List<String> validationErrors = parseReportFromMap(body, report, user);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("errors", validationErrors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        BackgroundContext.set(user);
        try {
            ReportWorkItem.queueReport(report);
            logger.info("Ad-hoc report execution queued successfully by user: " + user.getUsername());
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ad-hoc report execution queued successfully");
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Create a report template from a WatchList", response = Object.class)
    @RequestMapping(value = "/from-watchlist/{watchListId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createFromWatchlist(@PathVariable int watchListId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WatchList watchList = new WatchListDao().getWatchList(watchListId);
        if (watchList == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureWatchListPermission(user, watchList);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReportVO report = new ReportVO();
        report.setName(LocalizableMessage.getMessage(Common.getBundle(), "common.copyPrefix", watchList.getName()));
        report.setUserId(user.getId());
        for (DataPointVO dp : watchList.getPointList()) {
            ReportPointVO rp = new ReportPointVO();
            rp.setPointId(dp.getId());
            rp.setColour(dp.getChartColour());
            rp.setConsolidatedChart(true);
            report.getPoints().add(rp);
        }

        return ResponseEntity.ok(mapReportToMap(report));
    }

    @ApiOperation(value = "Get all report instances for current user", response = Object.class)
    @RequestMapping(value = "/instances", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getInstances(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ReportInstance> instances = new ReportDao().getReportInstances(user.getId());
        ResourceBundle bundle = Common.getBundle();
        List<Map<String, Object>> result = new ArrayList<>();
        if (instances != null) {
            for (ReportInstance i : instances) {
                i.setBundle(bundle);
                result.add(mapReportInstanceToMap(i));
            }
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Delete a report instance by ID", response = Object.class)
    @RequestMapping(value = "/instances/{instanceId}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteInstance(@PathVariable int instanceId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance != null) {
            try {
                Permissions.ensureReportInstancePermission(user, instance);
                BackgroundContext.set(user);
                try {
                    reportDao.deleteReportInstance(instanceId, user.getId());
                } finally {
                    BackgroundContext.remove();
                }
                logger.info("Report instance deleted: ID=" + instanceId + ", by user=" + user.getUsername());
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("instanceId", instanceId);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Set prevent purge flag on a report instance", response = Object.class)
    @RequestMapping(value = "/instances/{instanceId}/prevent-purge", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setPreventPurge(@PathVariable int instanceId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean preventPurge = false;
        if (body != null) {
            if (body.get("preventPurge") instanceof Boolean) {
                preventPurge = (Boolean) body.get("preventPurge");
            } else if (body.get("preventPurge") != null) {
                preventPurge = Boolean.parseBoolean(body.get("preventPurge").toString());
            }
        } else if (request.getParameter("preventPurge") != null) {
            preventPurge = Boolean.parseBoolean(request.getParameter("preventPurge"));
        }

        BackgroundContext.set(user);
        try {
            reportDao.setReportInstancePreventPurge(instanceId, preventPurge, user.getId());
        } finally {
            BackgroundContext.remove();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("instanceId", instanceId);
        response.put("preventPurge", preventPurge);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get generated chart HTML for a completed report instance", response = Object.class)
    @RequestMapping(value = "/instances/{instanceId}/chart", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInstanceChart(@PathVariable int instanceId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReportChartCreator creator = new ReportChartCreator(Common.getBundle());
        creator.createContent(instance, reportDao, null, false);

        Map<String, byte[]> imageData = new HashMap<>();
        imageData.put(creator.getChartName(), creator.getImageData());
        for (PointStatistics pointStatistics : creator.getPointStatistics()) {
            imageData.put(pointStatistics.getChartName(), pointStatistics.getImageData());
        }
        user.setReportImageData(imageData);

        Map<String, Object> response = new HashMap<>();
        response.put("instanceId", instanceId);
        response.put("chartHtml", creator.getHtml());
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get events associated with a report instance", response = Object.class)
    @RequestMapping(value = "/instances/{instanceId}/events", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getInstanceEvents(@PathVariable int instanceId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<EventInstance> events = reportDao.getReportInstanceEvents(instanceId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (events != null) {
            for (EventInstance e : events) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                if (e.getEventType() != null) {
                    map.put("typeId", e.getEventType().getEventSourceId());
                }
                map.put("activeTimestamp", e.getActiveTimestamp());
                map.put("alarmLevel", e.getAlarmLevel());
                map.put("message", e.getMessage() != null ? e.getMessage().getLocalizedMessage(Common.getBundle()) : "");
                map.put("acknowledged", e.isAcknowledged());
                if (e.isAcknowledged()) {
                    map.put("ackTimestamp", e.getAcknowledgedTimestamp());
                    map.put("ackUserId", e.getAcknowledgedByUserId());
                }
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Get user comments associated with a report instance", response = Object.class)
    @RequestMapping(value = "/instances/{instanceId}/comments", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getInstanceComments(@PathVariable int instanceId, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ReportUserComment> comments = reportDao.getReportInstanceUserComments(instanceId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (comments != null) {
            for (ReportUserComment c : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("username", c.getUsername());
                map.put("commentType", c.getCommentType());
                map.put("typeKey", c.getTypeKey());
                map.put("pointName", c.getPointName());
                map.put("timestamp", c.getTs());
                map.put("comment", c.getComment());
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Export report data as CSV stream")
    @RequestMapping(value = "/instances/{instanceId}/export/data", method = RequestMethod.GET)
    public void exportDataCsv(@PathVariable int instanceId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Common.getUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"report_data_" + instanceId + ".csv\"");
        ReportCsvStreamer creator = new ReportCsvStreamer(response.getWriter(), Common.getBundle());
        reportDao.reportInstanceData(instanceId, creator);
    }

    @ApiOperation(value = "Export report events as CSV stream")
    @RequestMapping(value = "/instances/{instanceId}/export/events", method = RequestMethod.GET)
    public void exportEventsCsv(@PathVariable int instanceId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Common.getUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"report_events_" + instanceId + ".csv\"");
        new EventCsvStreamer(response.getWriter(), reportDao.getReportInstanceEvents(instanceId), Common.getBundle());
    }

    @ApiOperation(value = "Export report user comments as CSV stream")
    @RequestMapping(value = "/instances/{instanceId}/export/comments", method = RequestMethod.GET)
    public void exportCommentsCsv(@PathVariable int instanceId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Common.getUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        ReportDao reportDao = new ReportDao();
        ReportInstance instance = reportDao.getReportInstance(instanceId);
        if (instance == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Permissions.ensureReportInstancePermission(user, instance);
        } catch (PermissionException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"report_comments_" + instanceId + ".csv\"");
        new UserCommentCsvStreamer(response.getWriter(), reportDao.getReportInstanceUserComments(instanceId), Common.getBundle());
    }

    private ResponseEntity<Map<String, Object>> parseAndSaveReport(int id, Map<String, Object> body, User user) {
        ReportDao reportDao = new ReportDao();
        ReportVO report;
        if (id == Common.NEW_ID) {
            report = new ReportVO();
            report.setId(Common.NEW_ID);
            report.setUserId(user.getId());
        } else {
            report = reportDao.getReport(id);
            if (report == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            try {
                Permissions.ensureReportPermission(user, report);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<String> validationErrors = parseReportFromMap(body, report, user);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("errors", validationErrors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        BackgroundContext.set(user);
        try {
            reportDao.saveReport(report);
            ReportJob.scheduleReportJob(report);
        } finally {
            BackgroundContext.remove();
        }
        logger.info("Report saved and scheduled successfully: ID=" + report.getId() + ", name=" + report.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("report", mapReportToMap(report));
        return ResponseEntity.ok(response);
    }

    private static int normalizePeriodType(int periodType) {
        switch (periodType) {
            case 8: // sent as Days by old frontend UI
                return Common.TimePeriods.DAYS;
            case 9: // sent as Weeks by old frontend UI
                return Common.TimePeriods.WEEKS;
            case 10: // sent as Months by old frontend UI
                return Common.TimePeriods.MONTHS;
            case 11: // sent as Years by old frontend UI
                return Common.TimePeriods.YEARS;
            case Common.TimePeriods.SECONDS:
            case Common.TimePeriods.MINUTES:
            case Common.TimePeriods.HOURS:
            case Common.TimePeriods.DAYS:
            case Common.TimePeriods.WEEKS:
            case Common.TimePeriods.MONTHS:
            case Common.TimePeriods.YEARS:
                return periodType;
            default:
                return Common.TimePeriods.DAYS;
        }
    }

    private static int normalizeSchedulePeriod(int periodType) {
        switch (periodType) {
            case 8: // sent as Daily by old frontend UI
                return Common.TimePeriods.DAYS;
            case 9: // sent as Weekly by old frontend UI
                return Common.TimePeriods.WEEKS;
            case 10: // sent as Monthly by old frontend UI
                return Common.TimePeriods.MONTHS;
            case 11: // sent as Yearly by old frontend UI
                return Common.TimePeriods.YEARS;
            case ReportVO.SCHEDULE_CRON:
            case Common.TimePeriods.HOURS:
            case Common.TimePeriods.DAYS:
            case Common.TimePeriods.WEEKS:
            case Common.TimePeriods.MONTHS:
            case Common.TimePeriods.YEARS:
                return periodType;
            default:
                return Common.TimePeriods.DAYS;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseReportFromMap(Map<String, Object> body, ReportVO report, User user) {
        List<String> errors = new ArrayList<>();

        if (body.get("name") != null) {
            String name = body.get("name").toString().trim();
            if (name.isEmpty()) {
                errors.add("reports.validate.required");
            } else if (name.length() > 100) {
                errors.add("reports.validate.longerThan100");
            } else {
                report.setName(name);
            }
        } else if (report.getName() == null || report.getName().isEmpty()) {
            errors.add("reports.validate.required");
        }

        if (body.get("includeEvents") instanceof Number) {
            int incEv = ((Number) body.get("includeEvents")).intValue();
            if (incEv == 0) {
                incEv = ReportVO.EVENTS_NONE;
            }
            report.setIncludeEvents(incEv);
        }
        if (body.get("includeUserComments") instanceof Boolean) {
            report.setIncludeUserComments((Boolean) body.get("includeUserComments"));
        }
        if (body.get("dateRangeType") instanceof Number) {
            int drt = ((Number) body.get("dateRangeType")).intValue();
            if (drt != ReportVO.DATE_RANGE_TYPE_RELATIVE && drt != ReportVO.DATE_RANGE_TYPE_SPECIFIC) {
                errors.add("reports.validate.invalidDateRangeType");
            } else {
                report.setDateRangeType(drt);
            }
        }
        if (body.get("relativeDateType") instanceof Number) {
            int rdt = ((Number) body.get("relativeDateType")).intValue();
            if (rdt != ReportVO.RELATIVE_DATE_TYPE_PAST && rdt != ReportVO.RELATIVE_DATE_TYPE_PREVIOUS) {
                errors.add("reports.validate.invalidRelativeDateType");
            } else {
                report.setRelativeDateType(rdt);
            }
        }

        if (body.get("previousPeriodCount") instanceof Number) {
            int count = ((Number) body.get("previousPeriodCount")).intValue();
            if (count < 1) {
                errors.add("reports.validate.periodCountLessThan1");
            } else {
                report.setPreviousPeriodCount(count);
            }
        }
        if (body.get("previousPeriodType") instanceof Number) {
            report.setPreviousPeriodType(normalizePeriodType(((Number) body.get("previousPeriodType")).intValue()));
        }
        if (body.get("pastPeriodCount") instanceof Number) {
            int count = ((Number) body.get("pastPeriodCount")).intValue();
            if (count < 1) {
                errors.add("reports.validate.periodCountLessThan1");
            } else {
                report.setPastPeriodCount(count);
            }
        }
        if (body.get("pastPeriodType") instanceof Number) {
            report.setPastPeriodType(normalizePeriodType(((Number) body.get("pastPeriodType")).intValue()));
        }

        if (body.get("fromNone") instanceof Boolean) report.setFromNone((Boolean) body.get("fromNone"));
        if (body.get("fromYear") instanceof Number) report.setFromYear(((Number) body.get("fromYear")).intValue());
        if (body.get("fromMonth") instanceof Number) report.setFromMonth(((Number) body.get("fromMonth")).intValue());
        if (body.get("fromDay") instanceof Number) report.setFromDay(((Number) body.get("fromDay")).intValue());
        if (body.get("fromHour") instanceof Number) report.setFromHour(((Number) body.get("fromHour")).intValue());
        if (body.get("fromMinute") instanceof Number) report.setFromMinute(((Number) body.get("fromMinute")).intValue());

        if (body.get("toNone") instanceof Boolean) report.setToNone((Boolean) body.get("toNone"));
        if (body.get("toYear") instanceof Number) report.setToYear(((Number) body.get("toYear")).intValue());
        if (body.get("toMonth") instanceof Number) report.setToMonth(((Number) body.get("toMonth")).intValue());
        if (body.get("toDay") instanceof Number) report.setToDay(((Number) body.get("toDay")).intValue());
        if (body.get("toHour") instanceof Number) report.setToHour(((Number) body.get("toHour")).intValue());
        if (body.get("toMinute") instanceof Number) report.setToMinute(((Number) body.get("toMinute")).intValue());

        if (body.get("schedule") instanceof Boolean) report.setSchedule((Boolean) body.get("schedule"));
        if (body.get("schedulePeriod") instanceof Number) {
            int sp = normalizeSchedulePeriod(((Number) body.get("schedulePeriod")).intValue());
            report.setSchedulePeriod(sp);
        }
        if (body.get("runDelayMinutes") instanceof Number) {
            report.setRunDelayMinutes(((Number) body.get("runDelayMinutes")).intValue());
        }
        if (body.get("scheduleCron") != null) {
            String cron = body.get("scheduleCron").toString().trim();
            report.setScheduleCron(cron);
            if (report.isSchedule() && report.getSchedulePeriod() == ReportVO.SCHEDULE_CRON) {
                try {
                    new CronTimerTrigger(cron);
                } catch (Exception e) {
                    logger.warn("Invalid cron expression for report: " + cron, e);
                    errors.add("reports.validate.cron");
                }
            }
        }

        if (body.get("email") instanceof Boolean) report.setEmail((Boolean) body.get("email"));
        if (body.get("includeData") instanceof Boolean) report.setIncludeData((Boolean) body.get("includeData"));
        if (body.get("zipData") instanceof Boolean) report.setZipData((Boolean) body.get("zipData"));

        if (body.get("points") instanceof List) {
            List<Map<String, Object>> pointsList = (List<Map<String, Object>>) body.get("points");
            List<ReportPointVO> newPoints = new ArrayList<>();
            DataPointDao dataPointDao = new DataPointDao();
            for (Map<String, Object> ptMap : pointsList) {
                int pointId = ptMap.get("pointId") instanceof Number ? ((Number) ptMap.get("pointId")).intValue() : 0;
                if (pointId > 0) {
                    DataPointVO dp = dataPointDao.getDataPoint(pointId);
                    if (dp == null) {
                        errors.add("Invalid pointId: " + pointId);
                        continue;
                    }
                    try {
                        Permissions.ensureDataPointReadPermission(user, dp);
                    } catch (PermissionException e) {
                        logger.warn("User " + user.getUsername() + " lacks read permission for report point: " + dp.getName());
                        errors.add("No read permission for point: " + dp.getName());
                        continue;
                    }

                    ReportPointVO rp = new ReportPointVO();
                    rp.setPointId(pointId);
                    String colour = ptMap.get("colour") != null ? ptMap.get("colour").toString() : "";
                    if (!StringUtils.isEmpty(colour)) {
                        try {
                            ColorUtils.toColor(colour);
                        } catch (InvalidArgumentException e) {
                            logger.warn("Invalid colour format specified for report point: " + colour, e);
                            errors.add("reports.validate.colour: " + colour);
                        }
                    }
                    rp.setColour(colour);
                    if (ptMap.get("consolidatedChart") instanceof Boolean) {
                        rp.setConsolidatedChart((Boolean) ptMap.get("consolidatedChart"));
                    } else {
                        rp.setConsolidatedChart(true);
                    }
                    newPoints.add(rp);
                }
            }
            if (newPoints.isEmpty()) {
                errors.add("reports.validate.needPoint");
            } else {
                report.setPoints(newPoints);
            }
        } else if (report.getPoints() == null || report.getPoints().isEmpty()) {
            errors.add("reports.validate.needPoint");
        }

        if (body.get("recipients") instanceof List) {
            List<Map<String, Object>> recList = (List<Map<String, Object>>) body.get("recipients");
            List<RecipientListEntryBean> newRecipients = new ArrayList<>();
            for (Map<String, Object> rMap : recList) {
                RecipientListEntryBean bean = new RecipientListEntryBean();
                if (rMap.get("recipientType") instanceof Number) {
                    bean.setRecipientType(((Number) rMap.get("recipientType")).intValue());
                }
                if (rMap.get("referenceId") instanceof Number) {
                    bean.setReferenceId(((Number) rMap.get("referenceId")).intValue());
                }
                if (rMap.get("referenceAddress") != null) {
                    bean.setReferenceAddress(rMap.get("referenceAddress").toString());
                }
                newRecipients.add(bean);
            }
            report.setRecipients(newRecipients);
        }

        return errors;
    }

    private Map<String, Object> mapReportToMap(ReportVO report) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", report.getId());
        map.put("userId", report.getUserId());
        map.put("name", report.getName());
        map.put("includeEvents", report.getIncludeEvents());
        map.put("includeUserComments", report.isIncludeUserComments());
        map.put("dateRangeType", report.getDateRangeType());
        map.put("relativeDateType", report.getRelativeDateType());
        map.put("previousPeriodCount", report.getPreviousPeriodCount());
        map.put("previousPeriodType", report.getPreviousPeriodType());
        map.put("pastPeriodCount", report.getPastPeriodCount());
        map.put("pastPeriodType", report.getPastPeriodType());
        map.put("fromNone", report.isFromNone());
        map.put("fromYear", report.getFromYear());
        map.put("fromMonth", report.getFromMonth());
        map.put("fromDay", report.getFromDay());
        map.put("fromHour", report.getFromHour());
        map.put("fromMinute", report.getFromMinute());
        map.put("toNone", report.isToNone());
        map.put("toYear", report.getToYear());
        map.put("toMonth", report.getToMonth());
        map.put("toDay", report.getToDay());
        map.put("toHour", report.getToHour());
        map.put("toMinute", report.getToMinute());
        map.put("schedule", report.isSchedule());
        map.put("schedulePeriod", report.getSchedulePeriod());
        map.put("runDelayMinutes", report.getRunDelayMinutes());
        map.put("scheduleCron", report.getScheduleCron());
        map.put("email", report.isEmail());
        map.put("includeData", report.isIncludeData());
        map.put("zipData", report.isZipData());

        DataPointDao dataPointDao = new DataPointDao();
        List<Map<String, Object>> pointsList = new ArrayList<>();
        if (report.getPoints() != null) {
            for (ReportPointVO rp : report.getPoints()) {
                Map<String, Object> ptMap = new HashMap<>();
                ptMap.put("pointId", rp.getPointId());
                ptMap.put("colour", rp.getColour());
                ptMap.put("consolidatedChart", rp.isConsolidatedChart());
                DataPointVO dp = dataPointDao.getDataPoint(rp.getPointId());
                if (dp != null) {
                    ptMap.put("pointName", dp.getName());
                    ptMap.put("xid", dp.getXid());
                    ptMap.put("deviceName", dp.getDeviceName());
                }
                pointsList.add(ptMap);
            }
        }
        map.put("points", pointsList);

        List<Map<String, Object>> recipientsList = new ArrayList<>();
        if (report.getRecipients() != null) {
            for (RecipientListEntryBean r : report.getRecipients()) {
                Map<String, Object> rMap = new HashMap<>();
                rMap.put("recipientType", r.getRecipientType());
                rMap.put("referenceId", r.getReferenceId());
                rMap.put("referenceAddress", r.getReferenceAddress());
                recipientsList.add(rMap);
            }
        }
        map.put("recipients", recipientsList);

        return map;
    }

    private Map<String, Object> mapReportInstanceToMap(ReportInstance i) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", i.getId());
        map.put("userId", i.getUserId());
        map.put("name", i.getName());
        map.put("includeEvents", i.getIncludeEvents());
        map.put("includeUserComments", i.isIncludeUserComments());
        map.put("reportStartTime", i.getReportStartTime());
        map.put("reportEndTime", i.getReportEndTime());
        map.put("runStartTime", i.getRunStartTime());
        map.put("runEndTime", i.getRunEndTime());
        map.put("recordCount", i.getRecordCount());
        map.put("preventPurge", i.isPreventPurge());
        map.put("state", i.getState());

        map.put("prettyReportStartTime", i.getPrettyReportStartTime());
        map.put("prettyReportEndTime", i.getPrettyReportEndTime());
        map.put("prettyRunStartTime", i.getPrettyRunStartTime());
        map.put("prettyRunEndTime", i.getPrettyRunEndTime());
        map.put("prettyRunDuration", i.getPrettyRunDuration());
        map.put("prettyRecordCount", i.getPrettyRecordCount());
        return map;
    }
}
