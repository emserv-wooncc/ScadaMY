# Reports API Documentation (`/api/reports`)

The **Reports API** (`my.com.emserv.web.api.ReportsAPI`) provides comprehensive RESTful endpoints for querying, creating, updating, running, and deleting report templates and generated report instances (the direct REST equivalent of `/reports.shtm`). It also exposes endpoints for streaming raw report data, alarms/events, and user comments as CSV attachments or generating interactive HTML charts.

- **Source File:** [ReportsAPI.java](../../src/my/com/emserv/web/api/ReportsAPI.java)
- **Base URL Path:** `/api/reports`
- **Authentication & Permission:** All endpoints require an active user session (`JSESSIONID` cookie) or valid authentication headers. Operations on individual report templates and instances enforce strict user access permissions (`Permissions.ensureReportPermission` and `Permissions.ensureReportInstancePermission`). Unauthenticated requests return `401 Unauthorized`; permission check failures return `403 Forbidden`.

---

## 1. Get Report Initialization Data

Retrieves the complete context required to render and initialize the Reports management view in a single round-trip: accessible report templates, generated report instances, user-readable data points, system mailing lists, active users, and watchlists.

- **URL:** `GET /api/reports/init`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "reports": [ /* array of report templates, see GET /api/reports */ ],
  "instances": [ /* array of report instances, see GET /api/reports */ ],
  "points": [
    {
      "id": 101,
      "xid": "DP_112045",
      "name": "Boiler Temperature",
      "deviceName": "PLC-01",
      "extendedName": "PLC-01 - Boiler Temperature",
      "dataTypeId": 3,
      "dataTypeMessage": "common.stats.numeric"
    }
  ],
  "mailingLists": [
    {
      "id": 1,
      "xid": "ML_OPERATORS",
      "name": "Shift Operators"
    }
  ],
  "users": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@sitevisor.local",
      "admin": true
    }
  ],
  "watchlists": [
    {
      "id": 1,
      "name": "Default Watchlist",
      "pointIds": [101, 102, 105]
    }
  ]
}
```

---

## 2. Get All Accessible Reports and Instances

Retrieves all report templates and generated report instances belonging or accessible to the currently authenticated user.

- **URL:** `GET /api/reports`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "reports": [
    {
      "id": 1,
      "userId": 1,
      "name": "Daily Factory Temperature Report",
      "includeEvents": 2,
      "includeUserComments": true,
      "dateRangeType": 1,
      "relativeDateType": 1,
      "previousPeriodCount": 1,
      "previousPeriodType": 3,
      "pastPeriodCount": 1,
      "pastPeriodType": 3,
      "fromNone": false,
      "toNone": false,
      "schedule": true,
      "schedulePeriod": 0,
      "runDelayMinutes": 5,
      "scheduleCron": "0 0 1 * * ?",
      "email": false,
      "includeData": true,
      "zipData": false,
      "points": [
        {
          "pointId": 101,
          "pointName": "Boiler Temperature",
          "xid": "DP_112045",
          "deviceName": "PLC-01",
          "colour": "#FF0000",
          "consolidatedChart": true
        }
      ],
      "recipients": []
    }
  ],
  "instances": [
    {
      "id": 15,
      "userId": 1,
      "name": "Daily Factory Temperature Report",
      "includeEvents": 2,
      "includeUserComments": true,
      "reportStartTime": 1689897600000,
      "reportEndTime": 1689984000000,
      "runStartTime": 1689984300000,
      "runEndTime": 1689984305000,
      "recordCount": 1440,
      "preventPurge": false,
      "state": 3,
      "prettyReportStartTime": "2023/07/21 00:00",
      "prettyReportEndTime": "2023/07/22 00:00",
      "prettyRunStartTime": "2023/07/22 00:05",
      "prettyRunEndTime": "2023/07/22 00:05",
      "prettyRunDuration": "5 seconds",
      "prettyRecordCount": "1440"
    }
  ]
}
```

---

## 2. Get Specific Report Template

Retrieves the full configuration details of a single report template by ID.

- **URL:** `GET /api/reports/{id}`
- **Method:** `GET`
- **Path Parameter:**
  - `id` (`int`, required): Numeric database ID of the report template, or `-1` (`Common.NEW_ID`) to obtain a default blank template.
- **Query Parameters:**
  - `copy` (`boolean`, optional, default: `false`): If `true`, resets the ID to `-1` and prepends the localized copy prefix to the report name.

### Response Body (`Code 200 OK` or `404 Not Found`)

Returns the report configuration object (`ReportVO` mapped to JSON).

---

## 3. Create or Update Report Template

Creates a new report template or updates an existing template.

- **URL:** `POST /api/reports` (Create) or `PUT /api/reports/{id}` (Update)
- **Method:** `POST` or `PUT`
- **Request Body (JSON):**

```json
{
  "id": -1,
  "name": "Weekly Summary Report",
  "includeEvents": 2,
  "includeUserComments": true,
  "dateRangeType": 1,
  "relativeDateType": 1,
  "previousPeriodCount": 7,
  "previousPeriodType": 3,
  "schedule": true,
  "schedulePeriod": 0,
  "scheduleCron": "0 0 2 ? * MON",
  "points": [
    {
      "pointId": 101,
      "colour": "#00FF00",
      "consolidatedChart": true
    }
  ]
}
```

### Key Field Enums & Formats

| Field | Enum Values / Mapping | Description |
|---|---|---|
| `includeEvents` | `1` = None<br>`2` = Alarms Only (Urgent & Critical)<br>`3` = All Events & Alarms | Determines alarm/event list included in the report. |
| `dateRangeType` | `1` = Relative Range<br>`2` = Specific Range | Relative uses period counts; Specific uses explicit timestamps. |
| `relativeDateType` | `1` = Previous (completed calendar/time unit)<br>`2` = Past (window back from execution moment) | Mode for relative date calculation. |
| `previousPeriodType`<br>`pastPeriodType` | `1` = Seconds<br>`2` = Minutes<br>`3` = Hours<br>`4` = Days<br>`5` = Weeks<br>`6` = Months<br>`7` = Years | Standard Mango `Common.TimePeriods` units. Legacy frontend offsets (8, 9, 10, 11) are automatically sanitized to 4, 5, 6, 7 by the API. |
| `schedulePeriod` | `0` = Custom Cron Pattern<br>`3` = Hourly<br>`4` = Daily<br>`5` = Weekly<br>`6` = Monthly<br>`7` = Yearly | Automated report execution frequency. |

### Response Body (`Code 200 OK` or `400 Bad Request`)

```json
{
  "success": true,
  "report": {
    "id": 2,
    "userId": 1,
    "name": "Weekly Summary Report",
    "...": "..."
  }
}
```
*(If validation fails, returns HTTP `400` with `"success": false` and an array of localized `errors`)*.

---

## 4. Delete Report Template

Unschedules any associated cron job and deletes the report template from the database.

- **URL:** `DELETE /api/reports/{id}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 2
}
```

---

## 5. Run Report Immediately

Immediately queues a report template for execution in the background.

- **URL:** `POST /api/reports/{id}/run`
- **Method:** `POST`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "message": "Report execution queued successfully"
}
```

---

## 6. Run Ad-Hoc Report

Validates and immediately executes an ad-hoc report using the provided configuration payload without saving a template to the database.

- **URL:** `POST /api/reports/run-ad-hoc`
- **Method:** `POST`
- **Request Body (JSON):** Same schema as saving a report.

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "message": "Ad-hoc report execution queued successfully"
}
```

---

## 7. Create Report Template from WatchList

Generates a new report template populated with all data points from a specific watch list.

- **URL:** `POST /api/reports/from-watchlist/{watchListId}`
- **Method:** `POST`

### Response Body (`Code 200 OK`)

Returns the newly constructed report template object (`id = -1`).

---

## 8. Report Instances Management

### List Report Instances
- **URL:** `GET /api/reports/instances`
- **Description:** Returns all report instances (`ReportInstance`) for the current user.

### Delete Report Instance
- **URL:** `DELETE /api/reports/instances/{instanceId}`
- **Description:** Deletes the report instance, associated historical point data, and event annotations.

### Set Prevent Purge Flag
- **URL:** `PUT /api/reports/instances/{instanceId}/prevent-purge`
- **Request Body:** `{"preventPurge": true}`
- **Description:** Protects or unprotects the report instance from automated background purging.

---

## 9. Report Instance Charts and Exports

### Get Instance Chart HTML
- **URL:** `GET /api/reports/instances/{instanceId}/chart`
- **Description:** Generates and returns the HTML presentation (`chartHtml`) for the report instance and caches chart image bytes into the user session.

### Get Instance Events
- **URL:** `GET /api/reports/instances/{instanceId}/events`
- **Description:** Returns JSON array of all alarms and system events captured during the report window.

### Get Instance Comments
- **URL:** `GET /api/reports/instances/{instanceId}/comments`
- **Description:** Returns JSON array of all user comments recorded on data points and events within the report duration.

### Export Report Data CSV Stream
- **URL:** `GET /api/reports/instances/{instanceId}/export/data`
- **Description:** Directly streams raw historical data points as a downloadable `report_data_{instanceId}.csv` file (`text/csv`).

### Export Events CSV Stream
- **URL:** `GET /api/reports/instances/{instanceId}/export/events`
- **Description:** Streams event/alarm history as a downloadable `report_events_{instanceId}.csv` file (`text/csv`).

### Export Comments CSV Stream
- **URL:** `GET /api/reports/instances/{instanceId}/export/comments`
- **Description:** Streams user comments as a downloadable `report_comments_{instanceId}.csv` file (`text/csv`).
