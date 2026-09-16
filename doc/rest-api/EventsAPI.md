# Events API Documentation (`/api/events`)

The **Events API** (`my.com.emserv.web.api.EventsAPI`) provides structured, JSON-based endpoints for monitoring, retrieving, and acknowledging alarms and system events in **SiteVisor**.

- **Source File:** [EventsAPI.java](../../src/my/com/emserv/web/api/EventsAPI.java)
- **Base URL Path:** `/api/events`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **WebSocket Integration:** Acknowledging events through this REST API automatically broadcasts live status updates to connected WebSocket clients via `/user/topic/events/update`.

---

## 1. Get Active Alarm Summary Statistics

Retrieves real-time counts and maximum severity levels of currently active alarms for the logged-in user. Ideal for driving top-bar warning indicators or siren alerts.

- **URL:** `GET /api/events/summary`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "activeAlarmCount": 0,
  "criticalAlarmCount": 0,
  "highestAlarmLevel": -1,
  "timestamp": 1784557481941
}
```

### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| **`activeAlarmCount`** | `Integer` | The total number of active (uncleared/unacknowledged) alarms assigned to or visible by the current user. |
| **`criticalAlarmCount`** | `Integer` | The number of active alarms with a severity level of Critical (Level 3) or higher. |
| **`highestAlarmLevel`** | `Integer` | The highest severity level across all active alarms for the user. <br><br>**Possible Values:**<br>â€¢ **`-1`** = **No Active Alarms** (returned when `activeAlarmCount` is `0`)<br>â€¢ **`0`** = **Information**<br>â€¢ **`1`** = **Urgent**<br>â€¢ **`2`** = **Critical**<br>â€¢ **`3`** = **Life Safety** |
| **`timestamp`** | `Long` | The server epoch timestamp (in milliseconds) when the summary check executed. |

---

## 2. List Active / Pending Events (`GET /api/events` vs `GET /api/events/pending`)

Retrieves the detailed list of active or pending event instances for the currently logged-in user.

- **URLs:** 
  - `GET /api/events` â€” Queries the **in-memory `EventManager`** (`Common.ctx.getEventManager().getActiveEventsByUser(user.getId())`) for ultra-fast, real-time status of currently active alarms.
  - `GET /api/events/pending` â€” Queries the **persistent database via `EventDao`** (`new EventDao().getPendingEvents(user.getId())`) directly from storage.
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

Both endpoints return an array of simplified JSON objects (`List<Map<String, Object>>`) directly without a DTO wrapper class. If there are no active events, an empty array `[]` is returned.

```json
[
  {
    "id": 1042,
    "eventType": {
      "typeId": 1,
      "dataPointId": 12,
      "duplicateHandling": 2
    },
    "alarmLevel": 3,
    "activeTimestamp": 1784557400123,
    "rtnApplicable": true,
    "rtnTimestamp": 0,
    "acknowledged": false,
    "ackUserId": 0,
    "ackUsername": null,
    "ackTimestamp": 0,
    "hasComments": false,
    "messageString": "High temperature threshold exceeded (28.5 > 25.0)"
  }
]
```

### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| **`id`** | `Integer` | Unique internal runtime ID of this specific event instance (`EventInstance`). Required when calling `/acknowledge`. |
| **`eventType`** | `Object` | Details about the source of the event (e.g., `typeId: 1` = Data Point detector, `typeId: 2` = Scheduled event, `typeId: 3` = System event). |
| **`alarmLevel`** | `Integer` | Severity level: `0` (Info), `1` (Urgent), `2` (Critical), `3` (Life Safety). |
| **`activeTimestamp`** | `Long` | Epoch timestamp (in milliseconds) when the alarm condition occurred. |
| **`rtnApplicable`** | `Boolean` | Whether this event requires a Return to Normal (RTN) transition to clear automatically. |
| **`rtnTimestamp`** | `Long` | Epoch timestamp when the event returned to normal (`0` if still active). |
| **`acknowledged`** | `Boolean` | `true` if an operator has acknowledged the event; `false` otherwise. |
| **`ackUserId`** | `Integer` | User ID of the operator who acknowledged the event (`0` if unacknowledged). |
| **`ackUsername`** | `String` | Username of the operator who acknowledged the event (`null` if unacknowledged). |
| **`ackTimestamp`** | `Long` | Epoch timestamp when the acknowledgment took place (`0` if unacknowledged). |
| **`hasComments`** | `Boolean` | `true` if this event instance has any user comments attached. |
| **`messageString`** | `String` | Localized, human-readable description of what triggered the alarm. |

---

## 3. Search & Historical Alarms

Queries both historical (`events` database table) and active alarms with support for multi-source filtering, status filtering, severity level filtering, keyword search, date range filtering, and pagination.

- **URLs:** 
  - `GET /api/events/search`
  - `POST /api/events/search`
- **Authentication Required:** Yes

### Request Parameters (Query or Form Params)

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| **`eventId`** | `Integer` | No | `0` | Filter by exact event ID (`0` matches any). |
| **`sourceType`** | `Integer[]` | No | `[]` (Any) | Array of event source type IDs (e.g., `1` = Data Point, `3` = Data Source, `4` = System, `6` = Scheduled). |
| **`status`** | `String[]` | No | `["*"]` | Array of status codes (`*` = All, `A` = Active, `R` = Returned to Normal, `N` = No RTN applicable). |
| **`alarmLevel`** | `Integer[]` | No | `[]` (Any) | Array of alarm levels (`0` = Info, `1` = Urgent, `2` = Critical, `3` = Life Safety). |
| **`keyword`** | `String` | No | `""` | Search keywords separated by spaces. Supports `-` prefix to exclude keywords (e.g., `"High -maintenance"`). |
| **`dateFrom`** | `Long` | No | `-1` | Start epoch timestamp (in milliseconds). `-1` for no start limit. |
| **`dateTo`** | `Long` | No | `-1` | End epoch timestamp (in milliseconds). `-1` for no end limit. |
| **`offset`** | `Integer` | No | `0` | Starting row index for pagination (`0`-indexed). |
| **`limit`** | `Integer` | No | `50` | Maximum number of results to return per page (`-1` or `0` for unlimited). |

### Response Body (`Code 200 OK`)

Returns a paginated search object containing the total matching row count (`totalCount`) across all pages, the current `offset`, `limit`, and the `events` array.

```json
{
  "totalCount": 142,
  "offset": 0,
  "limit": 50,
  "events": [
    {
      "id": 1042,
      "eventType": {
        "typeId": 1,
        "dataPointId": 12,
        "duplicateHandling": 2
      },
      "alarmLevel": 3,
      "activeTimestamp": 1784557400123,
      "rtnApplicable": true,
      "rtnTimestamp": 1784557500123,
      "acknowledged": true,
      "ackUserId": 1,
      "ackTimestamp": 1784557450123,
      "messageString": "High temperature threshold exceeded (28.5 > 25.0)"
    }
  ]
}
```

---

## 4. Acknowledge a Single Event

Marks a specific event instance as acknowledged by the current user. Also broadcasts an acknowledgment notification to all WebSocket clients on `/user/topic/events/update`.

- **URL:** `POST /api/events/{eventId}/acknowledge`
- **URL Parameters:** `eventId` (Integer) â€” The `id` of the event instance to acknowledge.
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "eventId": 1042,
  "ackTime": 1784557495120
}
```

### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| **`success`** | `Boolean` | Indicates whether the acknowledgment operation succeeded (`true`). |
| **`eventId`** | `Integer` | The event instance ID that was acknowledged. |
| **`ackTime`** | `Long` | Server epoch timestamp (in milliseconds) when the event was marked as acknowledged. |

---

## 5. Bulk Acknowledge All Events

Automatically iterates through and acknowledges **all** active, unacknowledged events currently assigned to or visible by the logged-in user. Broadcasts updates over `/user/topic/events/update` for each acknowledged event.

- **URL:** `POST /api/events/acknowledge-all`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "acknowledgedCount": 5,
  "ackTime": 1784557510450
}
```

### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| **`success`** | `Boolean` | Indicates whether the bulk acknowledgment operation succeeded (`true`). |
| **`acknowledgedCount`** | `Integer` | The exact number of unacknowledged events that were marked as acknowledged during this request. |
| **`ackTime`** | `Long` | Server epoch timestamp (in milliseconds) when the bulk acknowledgment executed. |

---

## 6. Get Event Comments

Retrieves the chronological list of user comments attached to a specific event.

- **URL:** `GET /api/events/{eventId}/comments`
- **URL Parameters:** `eventId` (Integer) â€” The `id` of the event instance.
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
[
  {
    "userId": 2,
    "username": "MY-OP-04",
    "ts": 1784557499000,
    "comment": "Checking on this issue right now."
  }
]
```

### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| **`userId`** | `Integer` | The user ID of the operator who posted the comment. |
| **`username`** | `String` | The username of the operator who posted the comment. |
| **`ts`** | `Long` | The epoch timestamp (in milliseconds) when the comment was posted. |
| **`comment`** | `String` | The actual text content of the comment. |

---

## 7. Add Event Comment

Appends a new user comment to a specific event instance.

- **URL:** `POST /api/events/{eventId}/comments`
- **URL Parameters:** `eventId` (Integer) â€” The `id` of the event instance.
- **Authentication Required:** Yes

### Request Body

```json
{
  "comment": "Issue resolved, resetting sensor."
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "userId": 2,
  "username": "MY-OP-04",
  "ts": 1784557510450,
  "comment": "Issue resolved, resetting sensor."
}
```
