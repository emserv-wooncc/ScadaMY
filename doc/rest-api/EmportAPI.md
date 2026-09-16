# Emport API Documentation (`/api/emport`)

The **Emport API** (`my.com.emserv.web.api.EmportAPI`) provides RESTful endpoints for full or selective configuration export and asynchronous background import across SiteVisor (the programmatic equivalent of `/emport.shtm`).

- **Source File:** [EmportAPI.java](../../src/my/com/emserv/web/api/EmportAPI.java)
- **Base URL Path:** `/api/emport`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** All endpoints require Admin privileges (`Permissions.ensureAdmin`). Unauthorized attempts return `403 Forbidden`.

---

## 1. Export System Configuration JSON

Generates a JSON string representing configuration data for selected subsystems (e.g., data sources, points, event handlers, users, views).

- **URL:** `POST /api/emport/export`
- **Method:** `POST`
- **Request Body:** Optional boolean options specifying subsystems to include. Defaults to `true` for most configuration items.

```json
{
  "prettyIndent": 3,
  "dataSources": true,
  "dataPoints": true,
  "eventHandlers": true,
  "users": true,
  "pointValues": false,
  "maxPointValues": 100
}
```

### Response Body (`Code 200 OK`)

Returns the formatted JSON export string directly, suitable for saving as a `.json` backup file.

---

## 2. Initiate Background Import

Starts an asynchronous background task (`ImportTask`) to validate and import system configuration from a JSON string.

- **URL:** `POST /api/emport/import`
- **Method:** `POST`
- **Request Body:**

```json
{
  "data": "{\n   \"dataSources\": [...],\n   \"dataPoints\": [...]\n}"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "importStarted": true
}
```

---

## 3. Check Import Status

Polls the progress and validation messages of the currently running background import task.

- **URL:** `GET /api/emport/import/status`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

When ongoing or completed:

```json
{
  "success": true,
  "complete": true,
  "messages": [
    {
      "level": "info",
      "text": "Imported data source: Modbus Master"
    }
  ]
}
```

When no task exists:

```json
{
  "success": true,
  "noImport": true
}
```

---

## 4. Cancel Active Import Task

Cancels the currently running import task if one is active.

- **URL:** `POST /api/emport/import/cancel`
- **Method:** `POST`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "message": "Import cancelled if active"
}
```

---

## 5. Export System Configuration ZIP

Generates a complete ZIP backup of the system configuration, optionally including uploaded files and graphics.

- **URL:** `GET /api/emport/export-zip`
- **Method:** `GET`
- **Query Parameters:**
  - `projectName`: String (required) - The name of the project.
  - `projectDescription`: String (optional) - The description of the project.
  - `includePointValues`: Boolean (optional) - Include historical point values.
  - `pointValuesMaxZip`: Integer (optional) - Max point values to include.
  - `includeUploadsFolder`: Boolean (optional) - Include user uploaded files.
  - `includeGraphicsFolder`: Boolean (optional) - Include custom graphics.

### Response (`Code 200 OK`)

Returns a binary stream (file download) of the generated `.zip` project file containing the `json_project.txt` and any included directories.

---

## 6. Initiate Background ZIP Import

Starts an asynchronous background task (`ImportTask`) to import a complete system configuration from a ZIP archive.

- **URL:** `POST /api/emport/import-zip`
- **Method:** `POST`
- **Content-Type:** `multipart/form-data`
- **Form Data:**
  - `importFile`: The `.zip` file to upload and import.

### Response (`Code 200 OK`)

```json
{
  "success": true,
  "importStarted": true
}
```

*Note: Once started, use the same `/api/emport/import/status` endpoint to poll the progress of the ZIP import.*
