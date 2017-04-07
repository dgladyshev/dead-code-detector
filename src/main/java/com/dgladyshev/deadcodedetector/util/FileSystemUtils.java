package com.dgladyshev.deadcodedetector.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public final class FileSystemUtils {

    private FileSystemUtils() {
    }

    /**
     * Silently deletes directory if it exists.
     *
     * @param dir path ot dir
     */
    public static void deleteDirectoryIfExists(String dir) {
        File file = new File(dir);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException | IllegalArgumentException ex) {
                log.error("There is no files to delete directory or deletion has failed {}", dir, ex);
            }
        }
    }

    public static String getCanonicalPath(String relativePath) throws IOException {
        return new File(relativePath).getCanonicalPath();
    }

    //Return false if check fails because of IOException
    public static boolean checkFileContainsString(String filePath, String substring) {
        try {
            return FileUtils
                    .readFileToString(new File(filePath), Charset.defaultCharset())
                    .contains(substring);
        } catch (IOException ex) {
            log.error("Failed to read contents of the file {} because of exception {}", filePath, ex);
            return false;
        }
    }

}