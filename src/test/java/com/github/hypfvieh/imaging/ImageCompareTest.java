package com.github.hypfvieh.imaging;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class ImageCompareTest extends AbstractBaseUtilTest {

    @Test
    public void testCompareDHashEqual() {
        int score = ImageCompare.compareWith(ImgDHash::computeHash, new File("src/test/resources/imaging/compare.jpg"), new File("src/test/resources/imaging/compare.jpg"));
        assertEquals(0, score);
    }

    @Test
    public void testCompareDHashUnEqual() {
        int score = ImageCompare.compareWith(ImgDHash::computeHash, new File("src/test/resources/imaging/compare.jpg"), new File("src/test/resources/imaging/nocompare.jpg"));
        assertEquals(13, score);
    }

    @Test
    public void testCompareAHashEqual() {
        int score = ImageCompare.compareWith(ImgAHash::computeHash, new File("src/test/resources/imaging/compare.jpg"), new File("src/test/resources/imaging/compare.jpg"));
        assertEquals(0, score);
    }

    @Test
    public void testCompareAHashUnEqual() {
        int score = ImageCompare.compareWith(ImgAHash::computeHash, new File("src/test/resources/imaging/compare.jpg"), new File("src/test/resources/imaging/nocompare.jpg"));
        assertEquals(15, score);
    }

    @Test
    public void testComparePHashEqual() {
        int score = ImageCompare.compareWith(ImgPHash::computeHash, new File("src/test/resources/imaging/compare.jpg"), new File("src/test/resources/imaging/compare.jpg"));
        assertEquals(0, score);
    }

    @Test
    public void testComparePHashUnEqual() {
        int score = ImageCompare.compareWith(ImgPHash::computeHash, new File("src/test/resources/imaging/compare.jpg"), new File("src/test/resources/imaging/nocompare.jpg"));
        assertEquals(11, score);
    }
}
