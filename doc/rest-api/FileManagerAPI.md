# File Manager API

This document details the REST API endpoints provided by `FileManagerAPI.java` for managing graphics files in the `WebContent/uploads` directory.

**Base Path:** `/api/file-manager`

> [!WARNING]
> **Security Notice**: All endpoints in this API are restricted. They can only be accessed by users with `Administrator` privileges. Non-administrators will receive a `404 Not Found` response so the endpoint appears completely unavailable.

---

## 1. List Graphic Files
**Endpoint:** `/`  
**Method:** `GET`  
**Description:** Scans the `WebContent/uploads` directory and returns a list of all supported graphics files (`.png`, `.jpg`, `.jpeg`, `.gif`, `.svg`, `.bmp`, `.webp`).

### Responses

#### `200 OK`
A JSON array containing objects for each graphic file found in the directory.

| Field | Type | Description |
| :--- | :--- | :--- |
| `filename` | `string` | The exact name of the file (e.g. `logo.png`). |
| `size` | `long` | The file size in bytes. |
| `lastModified` | `long` | The last modified timestamp of the file in milliseconds. |
| `url` | `string` | The relative URL path to serve the image (e.g. `/uploads/logo.png`). |

#### `404 Not Found`
Returned if the user is not authenticated or is not an administrator.

---

## 2. Upload/Replace Graphic File
**Endpoint:** `/upload`  
**Method:** `POST`  
**Description:** Uploads a new graphic file or replaces an existing one in the `WebContent/uploads` directory. The filename is sanitized to prevent directory traversal.

### Request Payload
**Content-Type:** `multipart/form-data`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `file` | `file` | Yes | The graphics file to upload. Must have a valid extension (`.png`, `.jpg`, `.jpeg`, `.gif`, `.svg`, `.bmp`, `.webp`). |

### Responses

#### `200 OK`
Returned when the file is successfully saved.

| Field | Type | Description |
| :--- | :--- | :--- |
| `success` | `boolean` | Always true. |
| `filename` | `string` | The final sanitized filename saved on the server. |
| `url` | `string` | The relative URL path to access the uploaded image. |

#### `400 Bad Request`
Returned if no file is provided, the filename is unsafe (contains `..` or `/`), or the file extension is not an allowed graphics format.

#### `404 Not Found`
Returned if the user is not an administrator.

#### `500 Internal Server Error`
Returned if there was an IO exception while transferring the file to the disk.

---

## 3. Delete Graphic File
**Endpoint:** `/`  
**Method:** `DELETE`  
**Description:** Deletes a specific graphic file from the `WebContent/uploads` directory.

### Query Parameters
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `filename` | `string` | Yes | The exact name of the file to delete (e.g. `logo.png`). |

### Responses

#### `200 OK`
Returned when the file is successfully deleted.

| Field | Type | Description |
| :--- | :--- | :--- |
| `success` | `boolean` | Always true. |
| `message` | `string` | "File deleted successfully". |

#### `400 Bad Request`
Returned if the filename is unsafe (contains `..` or `/`).

#### `404 Not Found`
Returned if the file does not exist, or if the user is not an administrator.
Returned if the file does not exist in the `uploads` directory or is not a regular file.

#### `500 Internal Server Error`
Returned if the file existed but the OS prevented the deletion.
