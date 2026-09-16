# Compound Events API Documentation (`/api/compound-events`)

The **Compound Events API** (`my.com.emserv.web.api.CompoundEventsAPI`) provides RESTful endpoints for querying, creating, updating, deleting, and validating compound event detectors across SiteVisor (the programmatic equivalent of `/compound_events.shtm`).

- **Source File:** [CompoundEventsAPI.java](../../src/my/com/emserv/web/api/CompoundEventsAPI.java)
- **Base URL Path:** `/api/compound-events`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Permissions:** Operations require data source permissions (`Permissions.ensureDataSourcePermission`). Unauthorized attempts return `403 Forbidden`.

---

## 1. Get Initialization Context Data

Retrieves the initialization structure, including existing compound event detectors, data points (and their attached detectors), and scheduled events that can be referenced inside boolean logic conditions.

- **URL:** `GET /api/compound-events/init`
- **Method:** `GET`
- **Authentication Required:** Yes

### Response Body (`Code 200 OK`)

```json
{
  "compoundEvents": [
    {
      "id": 1,
      "xid": "CED_001",
      "name": "High Temp OR High Pressure",
      "alarmLevel": 3,
      "returnToNormal": true,
      "condition": "DP_101 || DP_102",
      "disabled": false
    }
  ],
  "dataPoints": [
    {
      "id": 101,
      "name": "Temperature Sensor",
      "eventDetectors": [
        {
          "id": 10,
          "alias": "High Limit"
        }
      ]
    }
  ],
  "scheduledEvents": []
}
```

---

## 2. Get All Compound Event Detectors

Retrieves a list of all compound event detectors.

- **URL:** `GET /api/compound-events`
- **Method:** `GET`

---

## 3. Get Specific Compound Event Detector

Retrieves configuration for a detector by ID, or returns a default template when ID `-1` is requested.

- **URL:** `GET /api/compound-events/{id}`
- **Method:** `GET`

---

## 4. Create or Update Compound Event Detector

Registers (`POST /api/compound-events`) or updates (`PUT /api/compound-events/{id}`) a compound event detector.

- **Request Body:** JSON representation of `CompoundEventDetectorVO`.

```json
{
  "xid": "CED_LOGIC_01",
  "name": "Critical Pump Failure Condition",
  "alarmLevel": 4,
  "returnToNormal": true,
  "condition": "DP_201 && !SE_1",
  "disabled": false
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "compoundEvent": {
    "id": 2,
    "xid": "CED_LOGIC_01",
    "name": "Critical Pump Failure Condition"
  }
}
```

---

## 5. Delete Compound Event Detector

Deletes a compound event detector and stops its background detector instance.

- **URL:** `DELETE /api/compound-events/{id}`
- **Method:** `DELETE`

---

## 6. Validate Condition Logic String

Validates the syntax and references of a boolean logic condition expression against existing detector keys.

- **URL:** `POST /api/compound-events/validate-condition`
- **Method:** `POST`
- **Request Body:**

```json
{
  "condition": "DP_101 && DP_102"
}
```

### Response Body (`Code 200 OK`)

```json
{
  "success": true
}
```

---

## 7. Toggle or Update Disabled Status

Sets or toggles the enabled/disabled state of a compound event detector without re-validating the full object.

- **URL:** `PUT /api/compound-events/{id}/disabled`
- **Method:** `PUT`
- **Request Body (optional):**
```json
{
  "disabled": true
}
```
*Note: If `"disabled"` is omitted from the body, the detector's state will automatically be inverted.*

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "id": 2,
  "disabled": true
}
```

