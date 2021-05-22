package com.github.hypfvieh.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.hypfvieh.util.StringUtil;

/**
 * Formats table content into lines of columns of a given character width.
 * After creation of the object with a suitable split function, an optional
 * padding character (defaults to space) and an integer array of column widths,
 * call {@link #formatLine(String...)} for each String array of content.
 *
 */
public class TableColumnFormatter {

    private final int[]               columnWidths;
    private final StringSplitOperator splitOperator;
    private final String              padChar;

    public TableColumnFormatter(StringSplitOperator _splitOperator, char _padChar, int... _columnWidths) {
        if (_splitOperator == null) {
            throw new NullPointerException("Split operator required");
        }
        if (_columnWidths == null || _columnWidths.length == 0) {
            throw new IllegalArgumentException("Array of column widths required");
        }
        for (int i = 0; i < _columnWidths.length; i++) {
            if (_columnWidths[i] < 1) {
                throw new IllegalArgumentException("Invalid column width at index " + i);
            }
        }

        splitOperator = _splitOperator;
        padChar = String.valueOf(_padChar);
        columnWidths = _columnWidths;
    }

    public TableColumnFormatter(StringSplitOperator _splitOperator, int... _columnWidths) {
        this(_splitOperator, ' ', _columnWidths);
    }

    public String formatLine(String... _columnContent) {
        if (_columnContent == null || _columnContent.length == 0) {
            _columnContent = new String[] {null};
        } else if (_columnContent.length > columnWidths.length) {
            throw new IllegalArgumentException("Expected max " + columnWidths.length + " columns but got " + _columnContent.length);
        }

        List<List<String>> allTokens = new ArrayList<>();
        int maxLines = 1;
        for (int colIdx = 0; colIdx < columnWidths.length; colIdx++) {
            int colWidth = columnWidths[colIdx];

            if (colIdx < _columnContent.length) {
                // split current column content into tokens of max. length column width
                List<String> tokens = splitOperator.applySplit(_columnContent[colIdx], colWidth);
                if (tokens == null) {
                    tokens = new ArrayList<>();
                }
                // right-pad tokens to length == column width
                for (int lineIdx = 0; lineIdx < tokens.size(); lineIdx++) {
                    tokens.set(lineIdx, StringUtil.rightPad(tokens.get(lineIdx), colWidth, padChar));
                }
                maxLines = Math.max(maxLines, tokens.size());
                allTokens.add(colIdx, tokens);
            } else {
                // create list with one token of length == column width
                allTokens.add(colIdx, Arrays.asList(StringUtil.repeat(padChar, colWidth)));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int lineIdx = 0; lineIdx < maxLines; lineIdx++) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            for (int colIdx = 0; colIdx < columnWidths.length; colIdx++) {
                List<String> tokens = allTokens.get(colIdx);
                if (lineIdx < tokens.size()) {
                    sb.append(tokens.get(lineIdx));
                } else {
                    // add an empty line into current column
                    sb.append(StringUtil.repeat(padChar, columnWidths[colIdx]));
                }
                if (colIdx < columnWidths.length - 1) {
                    sb.append(padChar); // separate two columns with one padding char
                }
            }
        }

        return sb.toString();
    }

    public String fillLine(char _char) {
        int totalWidth = 0;
        for (int colIdx = 0; colIdx < columnWidths.length; colIdx++) {
            totalWidth += columnWidths[colIdx];
        }
        totalWidth += columnWidths.length - 1;

        return StringUtil.repeat(String.valueOf(_char), totalWidth);
    }

}
