package org.jsapar.parse.fixed;

import org.jsapar.error.ExceptionErrorEventListener;
import org.jsapar.error.JSaParException;
import org.jsapar.model.*;
import org.jsapar.schema.FixedWidthSchemaCell;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;

import static org.junit.Assert.*;

@SuppressWarnings("ThrowablePrintedToSystemOut")
public class FixedWidthCellParserTest {

    private int maxCacheSize=0;

    private ReadBuffer makeReadBuffer(String toParse){
        return new ReadBuffer("", new StringReader(toParse), 40, 40);
    }
    
    @Test
    public final void testBuild() throws IOException, JSaParException {
        String toParse = "   Jonas   ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("First name", 11);
        schemaCell.setAlignment(FixedWidthSchemaCell.Alignment.CENTER);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        Cell cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());

        assertEquals("Jonas", cell.getStringValue());
    }

    @Test
    public final void testBuild_dont_trim() throws IOException, JSaParException {
        String toParse = "   Jonas   ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("First name", 11);
        schemaCell.setAlignment(FixedWidthSchemaCell.Alignment.CENTER);
        schemaCell.setPadCharacter('!');

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        Cell cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());

        assertEquals("   Jonas   ", cell.getStringValue());
    }

    @Test
    public final void testBuildEmptyMandatory() throws IOException {
        String toParse = "           ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("First name", 11);
        schemaCell.setMandatory(true);

        try {
            FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
            cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
            fail("Should throw exception");

        } catch (JSaParException ex) {
            System.out.println(ex);
        }
    }

    @Test
    public final void testBuildEmptyOptional() throws IOException {
        String toParse = "           ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("First name", 11);

        Cell cell;
        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        assertTrue(cell.isEmpty());
        assertEquals("", cell.getValue());
    }

    @Test
    public final void testBuildEmptyOptionalInteger() throws IOException {
        String toParse = "           ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("ShoeSize", 11);
        schemaCell.setCellFormat(CellType.INTEGER);

        Cell cell;
        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        assertTrue(cell.isEmpty());
        assertEquals("", cell.getValue());
    }

    /**
     * A cell should not be considerede empty if blanks are not removed by trimming.
     */
    @Test
    public final void testBuildEmptyMandatoryNoTrim() throws Exception {
        String toParse = "           ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("First name", 11);
        schemaCell.setMandatory(true);
        schemaCell.setPadCharacter('!');

        Cell cell;
        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        assertEquals("           ", cell.getValue());
    }

    @Test
    public final void testBuild_empty() throws IOException {
        String toParse = "           ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("First name", 11);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        Cell cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());

        assertEquals("", cell.getValue());
        assertEquals("", cell.getStringValue());
    }

    @Test
    public final void testBuild_date() throws IOException {
        String toParse = "2007-04-10 16:15";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Date", 16);
        schemaCell.setCellFormat(CellType.DATE, "yyyy-MM-dd HH:mm");

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        DateCell cell = (DateCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        java.util.Date date = cell.getValue();
        Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(2007, calendar.get(Calendar.YEAR));
        assertEquals(3, calendar.get(Calendar.MONTH));
        assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(16, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, calendar.get(Calendar.MINUTE));
    }

    @Test
    public final void testParse_integer_pad_zero() throws IOException {
        String toParse = "000000123";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Integer", 11);
        schemaCell.setAlignment(FixedWidthSchemaCell.Alignment.RIGHT);
        schemaCell.setPadCharacter('0');
        schemaCell.setCellFormat(CellType.INTEGER);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        IntegerCell cell = (IntegerCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        int value = cell.getValue().intValue();

        assertEquals(123, value);
    }

    @Test
    public final void testParse_integer_pad_zero_zero() throws IOException {
        String toParse = "000000000";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Integer", 11);
        schemaCell.setAlignment(FixedWidthSchemaCell.Alignment.RIGHT);
        schemaCell.setPadCharacter('0');
        schemaCell.setCellFormat(CellType.INTEGER);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        IntegerCell cell = (IntegerCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        int value = cell.getValue().intValue();

        assertEquals(0, value);
    }


    @Test
    public final void testBuild_decimal_sv() throws IOException {
        String toParse = "-123 456,78  ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Decimal", 11);
        schemaCell.setCellFormat(CellType.DECIMAL, "#,###.#", new Locale("sv", "SE"));

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        BigDecimalCell cell = (BigDecimalCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        BigDecimal value = cell.getBigDecimalValue();

        assertEquals(new BigDecimal("-123456.78"), value);
    }


    @Test
    public final void testBuild_decimal_uk() throws IOException, JSaParException {
        String toParse = "-123,456.78  ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Decimal", 11);
        schemaCell.setCellFormat(CellType.DECIMAL, "#,###.#", Locale.UK);
        // schemaCell.setCellFormat(new SchemaCellFormat(CellType.DECIMAL));

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        BigDecimalCell cell = (BigDecimalCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        BigDecimal value = cell.getBigDecimalValue();

        assertEquals(new BigDecimal("-123456.78"), value);
    }

    @Test
    public final void testBuild_int() throws IOException, JSaParException {
        String toParse = "123456";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Integer", 6);
        schemaCell.setCellFormat(CellType.INTEGER);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        IntegerCell cell = (IntegerCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        int value = cell.getValue().intValue();

        assertEquals(123456, value);
    }

    @Test
    public final void testBuild_float() throws IOException, JSaParException {
        String toParse = "1123,234";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Float", 6);
        schemaCell.setCellFormat(CellType.FLOAT);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        FloatCell cell = (FloatCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        double value = cell.getValue().doubleValue();

        assertEquals(1123, 234, value);
    }

    @Test
    public final void testBuild_floatExp() throws IOException, JSaParException {
        String toParse = "1.234E6 ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Float", 8);
        schemaCell.setCellFormat(CellType.FLOAT, "#.###E0", schemaCell.getLocale());

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        FloatCell cell = (FloatCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        double value = cell.getValue().doubleValue();

        assertEquals(1.234e6, value, 0.001);
    }

    @Test
    public final void testBuild_floatExp_SE() throws IOException, JSaParException {
        String toParse = "1,234E6 ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("Float", 8);
        schemaCell.setLocale(new Locale("sv", "SE"));
        schemaCell.setCellFormat(CellType.FLOAT, "#.###E0", schemaCell.getLocale());

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        FloatCell cell = (FloatCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        double value = cell.getValue().doubleValue();

        assertEquals(1.234e6, value, 0.001);
    }

    @Test
    public final void testBuild_boolean() throws IOException, JSaParException {
        String toParse = "true ";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("True", 5);
        schemaCell.setCellFormat(CellType.BOOLEAN);

        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        BooleanCell cell = (BooleanCell) cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        boolean value = cell.getValue();

        assertEquals(true, value);
    }

    @Test
    public final void testBuildZeroLength() throws IOException {
        String toParse = "Next";
        FixedWidthSchemaCell schemaCell = new FixedWidthSchemaCell("DontRead", 0);

        Cell cell;
        FixedWidthCellParser cellParser = new FixedWidthCellParser(schemaCell, maxCacheSize);
        cell = cellParser.parse(makeReadBuffer(toParse), new ExceptionErrorEventListener());
        Assert.assertNotNull(cell);
        Assert.assertTrue(cell.isEmpty());
        Assert.assertEquals("", cell.getStringValue());
    }
}