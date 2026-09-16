# User Profiles API Documentation (`/api/user-profiles`)

The **User Profiles API** (`my.com.emserv.web.api.UserProfilesAPI`) provides RESTful endpoints for querying, creating, updating, and deleting reusable role/permission templates across SiteVisor (the programmatic equivalent of `/usersProfiles.shtm`).

- **Source File:** [UserProfilesAPI.java](../../src/my/com/emserv/web/api/UserProfilesAPI.java)
- **Base URL Path:** `/api/user-profiles`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** All endpoints require Admin privileges (`Permissions.ensureAdmin`). Unauthorized requests return `403 Forbidden`.

---

## 1. Get Initialization Context Data

Retrieves user profiles, data sources, watchlists, and views along with their access level codes.

- **URL:** `GET /api/user-profiles/init`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "admin": true,
  "profiles": [
    {
      "id": 2,
      "name": "Operator Profile"
    }
  ],
  "dataSources": [],
  "watchlists": [],
  "views": []
}
```

---

## 2. Get All User Profiles

Retrieves all configured user profiles in the system.

- **URL:** `GET /api/user-profiles`
- **Method:** `GET`

---

## 3. Get Specific User Profile

Retrieves details for a specific user profile ID, or returns a blank profile template when ID `-1` is requested.

- **URL:** `GET /api/user-profiles/{id}`
- **Method:** `GET`

---

## 4. Create or Update User Profile

Registers (`POST /api/user-profiles`) or updates (`PUT /api/user-profiles/{id}`) a user profile and applies permission templates across data sources, points, watchlists, and graphical views.

- **Request Body:**

```json
{
  "name": "Senior Operator",
  "dataSourcePermissions": [1],
  "dataPointPermissions": [
    {
      "dataPointId": 101,
      "permission": 2
    }
  ],
  "watchlistPermissions": [
    {
      "watchlistId": 3,
      "permission": 2
    }
  ],
  "viewsPermissions": [
    {
      "viewId": 1,
      "permission": 1
    }
  ]
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "data": {
    "userProfileId": 4
  }
}
```

---

## 5. Delete User Profile

Deletes a user profile and detaches it from assigned users.

- **URL:** `DELETE /api/user-profiles/{id}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 4
}
```
