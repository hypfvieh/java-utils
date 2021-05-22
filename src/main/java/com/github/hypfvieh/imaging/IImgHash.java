package com.github.hypfvieh.imaging;

import java.io.File;
import java.io.IOException;

/**
 * Interface which must be implemented by every image hashing class.
 *
 * @author hypfvieh
 * @since v1.0.6 - 2018-10-09
 */
@FunctionalInterface
public interface IImgHash {
    /**
     * Calculate the hash of given image.
     * @param _image image to hash
     * @return hash as string
     * @throws IOException when file processing fails
     */
    String computeHash(File _image) throws IOException;
}
