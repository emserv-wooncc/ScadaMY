# Mailing Lists API Documentation (`/api/mailing-lists`)

The **Mailing Lists API** (`my.com.emserv.web.api.MailingListsAPI`) provides RESTful endpoints for querying, creating, updating, deleting, and testing recipient groups for event notifications across SiteVisor (the programmatic equivalent of `/mailing_lists.shtm`).

- **Source File:** [MailingListsAPI.java](../../src/my/com/emserv/web/api/MailingListsAPI.java)
- **Base URL Path:** `/api/mailing-lists`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.

---

## 1. Get Initialization Context Data

Retrieves existing mailing lists along with all permissioned users available for candidate recipient entries.

- **URL:** `GET /api/mailing-lists/init`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "lists": [
    {
      "id": 1,
      "xid": "ML_001",
      "name": "On-Duty Operators"
    }
  ],
  "users": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@sitevisor.com"
    }
  ]
}
```

---

## 2. Get All Mailing Lists

Retrieves a list of all configured mailing lists.

- **URL:** `GET /api/mailing-lists`
- **Method:** `GET`

---

## 3. Get Specific Mailing List

Retrieves mailing list configuration and recipient entries by ID, or returns a blank template when ID `-1` is requested.

- **URL:** `GET /api/mailing-lists/{id}`
- **Method:** `GET`

---

## 4. Create or Update Mailing List

Registers (`POST /api/mailing-lists`) or updates (`PUT /api/mailing-lists/{id}`) a mailing list.

- **Request Body:**

```json
{
  "xid": "ML_MAINT_TEAM",
  "name": "Maintenance Technicians",
  "inactiveIntervals": [1, 2],
  "entries": [
    {
      "recipientType": 2,
      "referenceId": 2
    },
    {
      "recipientType": 3,
      "referenceAddress": "external-contractor@company.com"
    }
  ]
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "data": {
    "mlId": 3
  }
}
```

---

## 5. Delete Mailing List

Deletes a mailing list by ID. If the list is currently referenced by any active event handler (`EventHandlerVO`), deletion is blocked and code `409 Conflict` is returned.

- **URL:** `DELETE /api/mailing-lists/{id}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK` vs `409 Conflict`)

```json
{
  "success": false,
  "error": "Cannot delete mailing list because it is currently in use by one or more event handlers"
}
```

---

## 6. Send Test Email to List Entries

Queues a test notification email to all resolved recipient addresses in the provided candidate entry list.

- **URL:** `POST /api/mailing-lists/test-email`
- **Method:** `POST`
- **Request Body:**

```json
{
  "id": 1,
  "name": "On-Duty Operators",
  "entries": [
    {
      "recipientType": 3,
      "referenceAddress": "operator-desk@sitevisor.com"
    }
  ]
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "message": "Test email queued successfully"
}
```
