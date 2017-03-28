package com.dgladyshev.deadcodedetector.util;

import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public final class FileSystemUtils {

    private FileSystemUtils() {
    }

    /**
     * Silently deletes directory if it exists.
     * @param dir path ot dir
     */
    public static void deleteDirectoryIfExists(String dir) {
        File file = new File(dir);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException | IllegalArgumentException ex) {
                log.error("There is no files to delete directory or deletion has failed {}", dir);
            }
        }
    }
}