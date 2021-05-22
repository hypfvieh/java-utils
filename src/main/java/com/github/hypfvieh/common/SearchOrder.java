package com.github.hypfvieh.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.System.Logger.Level;

/**
 * Defines where to look for a library.
 *
 */
public enum SearchOrder {
    /** Look in any given external path */
    CUSTOM_PATH,
    /** Look in classpath, this includes directory and the jar(s) */
    CLASS_PATH,
    /** Look in system path (e.g. /usr/lib on linux/unix systems) */
    SYSTEM_PATH;

	/**
     * Search for the given filename in given {@link SearchOrder}.
     * @param _fileName filename
     * @param _order order
     * @return InputStream of first found matching file or null if no file found
     */
    public static InputStream findFile(String _fileName, SearchOrder... _order) {
        if (_fileName == null || _fileName.isEmpty() || _order == null) {
            return null;
        }

        InputStream result = null;
        for (SearchOrder so : _order) {
            switch (so) {
                case CLASS_PATH:
                    result = SearchOrder.class.getClassLoader().getResourceAsStream(_fileName);
                    if (result != null) {
                        return result;
                    }
                    break;
                case CUSTOM_PATH:
                    File file = new File(_fileName);
                    if (!file.exists()) {
                        continue;
                    }

                    result = toStream(file);
                    if (result != null) {
                        return result;
                    }
                    break;
                case SYSTEM_PATH:
                    String getenv = System.getenv("PATH");
                    getenv = getenv.replace(";", ":");
                    for (String p : getenv.split(":")) {
                        File curFile = new File (p, _fileName);
                        if (!curFile.exists()) {
                            continue;
                        }
                       result = toStream(curFile);
                       if (result != null) {
                           return result;
                       }
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * Convert file to input stream if possible.
     * @param _file file
     * @return InputStream or null if file could not be found
     */
    private static InputStream toStream(File _file) {
        try {
            return new FileInputStream(_file);
        } catch (FileNotFoundException _ex) {
            System.getLogger(SearchOrder.class.getName()).log(Level.DEBUG, "File {} not found", _file);
        }
        return null;
    }
}