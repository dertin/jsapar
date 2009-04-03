package org.jsapar.schema;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.jsapar.Cell;
import org.jsapar.JSaParException;
import org.jsapar.Line;
import org.jsapar.StringCell;
import org.jsapar.input.LineErrorEvent;
import org.jsapar.input.LineParsedEvent;
import org.jsapar.input.ParseException;
import org.jsapar.input.ParsingEventListener;

/**
 * Describes the schema how to parse or write a comma separated line.
 * 
 * @author stejon0
 * 
 */
public class CsvSchemaLine extends SchemaLine {

    private java.util.List<CsvSchemaCell> schemaCells = new java.util.LinkedList<CsvSchemaCell>();
    private boolean firstLineAsSchema = false;

    private String cellSeparator = ";";

    /**
     * Specifies quote characters used to encapsulate cells. Numerical value 0 indicates that quotes
     * are not used.
     */
    private char quoteChar = 0;

    /**
     * Creates an empty schema line.
     */
    public CsvSchemaLine() {
        super();
    }

    /**
     * Creates an empty schema line which occurs nOccurs number of times.
     * 
     * @param nOccurs
     */
    public CsvSchemaLine(int nOccurs) {
        super(nOccurs);
    }

    /**
     * The type of the line. You could say that this is the class of the line.
     * 
     * @param lineType
     */
    public CsvSchemaLine(String lineType) {
        super(lineType);
    }

    public CsvSchemaLine(String lineType, String lineTypeControlValue) {
        super(lineType, lineTypeControlValue);
    }

    /**
     * @return the cells
     */
    public java.util.List<CsvSchemaCell> getSchemaCells() {
        return schemaCells;
    }

    /**
     * Adds a schema cell to this row.
     * 
     * @param cell
     */
    public void addSchemaCell(CsvSchemaCell cell) {
        this.schemaCells.add(cell);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jsapar.schema.SchemaLine#parse(long, java.lang.String,
     * org.jsapar.input.ParsingEventListener)
     */
    @Override
    boolean parse(long nLineNumber, String sLine, ParsingEventListener listener) throws JSaParException {

        if (sLine.length() <= 0)
            return handleEmptyLine(nLineNumber, listener);

        Line line = new Line(getLineType(), (getSchemaCells().size() > 0) ? getSchemaCells().size() : 10);

        String[] asCells = this.split(sLine);
        java.util.Iterator<CsvSchemaCell> itSchemaCell = getSchemaCells().iterator();
        for (String sCell : asCells) {
            Cell cell = null;
            if (itSchemaCell.hasNext()) {
                CsvSchemaCell schemaCell = itSchemaCell.next();
                if (schemaCell.isIgnoreRead())
                    continue;
                try {
                    cell = schemaCell.makeCell(sCell);
                } catch (ParseException e) {
                    e.getCellParseError().setLineNumber(nLineNumber);
                    listener.lineErrorEvent(new LineErrorEvent(this, e.getCellParseError()));
                }
            } else {
                cell = new StringCell(sCell);
            }
            // If no parse error occurred, add the cell to the line.
            if (cell != null)
                line.addCell(cell);
        }
        if (line.getNumberOfCells() <= 0)
            return false;

        listener.lineParsedEvent(new LineParsedEvent(this, line, nLineNumber));
        return true;
    }

    /**
     * @param sLine
     * @return
     * @throws ParseException
     */
    String[] split(String sLine) throws ParseException {
        if (getQuoteChar() != 0)
            quoteChar = getQuoteChar();

        if (quoteChar == 0)
            return sLine.split(Pattern.quote(getCellSeparator()));

        java.util.List<String> cells = new java.util.ArrayList<String>(sLine.length() / 10);
        splitQuoted(cells, sLine, getCellSeparator(), quoteChar);
        return cells.toArray(new String[cells.size()]);
    }

    /**
     * Recursively find all quoted cells.
     * 
     * @param cells
     * @param sToSplit
     * @param sCellSeparator
     * @param quoteChar
     * @throws ParseException
     */
    private void splitQuoted(java.util.List<String> cells, String sToSplit, String sCellSeparator, char quoteChar)
            throws ParseException {
        int nIndex = 0;
        if (sToSplit.length() <= 0)
            return;

        int nFoundQuote = sToSplit.indexOf(quoteChar);
        if (nFoundQuote < 0) {
            cells.addAll(Arrays.asList(sToSplit.split(sCellSeparator)));
            return;
        } else if (nFoundQuote > 0) {
            String sUnquoted = sToSplit.substring(0, nFoundQuote);
            if (sUnquoted.lastIndexOf(sCellSeparator) != (sUnquoted.length() - sCellSeparator.length()))
                throw new ParseException(
                        "Miss-placed quote character. Start quote character has to be the first character of the cell");
            String[] asCells = sUnquoted.split(sCellSeparator);
            cells.addAll(Arrays.asList(asCells));
            nIndex = nFoundQuote + 1;
        } else
            // Quote is the first character.
            nIndex++;

        String sFound;
        int nFoundEnd = sToSplit.indexOf(quoteChar, nIndex);
        if (nFoundEnd < 0)
            throw new ParseException("Missing end quote character");
        sFound = sToSplit.substring(nIndex, nFoundEnd);
        nIndex = nFoundEnd + 1;
        int nFoundSplit = sToSplit.indexOf(sCellSeparator, nIndex);
        if (nFoundSplit > nIndex) {
            throw new ParseException(
                    "Miss-placed quote character. End quote character has to be the last character of the cell");
        }
        cells.add(sFound);
        if (nIndex < sToSplit.length())
            splitQuoted(cells, sToSplit.substring(nIndex, sToSplit.length()), sCellSeparator, quoteChar);
    }

    /**
     * @return the cellSeparator
     */
    public String getCellSeparator() {
        return cellSeparator;
    }

    /**
     * Sets the character sequence that separates each cell. This value overrides setting for the
     * schema. <br>
     * In output schemas the non-breaking space character '\u00A0' is not allowed since that
     * character is used to replace any occurrence of the separator within each cell.
     * 
     * @param cellSeparator
     *            the cellSeparator to set
     */
    public void setCellSeparator(String cellSeparator) {
        this.cellSeparator = cellSeparator;
    }

    /*
     * Writes the cells of the line to the writer. Inserts cell separator between cells.
     * (non-Javadoc)
     * 
     * @see org.jsapar.schema.SchemaLine#output(org.jsapar.Line, java.io.Writer)
     */
    @Override
    public void output(Line line, Writer writer) throws IOException {
        String sCellSeparator = getCellSeparator();

        Iterator<CsvSchemaCell> iter = getSchemaCells().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            CsvSchemaCell schemaCell = iter.next();
            Cell cell = findCell(line, schemaCell, i);
            char quoteChar = getQuoteChar();

            if (cell != null)
                schemaCell.output(cell, writer, sCellSeparator, quoteChar);
            if (iter.hasNext())
                writer.write(sCellSeparator);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public CsvSchemaLine clone() throws CloneNotSupportedException {
        CsvSchemaLine line = (CsvSchemaLine) super.clone();

        line.schemaCells = new java.util.LinkedList<CsvSchemaCell>();
        for (CsvSchemaCell cell : this.schemaCells) {
            line.addSchemaCell(cell.clone());
        }
        return line;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" cellSeparator='");
        sb.append(this.cellSeparator);
        sb.append("'");
        sb.append(" firstLineAsSchema=");
        sb.append(this.firstLineAsSchema);
        if (this.quoteChar != 0) {
            sb.append(" quoteChar=");
            sb.append(this.quoteChar);
        }
        sb.append(" schemaCells=");
        sb.append(this.schemaCells);
        return sb.toString();
    }

    /**
     * @return the firstLineAsSchema
     */
    public boolean isFirstLineAsSchema() {
        return firstLineAsSchema;
    }

    /**
     * @param firstLineAsSchema
     *            the firstLineAsSchema to set
     */
    public void setFirstLineAsSchema(boolean firstLineAsSchema) {
        this.firstLineAsSchema = firstLineAsSchema;
    }

    /**
     * @return the quotePattern
     */
    public char getQuoteChar() {
        return quoteChar;
    }

    /**
     * @return true if quote character is used, false otherwise.
     */
    public boolean isQuoteCharUsed(){
        return this.quoteChar == 0 ? false : true;
    }
    /**
     * @param quoteChar
     *            the quote character to set
     */
    public void setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
    }

    /**
     * @return
     * @throws JSaParException
     */
    Line buildHeaderLineFromSchema() throws JSaParException {
        Line line = new Line();

        for (CsvSchemaCell schemaCell : this.getSchemaCells()) {
            line.addCell(new StringCell(schemaCell.getName()));
        }

        return line;
    }

    /**
     * Writes header line if first line is schema.
     * 
     * @param writer
     * @throws IOException
     * @throws JSaParException
     */
    public void outputHeaderLine(Writer writer) throws IOException, JSaParException {
        output(this.buildHeaderLineFromSchema(), writer);
    }
}