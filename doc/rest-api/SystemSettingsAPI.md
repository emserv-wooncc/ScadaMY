# System Settings API Documentation (`/api/system-settings`)

The **System Settings API** (`my.com.emserv.web.api.SystemSettingsAPI`) provides full RESTful endpoints for viewing, updating, and triggering maintenance operations on SiteVisor system settings (the direct equivalent of `/system_settings.shtm`).

- **Source File:** [SystemSettingsAPI.java](../../src/my/com/emserv/web/api/SystemSettingsAPI.java)
- **Base URL Path:** `/api/system-settings`
- **Authentication & Permission:** All endpoints require an active administrator session (`JSESSIONID`) or valid admin credentials. Non-admin requests return `401 Unauthorized`.

---

## 1. Get All System Settings

Retrieves all current system configuration parameters categorized cleanly into `email`, `http`, `misc`, `info`, `colours`, and `alarmLevels`.

- **URL:** `GET /api/system-settings`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "email": {
    "smtpHost": "smtp.gmail.com",
    "smtpPort": 587,
    "fromAddress": "sitevisor@example.com",
    "fromName": "SiteVisor Server",
    "authorization": true,
    "smtpUsername": "user@example.com",
    "smtpPassword": "password",
    "tls": true,
    "contentType": 1
  },
  "http": {
    "useProxy": false,
    "proxyServer": "",
    "proxyPort": 8080,
    "proxyUsername": "",
    "proxyPassword": ""
  },
  "misc": {
    "eventPurgePeriodType": 3,
    "eventPurgePeriods": 6,
    "reportPurgePeriodType": 3,
    "reportPurgePeriods": 1,
    "uiPerformance": 2000,
    "groveLogging": false,
    "futureDateLimitPeriodType": 1,
    "futureDateLimitPeriods": 24
  },
  "info": {
    "instanceDescription": "Main Factory SCADA Node",
    "newVersionNotificationLevel": "S",
    "language": "en"
  },
  "colours": {
    "chartBackgroundColour": "#FFFFFF",
    "plotBackgroundColour": "#F5F5F5",
    "plotGridlineColour": "#E0E0E0"
  },
  "alarmLevels": {
    "systemEventTypes": [
      {
        "typeId": 3,
        "typeRef1": 1,
        "alarmLevel": 4,
        "description": "System start",
        "descriptionKey": "event.system.start"
      }
    ],
    "auditEventTypes": [
      {
        "typeId": 7,
        "typeRef1": 1,
        "alarmLevel": 1,
        "description": "Data source change",
        "descriptionKey": "event.audit.dataSource"
      }
    ]
  }
}
```

---

## 2. Get Database Size Statistics

Retrieves real-time statistics regarding database and file data storage utilization, along with top point history counts.

- **URL:** `GET /api/system-settings/database-size`
- **Method:** `GET`

### Response Body (`Code 200 OK`)

```json
{
  "dbType": "derby",
  "databaseSize": "14.5 MB",
  "filedataCount": 2,
  "filedataSize": "120 KB",
  "totalSize": "14.6 MB",
  "historyCount": 45120,
  "eventCount": 312,
  "topPoints": [
    {
      "pointId": 101,
      "pointName": "Main Boiler Pressure",
      "count": 12500
    }
  ]
}
```

---

## 3. Update Email Settings

Updates SMTP and notification email configuration.

- **URL:** `PUT /api/system-settings/email`
- **Method:** `PUT`
- **Content-Type:** `application/json`

### Request Body Example
```json
{
  "smtpHost": "smtp.mailgun.org",
  "smtpPort": 587,
  "fromAddress": "alerts@sitevisor.org",
  "fromName": "SiteVisor Alerts",
  "authorization": true,
  "smtpUsername": "smtp_user",
  "smtpPassword": "secret_password",
  "tls": true,
  "contentType": 1
}
```

---

## 4. Send Test Email

Saves/updates email settings and sends an instant test verification email to the currently logged-in administrator's email address.

- **URL:** `POST /api/system-settings/email/test`
- **Method:** `POST`
- **Content-Type:** `application/json`

Accepts the exact same JSON request body as **Update Email Settings** (`PUT /api/system-settings/email`).

---

## 5. Update HTTP Proxy Settings

Updates HTTP client proxy configuration used when SiteVisor communicates with external services.

- **URL:** `PUT /api/system-settings/http`
- **Method:** `PUT`

### Request Body Example
```json
{
  "useProxy": true,
  "proxyServer": "proxy.corporate.net",
  "proxyPort": 3128,
  "proxyUsername": "proxyuser",
  "proxyPassword": "proxypassword"
}
```

---

## 6. Update Miscellaneous Settings

Updates retention/purge periods and UI refresh performance intervals.

- **URL:** `PUT /api/system-settings/misc`
- **Method:** `PUT`

### Request Body Example
```json
{
  "eventPurgePeriodType": 3,
  "eventPurgePeriods": 12,
  "reportPurgePeriodType": 3,
  "reportPurgePeriods": 6,
  "uiPerformance": 2000,
  "groveLogging": true,
  "futureDateLimitPeriodType": 1,
  "futureDateLimitPeriods": 24
}
```
*(Period Types: `1` = Seconds, `2` = Minutes, `3` = Hours, `4` = Days, `5` = Weeks, `6` = Months, `7` = Years)*

---

## 7. Update Colour Settings

Updates default chart and plot background/gridline colors.

- **URL:** `PUT /api/system-settings/colours`
- **Method:** `PUT`

### Request Body Example
```json
{
  "chartBackgroundColour": "#F8F9FA",
  "plotBackgroundColour": "#FFFFFF",
  "plotGridlineColour": "#E9ECEF"
}
```

---

## 8. Update Info & Language Settings

Updates the instance description, version check notification level, and system language.

- **URL:** `PUT /api/system-settings/info`
- **Method:** `PUT`

### Request Body Example
```json
{
  "instanceDescription": "Production SiteVisor Node 1",
  "newVersionNotificationLevel": "S",
  "language": "en"
}
```
*(Notification Levels: `"S"` = Stable, `"C"` = Release Candidate, `"B"` = Beta)*

---

## 9. Update Alarm Levels

Updates configured alarm severity levels for system and audit events.

- **URL:** `PUT /api/system-settings/alarm-levels`
- **Method:** `PUT`

### Request Body Example
```json
{
  "systemEventTypes": [
    { "typeRef1": 1, "alarmLevel": 4 },
    { "typeRef1": 2, "alarmLevel": 3 }
  ],
  "auditEventTypes": [
    { "typeRef1": 1, "alarmLevel": 1 }
  ]
}
```
*(Alarm Levels: `0` = None, `1` = Information, `2` = Urgent, `3` = Critical, `4` = Life Safety)*

---

## 10. Trigger Manual Event Purge

Immediately executes the background data retention purge task according to configured retention rules.

- **URL:** `POST /api/system-settings/purge-events`
- **Method:** `POST`

---

## 11. Purge All Historical Point Data

Permanently removes all historical data point values stored across all data points.

- **URL:** `POST /api/system-settings/purge-all-data`
- **Method:** `POST`

### Response Body (`Code 200 OK`)
```json
{
  "success": true,
  "purgedCount": 142500,
  "message": "Purge all data completed. Purged values count: 142500"
}
```

---

## 12. Restart Server

Restarts the SiteVisor application server gracefully. This API only operates when the server has been launched via the designated restart loop scripts (`start.bat` or `start.sh`) or when the `SITEVISOR_RESTARTABLE=true` environment variable is detected. If these conditions are not met, the API returns an error indicating that manual intervention is required.

- **URL:** `POST /api/system-settings/restart`
- **Method:** `POST`

### Response Body (`Code 200 OK` - Success)
```json
{
  "success": true,
  "message": "Restarting server in 2 seconds..."
}
```

### Response Body (`Code 200 OK` - Failure)
```json
{
  "success": false,
  "error": "Restart scripts (start.bat/start.sh) not detected. Please manually restart the server."
}
```
