# Dashboard API

## Base Path: `/api/dashboard`
**Controller**: `my.com.emserv.web.api.DashboardAPI`

Provides endpoints for saving and loading the dashboard layout structure in JSON format.

### Endpoints

#### 1. Get Layout
- **URL**: `/layout`
- **Method**: `GET`
- **Response Format**: `application/json`
- **Authentication Required**: Yes (Admin only)
- **Description**: Retrieves the saved dashboard layout JSON file. If the file does not exist, an empty JSON object `{}` is returned.

**Response Structure (Example)**:
```json
{
  "widgets": [
    {
      "id": "abc-123",
      "type": "clock",
      "x": 0,
      "y": 0,
      "w": 4,
      "h": 2
    }
  ]
}
```

#### 2. Save Layout
- **URL**: `/layout`
- **Method**: `POST`
- **Request Format**: `application/json`
- **Response Format**: `application/json`
- **Authentication Required**: Yes (Admin only)
- **Description**: Saves or overwrites the dashboard layout JSON file.

**Request Body (Example)**:
```json
{
  "widgets": [
    {
      "id": "abc-123",
      "type": "clock",
      "x": 0,
      "y": 0,
      "w": 4,
      "h": 2
    }
  ]
}
```

**Success Response**:
```json
{
  "success": true
}
```
