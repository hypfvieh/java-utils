package com.github.hypfvieh.formatter;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;
import com.github.hypfvieh.util.StringUtil;

public class TableColumnFormatterTest extends AbstractBaseUtilTest {

    @Test
    public void testConstructorMissingSplitOperator() {
        assertThrows(NullPointerException.class, () -> new TableColumnFormatter(null, 2, 3, 4));
    }

    @Test
    public void testConstructorInvalidColumnWidths() {
        assertThrows(IllegalArgumentException.class, () -> new TableColumnFormatter(StringUtil::splitEqually, 2, -3, 4));
    }

    @Test
    public void testFormatLine() {
        TableColumnFormatter tableColumnFormatter = new TableColumnFormatter(StringUtil::splitEqually, '_', 1, 2, 3);

        assertEquals("1_2__3__", tableColumnFormatter.formatLine("1", "2", "3"));
        assertEquals("1_______", tableColumnFormatter.formatLine("1"));
        assertEquals("________", tableColumnFormatter.formatLine((String[]) null));
        assertEquals("A_______\nB_______\nC_______", tableColumnFormatter.formatLine("ABC"));
    }

}
