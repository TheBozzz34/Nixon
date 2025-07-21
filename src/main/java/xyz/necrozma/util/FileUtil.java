package xyz.necrozma.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for accessing and using files in the Nixon directory.
 *
 * @author Strikeless
 * @since 08/06/2021
 */
@UtilityClass
public class FileUtil {

    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String CLIENT_NAME = "nixon";
    private static final Path CLIENT_PATH = Paths.get(mc.mcDataDir.getAbsolutePath(), CLIENT_NAME);


    private final String SEPARATOR = File.separator;

    /**
     * Checks if a file exists.
     *
     * @param fileName the file name inside the Nixon directory
     * @return whether or not the file exists
     */
    public boolean exists(final String fileName) {
        return Files.exists(getPath(fileName));
    }

    /**
     * Checks if a file exists.
     *
     * @param path the path to check
     * @return whether or not the file exists
     */
    public boolean exists(final Path path) {
        return Files.exists(path);
    }

    /**
     * Checks if the Nixon directory exists.
     *
     * @return whether or not the Nixon directory exists.
     */
    public boolean clientDirectoryExists() {
        return Files.exists(CLIENT_PATH);
    }

    /**
     * Saves a string into the specified file using UTF-8 encoding.
     * If the file does not exist this will create it automatically.
     * Uses try-with-resources for automatic resource management.
     *
     * @param fileName the file name inside the Nixon directory
     * @param override whether or not we should override the file if it exists already
     * @param content  the string to write into the file
     * @return whether or not the file was saved successfully
     */
    public boolean saveFile(final String fileName, final boolean override, final String content) {
        final Path filePath = getPath(fileName);

        try {
            // Create directories if they don't exist
            createClientDirectory();

            // Check if file exists and override is false
            if (Files.exists(filePath) && !override) {
                LOGGER.info("File already exists and override is false: " + fileName);
                return false;
            }

            // Write content using UTF-8 encoding
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            LOGGER.info("Successfully saved file: " + fileName);
            return true;

        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save file: " + fileName, e);
            return false;
        }
    }

    /**
     * Loads a string from the specified file using UTF-8 encoding.
     * If the file does not exist this will return null.
     *
     * @param fileName the file name inside the Nixon directory
     * @return the string loaded from the file, or null if file doesn't exist
     */
    public String loadFile(final String fileName) {
        final Path filePath = getPath(fileName);

        try {
            if (!Files.exists(filePath)) {
                LOGGER.info("File does not exist: " + fileName);
                return null;
            }

            // Read all content at once using UTF-8 encoding
            final byte[] bytes = Files.readAllBytes(filePath);
            final String content = new String(bytes, StandardCharsets.UTF_8);

            LOGGER.info("Successfully loaded file: " + fileName);
            return content;

        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load file: " + fileName, e);
            return null;
        }
    }

    /**
     * Loads file content as a list of lines.
     *
     * @param fileName the file name inside the Nixon directory
     * @return list of lines, or null if file doesn't exist
     */
    public List<String> loadFileLines(final String fileName) {
        final Path filePath = getPath(fileName);

        try {
            if (!Files.exists(filePath)) {
                LOGGER.info("File does not exist: " + fileName);
                return null;
            }

            return Files.readAllLines(filePath, StandardCharsets.UTF_8);

        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load file lines: " + fileName, e);
            return null;
        }
    }

    /**
     * Creates the Nixon directory if absent.
     */
    public void createClientDirectory() {
        try {
            Files.createDirectories(CLIENT_PATH);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create client directory", e);
            throw new IllegalStateException("Unable to create Nixon directory!", e);
        }
    }

    /**
     * Creates a directory at the specified path.
     *
     * @param directoryName the directory name/path inside the Nixon directory
     */
    public void createDirectory(final String directoryName) {
        try {
            Files.createDirectories(getPath(directoryName));
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create directory: " + directoryName, e);
            throw new IllegalStateException("Unable to create directory!", e);
        }
    }

    /**
     * Creates a file at the specified path.
     *
     * @param fileName the file name inside the Nixon directory
     */
    public void createFile(final String fileName) {
        try {
            final Path filePath = getPath(fileName);
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create file: " + fileName, e);
            throw new IllegalStateException("Unable to create file!", e);
        }
    }

    /**
     * Lists files in the specified directory.
     *
     * @param path the directory path inside the Nixon directory
     * @return array of files, or null if directory doesn't exist
     */
    public File[] listFiles(final String path) {
        return getPath(path).toFile().listFiles();
    }

    /**
     * Get a Path object from the file name.
     *
     * @param fileName the file name inside the Nixon directory
     * @return the Path object
     */
    public Path getPath(final String fileName) {
        return CLIENT_PATH.resolve(fileName.replace("\\", "/"));
    }

    /**
     * Get a File object from the file name.
     *
     * @param fileName the file name inside the Nixon directory
     * @return the File object
     */
    public File getFile(final String fileName) {
        return getPath(fileName).toFile();
    }

    /**
     * Deletes the specified file if it exists.
     *
     * @param fileName the file name inside the Nixon directory
     */
    public void delete(final String fileName) {
        final Path filePath = getPath(fileName);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                LOGGER.info("Successfully deleted file: " + fileName);
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete file: " + fileName, e);
            throw new IllegalStateException("Unable to delete file!", e);
        }
    }

    /**
     * Deletes the specified file if it exists.
     *
     * @param path the path to delete
     */
    public void delete(final Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                LOGGER.info("Successfully deleted file: " + path.toString());
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete file: " + path.toString(), e);
            throw new IllegalStateException("Unable to delete file!", e);
        }
    }

    /**
     * Creates a backup of the specified file.
     *
     * @param fileName the file name inside the Nixon directory
     * @return true if backup was created successfully
     */
    public boolean createBackup(final String fileName) {
        final Path originalPath = getPath(fileName);
        final Path backupPath = getPath(fileName + ".backup");

        try {
            if (Files.exists(originalPath)) {
                Files.copy(originalPath, backupPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Created backup for file: " + fileName);
                return true;
            }
            return false;
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create backup for file: " + fileName, e);
            return false;
        }
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param fileName the file name inside the Nixon directory
     * @return file size in bytes, or -1 if file doesn't exist
     */
    public long getFileSize(final String fileName) {
        try {
            final Path filePath = getPath(fileName);
            return Files.exists(filePath) ? Files.size(filePath) : -1;
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to get file size: " + fileName, e);
            return -1;
        }
    }

    /**
     * Reads the content of an InputStream and returns it as a String.
     *
     * @param inputStream the InputStream to read from
     * @return the content of the InputStream as a String
     */

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public File getFileOrPath(final String fileName) {
        return new File(CLIENT_PATH + fileName.replace("\\", SEPARATOR));
    }
}