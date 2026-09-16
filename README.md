<p align="center">
  <picture>
    <!-- Displayed when the user uses GitHub Dark Mode -->
    <source media="(prefers-color-scheme: dark)" srcset="frontend/src/lib/assets/scadamy-logo.svg">
    <!-- Displayed when the user uses GitHub Light Mode / Fallback -->
    <img alt="SCADA MY Logo" src="frontend/src/lib/assets/scadamy-logo.svg" width="350">
  </picture>
</p>


# SCADAMY Documentation

SCADAMY is an open-source M2M / SCADA system based on ScadaBR, featuring an embedded Tomcat 9 standalone launcher, Spring MVC REST APIs, and automated Swagger API documentation.

---

## 1. Prerequisites

- **Java Development Kit (JDK)**: Java 11 or compatible JVM.
- **Gradle**: The project includes the Gradle Wrapper (`gradlew` / `gradlew.bat`), so a pre-installed Gradle is optional.

---

## 2. How to Build ScadaMY

You can build ScadaMY using the included Gradle wrapper from the project root directory

### Build Standalone Executable JAR (Recommended)
To build the standalone JAR with embedded Tomcat 9 (`target/ScadaMY.jar`):

**On Windows (Command Prompt / PowerShell):**
```powershell
.\gradlew.bat executableJar
```
Or if using Linux / macOS / Git Bash:
```bash
./gradlew executableJar
```

### Build Lightweight Standalone Launcher (`launcherJar`)
To build the lightweight standalone server JAR (`target/ScadaMY-launcher.jar`) without embedding `ScadaMY.war` (allowing direct execution against an unzipped `WebContent` folder):

```powershell
.\gradlew.bat launcherJar
```

### Build Standard Web Application Archive (WAR)
If you only want to generate the standard WAR (`target/ScadaMY.war`) for deployment to an external Tomcat/container:

```powershell
.\gradlew.bat war
```

### Clean Rebuild
To clear existing build outputs and compile from scratch:
```powershell
.\gradlew.bat clean executableJar
```

Upon a successful build, the output artifacts will be located in the `target/` directory:
- `target/ScadaMY.jar` (Fat executable JAR with embedded Tomcat and `ScadaMY.war`)
- `target/ScadaMY-launcher.jar` (Lightweight server JAR without embedded `.war`, runs directly from `WebContent`)
- `target/ScadaMY.war` (Web application archive)

---

## 3. How to Run the JAR File

ScadaMY can be run directly from the command line using `java -jar` without requiring a standalone web server installation.

### Default Execution
To launch the server using the default configuration (Port `8080`, root context path `""`):

```powershell
java -jar target\ScadaMY.jar
```
*(Or if using your local project JDK: `.\.jdk-11\bin\java -jar target\ScadaMY.jar`)*

When running, you should see output indicating that the embedded Tomcat server has started:
```
====================================================================
 ScadaMY is now running at: http://localhost:8080/
====================================================================
```

### Customizing Port, Context Path, and DocBase
The standalone launcher accepts command-line arguments to customize the HTTP port, context path, and web application folder:

```powershell
java -jar target\ScadaMY.jar --port 9090 --context-path /scadamy
```
Or when running the lightweight separated launcher (`ScadaMY-launcher.jar`) against an unzipped folder:
```powershell
java -jar target\ScadaMY-launcher.jar --port 8080 --doc-base .\WebContent
```

#### Available Options:
| Flag | Short Flag | Description | Default |
| :--- | :--- | :--- | :--- |
| `--port <port>` | `-p <port>` | HTTP server port for embedded Tomcat | `8080` |
| `--context-path <path>` | `-c <path>` | Web application context path (`""` for root) | `""` |
| `--doc-base <path>` | `-d <path>` | Path to unzipped web application directory (e.g. `WebContent`) | Embedded `.war` / `WebContent` |

---

## 4. How to Access Swagger API Documentation

ScadaMY integrates **Springfox Swagger 2** to provide interactive REST API documentation and an OpenAPI specification endpoint.

### Accessing Swagger (Default Settings)
If you launched ScadaMY with the default port (`8080`) and root context path (`""`):

- **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 2.0 JSON Specification (`v2/api-docs`)**: [http://localhost:8080/v2/api-docs](http://localhost:8080/v2/api-docs)

### Accessing Swagger with Custom Configuration
If you launched ScadaMY with a custom port or context path (for example, `--port 9090 --context-path /scadamy`), adjust the URLs accordingly:

- **Swagger UI**: `http://localhost:9090/scadamy/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:9090/scadamy/v2/api-docs`

---

## 6. REST API Reference Guides

Detailed markdown documentation specifications for ScadaMY's Spring MVC REST endpoints:

- **Reports API (`/api/reports`)**: [ReportsAPI.md](doc/rest-api/ReportsAPI.md)
- **Data Sources API (`/api/data-sources`)**: [DataSourcesAPI.md](doc/rest-api/DataSourcesAPI.md)
- **Data Points Edit API (`/api/data-points`)**: [DataPointEditAPI.md](doc/rest-api/DataPointEditAPI.md)
- **Events API (`/api/events`)**: [EventsAPI.md](doc/rest-api/EventsAPI.md)
- **Event Handlers API (`/api/event-handlers`)**: [EventHandlersAPI.md](doc/rest-api/EventHandlersAPI.md)
- **Scripts API (`/api/scripts`)**: [ScriptsAPI.md](doc/rest-api/ScriptsAPI.md)
- **System Settings API (`/api/system-settings`)**: [SystemSettingsAPI.md](doc/rest-api/SystemSettingsAPI.md)
- **Scheduled Events API (`/api/scheduled-events`)**: [ScheduledEventsAPI.md](doc/rest-api/ScheduledEventsAPI.md)
- **Compound Events API (`/api/compound-events`)**: [CompoundEventsAPI.md](doc/rest-api/CompoundEventsAPI.md)
- **Point Links API (`/api/point-links`)**: [PointLinksAPI.md](doc/rest-api/PointLinksAPI.md)
- **Users API (`/api/users`)**: [UsersAPI.md](doc/rest-api/UsersAPI.md)
- **User Profiles API (`/api/user-profiles`)**: [UserProfilesAPI.md](doc/rest-api/UserProfilesAPI.md)
- **Point Hierarchy API (`/api/point-hierarchy`)**: [PointHierarchyAPI.md](doc/rest-api/PointHierarchyAPI.md)
- **Mailing Lists API (`/api/mailing-lists`)**: [MailingListsAPI.md](doc/rest-api/MailingListsAPI.md)
- **Publishers API (`/api/publishers`)**: [PublishersAPI.md](doc/rest-api/PublishersAPI.md)
- **Maintenance Events API (`/api/maintenance-events`)**: [MaintenanceEventsAPI.md](doc/rest-api/MaintenanceEventsAPI.md)
- **Emport API (`/api/emport`)**: [EmportAPI.md](doc/rest-api/EmportAPI.md)
- **SQL Execution API (`/api/sql`)**: [SqlAPI.md](doc/rest-api/SqlAPI.md)
