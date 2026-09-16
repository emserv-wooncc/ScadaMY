# Authentication API

This document details the REST API endpoints provided by `AuthAPI.java` for managing user authentication sessions.

**Base Path:** `/api/auth`

---

## 1. Check Login Status
**Endpoint:** `/status`  
**Method:** `GET`  
**Description:** Returns the current authentication status of the user's session.

### Response `200 OK`
A JSON object indicating the login status. If the user is logged in, it will include their details and permissions.

| Field | Type | Description |
| :--- | :--- | :--- |
| `loggedIn` | `boolean` | True if the user is authenticated in the current session. |
| `username` | `string` | The user's username (only if loggedIn is true). |
| `userId` | `integer` | The user's internal ID (only if loggedIn is true). |
| `isAdmin` | `boolean` | True if the user has Administrator privileges (only if loggedIn is true). |
| `hasDataSourcePermission` | `boolean` | True if the user has Data Source permissions (only if loggedIn is true). |
| `homeUrl` | `string` | The user's configured home URL. |
| `evDiag` | `object` | Diagnostic information for Events (only returned if application context is successfully interrogated). |

---

## 2. Login
**Endpoint:** `/login`  
**Method:** `POST`  
**Description:** Authenticates a user using credentials. If successful, establishes a session cookie.

### Request Payload
**Content-Type:** `application/json`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `username` | `string` | Yes | The user's username. |
| `password` | `string` | Yes | The user's plain-text password (hashed internally). |

### Responses

#### `200 OK`
Returned when login is successful.

| Field | Type | Description |
| :--- | :--- | :--- |
| `success` | `boolean` | True indicating the login succeeded. |
| `username` | `string` | The user's username. |
| `isAdmin` | `boolean` | True if the user has Administrator privileges. |
| `hasDataSourcePermission` | `boolean` | True if the user has Data Source permissions. |
| `homeUrl` | `string` | The user's configured home URL. |

#### `400 Bad Request`
Returned if username or password are missing in the request.

#### `401 Unauthorized`
Returned if the username is not found, the password is incorrect, or the user is disabled.

| Field | Type | Description |
| :--- | :--- | :--- |
| `success` | `boolean` | False. |
| `error` | `string` | "Invalid username or password". |

---

## 3. Logout
**Endpoint:** `/logout`  
**Method:** `POST`  
**Description:** Invalidates the current user's HTTP session.

### Response `200 OK`

| Field | Type | Description |
| :--- | :--- | :--- |
| `success` | `boolean` | Always true. |
