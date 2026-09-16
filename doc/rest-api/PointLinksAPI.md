# Point Links API Documentation (`/api/point-links`)

The **Point Links API** (`my.com.emserv.web.api.PointLinksAPI`) provides RESTful endpoints for querying, creating, updating, deleting, and validating transformation scripts for point-to-point links in SiteVisor (the programmatic equivalent of `/point_links.shtm`).

- **Source File:** [PointLinksAPI.java](../../src/my/com/emserv/web/api/PointLinksAPI.java)
- **Base URL Path:** `/api/point-links`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** Operations require data source permissions (`Permissions.ensureDataSourcePermission`). Unauthorized attempts return `403 Forbidden`.

---

## 1. Get Initialization Context Data

Retrieves accessible source data points, settable target data points, and existing point links.

- **URL:** `GET /api/point-links/init`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "sourcePoints": [
    {
      "id": 101,
      "name": "Outside Temp Sensor"
    }
  ],
  "targetPoints": [
    {
      "id": 201,
      "name": "HVAC Setpoint Controller"
    }
  ],
  "pointLinks": [
    {
      "id": 1,
      "xid": "PL_001",
      "sourcePointId": 101,
      "targetPointId": 201,
      "script": "return source.value + 2.5;",
      "eventModify": 1,
      "disabled": false
    }
  ]
}
```

---

## 2. Get All Point Links

Retrieves a list of all configured point links.

- **URL:** `GET /api/point-links`
- **Method:** `GET`

---

## 3. Get Specific Point Link

Retrieves point link configuration by ID, or returns a default template when ID `-1` is requested.

- **URL:** `GET /api/point-links/{id}`
- **Method:** `GET`

---

## 4. Create or Update Point Link

Registers (`POST /api/point-links`) or updates (`PUT /api/point-links/{id}`) a point link.

- **Request Body:** JSON representation of `PointLinkVO`.

```json
{
  "xid": "PL_HVAC_SYNC",
  "sourcePointId": 101,
  "targetPointId": 201,
  "script": "if (source.value > 30) return 22; else return 24;",
  "eventModify": 1,
  "disabled": false
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "pointLink": {
    "id": 3,
    "xid": "PL_HVAC_SYNC"
  }
}
```

---

## 5. Delete Point Link

Deletes a point link by ID.

- **URL:** `DELETE /api/point-links/{id}`
- **Method:** `DELETE`

---

## 6. Validate Transformation Script

Validates server-side JavaScript against sample source and target data points.

- **URL:** `POST /api/point-links/validate-script`
- **Method:** `POST`
- **Request Body:**

```json
{
  "script": "return source.value * 1.8 + 32;",
  "sourcePointId": 101,
  "targetPointId": 201
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true
}
```
