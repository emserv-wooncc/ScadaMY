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

import java.util.List;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;

import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.hierarchy.PointFolder;
import com.serotonin.mango.vo.hierarchy.PointHierarchy;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing the Point Hierarchy (/point_hierarchy.shtm).
 */
@RestController
@RequestMapping("/api/point-hierarchy")
@Api(value = "Point Hierarchy API", tags = "Point Hierarchy Management")
public class PointHierarchyAPI {
    private static final Log logger = LogFactory.getLog(PointHierarchyAPI.class);

    @ApiOperation(value = "Get the root point hierarchy tree folder", response = PointFolder.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<PointFolder> getPointHierarchy(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DataPointDao dataPointDao = new DataPointDao();
        PointHierarchy ph = dataPointDao.getPointHierarchy().copyFoldersOnly();
        
        List<DataPointVO> points = dataPointDao.getDataPoints(DataPointExtendedNameComparator.instance, false);
        for (DataPointVO point : points) {
            if (Permissions.hasDataPointReadPermission(user, point)) {
                ph.addDataPoint(point.getId(), point.getPointFolderId(), point.getExtendedName());
            }
        }
        
        ph.parseEmptyFolders();
        return ResponseEntity.ok(ph.getRoot());
    }

    @ApiOperation(value = "Save a modified point hierarchy root tree structure", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> savePointHierarchy(@RequestBody PointFolder rootFolder, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureDataSourcePermission(user);
        } catch (PermissionException e) {
            logger.warn("User " + user.getUsername() + " lacks permission to save point hierarchy");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (rootFolder == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        BackgroundContext.set(user);
        try {
            new DataPointDao().savePointHierarchy(rootFolder);
            logger.info("Point hierarchy saved successfully by user=" + user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rootFolder", rootFolder);
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }
}
