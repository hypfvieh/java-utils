package com.github.hypfvieh.imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Class which implements PHash (perceptual hash) algorithm to create hashes of images.
 *
 * @author hypfvieh
 * @since 1.0.6 - 2018-10-09
 */
public class ImgPHash extends AbstractImgHash {

    private static final int REGULAR_SIZE = 32;
    private static final int SMALL_SIZE = 8;
    private static double[] COEFFICIENTS = initCoefficients(REGULAR_SIZE);

    private static final ImgPHash INSTANCE = new ImgPHash();

    private ImgPHash() {

    }

    private static double[] initCoefficients(int _size) {
        double coeffi[] = new double[_size];

        for (int i = 1; i < _size; i++) {
            coeffi[i] = 1;
        }
        coeffi[0] = 1 / Math.sqrt(2.0);

        return coeffi;
    }

    private double[][] applyDCT(double[][] f) {
        int nSize = REGULAR_SIZE;

        double[][] result = new double[nSize][nSize];
        for (int u = 0; u < nSize; u++) {
            for (int v = 0; v < nSize; v++) {
                double sum = 0.0;
                for (int i = 0; i < nSize; i++) {
                    for (int j = 0; j < nSize; j++) {
                        sum += Math.cos(((2 * i + 1) / (2.0 * nSize)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * nSize)) * v * Math.PI) * (f[i][j]);
                    }
                }
                sum *= ((COEFFICIENTS[u] * COEFFICIENTS[v]) / 4.0);
                result[u][v] = sum;
            }
        }
        return result;
    }

    /**
     * Compute the PHash of the given file (which should be a valid image).
     *
     * @param _image image to hash
     * @return hash string
     * @throws IOException on any error when reading file
     */
    public static String computeHash(File _image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(_image);
        BufferedImage resizedImage = INSTANCE.resizeAndGrayScale(REGULAR_SIZE, REGULAR_SIZE, bufferedImage);

        return INSTANCE.hash(resizedImage);
    }

    /**
     * Creates hash of the given buffered image.
     * @param _resizedImg image
     * @return hash string
     */
    private String hash(BufferedImage _resizedImg) {
        double[][] vals = new double[REGULAR_SIZE][REGULAR_SIZE];

        for (int x = 0; x < _resizedImg.getWidth(); x++) {
            for (int y = 0; y < _resizedImg.getHeight(); y++) {
                vals[x][y] = _resizedImg.getRGB(x, y);
            }
        }

        double[][] dctVals = applyDCT(vals);

        double total = 0;

        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                total += dctVals[x][y];
            }
        }
        total -= dctVals[0][0];

        double avg = total / ((SMALL_SIZE * SMALL_SIZE) - 1);

        boolean[] bitMask = new boolean[SMALL_SIZE * SMALL_SIZE];

        int c = 0;
        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                if (x != 0 && y != 0) {
                    bitMask[c++] = (dctVals[x][y] > avg);
                }
            }
        }

        return bitmaskToHex(bitMask, 16);
    }
}
