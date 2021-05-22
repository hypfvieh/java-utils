package com.github.hypfvieh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for compressing/decompressing files.
 *
 * @author hypfvieh
 *
 */
public class CompressionUtil {

	private CompressionUtil() {
	}

	private static final Logger LOGGER = System.getLogger(CompressionUtil.class.getName());

	/**
     * Extracts a GZIP compressed file to the given outputfile.
     *
     * @param _compressedFile
     * @param _outputFileName
     * @return file-object with outputfile or null on any error.
     */
    public static File extractFileGzip(String _compressedFile, String _outputFileName) {
        return decompress(CompressionMethod.GZIP, _compressedFile, _outputFileName);
    }

    /**
     * Compresses the given file with GZIP and writes the compressed file to outputFileName.
     * @param _sourceFile
     * @param _outputFileName
     * @return new File object with the compressed file or null on error
     */
    public static File compressFileGzip(String _sourceFile, String _outputFileName) {
        return compress(CompressionMethod.GZIP, _sourceFile, _outputFileName);
    }

    /**
     * Extract a file using the given {@link CompressionMethod}.
     * @param _method
     * @param _compressedFile
     * @param _outputFileName
     * @return file object which represents the uncompressed file or null on error
     */
    public static File decompress(CompressionMethod _method, String _compressedFile, String _outputFileName) {
    	if (_method == null || _compressedFile == null) {
    		return null;
    	}

    	File inputFile = new File(_compressedFile);

        if (!inputFile.exists()) {
            return null;
        }

        try {
            Constructor<? extends InputStream> constructor = _method.getInputStreamClass().getConstructor(InputStream.class);
            if (constructor != null) {
            	InputStream inputStream = constructor.newInstance(new FileInputStream(inputFile));

                // write decompressed stream to new file
                Path destPath = Paths.get(_outputFileName);
                Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
                return destPath.toFile();

            }
        } catch (IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException iOException) {
            LOGGER.log(Level.ERROR, "Cannot uncompress file: ", iOException);
        }

        return null;
    }

    /**
     * Compress a file using the given {@link CompressionMethod}.
     * @param _method
     * @param _sourceFile
     * @param _outputFileName
     * @return file object which represents the compressed file or null on error
     */
    public static File compress(CompressionMethod _method, String _sourceFile, String _outputFileName) {
    	if (_method == null || _sourceFile == null) {
    		return null;
    	}

        File inputFile = new File(_sourceFile);

        if (!inputFile.exists()) {
            return null;
        }

        byte[] buffer = new byte[1024];
        try {
        	Constructor<? extends DeflaterOutputStream> constructor = _method.getOutputStreamClass().getConstructor(OutputStream.class);
        	if (constructor != null) {
        		DeflaterOutputStream compressOutputStream = constructor.newInstance(new FileOutputStream(_outputFileName));
	            try (FileInputStream in = new FileInputStream(_sourceFile)) {
	                int len;
	                while ((len = in.read(buffer)) > 0) {
	                    compressOutputStream.write(buffer, 0, len);
	                }

	                in.close();

	                compressOutputStream.finish();
	                compressOutputStream.close();
	                return new File(_outputFileName);
	            }
        	}
        } catch (IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException _ex) {
            LOGGER.log(Level.ERROR, "Cannot uncompress file: ", _ex);
        }
        return null;

    }



    /**
     * Enum of supported CompressionMethods.
     */
    public static enum CompressionMethod {
    	GZIP(GZIPInputStream.class, GZIPOutputStream.class);

    	private final Class<? extends InputStream> inputStreamClass;
    	private final Class<? extends DeflaterOutputStream> outputStreamClass;

    	private CompressionMethod(Class<? extends InputStream> _inputStreamClass, Class<? extends DeflaterOutputStream> _outputStreamClass) {
    		inputStreamClass = _inputStreamClass;
    		outputStreamClass = _outputStreamClass;
    	}

		public Class<? extends InputStream> getInputStreamClass() {
			return inputStreamClass;
		}

		public Class<? extends DeflaterOutputStream> getOutputStreamClass() {
			return outputStreamClass;
		}

    }
}
