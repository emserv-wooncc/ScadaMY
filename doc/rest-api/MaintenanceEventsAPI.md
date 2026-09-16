# Maintenance Events API Documentation (`/api/maintenance-events`)

The **Maintenance Events API** (`my.com.emserv.web.api.MaintenanceEventsAPI`) provides RESTful endpoints for querying, creating, updating, deleting, and toggling scheduled maintenance periods across SiteVisor data sources (the programmatic equivalent of `/maintenance_events.shtm`). During maintenance events, event detectors and alarms on target data sources are suppressed or downgraded to prevent false notifications while maintenance is actively underway.

- **Source File:** [MaintenanceEventsAPI.java](../../src/my/com/emserv/web/api/MaintenanceEventsAPI.java)
- **Base URL Path:** `/api/maintenance-events`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** All endpoints require Admin privileges (`Permissions.ensureAdmin`). Unauthorized attempts return `403 Forbidden`.

---

## 1. Get All Maintenance Events

Retrieves all configured maintenance events (enriched with their current live `active` execution status and target data source name) along with all available system data sources.

- **URL:** `GET /api/maintenance-events`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "events": [
    {
      "id": 1,
      "xid": "ME_001",
      "dataSourceId": 2,
      "dataSourceName": "Modbus Master - Plant 1",
      "alias": "Weekly Boiler Calibration",
      "alarmLevel": 1,
      "scheduleType": 4,
      "disabled": false,
      "active": false,
      "activeYear": 2026,
      "activeMonth": 9,
      "activeDay": 1,
      "activeHour": 8,
      "activeMinute": 0,
      "activeSecond": 0,
      "activeCron": "",
      "inactiveYear": 2026,
      "inactiveMonth": 9,
      "inactiveDay": 5,
      "inactiveHour": 17,
      "inactiveMinute": 0,
      "inactiveSecond": 0,
      "inactiveCron": ""
    }
  ],
  "dataSources": [
    {
      "id": 2,
      "name": "Modbus Master - Plant 1"
    }
  ]
}
```

### Field Definitions

| Field | Type | Description |
| :--- | :--- | :--- |
| `events` | Array | List of configured maintenance event objects. |
| `events[].id` | Integer | Unique identifier of the maintenance event. |
| `events[].xid` | String | Unique external identifier. |
| `events[].dataSourceId`| Integer | ID of target data source whose alarms are suppressed. |
| `events[].dataSourceName` | String | Resolved name of target data source. |
| `events[].alias` | String | User-assigned human-friendly title/description. |
| `events[].alarmLevel` | Integer | System alarm level raised when maintenance starts (`0=None`, `1=Info`, `2=Urgent`, `3=Critical`, `4=Life Safety`). |
| `events[].scheduleType` | Integer | Schedule type: `1=MANUAL`, `2=HOURLY`, `3=DAILY`, `4=WEEKLY`, `5=MONTHLY`, `6=YEARLY`, `7=ONCE`, `8=CRON`. |
| `events[].disabled` | Boolean | Whether this maintenance event rule is disabled. |
| `events[].active` | Boolean | Live runtime status: `true` if maintenance suppression is currently active in the runtime engine. |
| `dataSources` | Array | Available data sources for dropdown selection. |

---

## 2. Get Specific Maintenance Event

Retrieves detailed configuration and current live activation state (`activated`) for a maintenance event ID, or returns a default template when ID `-1` is requested.

- **URL:** `GET /api/maintenance-events/{id}`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "me": {
    "id": 1,
    "xid": "ME_001",
    "dataSourceId": 2,
    "alias": "Weekly Boiler Calibration",
    "alarmLevel": 1,
    "scheduleType": 4,
    "disabled": false,
    "activeYear": 2026,
    "activeMonth": 9,
    "activeDay": 1,
    "activeHour": 8,
    "activeMinute": 0,
    "activeSecond": 0,
    "activeCron": "",
    "inactiveYear": 2026,
    "inactiveMonth": 9,
    "inactiveDay": 5,
    "inactiveHour": 17,
    "inactiveMinute": 0,
    "inactiveSecond": 0,
    "inactiveCron": ""
  },
  "activated": false
}
```

---

## 3. Create or Update Maintenance Event

Registers (`POST /api/maintenance-events`) or updates (`PUT /api/maintenance-events/{id}`) a maintenance event.

- **Request Body:** JSON representation of `MaintenanceEventVO`.

```json
{
  "xid": "ME_MODBUS_MAINT",
  "dataSourceId": 2,
  "alias": "Monthly Sensor Swap",
  "alarmLevel": 0,
  "scheduleType": 5,
  "disabled": false,
  "activeDay": 1,
  "activeHour": 2,
  "activeMinute": 0,
  "activeSecond": 0,
  "inactiveDay": 1,
  "inactiveHour": 6,
  "inactiveMinute": 0,
  "inactiveSecond": 0
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "maintenanceEvent": {
    "id": 3,
    "xid": "ME_MODBUS_MAINT",
    "alias": "Monthly Sensor Swap"
  }
}
```

---

## 4. Toggle Maintenance Event Disabled Status

Enables or disables a maintenance event rule without requiring full form submission.

- **URL:** `PUT /api/maintenance-events/{id}/disabled`
- **Method:** `PUT`

### Request Body

```json
{
  "disabled": true
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "disabled": true
}
```

---

## 5. Delete Maintenance Event

Deletes a maintenance event and stops any active runtime suppression.

- **URL:** `DELETE /api/maintenance-events/{id}`
- **Method:** `DELETE`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 3
}
```

---

## 6. Toggle Live Active State

Manually overrides and toggles a running maintenance event between active and inactive states (`rt.toggle()`). Useful for starting or stopping emergency maintenance ad-hoc.

- **URL:** `POST /api/maintenance-events/{id}/toggle`
- **Method:** `POST`

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "data": {
    "activated": true
  }
}
```
