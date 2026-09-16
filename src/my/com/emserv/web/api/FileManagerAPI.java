package my.com.emserv.web.api;

import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file-manager")
@Api(value = "File Manager API", tags = "File Management for Uploads")
public class FileManagerAPI {

    private boolean isAuthorized(User user) {
        if (user == null) {
            return false;
        }
        return user.isAdmin();
    }

    private File getUploadsDir(HttpServletRequest request) {
        String uploadsPath = request.getSession().getServletContext().getRealPath("/uploads");
        if (uploadsPath == null) {
            // Fallback for some environments
            uploadsPath = request.getSession().getServletContext().getRealPath("/") + "uploads";
        }
        File dir = new File(uploadsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private boolean isAllowedFile(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || lower.endsWith(".gif") || 
               lower.endsWith(".svg") || lower.endsWith(".bmp") || 
               lower.endsWith(".webp") || lower.endsWith(".json");
    }

    private boolean isSafeFilename(String filename) {
        return filename != null && !filename.contains("..") && !filename.contains("/") && !filename.contains("\\");
    }

    @ApiOperation(value = "List all graphic files in WebContent/uploads")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> listFiles(HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!isAuthorized(user)) {
            return ResponseEntity.notFound().build();
        }

        File uploadsDir = getUploadsDir(request);
        File[] files = uploadsDir.listFiles();

        List<Map<String, Object>> result = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isAllowedFile(file.getName())) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("filename", file.getName());
                    fileInfo.put("size", file.length());
                    fileInfo.put("lastModified", file.lastModified());
                    fileInfo.put("url", "/uploads/" + file.getName());
                    result.add(fileInfo);
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Upload or replace a graphics file in WebContent/uploads")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!isAuthorized(user)) {
            System.out.println("FileManagerAPI upload failed: Unauthorized");
            return ResponseEntity.notFound().build();
        }

        if (file == null || file.isEmpty()) {
            System.out.println("FileManagerAPI upload failed: file is null or empty");
            return ResponseEntity.badRequest().body("No file provided");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            System.out.println("FileManagerAPI upload failed: originalFilename is null");
            return ResponseEntity.badRequest().body("Invalid file name");
        }

        // Sanitize the filename to prevent directory traversal
        String filename = new File(originalFilename).getName();
        System.out.println("FileManagerAPI uploading file: " + filename);

        if (!isSafeFilename(filename)) {
            System.out.println("FileManagerAPI upload failed: isSafeFilename returned false for " + filename);
            return ResponseEntity.badRequest().body("Invalid file name");
        }

        if (!isAllowedFile(filename)) {
            System.out.println("FileManagerAPI upload failed: isAllowedFile returned false for " + filename);
            return ResponseEntity.badRequest().body("Only graphics files and JSON files are allowed");
        }

        File uploadsDir = getUploadsDir(request);
        File targetFile = new File(uploadsDir, filename);

        try {
            file.transferTo(targetFile);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("filename", filename);
            result.put("url", "/uploads/" + filename);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Delete a specific file from WebContent/uploads")
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deleteFile(@RequestParam("filename") String filename, HttpServletRequest request) {
        User user = Common.getUser(request);
        if (!isAuthorized(user)) {
            return ResponseEntity.notFound().build();
        }

        if (!isSafeFilename(filename)) {
            return ResponseEntity.badRequest().body("Invalid file name");
        }

        File uploadsDir = getUploadsDir(request);
        File targetFile = new File(uploadsDir, filename);

        if (!targetFile.exists() || !targetFile.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }

        if (targetFile.delete()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "File deleted successfully");
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }
    }
}
