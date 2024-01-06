package xyz.necrozma.util;

import xyz.necrozma.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    static public String ExportResource(String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        try {
            stream = FileUtil.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            resStreamOut = Files.newOutputStream(Paths.get(jarFolder + resourceName));
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            assert stream != null;
            stream.close();
            assert resStreamOut != null;
            resStreamOut.close();
        }

        return jarFolder + resourceName;
    }

    public static boolean KillProcess(String serviceName) {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM " + serviceName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean StartExe(String exePath) {
        try {
            File file = new File(exePath);
            ProcessBuilder processBuilder = new ProcessBuilder(file.getAbsolutePath());
            processBuilder.directory(file.getParentFile());

            try {
                processBuilder.start();

                if(processBuilder.start().isAlive()) {
                    PresenceManager.setPresence("In main menu", "Playing version " + Client.INSTANCE.getMC().getVersion(), "cover", Client.INSTANCE.getMC().getVersion());
                } else {
                    return false;
                }

                return true;
            } catch(Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
