# Data Sources API Documentation (`/api/data-sources`)

The **Data Sources API** (`my.com.emserv.web.api.DataSourcesAPI`) provides comprehensive RESTful endpoints for querying, toggling, copying, deleting, and inspecting SiteVisor data sources and their data points (the direct REST equivalent of `/data_sources.shtm`).

- **Source File:** [DataSourcesAPI.java](../../src/my/com/emserv/web/api/DataSourcesAPI.java)
- **Base URL Path:** `/api/data-sources`
- **Authentication & Permission:** All endpoints require an active session (`JSESSIONID`) or valid credentials. Operations on individual data sources enforce strict user access permissions (`Permissions.ensureDataSourcePermission`). Non-authenticated requests return `401 Unauthorized`; permission checks failing return `403 Forbidden`.

---

## 1. Get All Accessible Data Sources

Retrieves all data sources configured in the system that the currently authenticated user has permission to access.

- **URL:** `GET /api/data-sources`
- **Method:** `GET`
- **Query Parameters:**
  - `includePoints` (boolean, optional, default: `false`): If `true`, embeds the complete list of data points (`DataPointVO`) within each returned data source object.
  - `type` (string, optional): Filters the list by data source protocol type name (e.g., `MODBUS_IP`, `BACNET`, `VIRTUAL`).

### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 1,
    "xid": "DS_085934",
    "name": "Factory Floor Modbus IP",
    "enabled": true,
    "type": "MODBUS_IP",
    "typeId": 23,
    "typeKey": "dsEdit.modbusIp",
    "typeDescription": "Modbus IP",
    "connectionDescription": "192.168.1.100:502",
    "pointCount": 8,
    "points": [
      {
        "id": 101,
        "xid": "DP_112045",
        "name": "Boiler Temperature",
        "enabled": true,
        "dataSourceId": 1,
        "deviceName": "PLC-01",
        "settable": false,
        "dataTypeId": 3,
        "chartColour": "#FF0000",
        "engineeringUnits": 18
      }
    ]
  }
]
```

---

## 2. Get Supported Data Source Types

Retrieves the catalog of supported data source protocol types that can be created in SiteVisor.

- **URL:** `GET /api/data-sources/types`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 23,
    "name": "MODBUS_IP",
    "key": "dsEdit.modbusIp",
    "description": "Modbus IP"
  },
  {
    "id": 10,
    "name": "BACNET",
    "key": "dsEdit.bacnetIp",
    "description": "BACnet IP"
  },
  {
    "id": 20,
    "name": "VIRTUAL",
    "key": "dsEdit.virtual",
    "description": "Virtual Data Source"
  }
]
```

---

## 3. Get Specific Data Source by ID or XID

Retrieves detailed information for a specific data source identified either by its numeric `id` (e.g. `1`) or string `xid` (e.g. `DS_085934`).

- **URL:** `GET /api/data-sources/{idOrXid}`
- **Method:** `GET`
- **Query Parameters:**
  - `includePoints` (boolean, optional): If `true`, the `points` array will be populated with data points under this data source. Default is `false`.

### Response Body (`Code 200 OK`)
Returns the single data source object with `points` array populated. Returns `404 Not Found` if non-existent, or `403 Forbidden` if unauthorized.

---

## 4. Get Data Points under a Data Source

Retrieves only the list of data points belonging to the specified data source.

- **URL:** `GET /api/data-sources/{idOrXid}/points`
- **Method:** `GET`

### Response Body (`Code 200 OK`)
```json
[
  {
    "id": 101,
    "xid": "DP_112045",
    "name": "Boiler Temperature",
    "enabled": true,
    "dataSourceId": 1,
    "deviceName": "PLC-01",
    "settable": false,
    "dataTypeId": 3,
    "chartColour": "#FF0000",
    "engineeringUnits": 18
  }
]
```

---

## 5. Toggle Data Source Enabled State

Flips the current `enabled` status of the data source (`true` <-> `false`) and re-initializes or stops the runtime polling engine accordingly.

- **URL:** `POST /api/data-sources/{idOrXid}/toggle`
- **Method:** `POST`

### Response Body (`Code 200 OK`)
```json
{
  "id": 1,
  "xid": "DS_085934",
  "enabled": false,
  "success": true
}
```

---

## 6. Explicitly Set Data Source Enabled State

Explicitly enables (`enabled=true`) or disables (`enabled=false`) a data source.

- **URL:** `PUT /api/data-sources/{idOrXid}/enable?enabled=true`
- **Method:** `PUT`
- **Query Parameters:**
  - `enabled` (boolean, required): `true` to start the data source, `false` to stop it.

---

## 7. Copy Data Source

Clones an existing data source along with its configuration and assigns it a newly generated unique XID. Also updates user access permissions so the creator immediately has access.

- **URL:** `POST /api/data-sources/{idOrXid}/copy`
- **Method:** `POST`

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "newDataSourceId": 15,
  "newXid": "DS_884102",
  "newName": "Copy of Factory Floor Modbus IP",
  "message": "Data source copied successfully"
}
```

---

## 8. Delete Data Source

Permanently deletes the data source and all associated data points and history values from the system.

- **URL:** `DELETE /api/data-sources/{idOrXid}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "id": 1,
  "xid": "DS_085934",
  "message": "Data source deleted successfully"
}
```

---

## 9. Toggle Specific Data Point Enabled State

Flips the `enabled` status (`true` <-> `false`) of an individual data point (`pointIdOrXid`) under the specified data source (`idOrXid`).

- **URL:** `POST /api/data-sources/{idOrXid}/points/{pointIdOrXid}/toggle`
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

## 10. Enable All Data Points under Data Source

Convenience endpoint that scans all data points belonging to this data source and enables any that are currently disabled.

- **URL:** `POST /api/data-sources/{idOrXid}/points/enable-all`
- **Method:** `POST`

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "enabledCount": 4,
  "message": "Enabled 4 previously disabled data points"
}
```

---

## 11. Create Data Source

Creates a new data source from a JSON representation. The format of the JSON is identical to the format used in ScadaBR's system export.

- **URL:** `POST /api/data-sources`
- **Method:** `POST`
- **Request Body (`application/json`):**
  - Must include the `"type"` property (e.g., `VIRTUAL`, `MODBUS_IP`, etc.).
  - Other properties vary depending on the `type`.
  - If `"xid"` is omitted, a unique XID will be generated automatically.

### Example Request Body

```json
{
  "xid": "DS_MyNewVirtualSource",
  "name": "My New Virtual Source",
  "type": "VIRTUAL",
  "updatePeriods": 1,
  "updatePeriodType": "SECONDS"
}
```

### Response Body (`Code 201 Created` or `200 OK`)
```json
{
  "success": true,
  "id": 16,
  "xid": "DS_MyNewVirtualSource",
  "name": "My New Virtual Source",
  "message": "Data source created successfully"
}
```

---

## 12. Update Data Source

Updates an existing data source from a JSON representation. The payload uses the exact same structure as the creation endpoint, but it updates the target data source instead.

- **URL:** `PUT /api/data-sources/{idOrXid}`
- **Method:** `PUT`
- **Request Body (`application/json`):**
  - Should include the updated properties.
  - The `"type"` property is generally ignored since you cannot change the type of an existing data source.
  - The `"xid"` property can be provided to rename the XID, provided it does not conflict with another existing data source.

### Example Request Body

```json
{
  "xid": "DS_MyUpdatedVirtualSource",
  "name": "My Updated Virtual Source",
  "updatePeriods": 5,
  "updatePeriodType": "MINUTES"
}
```

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "id": 16,
  "xid": "DS_MyUpdatedVirtualSource",
  "name": "My Updated Virtual Source",
  "message": "Data source updated successfully"
}
```

---

## 13. Create Data Point

Creates a new telemetry data point under a specified data source.

- **URL:** `POST /api/data-points`
- **Method:** `POST`
- **Request Body (`application/json`):**
  - `dataSourceId` (number, required): Numeric ID of the parent data source.
  - `name` (string, required): Descriptive name of the data point.
  - `xid` (string, optional): Unique export ID. Auto-generated if omitted.
  - `deviceName` (string, optional): Target device/PLC identifier.
  - `dataTypeId` (number, optional, default `3`): Data type ID (`1`=Binary, `2`=Multistate, `3`=Numeric, `4`=Alphanumeric).
  - `settable` (boolean, optional, default `false`): Read/Write permission flag.
  - `engineeringUnits` (number, optional): Unit code.

### Request Payload Example
```json
{
  "dataSourceId": 1,
  "name": "Boiler Temperature Sensor",
  "xid": "DP_BOILER_01",
  "deviceName": "PLC-01",
  "dataTypeId": 3,
  "settable": false,
  "engineeringUnits": 18
}
```

### Response Body (`Code 201 Created`)
```json
{
  "id": 105,
  "xid": "DP_BOILER_01",
  "name": "Boiler Temperature Sensor",
  "enabled": false,
  "dataSourceId": 1,
  "deviceName": "PLC-01",
  "settable": false,
  "dataTypeId": 3,
  "engineeringUnits": 18
}
```

---

## 14. Update Data Point Details

Updates core configuration parameters of an existing data point.

- **URL:** `PUT /api/data-points/{idOrXid}`
- **Method:** `PUT`
- **Request Body (`application/json`):**
  - Accepts updated fields: `name`, `xid`, `deviceName`, `settable`, `dataTypeId`, `engineeringUnits`, `chartColour`, `logging`, `purge`.

### Request Payload Example
```json
{
  "name": "Boiler Temperature Sensor (Updated)",
  "settable": true,
  "deviceName": "PLC-02"
}
```

### Response Body (`Code 200 OK`)
```json
{
  "id": 105,
  "xid": "DP_BOILER_01",
  "name": "Boiler Temperature Sensor (Updated)",
  "enabled": false,
  "dataSourceId": 1,
  "deviceName": "PLC-02",
  "settable": true,
  "dataTypeId": 3
}
```

