package com.github.hypfvieh.imaging;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class ImageCompare {


    /**
     * Calculate hashes for _img1 and _img2 using _hasher and return the hamming distance as result.
     *
     * @param _hasher hash implemenation to use
     * @param _img1 first image
     * @param _img2 second image
     * @return distance score
     */
    public static int compareWith(IImgHash _hasher, File _img1, File _img2) {
        Objects.requireNonNull(_hasher);
        Objects.requireNonNull(_img1);
        Objects.requireNonNull(_img2);

        try {
            String img1Hash = _hasher.computeHash(_img1);
            String img2Hash = _hasher.computeHash(_img2);
            return calcHammingDistance(img1Hash, img2Hash);
        } catch (IOException _ex) {
            throw new UncheckedIOException(_ex);
        }
    }


    /**
     * Calculate the hamming distance of the two given strings.
     * String must have the same length!
     * <p>
     * For further explanation about the Hamming Distance, take a look at its
     * Wikipedia page at <a href="http://en.wikipedia.org/wiki/Hamming_distance">http://en.wikipedia.org/wiki/Hamming_distance</a>.
     * </p>
     * @param _left first checksum
     * @param _right second checksum
     * @return distance score
     * @throws IllegalArgumentException if strings do not have same length
     * @throws NullPointerException if either _left or _right string is null
     */
    public static int calcHammingDistance(String _left, String _right) {
        Objects.requireNonNull(_left, "Left string should not be null");
        Objects.requireNonNull(_right, "Right string should not be null");

        if (_left.length() != _right.length()) {
            throw new IllegalArgumentException("Strings must have the same length (left: " + _left.length() + ", right: " + _right.length() + ")");
        }

        int distance = 0;

        for (int i = 0; i < _left.length(); i++) {
            if (_left.charAt(i) != _right.charAt(i)) {
                distance++;
            }
        }

        return distance;
    }
}
