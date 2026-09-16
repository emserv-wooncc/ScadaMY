# Data Points API Documentation (`/api/data-points`)

The **Data Points API** (`my.com.emserv.web.api.DataPointEditAPI`) provides comprehensive RESTful endpoints for viewing, configuring, updating, toggling, purging, and managing event detectors for individual SiteVisor data points (the direct REST equivalent of `/data_point_edit.shtm`).

- **Source File:** [DataPointEditAPI.java](../../src/my/com/emserv/web/api/DataPointEditAPI.java)
- **Base URL Path:** `/api/data-points`
- **Authentication & Permission:** All endpoints require an active session (`JSESSIONID`) or valid credentials. Operations on individual data points enforce user access permissions on the parent data source (`Permissions.ensureDataSourcePermission`). Non-authenticated requests return `401 Unauthorized`; permission checks failing return `403 Forbidden`.

---

## 1. Get All Accessible Data Points

Retrieves all data points across all data sources that the currently authenticated user has permission to view.

- **URL:** `GET /api/data-points`
- **Method:** `GET`
- **Query Parameters:**
  - `dataSourceId` (integer, optional): Filter returned points to only those belonging to the specified data source ID.

### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 101,
    "xid": "DP_112045",
    "name": "Boiler Temperature",
    "enabled": true,
    "dataSourceId": 1,
    "dataSourceName": "Factory Floor Modbus IP",
    "dataSourceXid": "DS_085934",
    "dataSourceTypeId": 23,
    "deviceName": "PLC-01",
    "settable": false,
    "defaultCacheSize": 1,
    "engineeringUnits": 18,
    "chartColour": "#FF0000",
    "pointLocator": {
      "dataTypeId": 3,
      "dataTypeDescription": "Numeric",
      "settable": false,
      "relinquifiable": false
    },
    "logging": {
      "loggingType": 1,
      "intervalLoggingPeriodType": 1,
      "intervalLoggingPeriod": 1,
      "intervalLoggingType": 1,
      "tolerance": 0.1,
      "discardExtremeValues": false,
      "discardLowLimit": 0.0,
      "discardHighLimit": 100.0
    },
    "purge": {
      "purgeType": 1,
      "purgePeriod": 1
    },
    "eventDetectors": [
      {
        "id": 5,
        "xid": "PED_001",
        "alias": "High Temp Warning",
        "detectorType": 1,
        "alarmLevel": 2,
        "limit": 85.0,
        "duration": 10,
        "durationType": 1,
        "detectorTypeDescription": "High limit"
      }
    ]
  }
]
```

---

## 2. Get Specific Data Point by ID or XID

Retrieves the full configuration and event detector catalog of a specific data point.

- **URL:** `GET /api/data-points/{idOrXid}`
- **Method:** `GET`

### Response Body (`Code 200 OK`)
Returns the single data point object (`404 Not Found` if non-existent, `403 Forbidden` if unauthorized).

---

## 3. Update Data Point Configuration

Updates general properties (`name`, `xid`, `enabled`, `defaultCacheSize`, `engineeringUnits`, `chartColour`), logging properties (`logging`), or purge rules (`purge`) for a data point.

- **URL:** `PUT /api/data-points/{idOrXid}` (or `POST`)
- **Method:** `PUT` or `POST`
- **Request Body:**
```json
{
  "name": "Updated Boiler Temperature",
  "chartColour": "#00FF00",
  "logging": {
    "loggingType": 1,
    "tolerance": 0.5,
    "discardExtremeValues": true,
    "discardLowLimit": -10.0,
    "discardHighLimit": 150.0
  },
  "purge": {
    "purgeType": 3,
    "purgePeriod": 6
  }
}
```

---

## 4. Toggle Data Point Enabled State

Flips the `enabled` status of the data point (`true` <-> `false`) and re-initializes runtime monitoring.

- **URL:** `POST /api/data-points/{idOrXid}/toggle`
- **Method:** `POST`

### Response Body (`Code 200 OK`)
```json
{
  "id": 101,
  "xid": "DP_112045",
  "enabled": false,
  "success": true
}
```

---

## 5. Explicitly Set Data Point Enabled State

- **URL:** `PUT /api/data-points/{idOrXid}/enable?enabled=true`
- **Method:** `PUT`
- **Query Parameters:**
  - `enabled` (boolean, required): `true` to enable, `false` to disable.

---

## 6. Restart Data Point

Convenience endpoint that disables (`enabled=false`), saves, enables (`enabled=true`), and saves the data point immediately.

- **URL:** `POST /api/data-points/{idOrXid}/restart`
- **Method:** `POST`

### Response Body (`Code 200 OK`)
```json
{
  "id": 101,
  "xid": "DP_112045",
  "enabled": true,
  "success": true,
  "message": "Data point restarted successfully"
}
```

---

## 7. Copy Data Point

Clones a data point (including all its event detectors) and assigns it a unique XID. Optionally transfers the copy to a target data source.

- **URL:** `POST /api/data-points/{idOrXid}/copy`
- **Method:** `POST`
- **Query Parameters:**
  - `targetDataSourceId` (integer, optional): If provided, the copied point will be assigned to this target data source instead of its original parent data source.

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "newPointId": 108,
  "newXid": "DP_991823",
  "newName": "Copy of Boiler Temperature",
  "message": "Data point copied successfully"
}
```

---

## 8. Delete Data Point

Permanently deletes the data point along with all historical point values (`pointValues` table) and event detector records.

- **URL:** `DELETE /api/data-points/{idOrXid}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "id": 101,
  "xid": "DP_112045",
  "message": "Data point deleted successfully"
}
```

---

## 9. Purge Point History

Purges logged point values from the historical database for this specific data point.

- **URL:** `POST /api/data-points/{idOrXid}/purge`
- **Method:** `POST`
- **Query Parameters:**
  - `allData` (boolean, optional, default `false`): If `true`, deletes **all** historical point values for this point.
  - `purgeType` (integer, optional, default `1`): Time period type (`1`=Days, `2`=Weeks, `3`=Months, `4`=Years).
  - `purgePeriod` (integer, optional, default `1`): Number of time periods older than which values should be purged.

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "purgedCount": 1420,
  "message": "Purged 1420 point values"
}
```

---

## 10. Clear Runtime Point Cache

Resets the in-memory runtime values cache (`DataPointRT.resetValues()`) for this data point without deleting historical records.

- **URL:** `POST /api/data-points/{idOrXid}/clear-cache`
- **Method:** `POST`

---

## 11. Configure Text Renderer

Updates how values of this data point are rendered to text in user interfaces.

- **URL:** `PUT /api/data-points/{idOrXid}/text-renderer`
- **Method:** `PUT`
- **Request Body Types:**
  - **Analog (`analog`)**: `{ "type": "analog", "format": "#.##", "suffix": " Â°C" }`
  - **Binary (`binary`)**: `{ "type": "binary", "zeroLabel": "Off", "zeroColour": "blue", "oneLabel": "On", "oneColour": "red" }`
  - **Multistate (`multistate`)**: `{ "type": "multistate", "values": [ { "key": 0, "text": "Low", "colour": "blue" }, { "key": 1, "text": "High", "colour": "red" } ] }`
  - **Plain (`plain`)**: `{ "type": "plain", "suffix": " RPM" }`
  - **Range (`range`)**: `{ "type": "range", "format": "#.##", "values": [ { "from": 0, "to": 50, "text": "Normal", "colour": "green" } ] }`
  - **Time (`time`)**: `{ "type": "time", "format": "HH:mm:ss", "conversionExponent": 0 }`
  - **None (`none`)**: `{ "type": "none" }`

---

## 12. Configure Chart Renderer

Updates how historical charts of this data point are generated.

- **URL:** `PUT /api/data-points/{idOrXid}/chart-renderer`
- **Method:** `PUT`
- **Request Body Types:**
  - **Table (`table`)**: `{ "type": "table", "limit": 10 }`
  - **Image (`image`)**: `{ "type": "image", "timePeriod": 1, "numberOfPeriods": 24 }`
  - **Statistics (`statistics`)**: `{ "type": "statistics", "timePeriod": 1, "numberOfPeriods": 7, "includeSum": true }`
  - **Flipbook (`flipbook`)**: `{ "type": "flipbook", "limit": 10 }`
  - **None (`none`)**: `{ "type": "none" }`

---

## 13. Event Detector Management

Manage event detectors attached to this data point (`PointEventDetectorVO`).

### Get All Event Detectors
- **URL:** `GET /api/data-points/{idOrXid}/event-detectors`
- **Method:** `GET`

### Add New Event Detector
- **URL:** `POST /api/data-points/{idOrXid}/event-detectors?typeId={typeId}`
- **Method:** `POST`
- **Detector Type IDs (`typeId`):**
  - `1` = Analog High Limit
  - `2` = Analog Low Limit
  - `3` = Binary State
  - `4` = Multistate State
  - `5` = Point Change
  - `6` = State Change Count
  - `7` = No Change
  - `8` = No Update
  - `9` = Alphanumeric State
  - `10` = Positive CUSUM
  - `11` = Negative CUSUM

### Update Event Detector
- **URL:** `PUT /api/data-points/{idOrXid}/event-detectors/{pedId}`
- **Method:** `PUT`
- **Request Body:**
```json
{
  "alias": "Critical Boiler Overheat",
  "alarmLevel": 3,
  "limit": 95.0,
  "duration": 30,
  "durationType": 1
}
```
*(Alarm levels: `0`=None, `1`=Information, `2`=Urgent, `3`=Critical, `4`=Life Safety)*

### Delete Event Detector
- **URL:** `DELETE /api/data-points/{idOrXid}/event-detectors/{pedId}`
- **Method:** `DELETE`
