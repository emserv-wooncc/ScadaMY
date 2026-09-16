# Scheduled Events API Documentation (`/api/scheduled-events`)

The **Scheduled Events API** (`my.com.emserv.web.api.ScheduledEventsAPI`) provides RESTful endpoints for querying, creating, updating, and deleting scheduled events in SiteVisor (the programmatic equivalent of `/scheduled_events.shtm`).

- **Source File:** [ScheduledEventsAPI.java](../../src/my/com/emserv/web/api/ScheduledEventsAPI.java)
- **Base URL Path:** `/api/scheduled-events`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** Operations require data source read/write permissions (`Permissions.ensureDataSourcePermission`). Unauthorized attempts return `403 Forbidden`.

---

## 1. Get All Scheduled Events

Retrieves a list of all scheduled events currently defined in the system.

- **URL:** `GET /api/scheduled-events`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 1,
    "xid": "SE_001",
    "alias": "Daily Report Trigger",
    "alarmLevel": 1,
    "scheduleType": 3,
    "returnToNormal": true,
    "disabled": false,
    "activeYear": 2026,
    "activeMonth": 7,
    "activeDay": 21,
    "activeHour": 8,
    "activeMinute": 0,
    "activeSecond": 0,
    "activeCron": "0 0 8 * * ?",
    "inactiveYear": 2026,
    "inactiveMonth": 7,
    "inactiveDay": 21,
    "inactiveHour": 9,
    "inactiveMinute": 0,
    "inactiveSecond": 0,
    "inactiveCron": "0 0 9 * * ?"
  }
]
```

---

## 2. Get Specific Scheduled Event

Retrieves configuration details for a specific scheduled event by ID, or returns a default template when ID `-1` (`Common.NEW_ID`) is requested.

- **URL:** `GET /api/scheduled-events/{id}`
- **Method:** `GET`
- **URL Parameters:** `id` (integer) - The unique internal ID (`-1` for new default template)

### Response Body (`Code 200 OK`)

```json
{
  "id": 1,
  "xid": "SE_001",
  "alias": "Daily Report Trigger",
  "alarmLevel": 1,
  "scheduleType": 3,
  "returnToNormal": true,
  "disabled": false
}
```

---

## 3. Create Scheduled Event

Creates and registers a new scheduled event.

- **URL:** `GET /api/scheduled-events` (POST)
- **Method:** `POST`
- **Request Body:** JSON object representing `ScheduledEventVO`.

```json
{
  "xid": "SE_NEW_01",
  "alias": "Hourly System Check",
  "alarmLevel": 2,
  "scheduleType": 1,
  "returnToNormal": true,
  "disabled": false,
  "activeMinute": 0,
  "activeSecond": 0,
  "inactiveMinute": 30,
  "inactiveSecond": 0
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "scheduledEvent": {
    "id": 5,
    "xid": "SE_NEW_01",
    "alias": "Hourly System Check"
  }
}
```

---

## 4. Update Scheduled Event

Updates an existing scheduled event.

- **URL:** `PUT /api/scheduled-events/{id}`
- **Method:** `PUT`
- **URL Parameters:** `id` (integer) - The unique internal ID of the event to modify.

---

## 5. Delete Scheduled Event

Deletes a scheduled event by ID and stops its background schedule timer.

- **URL:** `DELETE /api/scheduled-events/{id}`
- **Method:** `DELETE`
- **URL Parameters:** `id` (integer) - The unique internal ID of the event to delete.

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 5
}
```

---

## 6. Toggle or Set Disabled State

Toggles or sets the active/disabled status of a scheduled event.

- **URL:** `PUT /api/scheduled-events/{id}/disabled`
- **Method:** `PUT`
- **URL Parameters:** `id` (integer) - The unique internal ID of the scheduled event.
- **Request Body (Optional):**

```json
{
  "disabled": true
}
```

If the `disabled` property is omitted from the request body, the endpoint toggles the current state automatically.

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 5,
  "disabled": true
}
```

