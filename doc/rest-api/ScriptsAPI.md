# Scripts API Documentation (`/api/scripts`)

The **Scripts API** (`my.com.emserv.web.api.ScriptsAPI`) provides full RESTful endpoints for configuring, querying, and immediately executing Server-Side JavaScript programs (the equivalent of `/scripting.shtm`).

- **Source File:** [ScriptsAPI.java](../../src/my/com/emserv/web/api/ScriptsAPI.java)
- **Base URL Path:** `/api/scripts`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.

---

## 1. Get Script Initialization Context

Retrieves available initialization context including all readable data points (for point variable binding) and available system objects (`DATASOURCE_COMMANDS`, `DATAPOINT_COMMANDS`).

- **URL:** `GET /api/scripts/init`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "dataPoints": [
    {
      "id": 142,
      "xid": "DP_8831",
      "name": "Modbus Master - Main Pump Temperature",
      "dataType": 3,
      "settable": true
    }
  ],
  "availableObjects": [
    {
      "id": 1,
      "name": "DATASOURCE_COMMANDS",
      "key": "script.dsCommands",
      "defaultVarName": "dsCommands",
      "help": "scriptDSObject"
    },
    {
      "id": 2,
      "name": "DATAPOINT_COMMANDS",
      "key": "script.dpCommands",
      "defaultVarName": "dpCommands",
      "help": "scriptDPObject"
    }
  ]
}
```

---

## 2. List All Configured Scripts

Retrieves a summary list of all scripts currently configured in the system.

- **URL:** `GET /api/scripts`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 1,
    "xid": "SC_28419",
    "name": "Daily Summary Script",
    "script": "var val = p1.value;\nif (val > 100) {\n  // do something\n}",
    "userId": 1,
    "pointsOnContext": [
      {
        "varName": "p1",
        "dataPointId": 142,
        "dataPointXid": "DP_8831",
        "dataPointName": "Main Pump Temperature"
      }
    ],
    "objectsOnContext": [
      {
        "varName": "dsCommands",
        "objectId": 1,
        "objectType": "DATASOURCE_COMMANDS"
      }
    ]
  }
]
```

---

## 2. Get Specific Script Details

Retrieves the full configuration details of a single script by its numeric ID.

- **URL:** `GET /api/scripts/{id}`
- **Method:** `GET`
- **Path Parameter:**
  - `id` (`int`, required): The numeric database ID of the script.

### Response Body (`Code 200 OK` or `404 Not Found`)

Returns the exact same JSON object representation as seen in the list endpoint above.

---

## 3. Create a New Script

Creates and validates a new server-side script with context variables.

- **URL:** `POST /api/scripts`
- **Method:** `POST`
- **Content-Type:** `application/json`

### Request Body Example

```json
{
  "xid": "SC_CUSTOM_01",
  "name": "Auto Reset Pump",
  "script": "if (tempSensor.value > 80) {\n  pumpCommand.set(0);\n}",
  "pointsOnContext": [
    {
      "varName": "tempSensor",
      "dataPointXid": "DP_TEMP_01"
    },
    {
      "varName": "pumpCommand",
      "dataPointId": 105
    }
  ],
  "objectsOnContext": [
    {
      "varName": "dpCmd",
      "objectType": "DATAPOINT_COMMANDS"
    }
  ]
}
```

### Request Field Descriptions

| Field | Type | Description |
| :--- | :--- | :--- |
| **`xid`** | `String` (Optional) | Unique external identifier. If omitted, the server automatically generates one (`SC_XXXX`). |
| **`name`** | `String` | Human-readable title of the script. |
| **`script`** | `String` | The raw JavaScript source code to execute. |
| **`pointsOnContext`** | `Array` (Optional) | List of data points bound as variables inside the script environment. For each point, supply `varName` along with either `dataPointId` (int) or `dataPointXid` (String). |
| **`objectsOnContext`** | `Array` (Optional) | List of system wrapper tools exposed to the script. Supply `varName` along with either `objectType` (`"DATASOURCE_COMMANDS"` or `"DATAPOINT_COMMANDS"`) or numeric `objectId` (`1` or `2`). |

### Response Body (`Code 200 OK`)
Returns the newly created script details including its assigned `id`.

If validation fails (e.g. missing point bindings or syntax errors), returns `400 Bad Request` with an array of validation `messages`.

---

## 4. Update an Existing Script

Updates an existing script configuration by ID.

- **URL:** `PUT /api/scripts/{id}`
- **Method:** `PUT`
- **Content-Type:** `application/json`
- **Path Parameter:** `id` (`int`, required)

Accepts the same JSON request body schema as **Create a New Script** (`POST /api/scripts`).

---

## 5. Delete a Script

Permanently deletes a script from the system.

- **URL:** `DELETE /api/scripts/{id}`
- **Method:** `DELETE`
- **Path Parameter:** `id` (`int`, required)

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "id": 1
}
```

---

## 6. Execute a Script Immediately

Triggers on-demand execution of the specified script right away and returns the execution status.

- **URL:** `POST /api/scripts/{id}/execute`
- **Method:** `POST`
- **Path Parameter:** `id` (`int`, required)

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "message": "Script executed successfully"
}
```

If a runtime JavaScript exception or context binding failure occurs (`Code 500 Internal Server Error`):
```json
{
  "success": false,
  "error": "ReferenceError: \"tempSensor\" is not defined"
}
```
