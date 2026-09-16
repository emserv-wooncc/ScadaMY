package my.com.emserv.web.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.db.spring.ConnectionCallbackVoid;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.DatabaseAccess;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.PermissionException;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.SerializationHelper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST API Controller for executing SQL Queries/Updates (/sql.shtm).
 */
@RestController
@RequestMapping("/api/sql")
@Api(value = "SQL API", tags = "Database SQL Execution (Admin Only)")
public class SqlAPI {
    private static final Log logger = LogFactory.getLog(SqlAPI.class);

    @ApiOperation(value = "Execute a SELECT SQL query against the system database (Admin only)", response = Object.class)
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final String sql = body.get("sql") != null ? body.get("sql").toString() : (body.get("query") != null ? body.get("query").toString() : "");
        if (sql.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "SQL query string is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        DatabaseAccess databaseAccess = Common.ctx.getDatabaseAccess();
        final List<String> headers = new ArrayList<>();
        final List<List<Object>> data = new LinkedList<>();

        try {
            databaseAccess.doInConnection(new ConnectionCallbackVoid() {
                public void doInConnection(Connection conn) throws SQLException {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    ResultSetMetaData meta = rs.getMetaData();
                    int columns = meta.getColumnCount();
                    for (int i = 0; i < columns; i++) {
                        headers.add(meta.getColumnLabel(i + 1));
                    }

                    while (rs.next()) {
                        List<Object> row = new ArrayList<>(columns);
                        data.add(row);
                        for (int i = 0; i < columns; i++) {
                            int type = meta.getColumnType(i + 1);
                            if (type == Types.CLOB) {
                                row.add(rs.getString(i + 1));
                            } else if (type == Types.LONGVARBINARY || type == Types.BLOB || type == Types.BINARY) {
                                try {
                                    Object o;
                                    if ("postgres".equals(Common.getEnvironmentProfile().getString("db.type"))) {
                                        o = SerializationHelper.readObject(rs.getBinaryStream(i + 1));
                                    } else {
                                        if (rs.getBlob(i + 1) != null) {
                                            o = SerializationHelper.readObject(rs.getBlob(i + 1).getBinaryStream());
                                        } else {
                                            o = "null";
                                        }
                                    }
                                    row.add("Serialized data(" + o + ")");
                                } catch (Exception ex) {
                                    row.add("Binary data (unserializable)");
                                }
                            } else {
                                row.add(rs.getObject(i + 1));
                            }
                        }
                    }
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("headers", headers);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("SQL query execution error by admin user=" + user.getUsername() + ": " + e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    @ApiOperation(value = "Execute a DML/DDL update SQL command against the system database (Admin only)", response = Object.class)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> executeUpdate(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Permissions.ensureAdmin(user);
        } catch (PermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final String sql = body.get("sql") != null ? body.get("sql").toString() : (body.get("update") != null ? body.get("update").toString() : "");
        if (sql.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "SQL update string is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        DatabaseAccess databaseAccess = Common.ctx.getDatabaseAccess();
        try {
            ExtendedJdbcTemplate ejt = new ExtendedJdbcTemplate();
            ejt.setDataSource(databaseAccess.getDataSource());
            int result = ejt.update(sql);

            logger.info("SQL update executed by admin user=" + user.getUsername() + ", affected rows=" + result);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("affectedRows", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("SQL update execution error by admin user=" + user.getUsername() + ": " + e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}
