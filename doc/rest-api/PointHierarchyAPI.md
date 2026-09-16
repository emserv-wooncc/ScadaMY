# Point Hierarchy API Documentation (`/api/point-hierarchy`)

The **Point Hierarchy API** (`my.com.emserv.web.api.PointHierarchyAPI`) provides RESTful endpoints for retrieving and saving the folder tree structure that organizes data points across SiteVisor (the programmatic equivalent of `/point_hierarchy.shtm`).

- **Source File:** [PointHierarchyAPI.java](../../src/my/com/emserv/web/api/PointHierarchyAPI.java)
- **Base URL Path:** `/api/point-hierarchy`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** Operations require data source permissions (`Permissions.ensureDataSourcePermission`). Unauthorized requests return `403 Forbidden`.

---

## 1. Get Point Hierarchy Root Folder

Retrieves the complete folder hierarchy tree starting from the root folder (`PointFolder`).

- **URL:** `GET /api/point-hierarchy`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "id": 0,
  "name": "root",
  "points": [
    {
      "key": 101,
      "value": "Outside Temp Sensor"
    }
  ],
  "subfolders": [
    {
      "id": 1,
      "name": "Building A",
      "points": [
        {
          "key": 102,
          "value": "Lobby HVAC"
        }
      ],
      "subfolders": []
    }
  ]
}
```

---

## 2. Save Point Hierarchy Structure

Persists a modified point hierarchy structure by updating the root folder tree (`PointFolder`).

- **URL:** `POST /api/point-hierarchy`
- **Method:** `POST`
- **Request Body:** JSON representation of the complete root `PointFolder` structure containing `subfolders` and `points`.

```json
{
  "id": 0,
  "name": "root",
  "points": [],
  "subfolders": [
    {
      "id": 1,
      "name": "Building A - Main Floor",
      "points": [
        {
          "key": 101,
          "value": "Outside Temp Sensor"
        },
        {
          "key": 102,
          "value": "Lobby HVAC"
        }
      ],
      "subfolders": []
    }
  ]
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "rootFolder": {
    "id": 0,
    "name": "Root",
    "subfolders": [
      {
        "id": 1,
        "name": "Building A - Main Floor"
      }
    ]
  }
}
```
