# Graphic Views API Documentation (`/api/views`)

The **Graphic Views API** (`my.com.emserv.web.api.ViewsAPI`) provides REST endpoints for querying, selecting, rendering, and interacting with Graphic Views (`/views.shtm` replacement) and their positioned on-canvas components in **SiteVisor**.

- **Source File:** [ViewsAPI.java](../../src/my/com/emserv/web/api/ViewsAPI.java)
- **Base URL Path:** `/api/views`
- **Authentication:** All requests require an active user session (`JSESSIONID` cookie) or valid authentication headers. Unauthenticated requests return `401 Unauthorized`.
- **Context Management:** Operations modifying point values wrap runtime execution in `BackgroundContext` to ensure proper user session attribution.

---

## Access Permission Levels

Graphic View permissions (`userAccess`) are categorized as:

| Access Code | Access Name | Description |
| :--- | :--- | :--- |
| `0` | **None** | User has no permission to view or interact with this view. |
| `1` | **Read** | User can view canvas components and read telemetry values. |
| `2` | **Set** | User can view canvas components, read telemetry values, and issue setpoint changes to bound data points. |
| `3` | **Owner** | User is the view owner or administrator, possessing full access including layout modification. |

---

## Endpoints Summary

| Method | Endpoint | Description | Permission Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/views` | Retrieves all Graphic Views accessible to the current user | Authenticated User |
| `POST` | `/api/views` | Creates a new Graphic View | Authenticated User |
| `GET` | `/api/views/selected` | Retrieves the user's currently selected Graphic View ID | Authenticated User |
| `PUT` | `/api/views/selected/{id}` | Updates the user's active/selected Graphic View session preference | Read Access on View |
| `GET` | `/api/views/{id}` | Retrieves detailed Graphic View metadata and positioned canvas components | Read Access on View |
| `PUT` | `/api/views/{id}` | Updates metadata of an existing Graphic View | Owner / Admin Access on View |
| `DELETE` | `/api/views/{id}` | Deletes a Graphic View | Owner / Admin Access on View |
| `GET` | `/api/views/editor-metadata` | Retrieves component types and available data points for graphic view editing | Authenticated User |
| `POST` | `/api/views/{id}/set-point` | Updates a data point value directly from a view control | Set/Owner Access on View + Data Point Set Permission |
| `POST` | `/api/views/{id}/components` | Adds a new canvas component to a Graphic View | Owner / Admin Access on View |
| `PUT` | `/api/views/{id}/components/{componentId}` | Updates position, properties, or point bindings of a component | Owner / Admin Access on View |
| `DELETE` | `/api/views/{id}/components/{componentId}` | Deletes a component from a Graphic View | Owner / Admin Access on View |
| `POST` | `/api/views/evaluate-script` | Evaluates a view script snippet on the backend using Mozilla Rhino engine | Authenticated User |

---

## Endpoint Details

### 1. Get All Accessible Graphic Views

Retrieves a list of all Graphic Views accessible to the logged-in user. Admins receive all system views; non-admins receive views they own, views explicitly shared with them, or views permitted by their User Profile. Results are ordered alphabetically by view name.

- **URL:** `GET /api/views`
- **Authentication Required:** Yes

#### Response Body (`Code 200 OK`)

```json
[
  {
    "id": 1,
    "xid": "GV_123456",
    "name": "Main Factory Overview",
    "backgroundFilename": "graphics/Backgrounds/factory.png",
    "userId": 1,
    "owner": true,
    "userAccess": 3
  },
  {
    "id": 2,
    "xid": "GV_789012",
    "name": "HVAC Control Panel",
    "backgroundFilename": "graphics/Backgrounds/hvac.png",
    "userId": 4,
    "owner": false,
    "userAccess": 2
  }
]
```

#### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `Integer` | Internal database unique identifier for the Graphic View. |
| `xid` | `String` | External unique identifier string. |
| `name` | `String` | User-defined display name of the Graphic View. |
| `backgroundFilename` | `String` | Relative image path or background color token for the canvas. |
| `userId` | `Integer` | User ID of the view owner. |
| `owner` | `Boolean` | `true` if current user is owner or an administrator. |
| `userAccess` | `Integer` | Access code (`0`: None, `1`: Read, `2`: Set, `3`: Owner). |

---

### 2. Get User's Selected Graphic View ID

Retrieves the active Graphic View ID currently associated with the user's session.

- **URL:** `GET /api/views/selected`
- **Authentication Required:** Yes

#### Response Body (`Code 200 OK`)

```json
{
  "selectedViewId": 1
}
```

#### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| `selectedViewId` | `Integer` | Currently selected Graphic View ID for the session. Returns `-1` if no accessible views exist. |

---

### 3. Update User's Selected Graphic View ID

Updates the logged-in user's active/selected Graphic View session preference.

- **URL:** `PUT /api/views/selected/{id}`
- **Authentication Required:** Yes
- **Path Parameters:**
  - `id` (`Integer`): The target Graphic View ID to select.

#### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "selectedViewId": 1
}
```

#### Error Responses

- **`403 Forbidden`**: Returned if the user lacks view permission for the requested Graphic View.
- **`404 Not Found`**: Returned if no Graphic View exists with the specified ID.

---

### 4. Get Detailed Graphic View Details

Retrieves complete Graphic View properties including all positioned canvas components (`ViewComponent`), data point telemetry bindings, current live values, and formatted display strings.

- **URL:** `GET /api/views/{id}`
- **Authentication Required:** Yes
- **Path Parameters:**
  - `id` (`Integer`): The Graphic View ID to fetch.

#### Response Body (`Code 200 OK`)

```json
{
  "id": 1,
  "xid": "GV_123456",
  "name": "Main Factory Overview",
  "backgroundFilename": "graphics/Backgrounds/factory.png",
  "userId": 1,
  "owner": true,
  "userAccess": 3,
  "anonymousAccess": 0,
  "components": [
    {
      "id": "c1",
      "index": 1,
      "x": 120,
      "y": 80,
      "style": "font-weight: bold;",
      "componentType": "SimplePointComponent",
      "pointId": 42,
      "pointXid": "DP_TEMP_01",
      "pointName": "Temperature Sensor 1",
      "extendedName": "HVAC - Temperature Sensor 1",
      "unit": "Â°C",
      "dataTypeId": 3,
      "bkgdColor": "#FFFFFF",
      "settable": true,
      "value": 24.5,
      "renderedValue": "24.5 Â°C",
      "timestamp": 1774620000000
    },
    {
      "id": "c2",
      "index": 2,
      "x": 300,
      "y": 150,
      "style": "",
      "componentType": "HtmlComponent",
      "content": "<div class='text-lg text-primary'>Factory Zone A</div>"
    },
    {
      "id": "c3",
      "index": 3,
      "x": 450,
      "y": 200,
      "style": "",
      "componentType": "ImageSetComponent",
      "pointId": 15,
      "pointXid": "DP_PUMP_STATUS",
      "pointName": "Pump Status",
      "extendedName": "Pumps - Pump 1 Status",
      "unit": "",
      "dataTypeId": 1,
      "bkgdColor": "",
      "settable": false,
      "value": 1,
      "renderedValue": "ON",
      "timestamp": 1774620000000,
      "imagePath": "graphics/Pumps/pump_on.png",
      "displayText": true
    },
    {
      "id": "c4",
      "index": 4,
      "x": 600,
      "y": 100,
      "style": "",
      "componentType": "ButtonComponent",
      "whenOffLabel": "Start Pump",
      "whenOnLabel": "Stop Pump",
      "width": 100,
      "height": 40,
      "pointId": 15
    },
    {
      "id": "c5",
      "index": 5,
      "x": 50,
      "y": 300,
      "style": "",
      "componentType": "LinkComponent",
      "linkUrl": "/app/reports",
      "text": "Go to Reports"
    }
  ]
}
```

#### Canvas Component Schemas (`components[]`)

##### Standard Base Component Fields

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | Component instance string ID. |
| `index` | `Integer` | Order/z-index position of component on canvas. |
| `x` | `Integer` | Horizontal X position (in pixels). |
| `y` | `Integer` | Vertical Y position (in pixels). |
| `style` | `String` | Custom inline CSS style overrides. |
| `componentType` | `String` | Java component class name (`SimplePointComponent`, `ImageSetComponent`, `HtmlComponent`, `ButtonComponent`, `LinkComponent`, `CompoundComponent`, etc.). |

##### Data Point Bound Components (`PointComponent`)

Included when `componentType` derives from `PointComponent`:

| Field | Type | Description |
| :--- | :--- | :--- |
| `pointId` | `Integer` | Bound data point ID. |
| `pointXid` | `String` | External XID of the data point. |
| `pointName` | `String` | Resolved display name (`nameOverride` if present, else original `rawPointName`). |
| `nameOverride` | `String` | Custom component instance display name override (empty string if unassigned). |
| `rawPointName` | `String` | Original database name of the bound data point. |
| `extendedName` | `String` | Full extended name of the data point. |
| `unit` | `String` | Engineering units string (e.g., `Â°C`, `bar`, `kW`). |
| `dataTypeId` | `Integer` | Data type ID (`1`: Binary, `2`: Multistate, `3`: Numeric, `4`: Alphanumeric). |
| `bkgdColor` | `String` | Optional background color override. |
| `bkgdColorOverride` | `String` | Configured background color override string. |
| `settableOverride` | `Boolean` | Configured setpoint permission override flag. |
| `displayControls` | `Boolean` | `true` if control buttons are displayed. |
| `settable` | `Boolean` | `true` if data point is settable and user possesses set permissions. |
| `value` | `Object` | Raw telemetry value (primitive or null if missing). |
| `renderedValue` | `String` | Formatted string representation of telemetry value. |
| `timestamp` | `Long` | Millisecond epoch timestamp of the telemetry reading. |

##### ImageSet Component Extra Fields (`ImageSetComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `imagePath` | `String` | Resolved graphic image file path matching current point value. |
| `displayText` | `Boolean` | `true` if text label is enabled alongside image. |
| `imageSetId` | `String` | ID string of bound ImageSet asset. |
| `imageSetName` | `String` | Display name of bound ImageSet asset. |
| `imageFilenames` | `List<String>` | List of relative image filenames contained in the bound ImageSet. |

##### Analog Graphic Component Extra Fields (`AnalogGraphicComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `min` | `Double` | Minimum scale threshold for graphic step calculation. |
| `max` | `Double` | Maximum scale threshold for graphic step calculation. |

##### Binary Graphic Component Extra Fields (`BinaryGraphicComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `zeroImageIndex` | `Integer` | Array index of the OFF / false state image in the ImageSet. |
| `oneImageIndex` | `Integer` | Array index of the ON / true state image in the ImageSet. |

##### Dynamic Graphic Component Extra Fields (`DynamicGraphicComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `min` | `Double` | Minimum scale limit for dynamic fill calculation. |
| `max` | `Double` | Maximum scale limit for dynamic fill calculation. |
| `dynamicImageId` | `String` | ID string of bound DynamicImage asset. |
| `dynamicImageName` | `String` | Display name of bound DynamicImage asset. |

##### Multistate Graphic Component Extra Fields (`MultistateGraphicComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `defaultImageIndex` | `Integer` | Default fallback image index in ImageSet. |
| `imageStateList` | `List` | Mapped state-to-image index key-value pairs. |

##### Simple Point Component Extra Fields (`SimplePointComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `displayPointName` | `Boolean` | `true` if point name text is rendered on component card. |
| `styleAttribute` | `String` | Custom style attribute configuration string. |

##### Thumbnail Component Extra Fields (`ThumbnailComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `scalePercent` | `Integer` | Image scaling percentage (e.g., `100`). |

##### Script Component Extra Fields (`ScriptComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `script` | `String` | Server-side JavaScript code executed to dynamically compute component output. |

##### Script Button Component Extra Fields (`ScriptButtonComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `content` | `String` | Rendered button HTML string. |
| `scriptXid` | `String` | XID of the target Server Script to execute on click. |
| `text` | `String` | Button label text. |

##### HTML Component Extra Fields (`HtmlComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `content` | `String` | Raw HTML string markup to render. |

##### Button Component Extra Fields (`ButtonComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `whenOffLabel` | `String` | Button label text when point value is OFF/false. |
| `whenOnLabel` | `String` | Button label text when point value is ON/true. |
| `width` | `Integer` | Width in pixels. |
| `height` | `Integer` | Height in pixels. |
| `pointId` | `Integer` | Bound data point ID for toggling. |

##### Link Component Extra Fields (`LinkComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `linkUrl` | `String` | Target hyperlink URL. |
| `text` | `String` | Anchor link display text. |

##### Alarm List Component Extra Fields (`AlarmListComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `minAlarmLevel` | `Integer` | Minimum alarm level filter (`0`: None, `1`: Info, `2`: Urgent, `3`: Critical, `4`: Life Safety). |
| `maxListSize` | `Integer` | Maximum number of active alarm entries to display. |
| `width` | `Integer` | Table width in pixels. |

##### Compound Component Extra Fields (`CompoundComponent` / `SimpleCompoundComponent` / `ImageChartComponent`)

| Field | Type | Description |
| :--- | :--- | :--- |
| `isCompound` | `Boolean` | Always `true` for compound components. |
| `name` | `String` | Compound component group name (for `SimpleCompoundComponent`). |
| `backgroundColour` | `String` | Container background color string (for `SimpleCompoundComponent`). |
| `width` | `Integer` | Chart image width (for `ImageChartComponent`). |
| `height` | `Integer` | Chart image height (for `ImageChartComponent`). |
| `durationPeriods` | `Integer` | Chart trend duration period count (for `ImageChartComponent`). |
| `durationType` | `Integer` | Chart trend duration time unit (`1`: Min, `2`: Hours, `3`: Days, `4`: Weeks). |
| `childComponents` | `List` | Nested list of child `ViewComponent` objects. |

---

### 5. Get View Editor Initialization Metadata

Retrieves component definitions (`componentTypes`) and readable data points (`availablePoints`) for initializing a Graphic View editor UI.

- **URL:** `GET /api/views/editor-metadata`
- **Authentication Required:** Yes

#### Response Body (`Code 200 OK`)

```json
{
  "componentTypes": [
    {
      "name": "html",
      "exportName": "HTML",
      "nameKey": "graphic.html"
    },
    {
      "name": "button",
      "exportName": "BUTTON",
      "nameKey": "graphic.button"
    },
    {
      "name": "binaryGraphic",
      "exportName": "BINARY_GRAPHIC",
      "nameKey": "graphic.binaryGraphic"
    }
  ],
  "availablePoints": [
    {
      "id": 42,
      "xid": "DP_343464",
      "name": "Fan Status",
      "extendedName": "HVAC PLC - Fan Status",
      "dataTypeId": 1
    }
  ]
}
```

#### Response Field Explanations

##### `componentTypes[]`
| Field | Type | Description |
| :--- | :--- | :--- |
| `name` | `String` | Internal component definition key. |
| `exportName` | `String` | System export/Emport definition identifier. |
| `nameKey` | `String` | Internationalization key string for UI translation. |

##### `availablePoints[]`
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `Integer` | Data Point database ID. |
| `xid` | `String` | Data Point external ID string. |
| `name` | `String` | Data Point display name. |
| `extendedName` | `String` | Data Source + Data Point full name string. |
| `dataTypeId` | `Integer` | Data type ID (`1`: Binary, `2`: Multistate, `3`: Numeric, `4`: Alphanumeric). |

---

### 6. Set Point Value on Graphic View

Sets a new telemetry value on a bound data point from a Graphic View control or interactive widget. Wraps operation in `BackgroundContext` for session auditing.

- **URL:** `POST /api/views/{id}/set-point`
- **Authentication Required:** Yes
- **Permission Required:** `SET` or `OWNER` access on view + Data Point Set Permission on the point.
- **Path Parameters:**
  - `id` (`Integer`): The target Graphic View ID.

#### Request Payload

```json
{
  "pointId": 42,
  "value": "26.5"
}
```

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `pointId` | `Integer` | **Yes** | Target Data Point ID to modify. |
| `value` | `Object` / `String` | **Yes** | New value to apply to the data point. |

#### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "pointId": 42,
  "value": "26.5"
}
```

#### Response Field Explanations

| Field | Type | Description |
| :--- | :--- | :--- |
| `success` | `Boolean` | `true` indicating value was dispatched successfully. |
| `pointId` | `Integer` | Data Point ID modified. |
| `value` | `Object` | Value applied. |

#### Error Responses

- **`400 Bad Request`**: Returned if `pointId` or `value` is missing in request payload.
  ```json
  {
    "error": "pointId and value are required"
  }
  ```
- **`403 Forbidden`**: Returned if user lacks `SET`/`OWNER` permission on the view or lacks set permission on the data point.
- **`404 Not Found`**: Returned if Graphic View or Data Point with specified ID is not found.
- **`500 Internal Server Error`**: Returned if setting point value fails during runtime processing.

---

### 7. Create New Graphic View

Creates a new Graphic View owned by the logged-in user. Automatically generates a unique XID if none is supplied.

- **URL:** `POST /api/views`
- **Authentication Required:** Yes

#### Request Payload

```json
{
  "name": "Boiler Room Overview",
  "xid": "GV_BOILER_01",
  "backgroundFilename": "graphics/Backgrounds/boiler.png",
  "anonymousAccess": 0
}
```

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | `String` | **Yes** | Display name of the Graphic View. Must not be empty. |
| `xid` | `String` | No | Custom unique XID string. Auto-generated if omitted. |
| `backgroundFilename` | `String` | No | Canvas background image relative path or color token. |
| `anonymousAccess` | `Integer` | No | Anonymous access level (`0`: None, `1`: Read, `2`: Set). Defaults to `0`. |

#### Response Body (`Code 201 Created`)

```json
{
  "id": 15,
  "xid": "GV_BOILER_01",
  "name": "Boiler Room Overview",
  "backgroundFilename": "graphics/Backgrounds/boiler.png",
  "userId": 1,
  "owner": true,
  "userAccess": 3,
  "anonymousAccess": 0
}
```

#### Error Responses

- **`400 Bad Request`**: Returned if `name` is missing or empty.
- **`401 Unauthorized`**: Returned if user session is invalid.
- **`500 Internal Server Error`**: Returned if database persistence fails.

---

### 8. Update Graphic View Metadata

Updates metadata properties of an existing Graphic View.

- **URL:** `PUT /api/views/{id}`
- **Authentication Required:** Yes (Owner or Admin access on View)

#### Request Payload

```json
{
  "name": "Boiler Room Overview - Updated",
  "backgroundFilename": "graphics/Backgrounds/boiler_v2.png",
  "anonymousAccess": 1
}
```

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | `String` | No | New display name for the Graphic View. Must not be empty if specified. |
| `xid` | `String` | No | Updated unique XID. |
| `backgroundFilename` | `String` | No | Updated canvas background image relative path. |
| `anonymousAccess` | `Integer` | No | Updated anonymous access level (`0`: None, `1`: Read, `2`: Set). |

#### Response Body (`Code 200 OK`)

```json
{
  "id": 15,
  "xid": "GV_BOILER_01",
  "name": "Boiler Room Overview - Updated",
  "backgroundFilename": "graphics/Backgrounds/boiler_v2.png",
  "userId": 1,
  "owner": true,
  "userAccess": 3,
  "anonymousAccess": 1
}
```

#### Error Responses

- **`400 Bad Request`**: Returned if `name` parameter is provided as empty string.
- **`403 Forbidden`**: Returned if user is not the view owner or administrator.
- **`404 Not Found`**: Returned if view ID does not exist.

---

### 9. Delete Graphic View

Deletes an existing Graphic View and all associated user permissions.

- **URL:** `DELETE /api/views/{id}`
- **Authentication Required:** Yes (Owner or Admin access on View)

#### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "id": 15
}
```

#### Error Responses

- **`403 Forbidden`**: Returned if user is not the view owner or administrator.
- **`404 Not Found`**: Returned if view ID does not exist.

---

### 10. Add Component to Graphic View

Adds and positions a new canvas component in a Graphic View.

- **URL:** `POST /api/views/{id}/components`
- **Authentication Required:** Yes (Owner or Admin access on View)

#### Request Payload

```json
{
  "componentType": "SimplePointComponent",
  "x": 150,
  "y": 200,
  "pointId": 12,
  "nameOverride": "Supply Temp",
  "bkgdColorOverride": "#f0f4f8",
  "displayControls": true,
  "displayPointName": true
}
```

#### Request Field Explanations

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `componentType` | `String` | **Yes** | Type identifier for the component (e.g. `SimplePointComponent`, `HtmlComponent`, `AnalogGraphicComponent`, `BinaryGraphicComponent`, `DynamicGraphicComponent`, `MultistateGraphicComponent`, `ScriptComponent`, `ThumbnailComponent`, `ButtonComponent`, `LinkComponent`, `ScriptButtonComponent`, `AlarmListComponent`). Matches class simple name, export name, or definition name. |
| `x` | `Integer` | No | Horizontal canvas offset in pixels. |
| `y` | `Integer` | No | Vertical canvas offset in pixels. |
| `pointId` | `Integer` | No | Bound Data Point ID for point components or button controls. |
| `nameOverride` | `String` | No | Custom display label overriding the data point name. |
| `settableOverride` | `Boolean` | No | Allows overriding settable flag (requires Data Point Set permission). |
| `bkgdColorOverride` | `String` | No | Custom background color CSS hex or token. |
| `displayControls` | `Boolean` | No | Whether interactive controls (e.g. set input) are displayed. |
| `content` | `String` | No | HTML markup (for `HtmlComponent` / `ScriptButtonComponent`). |
| `linkUrl` | `String` | No | Target URL (for `LinkComponent`). |
| `text` | `String` | No | Display text label (for `LinkComponent` / `ScriptButtonComponent`). |
| `scriptXid` | `String` | No | Bound script XID (for `ScriptButtonComponent`). |
| `script` | `String` | No | Inline script body (for `ScriptComponent`). |
| `imageSetId` | `String` | No | Image set identifier (for graphic components). |
| `dynamicImageId` | `String` | No | Dynamic image identifier (for `DynamicGraphicComponent`). |
| `zeroImageIndex` | `Integer` | No | Image index for zero/off state (for `BinaryGraphicComponent`). |
| `oneImageIndex` | `Integer` | No | Image index for one/on state (for `BinaryGraphicComponent`). |
| `min` | `Double` | No | Lower bound threshold (for `AnalogGraphicComponent` / `DynamicGraphicComponent`). |
| `max` | `Double` | No | Upper bound threshold (for `AnalogGraphicComponent` / `DynamicGraphicComponent`). |
| `whenOnLabel` | `String` | No | Button label when active (for `ButtonComponent`). |
| `whenOffLabel` | `String` | No | Button label when inactive (for `ButtonComponent`). |
| `width` | `Integer` | No | Component width in pixels. |
| `height` | `Integer` | No | Component height in pixels. |
| `minAlarmLevel` | `Integer` | No | Minimum alarm level filter (`0`: None, `1`: Info, `2`: Urgent, `3`: Critical, `4`: Life Safety). |
| `maxListSize` | `Integer` | No | Maximum rows displayed (for `AlarmListComponent`). |

#### Response Body (`Code 201 Created`)

```json
{
  "id": "c101",
  "index": 1,
  "x": 150,
  "y": 200,
  "componentType": "SimplePointComponent",
  "pointId": 12,
  "pointName": "Supply Temp",
  "value": 24.5,
  "renderedValue": "24.5 Â°C",
  "timestamp": 1754200000000
}
```

#### Error Responses

- **`400 Bad Request`**: Returned if `componentType` is missing or invalid.
- **`403 Forbidden`**: Returned if user lacks view edit permission.
- **`404 Not Found`**: Returned if view ID does not exist.

---

### 11. Update Component in Graphic View

Updates coordinates, data point binding, or properties of an existing view component.

- **URL:** `PUT /api/views/{id}/components/{componentId}`
- **Authentication Required:** Yes (Owner or Admin access on View)

#### Request Payload

```json
{
  "x": 180,
  "y": 220,
  "nameOverride": "Supply Temperature - Line 1"
}
```

#### Response Body (`Code 200 OK`)

```json
{
  "id": "c101",
  "index": 1,
  "x": 180,
  "y": 220,
  "componentType": "SimplePointComponent",
  "pointId": 12,
  "pointName": "Supply Temperature - Line 1",
  "value": 24.5,
  "renderedValue": "24.5 Â°C",
  "timestamp": 1754200000000
}
```

#### Error Responses

- **`403 Forbidden`**: Returned if user lacks view edit permission.
- **`404 Not Found`**: Returned if view ID or component ID does not exist.

---

### 12. Delete Component from Graphic View

Removes a component from a Graphic View.

- **URL:** `DELETE /api/views/{id}/components/{componentId}`
- **Authentication Required:** Yes (Owner or Admin access on View)

#### Response Body (`Code 200 OK`)

```json
{
  "success": true,
  "componentId": "c101"
}
```

#### Error Responses

- **`403 Forbidden`**: Returned if user lacks view edit permission.
- **`404 Not Found`**: Returned if view ID or component ID does not exist.

---

### 13. Evaluate Script Snippet (Rhino Engine)

Evaluates a JavaScript script snippet using the backend's Mozilla Rhino engine with `value`, `raw`, `pointComponent`, `point`, and `renderedText` context.

- **URL:** `POST /api/views/evaluate-script`
- **Authentication Required:** Yes

#### Request Body

```json
{
  "script": "var val = Number(value) || 0; return '<div>Value: ' + val + '</div>';",
  "pointId": 12
}
```

#### Response Body (`Code 200 OK`)

```json
{
  "result": "<div>Value: 24.5</div>"
}
```



