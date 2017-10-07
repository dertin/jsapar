package org.jsapar.input.parse.fixed;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.jsapar.Document;
import org.jsapar.JSaParException;
import org.jsapar.Line;
import org.jsapar.StringCell;
import org.jsapar.input.LineErrorEvent;
import org.jsapar.input.LineParsedEvent;
import org.jsapar.input.ParseException;
import org.jsapar.input.ParsingEventListener;
import org.jsapar.schema.FixedWidthSchema;
import org.jsapar.schema.FixedWidthSchemaCell;
import org.jsapar.schema.FixedWidthSchemaLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FixedWidthParserTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testParse_Flat() throws JSaParException, IOException {
        String toParse = "JonasStenbergFridaStenberg";
        org.jsapar.schema.FixedWidthSchema schema = new org.jsapar.schema.FixedWidthSchema();
        FixedWidthSchemaLine schemaLine = new FixedWidthSchemaLine(2);
        schema.setLineSeparator("");

        schemaLine.addSchemaCell(new FixedWidthSchemaCell("First name", 5));
        schemaLine.addSchemaCell(new FixedWidthSchemaCell("Last name", 8));
        schema.addSchemaLine(schemaLine);

        Reader reader = new StringReader(toParse);
        DocumentBuilder builder = new DocumentBuilder();
        Document doc = builder.parse(reader, schema);

        assertEquals("Jonas", doc.getLine(0).getCell(0).getStringValue());
        assertEquals("Stenberg", doc.getLine(0).getCell("Last name").getStringValue());

        assertEquals("Frida", doc.getLine(1).getCell(0).getStringValue());
        assertEquals("Stenberg", doc.getLine(1).getCell("Last name").getStringValue());
    }

    @Test
    public final void testOutput_Flat() throws IOException, JSaParException {
        String sExpected = "JonasStenbergFridaBergsten";
        org.jsapar.schema.FixedWidthSchema schema = new org.jsapar.schema.FixedWidthSchema();
        FixedWidthSchemaLine schemaLine = new FixedWidthSchemaLine(2);
        schema.setLineSeparator("");

        schemaLine.addSchemaCell(new FixedWidthSchemaCell("First name", 5));
        schemaLine.addSchemaCell(new FixedWidthSchemaCell("Last name", 8));
        schema.addSchemaLine(schemaLine);

        Line line1 = new Line();
        line1.addCell(new StringCell("Jonas"));
        line1.addCell(new StringCell("Stenberg"));

        Line line2 = new Line();
        line2.addCell(new StringCell("Frida"));
        line2.addCell(new StringCell("Bergsten"));

        Document doc = new Document();
        doc.addLine(line1);
        doc.addLine(line2);

        java.io.Writer writer = new java.io.StringWriter();
        schema.output(doc.getLineIterator(), writer);

        assertEquals(sExpected, writer.toString());
    }

    private class DocumentBuilder {
        private Document             document = new Document();
        private ParsingEventListener listener;

        public DocumentBuilder() {
            listener = new ParsingEventListener() {

                @Override
                public void lineErrorEvent(LineErrorEvent event) throws ParseException {
                    throw new ParseException(event.getCellParseError());
                }

                @Override
                public void lineParsedEvent(LineParsedEvent event) {
                    document.addLine(event.getLine());
                }
            };
        }

        public Document parse(java.io.Reader reader, FixedWidthSchema schema) throws JSaParException, IOException {

            FixedWidthParser parser = new FixedWidthParser(reader, schema);
            parser.parse(listener);
            return this.document;
        }
    }

}