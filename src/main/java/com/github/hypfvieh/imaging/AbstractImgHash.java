package com.github.hypfvieh.imaging;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public abstract class AbstractImgHash {

    private static final ColorConvertOp COLOR_CONVERTER = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    /**
     * Convert the given image to an array of integer, where each array entry represents one pixel.
     * The image is read from top to bottom, left to right.
     *
     * @param _image image
     * @return int array or null if _image is null
     */
    protected int[] createPixelArray(BufferedImage _image) {
        if (_image == null) {
            return null;
        }

        int[] result = new int[_image.getHeight()*_image.getWidth()];
        int c = 0;
        for (int y = 0; y < _image.getHeight(); y++) {
            for (int x = 0; x < _image.getWidth(); x++) {
                  int  clr   = _image.getRGB(x, y);
                  result[c++] = clr;
            }
        }

        return result;
    }

    /**
     * Creates a pixel array matrix from the given image.
     *
     * @param _image image
     * @return array of double array or null if _image is null
     */
    protected double[][] createPixelArrayMatrix(BufferedImage _image) {
        if (_image == null) {
            return null;
        }
        double[][] result = new double[_image.getHeight()][_image.getWidth()];
        for (int y = 0; y < _image.getHeight(); y++) {
            for (int x = 0; x < _image.getWidth(); x++) {
                  int  clr   = _image.getRGB(x, y);
                  result[y][x] = clr;
            }
        }

        return result;
    }

    /**
     * Convert the given image to grayscale and resize it to the given scale.
     * Will return a new {@link BufferedImage} instance.
     * @param _width width
     * @param _height heigth
     * @param _input  input image
     * @return {@link BufferedImage} or null if _input is null
     */
    protected BufferedImage resizeAndGrayScale(int _width, int _height, BufferedImage _input) {
        if (_input == null) {
            return null;
        }
        // create new resized and gray-scaled image
        BufferedImage scaledImage = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();

        if (_input.getColorModel().hasAlpha()) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(_input, 0, 0, _width, _height, null);
        g.dispose();

        COLOR_CONVERTER.filter(scaledImage, scaledImage);

        return scaledImage;
    }

    /**
     * Converts a bitmask (array of boolean) to a hex string.
     *
     * @param bitMask mask
     * @param _minLen length
     * @return string
     */
    protected String bitmaskToHex(boolean[] bitMask, int _minLen) {
        long bitSetInt = 0;
        for (int i = 0 ; i < bitMask.length ; i++) {
           bitSetInt = (bitSetInt | (bitMask[i]?1:0)) << 1;
        }
        if (_minLen <= -1) {
        	return String.format("%x", bitSetInt);
        }
        return String.format("%0"+ _minLen + "x", bitSetInt);
    }
}
