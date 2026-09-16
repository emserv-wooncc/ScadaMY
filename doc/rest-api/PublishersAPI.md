# Publishers API Documentation (`/api/publishers`)

The **Publishers API** (`my.com.emserv.web.api.PublishersAPI`) provides RESTful endpoints for querying, creating, updating, toggling, and testing data publishers across SiteVisor (the programmatic equivalent of `/publishers.shtm`).

- **Source File:** [PublishersAPI.java](../../src/my/com/emserv/web/api/PublishersAPI.java)
- **Base URL Path:** `/api/publishers`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** Operations enforce data source permissions (`Permissions.ensureDataSourcePermission`). Unauthorized attempts return `403 Forbidden`.

---

## 1. Get Initialization Context Data

Retrieves available publisher type codes and existing publishers.

- **URL:** `GET /api/publishers/init`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "types": [
    {
      "key": 1,
      "message": "HTTP Sender"
    },
    {
      "key": 2,
      "message": "Pachube / Cosm"
    },
    {
      "key": 3,
      "message": "Persistent TCP"
    }
  ],
  "publishers": []
}
```

---

## 2. Get All Publishers

Retrieves a list of all configured publishers.

- **URL:** `GET /api/publishers`
- **Method:** `GET`

---

## 3. Get Specific Publisher

Retrieves detailed configuration for a specific publisher by ID.

- **URL:** `GET /api/publishers/{id}`
- **Method:** `GET`

---

## 4. Create or Update Publisher

Registers (`POST /api/publishers`) or updates (`PUT /api/publishers/{id}`) a publisher configuration (HTTP, Pachube, or Persistent TCP).

- **Request Body:** JSON object representing `PublisherVO<?>`.

```json
{
  "xid": "PUB_HTTP_01",
  "name": "Cloud Analytics Sync",
  "enabled": true,
  "url": "https://analytics.sitevisor.com/ingest",
  "usePost": true,
  "cacheWarningSize": 100,
  "changesOnly": false,
  "sendSnapshot": true,
  "snapshotSendPeriods": 15,
  "snapshotSendPeriodType": 2
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "publisher": {
    "id": 1,
    "xid": "PUB_HTTP_01",
    "name": "Cloud Analytics Sync"
  }
}
```

---

## 5. Delete Publisher

Deletes a publisher by ID and terminates its runtime instance.

- **URL:** `DELETE /api/publishers/{id}`
- **Method:** `DELETE`

---

## 6. Toggle Enabled Status

Toggles a publisher between enabled and disabled runtime states.

- **URL:** `POST /api/publishers/{id}/toggle`
- **Method:** `POST`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "data": {
    "enabled": false,
    "id": 1
  }
}
```

---

## 7. Initiate HTTP Sender Test

Runs the HTTP sender testing utility against a target URL and parameters.

- **URL:** `POST /api/publishers/test-http-sender`
- **Method:** `POST`
- **Request Body:**

```json
{
  "url": "https://httpbin.org/post",
  "usePost": true,
  "staticHeaders": [
    {
      "key": "Authorization",
      "value": "Bearer token123"
    }
  ],
  "staticParameters": [
    {
      "key": "stationId",
      "value": "MY_01"
    }
  ]
}
```

---

## 8. Get HTTP Sender Test Result

Polls or retrieves the latest execution log/status string from the running HTTP sender test.

- **URL:** `GET /api/publishers/test-http-sender/result`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "result": "HTTP/1.1 200 OK - 42 bytes received."
}
```
