# Users API Documentation (`/api/users`)

The **Users API** (`my.com.emserv.web.api.UsersAPI`) provides RESTful endpoints for querying, creating, updating, deleting, and testing email configurations for user accounts across SiteVisor (the programmatic equivalent of `/users.shtm`).

- **Source File:** [UsersAPI.java](../../src/my/com/emserv/web/api/UsersAPI.java)
- **Base URL Path:** `/api/users`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** Listing, creating, or deleting users requires Admin privileges (`Permissions.ensureAdmin`). Users can view and update their own profile information (`password`, `email`, `phone`, alarm options) via `PUT /api/users/{id}` where `{id}` matches their own user ID.

---

## 1. Get Initialization Context Data

Retrieves users list, user profiles, and data source/point permission structures.

- **URL:** `GET /api/users/init`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "admin": true,
  "users": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@sitevisor.com",
      "admin": true,
      "disabled": false
    }
  ],
  "usersProfiles": [
    {
      "id": 2,
      "name": "Operator Profile"
    }
  ],
  "dataSources": []
}
```

---

## 2. Get All Users (Admin Only)

Retrieves all permissioned users and their attached profiles.

- **URL:** `GET /api/users`
- **Method:** `GET`

---

## 3. Get Specific User Details

Retrieves details for a specific user ID, or returns a blank user structure when ID `-1` (`Common.NEW_ID`) is requested.

- **URL:** `GET /api/users/{id}`
- **Method:** `GET`

---

## 4. Create or Update User

Registers (`POST /api/users`) or updates (`PUT /api/users/{id}`) a user account. Admins can update any user and set permissions; non-admin users can update their own email, phone, password, and notification options.

- **Request Body:**

```json
{
  "username": "operator1",
  "password": "SecurePassword123!",
  "email": "operator1@sitevisor.com",
  "phone": "+60123456789",
  "admin": false,
  "disabled": false,
  "receiveAlarmEmails": 1,
  "receiveOwnAuditEvents": true,
  "usersProfileId": 2,
  "dataSourcePermissions": [1, 2],
  "dataPointPermissions": [
    {
      "dataPointId": 101,
      "permission": 2
    }
  ]
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "data": {
    "userId": 5
  }
}
```

---

## 5. Delete User (Admin Only)

Deletes a user account. System safeguards prevent admins from deleting their own currently logged-in account.

- **URL:** `DELETE /api/users/{id}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 5
}
```

---

## 6. Send Test Email (Admin Only)

Sends a test email to verify SMTP configuration and user email reachability.

- **URL:** `POST /api/users/test-email`
- **Method:** `POST`
- **Request Body:**

```json
{
  "email": "operator1@sitevisor.com",
  "username": "operator1"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "message": "Test email queued for sending to operator1@sitevisor.com"
}
```

---

## 7. Update Home Page URL

Updates the default home page URL (`homeUrl`) preference for the currently authenticated user.

- **URL:** `PUT /api/users/home-url`
- **Method:** `PUT`
- **Request Body:**

```json
{
  "homeUrl": "watch_list.shtm"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "homeUrl": "watch_list.shtm"
}
```

