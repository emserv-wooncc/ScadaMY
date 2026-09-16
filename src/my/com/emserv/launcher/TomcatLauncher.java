package my.com.emserv.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.websocket.server.WsSci;

/**
 * Main launcher class for running ScadaMY as a standalone executable JAR using
 * embedded Tomcat 9.
 */
public class TomcatLauncher {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String contextPath = "";
        String docBasePath = null;

        for (int i = 0; i < args.length; i++) {
            if (("-p".equals(args[i]) || "--port".equals(args[i])) && i + 1 < args.length) {
                port = Integer.parseInt(args[i + 1]);
                i++;
            } else if (("-c".equals(args[i]) || "--context-path".equals(args[i])) && i + 1 < args.length) {
                contextPath = args[i + 1];
                if (!contextPath.startsWith("/") && !contextPath.isEmpty()) {
                    contextPath = "/" + contextPath;
                }
                i++;
            } else if (("-d".equals(args[i]) || "--doc-base".equals(args[i])) && i + 1 < args.length) {
                docBasePath = args[i + 1];
                i++;
            } else if ("--disable-swagger".equals(args[i])) {
                System.setProperty("scadamy.disableSwagger", "true");
            } else if ("--disable-legacy-web".equals(args[i])) {
                System.setProperty("scadamy.disableLegacyWeb", "true");
            } else if ("--disable-evaluate-script".equals(args[i])) {
                System.setProperty("scadamy.disableEvaluateScript", "true");
            }
        }

        File docBase = resolveDocBase(port, docBasePath);
        System.out.println(
                "Starting ScadaMY embedded Tomcat on port " + port + " with context path '" + contextPath + "'");
        System.out.println("Using docBase: " + docBase.getAbsolutePath());

        Tomcat tomcat = new Tomcat();
        File baseDir = new File(System.getProperty("java.io.tmpdir"), "tomcat-scadamy-" + port);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        new File(baseDir, "webapps").mkdirs();
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.setPort(port);

        Connector connector = tomcat.getConnector();
        connector.setURIEncoding("UTF-8");

        Context ctx = tomcat.addWebapp(contextPath, docBase.getAbsolutePath());
        // Ensure webapp classloader is properly initialized
        ctx.setParentClassLoader(TomcatLauncher.class.getClassLoader());
        ctx.addServletContainerInitializer(new WsSci(), null);

        tomcat.start();
        System.out.println("====================================================================");
        System.out.println(" ScadaMY is now running at: http://localhost:" + port + contextPath);
        System.out.println("====================================================================");
        tomcat.getServer().await();
    }

    private static File resolveDocBase(int port, String docBasePath) throws IOException {
        if (docBasePath != null && !docBasePath.trim().isEmpty()) {
            File customDocBase = new File(docBasePath);
            if (!customDocBase.exists() || !customDocBase.isDirectory()) {
                throw new IllegalArgumentException(
                        "Specified docBase directory does not exist or is not a directory: "
                                + customDocBase.getAbsolutePath());
            }
            return customDocBase;
        }

        // 1. Check if we are running inside the standalone JAR where ScadaMY.war is
        // embedded in root
        InputStream warStream = TomcatLauncher.class.getResourceAsStream("/ScadaMY.war");
        if (warStream != null) {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "scadamy-standalone-" + port);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            File extractedWar = new File(tempDir, "ScadaMY.war");
            File webappDir = new File(tempDir, "scadamy-webapp");
            System.out.println("Extracting embedded ScadaMY.war to: " + extractedWar.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(extractedWar)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = warStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            } finally {
                warStream.close();
            }
            return unzipWar(extractedWar, webappDir);
        }

        // 2. Fallback: Check if WebContent directory exists directly
        File webContent = new File("WebContent");
        if (webContent.exists() && webContent.isDirectory()) {
            return webContent;
        }

        // 3. Fallback: Check if target/ScadaMY.war exists on filesystem (e.g. running
        // via gradle/IDE)
        File targetWar = new File("target/ScadaMY.war");
        if (targetWar.exists()) {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "scadamy-dev-" + port);
            File webappDir = new File(tempDir, "scadamy-webapp");
            return unzipWar(targetWar, webappDir);
        }

        throw new IllegalStateException(
                "Could not find ScadaMY.war inside JAR resource or on filesystem, nor WebContent directory.");
    }

    private static File unzipWar(File warFile, File destDir) throws IOException {
        if (destDir.exists()) {
            deleteDirectory(destDir);
        }
        destDir.mkdirs();
        System.out.println("Unpacking " + warFile.getName() + " into directory: " + destDir.getAbsolutePath());
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                new java.io.FileInputStream(warFile))) {
            java.util.zip.ZipEntry entry = zis.getNextEntry();
            byte[] buffer = new byte[8192];
            while (entry != null) {
                File newFile = new File(destDir, entry.getName());
                // Protect against zip slip:
                String destDirPath = destDir.getCanonicalPath();
                String destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }
        }
        return destDir;
    }

    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
