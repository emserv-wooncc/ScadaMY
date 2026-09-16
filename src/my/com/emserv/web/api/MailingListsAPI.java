package my.com.emserv.web.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.serotonin.mango.db.dao.MailingListDao;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.mailingList.EmailRecipient;
import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.web.dwr.MailingListsDwr;
import com.serotonin.mango.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.web.dwr.DwrResponseI18n;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for managing Mailing Lists (/mailing_lists.shtm).
 */
@RestController
@RequestMapping("/api/mailing-lists")
@Api(value = "Mailing Lists API", tags = "Mailing Lists Management")
public class MailingListsAPI {
    private static final Log logger = LogFactory.getLog(MailingListsAPI.class);

    @ApiOperation(value = "Get initialization context data including mailing lists and candidate users", response = Object.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getInitData(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            MailingListsDwr dwr = new MailingListsDwr();
            DwrResponseI18n dwrResp = dwr.init();
            return ResponseEntity.ok(dwrResp.getData());
        } catch (Exception e) {
            logger.error("Error retrieving mailing lists initialization data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Get all mailing lists in the system", response = MailingList.class, responseContainer = "List")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<MailingList>> getMailingLists(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MailingList> list = new MailingListDao().getMailingLists();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "Get details of a specific mailing list by ID (or new structure for id -1)", response = MailingList.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<MailingList> getMailingList(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MailingList ml;
        if (id == Common.NEW_ID) {
            ml = new MailingList();
            ml.setId(Common.NEW_ID);
            ml.setXid(new MailingListDao().generateUniqueXid());
            ml.setEntries(new LinkedList<EmailRecipient>());
        } else {
            ml = new MailingListDao().getMailingList(id);
            if (ml == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.ok(ml);
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Create a new mailing list", response = Object.class)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createMailingList(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BackgroundContext.set(user);
        try {
            return parseAndSaveMailingList(Common.NEW_ID, body);
        } finally {
            BackgroundContext.remove();
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Update an existing mailing list by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Map<String, Object>> updateMailingList(@PathVariable int id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (new MailingListDao().getMailingList(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            return parseAndSaveMailingList(id, body);
        } finally {
            BackgroundContext.remove();
        }
    }

    @ApiOperation(value = "Delete a mailing list by ID", response = Object.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, Object>> deleteMailingList(@PathVariable int id, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (new MailingListDao().getMailingList(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        BackgroundContext.set(user);
        try {
            MailingListsDwr dwr = new MailingListsDwr();
            boolean deleted = dwr.deleteMailingList(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            if (!deleted) {
                response.put("error", "Cannot delete mailing list because it is currently in use by one or more event handlers");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                response.put("id", id);
                return ResponseEntity.ok(response);
            }
        } finally {
            BackgroundContext.remove();
        }
    }

    @SuppressWarnings("unchecked")
    @ApiOperation(value = "Send a test email to the recipients configured in the payload", response = Object.class)
    @RequestMapping(value = "/test-email", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BackgroundContext.set(user);
        try {
            int id = body.get("id") instanceof Number ? ((Number) body.get("id")).intValue() : Common.NEW_ID;
            String name = body.get("name") != null ? body.get("name").toString() : "Test List";

            List<RecipientListEntryBean> entryBeans = parseEntryBeans(body.get("entries"));

            MailingListsDwr dwr = new MailingListsDwr();
            DwrResponseI18n dwrResp = dwr.sendTestEmail(id, name, entryBeans);

            Map<String, Object> response = new HashMap<>();
            response.put("success", !dwrResp.getHasMessages());
            if (dwrResp.getHasMessages()) {
                response.put("messages", dwrResp.getMessages());
            } else {
                response.put("message", "Test email queued successfully");
            }
            return ResponseEntity.ok(response);
        } finally {
            BackgroundContext.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> parseAndSaveMailingList(int id, Map<String, Object> body) {
        String xid = body.get("xid") != null ? body.get("xid").toString().trim() : "";
        String name = body.get("name") != null ? body.get("name").toString().trim() : "";

        List<RecipientListEntryBean> entryBeans = parseEntryBeans(body.get("entries"));

        List<Integer> inactiveIntervals = new ArrayList<>();
        if (body.get("inactiveIntervals") instanceof List) {
            for (Object obj : (List<?>) body.get("inactiveIntervals")) {
                if (obj instanceof Number) {
                    inactiveIntervals.add(((Number) obj).intValue());
                }
            }
        }

        MailingListsDwr dwr = new MailingListsDwr();
        DwrResponseI18n dwrResp = dwr.saveMailingList(id, xid, name, entryBeans, inactiveIntervals);

        Map<String, Object> response = new HashMap<>();
        response.put("success", !dwrResp.getHasMessages());
        if (dwrResp.getHasMessages()) {
            response.put("messages", dwrResp.getMessages());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            response.put("data", dwrResp.getData());
            return ResponseEntity.ok(response);
        }
    }

    @SuppressWarnings("unchecked")
    private List<RecipientListEntryBean> parseEntryBeans(Object obj) {
        List<RecipientListEntryBean> list = new ArrayList<>();
        if (obj instanceof List) {
            for (Object item : (List<?>) obj) {
                if (item instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) item;
                    RecipientListEntryBean bean = new RecipientListEntryBean();
                    if (map.get("recipientType") instanceof Number) {
                        bean.setRecipientType(((Number) map.get("recipientType")).intValue());
                    }
                    if (map.get("referenceId") instanceof Number) {
                        bean.setReferenceId(((Number) map.get("referenceId")).intValue());
                    }
                    if (map.get("referenceAddress") != null) {
                        bean.setReferenceAddress(map.get("referenceAddress").toString().trim());
                    }
                    list.add(bean);
                }
            }
        }
        return list;
    }
}
