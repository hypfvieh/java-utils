package com.github.hypfvieh.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.hypfvieh.common.SearchOrder;
import com.github.hypfvieh.util.SystemUtil;

/**
 * Helper class to load a native library either from a given path, the classpath or system library path.
 */
public final class NativeLibraryLoader {

    private static final NativeLibraryLoader INSTANCE = new NativeLibraryLoader();

    private boolean enabled = true;

    /**
     * Private constructor - this class will be a singleton
     */
    private NativeLibraryLoader() {
    }

    /**
     * Check if {@link NativeLibraryLoader} will is active and will load libraries.
     * @return
     */
    public static boolean isEnabled() {
        return INSTANCE.enabled;
    }

    /**
     * Enable/Disable library loading.
     * @param _enabled
     */
    public static void setEnabled(boolean _enabled) {
        INSTANCE.enabled = _enabled;
    }


    /**
     * Load the given _libName from one of the given pathes (will search for the library and uses first match).
     *
     * @param _libName library to load
     * @param _searchPathes pathes to search
     */
    public static void loadLibrary(boolean _trySystemLibsFirst, String _libName, String... _searchPathes) {
        if (!isEnabled()) {
            return;
        }

        List<SearchOrder> loadOrder = new ArrayList<>();
        
        if (_trySystemLibsFirst) {
            loadOrder.add(SearchOrder.SYSTEM_PATH);
        }
        
        loadOrder.add(SearchOrder.CUSTOM_PATH);
        loadOrder.add(SearchOrder.CLASS_PATH);
        
        loadLibrary(_libName, loadOrder.toArray(new SearchOrder[] {}), _searchPathes);
    }

    /**
     * Tries to load the given library using the given load/search order.
     */
    public static void loadLibrary(String _libName, SearchOrder[] _loadOrder, String... _searchPathes) {
        if (!isEnabled()) {
            return;
        }
        
        for (SearchOrder order : _loadOrder) {
            if (INSTANCE.findProperNativeLib(order, _libName, _searchPathes) == null) {
                return;
            }
        }
        throw new RuntimeException("Could not load library from any given source: " + Arrays.toString(_loadOrder));
    }
    
    /**
     * Tries to load a library from the given search path, depending on given {@link SearchOrder} value.
     * 
     * @param _order search order option
     * @param _libName name of the library
     * @param _searchPathes pathes to search for library
     * @return {@link Throwable} if loading fails, null if loading was successful
     */
    private Throwable findProperNativeLib(SearchOrder _order, String _libName, String[] _searchPathes) {
        String arch = System.getProperty("os.arch");
        Throwable lastErr = null;
        
        if (_order == SearchOrder.SYSTEM_PATH) { // search in system pathes (e.g. content of LD_LIBRARY_PATH)
        	lastErr = loadSystemLib(_libName);
            if (lastErr == null) {
                return null;
            }
            
        } else if (_order == SearchOrder.CLASS_PATH) {
            for (String path : _searchPathes) {
                // first, try with OS architecture in path name
                String fileNameWithPath = SystemUtil.concatFilePath(false, path, arch, _libName);
                InputStream libAsStream = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(fileNameWithPath);
                lastErr = loadFromStream(fileNameWithPath, libAsStream);
                if (lastErr == null) {
                    return null;
                } else { // then try without OS architecture in path name
                    
                    fileNameWithPath = SystemUtil.concatFilePath(false, path, _libName);
                    libAsStream = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(fileNameWithPath);
                    lastErr = loadFromStream(fileNameWithPath, libAsStream);
                    if (lastErr == null) {
                        return null;
                    }
                }
            }
        } else { // search in custom pathes
            
            for (String path : _searchPathes) {
                File file = new File(SystemUtil.concatFilePath(false, path, arch, _libName));
                if (!file.exists()) { // file not exists in file system, go to next entry
                    continue;
                }
                lastErr = loadLib(file.getAbsolutePath());
                if (lastErr == null) {
                    return null;
                }
            }
            lastErr = new IOException("No library in custom path found.");
        }
        
        return lastErr;
    }
    
    /**
     * Loads a library from the given stream, using the given filename (including path).
     * 
     * @param _fileNameWithPath
     * @param _libAsStream
     * @return {@link Throwable} if any Exception/Error occurs, null otherwise
     */
    private Throwable loadFromStream(String _fileNameWithPath, InputStream _libAsStream) {
        String fileExt = getFileExtension(_fileNameWithPath);
        String prefix = _fileNameWithPath.replace(new File(_fileNameWithPath).getParent(), "").replace("." + fileExt, "");

        // extract the library
        try {
            File tmpFile = extractToTemp(_libAsStream, prefix, fileExt);
            Throwable loadLibErr = loadLib(tmpFile.getAbsolutePath());
            if (loadLibErr != null) {
                return loadLibErr;
            }
        } catch (Exception _ex) {
            return _ex;
        }
        
        return null;
    }
   
    /**
     * Tries to load a library from the given path.
     * Will catches exceptions and return them to the caller.
     * Will return null if no exception has been thrown.
     *
     * @param _lib
     * @return null on success, {@link Throwable} otherwise
     */
    private Throwable loadLib(String _lib) {
        try {
            System.load(_lib);
            return null;
        } catch (Throwable _ex) {
            return _ex;
        }
    }

    /**
     * Tries to load a library using System.loadLibrary.
     * This will also build different filename combinations based on the given filename
     * (without prefix like 'lib' and without suffix like '.so' or '.dll')
     * @param _libName
     * @return null on success, {@link Throwable} otherwise
     */
    private Throwable loadSystemLib(String _libName) {
        // system library could have different name then the real lib (e.g. instead of 'libsomething.so' it could be called 'something')
        List<String> possibleNames = new ArrayList<>();
        String fileExtension = getFileExtension(_libName);

        String fileWithoutExt = null;

        if (fileExtension != null && !fileExtension.isEmpty()) {
            fileWithoutExt = _libName.replace("." + fileExtension, "");
            possibleNames.add(_libName.replace("." + fileExtension, "")); // 'libsomething'
        }
        if (_libName.startsWith("lib")) { // most librarys on *nix systems start with 'lib'
            if (fileWithoutExt != null) {
                possibleNames.add(fileWithoutExt.replaceFirst("^lib", "")); // 'something'
            }
            possibleNames.add(_libName.replaceFirst("^lib", "")); // 'something.so'
        }

        Throwable lastError = null;
        for (String name : possibleNames) {
            try {
                System.loadLibrary(name);
                return null; // if no exception has been thrown, loading has succedded, so we are done here
            } catch (Throwable _ex) {
                lastError = _ex;
            }
        }
       return lastError; // if we reach this, no system library was found
    }

    /**
     * Extract the file behind InputStream _fileToExtract to the tmp-folder.
     *
     * @param _fileToExtract InputStream with file to extract
     * @param _tmpName temp file name
     * @param _fileSuffix temp file suffix
     *
     * @return temp file object
     * @throws IOException on any error
     */
    private File extractToTemp(InputStream _fileToExtract, String _tmpName, String _fileSuffix) throws IOException {
        if (_fileToExtract == null) {
            throw new IOException("Null stream");
        }
        File tempFile = File.createTempFile(_tmpName, _fileSuffix);
        tempFile.deleteOnExit();

        if (!tempFile.exists()) {
            throw new FileNotFoundException("File " + tempFile.getAbsolutePath() + " could not be created");
        }

        byte[] buffer = new byte[1024];
        int readBytes;

        OutputStream os = new FileOutputStream(tempFile);
        try {
            while ((readBytes = _fileToExtract.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } finally {
            os.close();
            _fileToExtract.close();
        }
        return tempFile;
    }

    /**
     * Extracts the file extension (part behind last dot of a filename).
     * Only returns the extension, without the leading dot.
     *
     * @param _fileName
     * @return extension, empty string if not dot was found in filename or null if given String was null
     */
    public static String getFileExtension(String _fileName) {
        if (_fileName == null) {
            return null;
        }
        int lastDot = _fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return _fileName.substring(lastDot + 1);
    }
}
