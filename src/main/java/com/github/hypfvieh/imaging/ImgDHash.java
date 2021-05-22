package com.github.hypfvieh.imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Class which implements the DHash (difference hash) algorithm to create hashes of images.
 *
 * @author hypfvieh
 * @since 1.0.6 - 2018-10-09
 */
public class ImgDHash extends AbstractImgHash {

    private static final ImgDHash INSTANCE = new ImgDHash();

    // utility class, hide constructor
    private ImgDHash() {

    }

    /**
     * Compute the DHash of the given file (which should be a valid image).
     *
     * @param _image image to hash
     * @return hash string
     * @throws IOException on any error when reading file
     */
    public static String computeHash(File _image) throws IOException {
        BufferedImage bufferedImage;
        bufferedImage = ImageIO.read(_image);
        BufferedImage resizedImage = INSTANCE.resizeAndGrayScale(9, 8, bufferedImage);

        int[] d = INSTANCE.createPixelArray(resizedImage);

        String s = INSTANCE.hash(d);
        return s;
    }

    /**
     * Read the given pixel array and convert it to a hash.<br>
     * First the pixel array is converted to a boolean array (bitMask).<br>
     * Every true value in the array is calculated by comparing the pixel against<br>
     * the next pixel (neighbor). If the current pixel is smaller than the next, true is set.<br>
     * <br>
     * After that, the boolean array is converted to a long and returned as hex-string
     *
     * @param _pixelArray pixel array
     * @return hex string
     */
    private String hash(int[] _pixelArray) {

        boolean[] bitMask = new boolean[_pixelArray.length-1];

        for (int i = 0; i < _pixelArray.length - 1; i++) {
            int n = i + 1;
            if (n > _pixelArray.length -1) {
                break;
            }

            if (_pixelArray[i] < _pixelArray[n]) {
                bitMask[i] = true;
            }
        }

        return bitmaskToHex(bitMask, 16);
    }

}
