# SQL Execution API Documentation (`/api/sql`)

The **SQL Execution API** (`my.com.emserv.web.api.SqlAPI`) provides RESTful endpoints allowing system administrators to execute direct database query and update statements across SiteVisor's underlying database (the programmatic equivalent of `/sql.shtm`).

- **Source File:** [SqlAPI.java](../../src/my/com/emserv/web/api/SqlAPI.java)
- **Base URL Path:** `/api/sql`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** **Strict Admin Only**. Both query and update endpoints require Admin privileges (`Permissions.ensureAdmin`). Unauthorized requests return `403 Forbidden`.

---

## 1. Execute SELECT Query

Runs a SQL `SELECT` query and returns structured column labels along with row array data. Binary (`BLOB`, `CLOB`, `LONGVARBINARY`) columns are automatically inspected and serialized into string summaries.

- **URL:** `POST /api/sql/query`
- **Method:** `POST`
- **Request Body:**

```json
{
  "sql": "SELECT id, xid, name FROM dataSources ORDER BY id"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "headers": ["id", "xid", "name"],
  "data": [
    [1, "DS_VIRTUAL", "Virtual Data Source"],
    [2, "DS_MODBUS", "Modbus Master Plant 1"]
  ]
}
```

---

## 2. Execute DML / DDL Update Statement

Runs a SQL update command (`INSERT`, `UPDATE`, `DELETE`, or `ALTER/CREATE` DDL) and returns the number of affected database rows.

- **URL:** `POST /api/sql/update`
- **Method:** `POST`
- **Request Body:**

```json
{
  "sql": "UPDATE dataPoints SET enabled='N' WHERE dataSourceId=2"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "affectedRows": 15
}
```
