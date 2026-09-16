# Event Handlers API Documentation (`/api/event-handlers`)

The **Event Handlers API** (`my.com.emserv.web.api.EventHandlersAPI`) provides comprehensive RESTful endpoints for querying, creating, updating, deleting, and testing event handlers across SiteVisor (the programmatic equivalent of `/event_handlers.shtm`).

- **Source File:** [EventHandlersAPI.java](../../src/my/com/emserv/web/api/EventHandlersAPI.java)
- **Base URL Path:** `/api/event-handlers`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** All operations enforce strict data source and event type permissions (`Permissions.ensureEventTypePermission` and `Permissions.ensureDataSourcePermission`). Unauthorized attempts return `403 Forbidden`.

---

## Handler Types Summary

Event handlers are categorized by their `handlerType` numeric ID:

| Type ID | Type Name | Description |
| :---: | :--- | :--- |
| **`1`** | **Set Point** (`TYPE_SET_POINT`) | Sets a target data point to a static or dynamic value when an event goes active or inactive. |
| **`2`** | **Email** (`TYPE_EMAIL`) | Sends email notifications (active, escalation, and inactive) to configured recipient lists or users. |
| **`3`** | **Process** (`TYPE_PROCESS`) | Executes system shell/terminal commands when an event triggers or returns to normal. |
| **`4`** | **Script** (`TYPE_SCRIPT`) | Runs configured server-side JavaScript scripts when an event triggers or returns to normal. |

---

## 1. Get Initialization Context Data

Retrieves the full initialization context (`EventHandlersDwr.getInitData()`), including all system event sources (data points, scheduled events, compound events, data sources, publishers, maintenance events, system events, and audit events) and their currently attached event handlers, as well as users, mailing lists, and available target data points.

- **URL:** `GET /api/event-handlers/init`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "scheduledEvents": [
    {
      "eventSourceId": 2,
      "referenceId1": 1,
      "referenceId2": 0,
      "description": "Daily Nightly Backup Trigger",
      "handlers": [
        {
          "id": 14,
          "xid": "EH_88219",
          "alias": "Notify Admins on Backup",
          "handlerType": 2,
          "disabled": false
        }
      ]
    }
  ],
  "systemEvents": [],
  "auditEvents": [],
  "compoundEvents": [],
  "dataSources": [],
  "allPoints": [
    {
      "id": 102,
      "name": "Main Pump Command",
      "dataTypeId": 3
    }
  ],
  "mailingLists": [
    {
      "id": 1,
      "name": "On-Call Engineers"
    }
  ],
  "users": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com"
    }
  ]
}
```

---

## 2. List All Event Handlers

Retrieves a list of all event handlers currently configured across the system.

- **URL:** `GET /api/event-handlers`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 1,
    "xid": "EH_10481",
    "alias": "Auto Shutoff Pump",
    "handlerType": 1,
    "disabled": false,
    "eventSourceId": 1,
    "eventTypeRef1": 42,
    "eventTypeRef2": 1,
    "targetPointId": 105,
    "activeAction": 2,
    "activeValueToSet": "0",
    "activePointId": 0,
    "inactiveAction": 0,
    "inactiveValueToSet": "",
    "inactivePointId": 0
  },
  {
    "id": 2,
    "xid": "EH_10482",
    "alias": "Send High Temp Alert",
    "handlerType": 2,
    "disabled": false,
    "eventSourceId": 1,
    "eventTypeRef1": 42,
    "eventTypeRef2": 1,
    "activeRecipients": [
      {
        "recipientType": 1,
        "referenceId": 1,
        "referenceAddress": ""
      }
    ],
    "sendEscalation": true,
    "escalationDelayType": 1,
    "escalationDelay": 15,
    "escalationRecipients": [
      {
        "recipientType": 3,
        "referenceId": 0,
        "referenceAddress": "manager@example.com"
      }
    ],
    "sendInactive": true,
    "inactiveOverride": false,
    "inactiveRecipients": []
  }
]
```

---

## 3. Get Specific Event Handler Details

Retrieves the complete configuration of a single event handler by its numeric ID.

- **URL:** `GET /api/event-handlers/{id}`
- **Method:** `GET`
- **Path Parameter:**
  - `id` (`int`, required): The numeric database ID of the event handler.

### Response Body (`Code 200 OK` or `404 Not Found`)

Returns the exact JSON object representation of the requested handler, structured according to its `handlerType`.

---

## 4. Create a New Event Handler

Creates, validates, and persists a new event handler linked to a specified event type.

- **URL:** `POST /api/event-handlers`
- **Method:** `POST`
- **Content-Type:** `application/json`

### Common Request Fields (All Handler Types)

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| **`handlerType`** | `int` | Yes | Numeric type of the handler (`1` = Set Point, `2` = Email, `3` = Process, `4` = Script). |
| **`eventSourceId`** | `int` | Yes | The source type ID of the event being handled (`1` = Data Point, `2` = Scheduled, `3` = Compound, etc.). Can also be supplied as `eventTypeId`. |
| **`eventTypeRef1`** | `int` | No | First reference ID of the event type (e.g., Data Point ID or Scheduled Event ID). |
| **`eventTypeRef2`** | `int` | No | Second reference ID of the event type (e.g., Point Event Detector ID). |
| **`xid`** | `String` | No | Unique external identifier. If omitted, the server automatically generates one (`EH_XXXX`). |
| **`alias`** | `String` | No | Human-readable title or description of the handler. |
| **`disabled`** | `boolean`| No | Whether the event handler is currently disabled (defaults to `false`). |

---

### Type 1: Set Point Handler (`handlerType: 1`)

Sets a target point value when the event triggers (`activeAction`) or returns to normal (`inactiveAction`).
Action codes (`activeAction` / `inactiveAction`): `0` = None, `1` = Point Value (copy from another point), `2` = Static Value.

```json
{
  "handlerType": 1,
  "eventSourceId": 1,
  "eventTypeRef1": 101,
  "eventTypeRef2": 1,
  "alias": "Set Pump Speed to Zero",
  "disabled": false,
  "targetPointId": 105,
  "activeAction": 2,
  "activeValueToSet": "0",
  "activePointId": 0,
  "inactiveAction": 1,
  "inactiveValueToSet": "",
  "inactivePointId": 102
}
```

---

### Type 2: Email Handler (`handlerType: 2`)

Sends email alerts to recipients on active, escalation, or inactive states.
Recipient entry types (`recipientType`): `1` = Mailing List (`referenceId` is mailing list ID), `2` = User (`referenceId` is user ID), `3` = Address (`referenceAddress` is raw email string).

```json
{
  "handlerType": 2,
  "eventSourceId": 1,
  "eventTypeRef1": 101,
  "eventTypeRef2": 1,
  "alias": "Email Alert to Team",
  "disabled": false,
  "activeRecipients": [
    { "recipientType": 1, "referenceId": 1, "referenceAddress": "" },
    { "recipientType": 3, "referenceId": 0, "referenceAddress": "ops@example.com" }
  ],
  "sendEscalation": true,
  "escalationDelayType": 1,
  "escalationDelay": 30,
  "escalationRecipients": [
    { "recipientType": 2, "referenceId": 1, "referenceAddress": "" }
  ],
  "sendInactive": true,
  "inactiveOverride": false,
  "inactiveRecipients": []
}
```

---

### Type 3: Process Handler (`handlerType: 3`)

Executes system commands when the event state transitions.

```json
{
  "handlerType": 3,
  "eventSourceId": 1,
  "eventTypeRef1": 101,
  "eventTypeRef2": 1,
  "alias": "Run External Diagnostics Script",
  "disabled": false,
  "activeProcessCommand": "/usr/local/bin/notify_slack.sh --alert high",
  "inactiveProcessCommand": "/usr/local/bin/notify_slack.sh --alert resolved"
}
```

---

### Type 4: Script Handler (`handlerType: 4`)

Runs configured Server-Side JavaScript scripts (referenced by their Script ID) on event transitions.

```json
{
  "handlerType": 4,
  "eventSourceId": 1,
  "eventTypeRef1": 101,
  "eventTypeRef2": 1,
  "alias": "Execute Custom JS Handler",
  "disabled": false,
  "activeScriptCommand": 5,
  "inactiveScriptCommand": 6
}
```

### Response Body (`Code 200 OK`)

Returns the created handler object inside `handler` along with `success: true`.

```json
{
  "success": true,
  "handler": {
    "id": 18,
    "xid": "EH_99412",
    "alias": "Execute Custom JS Handler",
    "handlerType": 4,
    "disabled": false,
    "eventSourceId": 1,
    "eventTypeRef1": 101,
    "eventTypeRef2": 1,
    "activeScriptCommand": 5,
    "inactiveScriptCommand": 6
  }
}
```

If validation fails (e.g., missing target point or invalid number format), returns `400 Bad Request`:
```json
{
  "success": false,
  "messages": [
    "Target point not found"
  ]
}
```

---

## 5. Update an Existing Event Handler

Updates an existing event handler configuration by numeric ID.

- **URL:** `PUT /api/event-handlers/{id}`
- **Method:** `PUT`
- **Content-Type:** `application/json`
- **Path Parameter:** `id` (`int`, required)

Accepts the same JSON body schemas as **Create a New Event Handler** (`POST /api/event-handlers`). If `eventSourceId` / `eventTypeId` is omitted, the handler retains its existing linked event type.

---

## 6. Delete an Event Handler

Permanently deletes an event handler by ID.

- **URL:** `DELETE /api/event-handlers/{id}`
- **Method:** `DELETE`
- **Path Parameter:** `id` (`int`, required)

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 18
}
```

---

## 7. Test Process Command

Executes a system shell/terminal command immediately on the server to verify execution behavior and output.

- **URL:** `POST /api/event-handlers/test-process-command`
- **Method:** `POST`
- **Content-Type:** `application/json`

### Request Body Example

```json
{
  "command": "ping -c 1 127.0.0.1"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "message": "Command executed successfully"
}
```

---

## 8. Test Set Value Content

Generates sample rendered set-point hint text and raw text for a specific point and string value, useful for validating formatting before configuring a Set Point handler.

- **URL:** `POST /api/event-handlers/test-set-value`
- **Method:** `POST`
- **Content-Type:** `application/json`

### Request Body Example

```json
{
  "pointId": 105,
  "valueStr": "25.5",
  "idSuffix": "_test"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "content": "<span class='hint'>Value set to: 25.5Â°C</span>"
}
```
