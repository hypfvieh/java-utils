package com.github.hypfvieh.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.hypfvieh.util.StringUtil;
import com.github.hypfvieh.util.SystemUtil;

/**
 * Class to provide a simple file name rollover feature.<br>
 *<br>
 * The output file name is specified by a file name pattern.<br>
 *<br>
 * Supported placeholders are<br>
 * <pre>
 * %{n} &rArr; replaced by current number of file
 * %{ext} &rArr; extension of the file without leading dot
 * %{date:PATTERN} &rArr; use date/time as pattern, where PATTERN is a valid {@link DateTimeFormatter} pattern
 * </pre>
 *
 * Every pattern has to specify exactly one %{n}-placeholder which will be replaced by the current<br>
 * number of files. Providing no %{n} placeholder or more than one will throw {@link IllegalArgumentException}.<br>
 *<br>
 * Older files will be moved to a new name until the given maximum of files is reached.<br>
 * When maximum is reached, the oldest file will be removed and all other files will be renamed.<br>
 *<br>
 * The newest file will always have the lowest number.<br>
 *
 * @author hypfvieh
 * @since v1.0.6 - 2019-04-29
 */
public class RollingFileUtil {

    /**
     * Do a rollover of the given source file using the provided pattern.
     *
     * @param _fileToRollOver file to roll over
     * @param _pattern pattern of the target file
     * @param _maxFiles maximum amount of files created before deleting
     * @return File object with the new name of the given source, null on error
     * @throws IOException if moving fails
     */
    public static File doFileRolling(File _fileToRollOver, String _pattern, int _maxFiles) throws IOException {
        if (_fileToRollOver == null || !_fileToRollOver.exists()) {
            throw new FileNotFoundException("Given file " + _fileToRollOver + " is invalid or does not exist");
        }

        RollingFileUtil util = new RollingFileUtil();

        util.validatePattern(_pattern);

        File checkAndMove = util.checkAndMove(_fileToRollOver, _pattern, _maxFiles);

        return checkAndMove;
    }

    /**
     * Rename the given source file to given target file and return the new file name.
     *
     * @param _srcFile source
     * @param _targetFile target
     * @return _targetFile
     *
     * @throws IOException if move fails
     */
    private File renameFile(File _srcFile, File _targetFile) throws IOException {
        Files.move(_srcFile.toPath(), _targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        return _targetFile;
    }

    /**
     * Checks and rolls over the given file.
     *
     * @param _fileToRollOver file to roll-over
     * @param _filenamePattern filename pattern to use
     * @param _maxFiles maximum files allowed to exist
     * @return filename of the last successful move
     *
     * @throws IOException if move fails
     */
    private File checkAndMove(File _fileToRollOver, String _filenamePattern, int _maxFiles) throws IOException {
        String baseFileName = expandFilePattern(_filenamePattern, _fileToRollOver.getName());
        int zeropad = (_maxFiles / 10) + 1;

        Map<File, File> moveMap = new LinkedHashMap<>();

        for (int i = 1; i < _maxFiles +1; i++) {
            File expectedFile = new File(_fileToRollOver.getParentFile(), baseFileName.replace("%{n}", String.format("%" + (zeropad == 0 ? "d" : "0" + zeropad + "d"), i)));
            File newFileName = new File(_fileToRollOver.getParentFile(), baseFileName.replace("%{n}", String.format("%" + (zeropad == 0 ? "d" : "0" + zeropad + "d"), i+1)));

            if (i+1 > _maxFiles) { // a value of null means: delete this file
                newFileName = null;
            }

            moveMap.put(expectedFile, newFileName);
        }

        List<File> mapKeys = new ArrayList<>(moveMap.keySet());
        Collections.reverse(mapKeys);

        // check if first desired file name is "available", if so, use it, otherwise we have to roll over
        File firstFile = mapKeys.get(mapKeys.size() - 1);
        if (!firstFile.exists()) {
            renameFile(_fileToRollOver, firstFile);
            return firstFile;
        }

        for (File k : mapKeys) {
            if (!k.exists()) {
                moveMap.remove(k);
                continue;
            }

            File fileInMap = moveMap.get(k);

            if (fileInMap == null) { // null means this file should be removed
                moveMap.remove(k);
                k.delete();
                continue;
            }

            if (k.exists() && !fileInMap.exists()) {
                renameFile(k, fileInMap);
                moveMap.remove(k);
            }
        }

        // all files have been moved or deleted, re-run method to move the original file to the first new name
        if (moveMap.isEmpty()) {
            return checkAndMove(_fileToRollOver, _filenamePattern, _maxFiles);
        }

        // all files have been moved, so the first name should be available now
        if (moveMap.size() > 1) {
            throw new IllegalStateException("Map should only contain one entry at this point");
        }

        return renameFile(_fileToRollOver, moveMap.get(new ArrayList<>(moveMap.keySet()).get(0)));
    }

    /**
     * Expand the given pattern to a proper filename (%{n} is not expanded here).
     *
     * @param _pattern pattern to expand
     * @param _origFilename original file name
     *
     * @return new filename
     */
    private String expandFilePattern(String _pattern, String _origFilename) {
        String filePattern = _pattern;

        Pattern datePattern = Pattern.compile("%\\{date:([^\\}]+)\\}");
        Matcher matcher = datePattern.matcher(filePattern);

        while (matcher.find()) {
            String javaDatePattern = matcher.group(1);
            String date;
            try {
                date = DateTimeFormatter.ofPattern(javaDatePattern).format(LocalDateTime.now());
            } catch (Exception _ex) {
                throw new IllegalArgumentException("Given date format is not a valid java DateTimeFormatter format!", _ex);
            }

            filePattern = filePattern.replaceFirst("%\\{date:" + javaDatePattern + "\\}", date);

        }

        String fileExtension = SystemUtil.getFileExtension(_origFilename);
        filePattern = filePattern.replace("%{ext}", fileExtension);

        return filePattern;
    }

    /**
     * Checks if the provided pattern is valid and does not have too many or too less %{n} placeholders.
     *
     * @param _pattern pattern to check
     */
    private void validatePattern(String _pattern) {

        if (_pattern == null) {
            throw new IllegalArgumentException("No filename pattern provided");
        } else if (StringUtil.isBlank(_pattern)) {
            throw new IllegalArgumentException("Invalid filename pattern provided");
        }


        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = _pattern.indexOf("%{n}",lastIndex);

            if(lastIndex != -1){
                count++;
                lastIndex += "%{n}".length();
            }
        }

        if (count > 1) {
            throw new IllegalArgumentException("Placeholder %{n} can only be used once");
        } else if (count == 0) {
            throw new IllegalArgumentException("Usage of placeholder %{n} is always required");
        }
    }

}
