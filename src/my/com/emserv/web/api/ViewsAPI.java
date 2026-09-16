package my.com.emserv.web.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.ViewDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.view.ImplDefinition;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.view.View;
import com.serotonin.mango.view.ImageSet;
import com.serotonin.mango.view.component.AnalogGraphicComponent;
import com.serotonin.mango.view.component.BinaryGraphicComponent;
import com.serotonin.mango.view.component.CompoundChild;
import com.serotonin.mango.view.component.CompoundComponent;
import com.serotonin.mango.view.component.DynamicGraphicComponent;
import com.serotonin.mango.view.component.HtmlComponent;
import com.serotonin.mango.view.component.ImageChartComponent;
import com.serotonin.mango.view.component.ImageSetComponent;
import com.serotonin.mango.view.component.MultistateGraphicComponent;
import com.serotonin.mango.view.component.PointComponent;
import com.serotonin.mango.view.component.ScriptComponent;
import com.serotonin.mango.view.component.SimpleCompoundComponent;
import com.serotonin.mango.view.component.SimplePointComponent;
import com.serotonin.mango.view.component.ThumbnailComponent;
import com.serotonin.mango.view.component.ViewComponent;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;

import br.org.scadabr.db.dao.UsersProfileDao;
import br.org.scadabr.view.component.AlarmListComponent;
import br.org.scadabr.view.component.ButtonComponent;
import br.org.scadabr.view.component.LinkComponent;
import br.org.scadabr.view.component.ScriptButtonComponent;
import br.org.scadabr.vo.usersProfiles.UsersProfileVO;
import br.org.scadabr.workarounds.ViewManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for Graphical Views (/views.shtm replacement).
 */
@RestController
@RequestMapping("/api/views")
@Api(value = "Graphic Views API", tags = "Graphic Views Management")
public class ViewsAPI {
    private static final Log logger = LogFactory.getLog(ViewsAPI.class);

    @ApiOperation(value = "Get all accessible graphic views for current user", response = List.class)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Map<String, Object>>> getViews(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        List<IntValuePair> viewPairs;

        if (user.isAdmin()) {
            viewPairs = viewDao.getAllViewNames();
        } else {
            UsersProfileDao usersProfileDao = new UsersProfileDao();
            UsersProfileVO userProfile = usersProfileDao.getUserProfileByUserId(user.getId());
            int profileId = userProfile != null ? userProfile.getId() : -1;
            viewPairs = viewDao.getViewNamesWithReadOrWritePermissions(user.getId(), profileId);
        }

        Collections.sort(viewPairs, new Comparator<IntValuePair>() {
            @Override
            public int compare(IntValuePair o1, IntValuePair o2) {
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (IntValuePair pair : viewPairs) {
            View view = viewDao.getView(pair.getKey());
            if (view != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", view.getId());
                map.put("xid", view.getXid());
                map.put("name", view.getName());
                map.put("backgroundFilename", view.getBackgroundFilename());
                map.put("userId", view.getUserId());
                boolean isOwner = (user.getId() == view.getUserId()) || user.isAdmin();
                map.put("owner", isOwner);
                map.put("userAccess", view.getUserAccess(user));
                result.add(map);
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Create a new Graphic View", response = Map.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createView(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String name = (String) payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "View name is required");
            return ResponseEntity.badRequest().body(err);
        }

        ViewDao viewDao = new ViewDao();

        String xid = (String) payload.get("xid");
        if (xid == null || xid.trim().isEmpty()) {
            xid = viewDao.generateUniqueXid();
        }

        String backgroundFilename = (String) payload.get("backgroundFilename");
        Integer anonymousAccess = (Integer) payload.get("anonymousAccess");
        if (anonymousAccess == null) {
            anonymousAccess = ShareUser.ACCESS_NONE;
        }

        View view = new View();
        view.setId(Common.NEW_ID);
        view.setXid(xid);
        view.setName(name.trim());
        view.setBackgroundFilename(backgroundFilename != null ? backgroundFilename : "");
        view.setUserId(user.getId());
        view.setAnonymousAccess(anonymousAccess);

        BackgroundContext.set(user);
        try {
            viewDao.saveView(view);
            ViewManager.addView(view);

            Map<String, Object> response = new HashMap<>();
            response.put("id", view.getId());
            response.put("xid", view.getXid());
            response.put("name", view.getName());
            response.put("backgroundFilename", view.getBackgroundFilename());
            response.put("userId", view.getUserId());
            response.put("owner", true);
            response.put("userAccess", ShareUser.ACCESS_OWNER);
            response.put("anonymousAccess", view.getAnonymousAccess());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to create graphic view", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Get user's selected view ID", response = Map.class)
    @RequestMapping(value = "/selected", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSelectedView(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        View currentView = user.getView();
        int selectedId = (currentView != null) ? currentView.getId() : -1;

        if (selectedId <= 0) {
            ViewDao viewDao = new ViewDao();
            List<IntValuePair> viewPairs = user.isAdmin() ? viewDao.getAllViewNames()
                    : viewDao.getViewNames(user.getId());
            if (viewPairs != null && !viewPairs.isEmpty()) {
                selectedId = viewPairs.get(0).getKey();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("selectedViewId", selectedId);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update user's selected view ID", response = Map.class)
    @RequestMapping(value = "/selected/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> setSelectedView(@PathVariable("id") int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        user.setView(view);
        ViewManager.addView(view);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("selectedViewId", id);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get detailed view details by ID", response = Map.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getViewDetail(@PathVariable("id") int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        view.validateViewComponents(false);
        user.setView(view);
        ViewManager.addView(view);

        boolean isOwner = (user.getId() == view.getUserId()) || user.isAdmin();
        int userAccess = view.getUserAccess(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", view.getId());
        response.put("xid", view.getXid());
        response.put("name", view.getName());
        response.put("backgroundFilename", view.getBackgroundFilename());
        response.put("userId", view.getUserId());
        response.put("owner", isOwner);
        response.put("userAccess", userAccess);
        response.put("anonymousAccess", view.getAnonymousAccess());

        RuntimeManager rtm = Common.ctx.getRuntimeManager();
        List<Map<String, Object>> components = new ArrayList<>();

        for (ViewComponent vc : view.getViewComponents()) {
            Map<String, Object> compMap = formatViewComponent(vc, rtm, user, isOwner, userAccess);
            components.add(compMap);
        }

        response.put("components", components);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update an existing Graphic View metadata", response = Map.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateView(
            @PathVariable("id") int id,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewEditPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        if (payload.containsKey("name")) {
            String name = (String) payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "View name cannot be empty");
                return ResponseEntity.badRequest().body(err);
            }
            view.setName(name.trim());
        }

        if (payload.containsKey("xid")) {
            String xid = (String) payload.get("xid");
            if (xid != null && !xid.trim().isEmpty()) {
                view.setXid(xid.trim());
            }
        }

        if (payload.containsKey("backgroundFilename")) {
            String bg = (String) payload.get("backgroundFilename");
            view.setBackgroundFilename(bg != null ? bg : "");
        }

        if (payload.containsKey("anonymousAccess")) {
            Integer anonAccess = (Integer) payload.get("anonymousAccess");
            if (anonAccess != null) {
                view.setAnonymousAccess(anonAccess);
            }
        }

        if (payload.containsKey("components")) {
            Object compsObj = payload.get("components");
            if (compsObj instanceof List) {
                List<?> compPayloads = (List<?>) compsObj;
                for (Object item : compPayloads) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> compPayload = (Map<String, Object>) item;
                        if (compPayload.containsKey("id")) {
                            String compIdStr = String.valueOf(compPayload.get("id"));
                            ViewComponent vc = findViewComponent(view, compIdStr);
                            if (vc != null) {
                                applyComponentProperties(vc, compPayload, user);
                                vc.validateDataPoint(user, false);
                            }
                        }
                    }
                }
            }
        }

        BackgroundContext.set(user);
        try {
            viewDao.saveView(view);
            ViewManager.updateView(view);

            boolean isOwner = (user.getId() == view.getUserId()) || user.isAdmin();
            Map<String, Object> response = new HashMap<>();
            response.put("id", view.getId());
            response.put("xid", view.getXid());
            response.put("name", view.getName());
            response.put("backgroundFilename", view.getBackgroundFilename());
            response.put("userId", view.getUserId());
            response.put("owner", isOwner);
            response.put("userAccess", view.getUserAccess(user));
            response.put("anonymousAccess", view.getAnonymousAccess());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update graphic view", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Delete a Graphic View", response = Map.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteView(
            @PathVariable("id") int id,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewEditPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        BackgroundContext.set(user);
        try {
            viewDao.removeView(id);

            if (user.getView() != null && user.getView().getId() == id) {
                user.setView(null);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete graphic view", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Set a data point value on the graphic view", response = Map.class)
    @RequestMapping(value = "/{id}/set-point", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> setPointValue(
            @PathVariable("id") int id,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        int access = view.getUserAccess(user);
        boolean isOwner = (user.getId() == view.getUserId()) || user.isAdmin();
        if (!isOwner && access < ShareUser.ACCESS_SET) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Integer pointId = (Integer) payload.get("pointId");
        Object rawValue = payload.get("value");

        if (pointId == null || rawValue == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "pointId and value are required");
            return ResponseEntity.badRequest().body(err);
        }

        DataPointDao dataPointDao = new DataPointDao();
        DataPointVO pointVO = dataPointDao.getDataPoint(pointId);
        if (pointVO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin() && !Permissions.hasDataPointSetPermission(user, pointVO)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BackgroundContext.set(user);
        try {
            RuntimeManager rtm = Common.ctx.getRuntimeManager();
            MangoValue mangoValue = MangoValue.stringToValue(rawValue.toString(),
                    pointVO.getPointLocator().getDataTypeId());
            rtm.setDataPointValue(pointVO.getId(), mangoValue, user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pointId", pointId);
            response.put("value", rawValue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to set view data point value", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Get view editor initialization metadata", response = Map.class)
    @RequestMapping(value = "/editor-metadata", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getEditorMetadata(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> result = new HashMap<>();

        List<Map<String, String>> componentTypes = new ArrayList<>();
        for (ImplDefinition impl : ViewComponent.getImplementations()) {
            Map<String, String> typeMap = new HashMap<>();
            typeMap.put("name", impl.getName());
            typeMap.put("exportName", impl.getExportName());
            typeMap.put("nameKey", impl.getNameKey());
            componentTypes.add(typeMap);
        }
        result.put("componentTypes", componentTypes);

        List<Map<String, Object>> imageSets = new ArrayList<>();
        for (ImageSet imgSet : Common.ctx.getImageSets()) {
            Map<String, Object> imgMap = new HashMap<>();
            imgMap.put("id", imgSet.getId());
            imgMap.put("name", imgSet.getName());
            imgMap.put("imageCount", imgSet.getImageCount());
            imgMap.put("imageFilenames", imgSet.getImageFilenames());
            imgMap.put("width", imgSet.getWidth());
            imgMap.put("height", imgSet.getHeight());
            imageSets.add(imgMap);
        }
        result.put("imageSets", imageSets);

        List<Map<String, Object>> dynamicImages = new ArrayList<>();
        for (com.serotonin.mango.view.DynamicImage dynImg : Common.ctx.getDynamicImages()) {
            Map<String, Object> dynMap = new HashMap<>();
            dynMap.put("id", dynImg.getId());
            dynMap.put("name", dynImg.getName());
            dynMap.put("imageFilename", dynImg.getImageFilename());
            dynamicImages.add(dynMap);
        }
        result.put("dynamicImages", dynamicImages);

        List<Map<String, String>> scriptsList = new ArrayList<>();
        for (br.org.scadabr.vo.scripting.ScriptVO<?> script : new br.org.scadabr.db.dao.ScriptDao().getScripts()) {
            Map<String, String> scriptMap = new HashMap<>();
            scriptMap.put("xid", script.getXid());
            scriptMap.put("name", script.getName());
            scriptsList.add(scriptMap);
        }
        result.put("scripts", scriptsList);

        DataPointDao dataPointDao = new DataPointDao();
        List<DataPointVO> allPoints = dataPointDao.getDataPoints(DataPointExtendedNameComparator.instance, false);
        List<Map<String, Object>> availablePoints = new ArrayList<>();
        for (DataPointVO dp : allPoints) {
            if (Permissions.hasDataPointReadPermission(user, dp)) {
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("id", dp.getId());
                pMap.put("xid", dp.getXid());
                pMap.put("name", dp.getName());
                pMap.put("extendedName", dp.getExtendedName());
                pMap.put("dataTypeId", dp.getPointLocator().getDataTypeId());
                availablePoints.add(pMap);
            }
        }
        result.put("availablePoints", availablePoints);

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Add a new component to a graphic view", response = Map.class)
    @RequestMapping(value = "/{id}/components", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addComponentToView(
            @PathVariable("id") int id,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewEditPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String componentType = (String) payload.get("componentType");
        if (componentType == null || componentType.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "componentType is required");
            return ResponseEntity.badRequest().body(err);
        }

        ViewComponent vc = createComponentInstance(componentType);
        if (vc == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Invalid or unknown componentType: " + componentType);
            return ResponseEntity.badRequest().body(err);
        }

        view.addViewComponent(vc);
        applyComponentProperties(vc, payload, user);
        vc.validateDataPoint(user, false);

        BackgroundContext.set(user);
        try {
            viewDao.saveView(view);
            ViewManager.updateView(view);
            if (user.getView() != null && user.getView().getId() == view.getId()) {
                user.setView(view);
            }

            boolean isOwner = (user.getId() == view.getUserId()) || user.isAdmin();
            int userAccess = view.getUserAccess(user);
            RuntimeManager rtm = Common.ctx.getRuntimeManager();
            Map<String, Object> response = formatViewComponent(vc, rtm, user, isOwner, userAccess);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Failed to add view component", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Update an existing component in a graphic view", response = Map.class)
    @RequestMapping(value = "/{id}/components/{componentId}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateViewComponent(
            @PathVariable("id") int id,
            @PathVariable("componentId") String componentId,
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewEditPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        ViewComponent vc = findViewComponent(view, componentId);
        if (vc == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Component not found with ID: " + componentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        applyComponentProperties(vc, payload, user);
        vc.validateDataPoint(user, false);

        BackgroundContext.set(user);
        try {
            viewDao.saveView(view);
            ViewManager.updateView(view);
            if (user.getView() != null && user.getView().getId() == view.getId()) {
                user.setView(view);
            }

            boolean isOwner = (user.getId() == view.getUserId()) || user.isAdmin();
            int userAccess = view.getUserAccess(user);
            RuntimeManager rtm = Common.ctx.getRuntimeManager();
            Map<String, Object> response = formatViewComponent(vc, rtm, user, isOwner, userAccess);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update view component", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Delete a component from a graphic view", response = Map.class)
    @RequestMapping(value = "/{id}/components/{componentId}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteViewComponent(
            @PathVariable("id") int id,
            @PathVariable("componentId") String componentId,
            HttpServletRequest request) {

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewDao viewDao = new ViewDao();
        View view = viewDao.getView(id);
        if (view == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!user.isAdmin()) {
            try {
                Permissions.ensureViewEditPermission(user, view);
            } catch (PermissionException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        ViewComponent vc = findViewComponent(view, componentId);
        if (vc == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Component not found with ID: " + componentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        view.removeViewComponent(vc);

        BackgroundContext.set(user);
        try {
            viewDao.saveView(view);
            ViewManager.updateView(view);
            if (user.getView() != null && user.getView().getId() == view.getId()) {
                user.setView(view);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("componentId", componentId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete view component", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Evaluate a view script snippet using backend Rhino engine", response = Map.class)
    @RequestMapping(value = "/evaluate-script", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> evaluateScript(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        if (Boolean.getBoolean("scadamy.disableEvaluateScript")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String script = (String) payload.get("script");
        if (script == null) {
            script = "";
        }

        Integer pointId = payload.get("pointId") instanceof Number ? ((Number) payload.get("pointId")).intValue() : null;
        DataPointVO point = null;
        PointValueTime pvt = null;

        if (pointId != null && pointId > 0) {
            point = new DataPointDao().getDataPoint(pointId);
            if (point != null && Permissions.hasDataPointReadPermission(user, point)) {
                RuntimeManager rtm = Common.ctx.getRuntimeManager();
                DataPointRT dpRT = rtm.getDataPoint(pointId);
                pvt = (dpRT != null) ? dpRT.getPointValue() : point.lastValue();
            }
        }

        ScriptComponent sc = new ScriptComponent();
        sc.setScript(script);
        if (point != null) {
            sc.tsetDataPoint(point);
        }

        Map<String, Object> model = new HashMap<>();
        sc.addDataToModel(model, pvt);

        String result = (String) model.get("scriptContent");

        Map<String, Object> response = new HashMap<>();
        response.put("result", result != null ? result : "");
        return ResponseEntity.ok(response);
    }

    private ViewComponent findViewComponent(View view, String componentId) {
        if (view == null || componentId == null) {
            return null;
        }
        for (ViewComponent vc : view.getViewComponents()) {
            if (componentId.equals(vc.getId())) {
                return vc;
            }
        }
        return null;
    }

    private ViewComponent createComponentInstance(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return null;
        }
        String cleanType = typeName.trim();
        for (ImplDefinition impl : ViewComponent.getImplementations()) {
            if (cleanType.equalsIgnoreCase(impl.getName())
                    || cleanType.equalsIgnoreCase(impl.getExportName())
                    || cleanType.equalsIgnoreCase(impl.getNameKey())) {
                return ViewComponent.newInstance(impl.getName());
            }
        }
        for (ImplDefinition impl : ViewComponent.getImplementations()) {
            try {
                ViewComponent testVc = ViewComponent.newInstance(impl.getName());
                if (testVc != null && testVc.getClass().getSimpleName().equalsIgnoreCase(cleanType)) {
                    return testVc;
                }
            } catch (Exception e) {
                // Ignore and try next implementation
            }
        }
        try {
            return ViewComponent.newInstance(cleanType);
        } catch (Exception e) {
            return null;
        }
    }

    private void applyComponentProperties(ViewComponent vc, Map<String, Object> payload, User user) {
        if (payload.containsKey("x") || payload.containsKey("y")) {
            int x = payload.containsKey("x") && payload.get("x") instanceof Number
                    ? ((Number) payload.get("x")).intValue() : vc.getX();
            int y = payload.containsKey("y") && payload.get("y") instanceof Number
                    ? ((Number) payload.get("y")).intValue() : vc.getY();
            vc.setLocation(x, y);
        }

        if (vc.isPointComponent()) {
            PointComponent pc = (PointComponent) vc;
            if (payload.containsKey("pointId")) {
                Integer pointId = payload.get("pointId") instanceof Number
                        ? ((Number) payload.get("pointId")).intValue() : null;
                if (pointId != null && pointId > 0) {
                    DataPointVO dp = new DataPointDao().getDataPoint(pointId);
                    if (dp != null && Permissions.hasDataPointReadPermission(user, dp)) {
                        pc.tsetDataPoint(dp);
                    }
                } else if (pointId == null || pointId == 0) {
                    pc.tsetDataPoint(null);
                }
            }
            if (payload.containsKey("nameOverride")) {
                pc.setNameOverride((String) payload.get("nameOverride"));
            }
            if (payload.containsKey("settableOverride")) {
                Boolean settable = (Boolean) payload.get("settableOverride");
                pc.setSettableOverride(settable != null && settable && pc.tgetDataPoint() != null
                        && Permissions.hasDataPointSetPermission(user, pc.tgetDataPoint()));
            }
            if (payload.containsKey("bkgdColorOverride")) {
                pc.setBkgdColorOverride((String) payload.get("bkgdColorOverride"));
            }
            if (payload.containsKey("displayControls")) {
                Boolean dc = (Boolean) payload.get("displayControls");
                if (dc != null) {
                    pc.setDisplayControls(dc);
                }
            }
        }

        if (vc instanceof HtmlComponent && payload.containsKey("content")) {
            ((HtmlComponent) vc).setContent((String) payload.get("content"));
        }

        if (vc instanceof LinkComponent) {
            LinkComponent lc = (LinkComponent) vc;
            if (payload.containsKey("linkUrl")) {
                lc.setLink((String) payload.get("linkUrl"));
            }
            if (payload.containsKey("text")) {
                lc.setText((String) payload.get("text"));
            }
        }

        if (vc instanceof ScriptButtonComponent) {
            ScriptButtonComponent sbc = (ScriptButtonComponent) vc;
            if (payload.containsKey("text")) {
                sbc.setText((String) payload.get("text"));
            }
            if (payload.containsKey("scriptXid")) {
                sbc.setScriptXid((String) payload.get("scriptXid"));
            }
            if (payload.containsKey("content")) {
                sbc.setContent((String) payload.get("content"));
            }
        }

        if (vc instanceof ScriptComponent && payload.containsKey("script")) {
            ((ScriptComponent) vc).setScript((String) payload.get("script"));
        }

        if (vc instanceof AnalogGraphicComponent) {
            AnalogGraphicComponent agc = (AnalogGraphicComponent) vc;
            if (payload.get("min") instanceof Number) {
                agc.setMin(((Number) payload.get("min")).doubleValue());
            }
            if (payload.get("max") instanceof Number) {
                agc.setMax(((Number) payload.get("max")).doubleValue());
            }
            if (payload.containsKey("displayText")) {
                agc.setDisplayText(Boolean.TRUE.equals(payload.get("displayText")));
            }
            if (payload.containsKey("imageSetId")) {
                String imgSetId = (String) payload.get("imageSetId");
                if (imgSetId != null) {
                    agc.tsetImageSet(Common.ctx.getImageSet(imgSetId));
                }
            }
        }

        if (vc instanceof BinaryGraphicComponent) {
            BinaryGraphicComponent bgc = (BinaryGraphicComponent) vc;
            if (payload.get("zeroImageIndex") instanceof Number) {
                bgc.setZeroImage(((Number) payload.get("zeroImageIndex")).intValue());
            }
            if (payload.get("oneImageIndex") instanceof Number) {
                bgc.setOneImage(((Number) payload.get("oneImageIndex")).intValue());
            }
            if (payload.containsKey("displayText")) {
                bgc.setDisplayText(Boolean.TRUE.equals(payload.get("displayText")));
            }
            if (payload.containsKey("imageSetId")) {
                String imgSetId = (String) payload.get("imageSetId");
                if (imgSetId != null) {
                    bgc.tsetImageSet(Common.ctx.getImageSet(imgSetId));
                }
            }
        }

        if (vc instanceof DynamicGraphicComponent) {
            DynamicGraphicComponent dgc = (DynamicGraphicComponent) vc;
            if (payload.get("min") instanceof Number) {
                dgc.setMin(((Number) payload.get("min")).doubleValue());
            }
            if (payload.get("max") instanceof Number) {
                dgc.setMax(((Number) payload.get("max")).doubleValue());
            }
            if (payload.containsKey("displayText")) {
                dgc.setDisplayText(Boolean.TRUE.equals(payload.get("displayText")));
            }
            if (payload.containsKey("dynamicImageId")) {
                String dynImgId = (String) payload.get("dynamicImageId");
                if (dynImgId != null) {
                    dgc.tsetDynamicImage(Common.ctx.getDynamicImage(dynImgId));
                }
            }
        }

        if (vc instanceof MultistateGraphicComponent) {
            MultistateGraphicComponent mgc = (MultistateGraphicComponent) vc;
            if (payload.get("defaultImageIndex") instanceof Number) {
                mgc.setDefaultImage(((Number) payload.get("defaultImageIndex")).intValue());
            }
            if (payload.containsKey("displayText")) {
                mgc.setDisplayText(Boolean.TRUE.equals(payload.get("displayText")));
            }
            if (payload.containsKey("imageSetId")) {
                String imgSetId = (String) payload.get("imageSetId");
                if (imgSetId != null) {
                    mgc.tsetImageSet(Common.ctx.getImageSet(imgSetId));
                }
            }
            if (payload.containsKey("imageStateList")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) payload.get("imageStateList");
                List<IntValuePair> stateList = new ArrayList<>();
                logger.info("ViewsAPI - mgc.setImageStateList payload received: " + list);
                if (list != null) {
                    for (Map<String, Object> map : list) {
                        Number key = (Number) map.get("key");
                        String value = (String) map.get("value");
                        logger.info("ViewsAPI - imageStateList item: key=" + key + ", value=" + value);
                        if (key != null && value != null) {
                            stateList.add(new IntValuePair(key.intValue(), value));
                        }
                    }
                }
                mgc.setImageStateList(stateList);
                logger.info("ViewsAPI - mgc.getImageStateList after set: " + mgc.getImageStateList());
            }
        }

        if (vc instanceof SimplePointComponent) {
            SimplePointComponent spc = (SimplePointComponent) vc;
            if (payload.containsKey("displayPointName")) {
                spc.setDisplayPointName(Boolean.TRUE.equals(payload.get("displayPointName")));
            }
            if (payload.containsKey("styleAttribute")) {
                spc.setStyleAttribute((String) payload.get("styleAttribute"));
            }
        }

        if (vc instanceof ThumbnailComponent && payload.get("scalePercent") instanceof Number) {
            ((ThumbnailComponent) vc).setScalePercent(((Number) payload.get("scalePercent")).intValue());
        }

        if (vc instanceof ButtonComponent) {
            ButtonComponent bc = (ButtonComponent) vc;
            if (payload.containsKey("whenOnLabel")) {
                bc.setWhenOnLabel((String) payload.get("whenOnLabel"));
            }
            if (payload.containsKey("whenOffLabel")) {
                bc.setWhenOffLabel((String) payload.get("whenOffLabel"));
            }
            if (payload.get("width") instanceof Number) {
                bc.setWidth(((Number) payload.get("width")).intValue());
            }
            if (payload.get("height") instanceof Number) {
                bc.setHeight(((Number) payload.get("height")).intValue());
            }
            if (payload.containsKey("pointId")) {
                Integer pointId = payload.get("pointId") instanceof Number ? ((Number) payload.get("pointId")).intValue() : null;
                if (pointId != null && pointId > 0) {
                    DataPointVO dp = new DataPointDao().getDataPoint(pointId);
                    if (dp != null && Permissions.hasDataPointReadPermission(user, dp)) {
                        bc.tsetDataPoint(dp);
                    }
                }
            }
        }

        if (vc instanceof AlarmListComponent) {
            AlarmListComponent alc = (AlarmListComponent) vc;
            if (payload.get("minAlarmLevel") instanceof Number) {
                alc.setMinAlarmLevel(((Number) payload.get("minAlarmLevel")).intValue());
            }
            if (payload.get("maxListSize") instanceof Number) {
                alc.setMaxListSize(((Number) payload.get("maxListSize")).intValue());
            }
            if (payload.get("width") instanceof Number) {
                alc.setWidth(((Number) payload.get("width")).intValue());
            }
            if (payload.containsKey("hideIdColumn")) {
                alc.setHideIdColumn(Boolean.TRUE.equals(payload.get("hideIdColumn")));
            }
            if (payload.containsKey("hideAlarmLevelColumn")) {
                alc.setHideAlarmLevelColumn(Boolean.TRUE.equals(payload.get("hideAlarmLevelColumn")));
            }
            if (payload.containsKey("hideTimestampColumn")) {
                alc.setHideTimestampColumn(Boolean.TRUE.equals(payload.get("hideTimestampColumn")));
            }
            if (payload.containsKey("hideInactivityColumn")) {
                alc.setHideInactivityColumn(Boolean.TRUE.equals(payload.get("hideInactivityColumn")));
            }
            if (payload.containsKey("hideAckColumn")) {
                alc.setHideAckColumn(Boolean.TRUE.equals(payload.get("hideAckColumn")));
            }
        } else if (vc instanceof DynamicGraphicComponent) {
            DynamicGraphicComponent dgc = (DynamicGraphicComponent) vc;
            if (payload.containsKey("min") && payload.get("min") instanceof Number) {
                dgc.setMin(((Number) payload.get("min")).doubleValue());
            }
            if (payload.containsKey("max") && payload.get("max") instanceof Number) {
                dgc.setMax(((Number) payload.get("max")).doubleValue());
            }
            if (payload.containsKey("dynamicImageId") && payload.get("dynamicImageId") instanceof String) {
                dgc.tsetDynamicImage(Common.ctx.getDynamicImage((String) payload.get("dynamicImageId")));
            }
        } else if (vc instanceof ImageChartComponent) {
            ImageChartComponent icc = (ImageChartComponent) vc;
            if (payload.get("width") instanceof Number) {
                icc.setWidth(((Number) payload.get("width")).intValue());
            }
            if (payload.get("height") instanceof Number) {
                icc.setHeight(((Number) payload.get("height")).intValue());
            }
            if (payload.get("durationPeriods") instanceof Number) {
                icc.setDurationPeriods(((Number) payload.get("durationPeriods")).intValue());
            }
            if (payload.get("durationType") instanceof Number) {
                icc.setDurationType(((Number) payload.get("durationType")).intValue());
            }
            if (payload.get("childComponents") instanceof List) {
                List<?> children = (List<?>) payload.get("childComponents");
                for (int i = 0; i < 10 && i < children.size(); i++) {
                    Object childObj = children.get(i);
                    if (childObj instanceof Map) {
                        Map<?, ?> childMap = (Map<?, ?>) childObj;
                        String pointIdStr = "point" + (i + 1);
                        Integer pointId = childMap.get("pointId") instanceof Number ? ((Number) childMap.get("pointId")).intValue() : null;
                        if (pointId != null && pointId > 0) {
                            DataPointVO dp = new DataPointDao().getDataPoint(pointId);
                            if (dp != null && Permissions.hasDataPointReadPermission(user, dp)) {
                                icc.setDataPoint(pointIdStr, dp);
                            }
                        } else {
                            icc.setDataPoint(pointIdStr, null); // Clear point
                        }
                    }
                }
            }
        }
    }

    private Map<String, Object> formatViewComponent(ViewComponent vc, RuntimeManager rtm, User user, boolean isOwner,
            int userAccess) {
        Map<String, Object> compMap = new HashMap<>();
        compMap.put("id", vc.getId());
        compMap.put("index", vc.getIndex());
        compMap.put("x", vc.getX());
        compMap.put("y", vc.getY());
        compMap.put("style", vc.getStyle());

        String typeName = vc.getClass().getSimpleName();
        compMap.put("componentType", typeName);

        if (vc.isPointComponent()) {
            PointComponent pc = (PointComponent) vc;
            compMap.put("nameOverride", pc.getNameOverride() != null ? pc.getNameOverride() : "");
            compMap.put("settableOverride", pc.isSettableOverride());
            compMap.put("bkgdColorOverride", pc.getBkgdColorOverride() != null ? pc.getBkgdColorOverride() : "");
            compMap.put("bkgdColor", pc.getBkgdColorOverride() != null ? pc.getBkgdColorOverride() : "");
            compMap.put("displayControls", pc.isDisplayControls());

            DataPointVO dp = pc.tgetDataPoint();
            if (dp != null) {
                compMap.put("pointId", dp.getId());
                compMap.put("pointXid", dp.getXid());
                compMap.put("pointName",
                        pc.getNameOverride() != null && !pc.getNameOverride().isEmpty() ? pc.getNameOverride()
                                : dp.getName());
                compMap.put("rawPointName", dp.getName());
                compMap.put("extendedName", dp.getExtendedName());
                compMap.put("unit", dp.getEngineeringUnits());
                compMap.put("dataTypeId", dp.getPointLocator().getDataTypeId());

                boolean isSettable = dp.getPointLocator().isSettable()
                        && (isOwner || userAccess >= ShareUser.ACCESS_SET);
                compMap.put("settable", isSettable);

                DataPointRT dpRT = rtm.getDataPoint(dp.getId());
                PointValueTime pvt = (dpRT != null) ? dpRT.getPointValue() : dp.lastValue();
                if (pvt != null) {
                    compMap.put("value", pvt.getValue() != null ? pvt.getValue().getObjectValue() : null);
                    String rendered = "";
                    if (pvt.getValue() != null) {
                        rendered = dp.getTextRenderer() != null
                            ? dp.getTextRenderer().getText(pvt, com.serotonin.mango.view.text.TextRenderer.HINT_FULL)
                            : pvt.getValue().toString();
                    }
                    compMap.put("renderedValue", rendered);
                    compMap.put("timestamp", pvt.getTime());
                } else {
                    compMap.put("value", null);
                    compMap.put("renderedValue", "---");
                    compMap.put("timestamp", null);
                }

                if (vc instanceof ImageSetComponent) {
                    ImageSetComponent isc = (ImageSetComponent) vc;
                    String imgPath = isc.getImage(pvt);
                    if (imgPath == null) {
                        imgPath = isc.defaultImage();
                    }
                    compMap.put("imagePath", imgPath);
                    compMap.put("displayText", isc.isDisplayText());
                    if (isc.tgetImageSet() != null) {
                        compMap.put("imageSetId", isc.tgetImageSet().getId());
                        compMap.put("imageSetName", isc.tgetImageSet().getName());
                        compMap.put("imageFilenames", isc.tgetImageSet().getImageFilenames());
                        compMap.put("imageSetWidth", isc.tgetImageSet().getWidth());
                        compMap.put("imageSetHeight", isc.tgetImageSet().getHeight());
                    }
                }

                if (vc instanceof AnalogGraphicComponent) {
                    AnalogGraphicComponent agc = (AnalogGraphicComponent) vc;
                    compMap.put("min", agc.getMin());
                    compMap.put("max", agc.getMax());
                } else if (vc instanceof BinaryGraphicComponent) {
                    BinaryGraphicComponent bgc = (BinaryGraphicComponent) vc;
                    compMap.put("zeroImageIndex", bgc.getZeroImage());
                    compMap.put("oneImageIndex", bgc.getOneImage());
                } else if (vc instanceof DynamicGraphicComponent) {
                    DynamicGraphicComponent dgc = (DynamicGraphicComponent) vc;
                    compMap.put("min", dgc.getMin());
                    compMap.put("max", dgc.getMax());
                    if (dgc.tgetDynamicImage() != null) {
                        compMap.put("dynamicImageId", dgc.tgetDynamicImage().getId());
                        compMap.put("dynamicImageName", dgc.tgetDynamicImage().getName());
                    }
                    compMap.put("imagePath", dgc.getImage());
                    compMap.put("displayText", dgc.isDisplayText());
                } else if (vc instanceof MultistateGraphicComponent) {
                    MultistateGraphicComponent mgc = (MultistateGraphicComponent) vc;
                    compMap.put("defaultImageIndex", mgc.getDefaultImage());
                    compMap.put("imageStateList", mgc.getImageStateList());
                } else if (vc instanceof SimplePointComponent) {
                    SimplePointComponent spc = (SimplePointComponent) vc;
                    compMap.put("displayPointName", spc.isDisplayPointName());
                    compMap.put("styleAttribute", spc.getStyleAttribute());
                } else if (vc instanceof ThumbnailComponent) {
                    ThumbnailComponent tc = (ThumbnailComponent) vc;
                    compMap.put("scalePercent", tc.getScalePercent());
                } else if (vc instanceof ScriptComponent) {
                    compMap.put("script", ((ScriptComponent) vc).getScript());
                }
            }
        } else if (vc instanceof ScriptButtonComponent) {
            ScriptButtonComponent sbc = (ScriptButtonComponent) vc;
            compMap.put("content", sbc.getContent());
            compMap.put("scriptXid", sbc.getScriptXid());
            compMap.put("text", sbc.getText());
        } else if (vc instanceof LinkComponent) {
            LinkComponent lc = (LinkComponent) vc;
            compMap.put("linkUrl", lc.getLink());
            compMap.put("text", lc.getText());
        } else if (vc instanceof HtmlComponent) {
            compMap.put("content", ((HtmlComponent) vc).getContent());
        } else if (vc instanceof ButtonComponent) {
            ButtonComponent bc = (ButtonComponent) vc;
            compMap.put("whenOffLabel", bc.getWhenOffLabel());
            compMap.put("whenOnLabel", bc.getWhenOnLabel());
            compMap.put("width", bc.getWidth());
            compMap.put("height", bc.getHeight());
            if (bc.tgetDataPoint() != null) {
                compMap.put("pointId", bc.tgetDataPoint().getId());
            }
        } else if (vc instanceof AlarmListComponent) {
            AlarmListComponent alc = (AlarmListComponent) vc;
            compMap.put("minAlarmLevel", alc.getMinAlarmLevel());
            compMap.put("maxListSize", alc.getMaxListSize());
            compMap.put("width", alc.getWidth());
            compMap.put("hideIdColumn", alc.isHideIdColumn());
            compMap.put("hideAlarmLevelColumn", alc.isHideAlarmLevelColumn());
            compMap.put("hideTimestampColumn", alc.isHideTimestampColumn());
            compMap.put("hideInactivityColumn", alc.isHideInactivityColumn());
            compMap.put("hideAckColumn", alc.isHideAckColumn());
        } else if (vc.isCompoundComponent()) {
            CompoundComponent cc = (CompoundComponent) vc;
            compMap.put("isCompound", true);
            if (vc instanceof SimpleCompoundComponent) {
                SimpleCompoundComponent scc = (SimpleCompoundComponent) vc;
                compMap.put("name", scc.getName());
                compMap.put("backgroundColour", scc.getBackgroundColour());
            } else if (vc instanceof ImageChartComponent) {
                ImageChartComponent icc = (ImageChartComponent) vc;
                compMap.put("width", icc.getWidth());
                compMap.put("height", icc.getHeight());
                compMap.put("durationPeriods", icc.getDurationPeriods());
                compMap.put("durationType", icc.getDurationType());
            }

            List<Map<String, Object>> children = new ArrayList<>();
            for (CompoundChild child : cc.getChildComponents()) {
                if (child.getViewComponent() != null) {
                    children.add(formatViewComponent(child.getViewComponent(), rtm, user, isOwner, userAccess));
                }
            }
            compMap.put("childComponents", children);
        }

        return compMap;
    }
}
