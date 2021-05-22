package com.github.hypfvieh.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RollingFileUtilTest {

    private static File testSource;
    private static List<File> createdTempFiles = new ArrayList<>();

    @BeforeAll
    public static void beforeAll() throws IOException {
        testSource = createTempFile();
        createdTempFiles.add(testSource);
    }

    @AfterAll
    public static void afterAll() {
        if (!createdTempFiles.isEmpty()) {
            createdTempFiles.forEach(f -> f.delete());
        }
    }

    static File createTempFile() throws IOException {
        return File.createTempFile("rolling-test", ".tmp");
    }

    @Test
    public void testBrokenPlaceholderRepeated() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> RollingFileUtil.doFileRolling(testSource, "%{n}-broken-%{n}", 10));
    }

    @Test
    public void testBrokenPlaceholderMissing() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> RollingFileUtil.doFileRolling(testSource, "broken", 10));
    }

    @Test
    public void testRolling5() throws IOException {
        RollingFileUtil.doFileRolling(createTempFile(), "rollover5-%{n}.%{ext}", 5);
        RollingFileUtil.doFileRolling(createTempFile(), "rollover5-%{n}.%{ext}", 5);
        RollingFileUtil.doFileRolling(createTempFile(), "rollover5-%{n}.%{ext}", 5);
        RollingFileUtil.doFileRolling(createTempFile(), "rollover5-%{n}.%{ext}", 5);
        RollingFileUtil.doFileRolling(createTempFile(), "rollover5-%{n}.%{ext}", 5);


        List<File> toDeleteLater = new ArrayList<>();

        Files.walkFileTree(testSource.getParentFile().toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().startsWith("rollover5-")) {
                    toDeleteLater.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e)
                throws IOException {

                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {

                return FileVisitResult.CONTINUE;
            }
        });

        assertEquals(5, toDeleteLater.size());
        toDeleteLater.forEach(f -> f.delete());
    }

    @Test
    public void testRolling3WithDate() throws IOException {
        RollingFileUtil.doFileRolling(createTempFile(), "rolloverWithDate3-%{date:yyyy-MM-dd}_%{n}.%{ext}", 3);
        RollingFileUtil.doFileRolling(createTempFile(), "rolloverWithDate3-%{date:yyyy-MM-dd}_%{n}.%{ext}", 3);
        RollingFileUtil.doFileRolling(createTempFile(), "rolloverWithDate3-%{date:yyyy-MM-dd}_%{n}.%{ext}", 3);


        List<File> toDeleteLater = new ArrayList<>();

        Files.walkFileTree(testSource.getParentFile().toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().startsWith("rolloverWithDate3-")) {
                    toDeleteLater.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e)
                throws IOException {

                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {

                return FileVisitResult.CONTINUE;
            }
        });

        assertEquals(3, toDeleteLater.size());
        toDeleteLater.forEach(f -> f.delete());
    }
}
