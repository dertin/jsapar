package org.jsapar.schema;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import org.jsapar.Cell;
import org.jsapar.JSaParException;
import org.jsapar.Line;
import org.jsapar.input.LineErrorEvent;
import org.jsapar.input.LineParsedEvent;
import org.jsapar.input.ParseException;
import org.jsapar.input.ParsingEventListener;

/**
 * This class represents the schema for a line of a fixed with file. Each cell within the line has a
 * specified size. There are no delimiter characters.
 * 
 * @author stejon0
 * 
 */
public class FixedWidthSchemaLine extends SchemaLine {

    private java.util.List<FixedWidthSchemaCell> schemaCells = new java.util.LinkedList<FixedWidthSchemaCell>();
    private boolean trimFillCharacters = false;
    private char fillCharacter = ' ';

    /**
     * Creates an empty schema line.
     */
    public FixedWidthSchemaLine() {
	super();
    }

    /**
     * Creates an empty schema line which will occur nOccurs times within the file. When the line
     * has occured nOccurs times this schema-line will not be used any more.
     * 
     * @param nOccurs
     *            The number of times this schema line is used while parsing or writing.
     */
    public FixedWidthSchemaLine(int nOccurs) {
	super(nOccurs);
    }

    /**
     * Creates an empty schema line which parses lines of type lineType.
     * 
     * @param lineType
     *            The line type for which this schema line is used. The line type is stored as the
     *            lineType of the generated Line.
     */
    public FixedWidthSchemaLine(String lineType) {
	super(lineType);
    }

    /**
     * Creates a schema line with the supplied line type and control value.
     * 
     * @param lineType
     *            The name of the type of the line.
     * @param lineTypeControlValue
     *            The tag that determines which type of line it is.
     */
    public FixedWidthSchemaLine(String lineType, String lineTypeControlValue) {
	super(lineType, lineTypeControlValue);
    }

    /**
     * @return the cells
     */
    public java.util.List<FixedWidthSchemaCell> getSchemaCells() {
	return schemaCells;
    }

    /**
     * Adds a schema cell to this row.
     * 
     * @param schemaCell
     */
    public void addSchemaCell(FixedWidthSchemaCell schemaCell) {
	this.schemaCells.add(schemaCell);
    }

    /* (non-Javadoc)
     * @see org.jsapar.schema.SchemaLine#parse(long, java.lang.String, org.jsapar.input.ParsingEventListener)
     */
    @Override
    boolean parse(long nLineNumber, String sLine, ParsingEventListener listener) throws IOException, JSaParException {
	if (sLine.length() <= 0)
	    return handleEmptyLine(nLineNumber, listener);
	java.io.Reader reader = new java.io.StringReader(sLine);
	return parse(nLineNumber, reader, listener);
    }

    /**
     * Reads characters from the reader and parses them into a complete line. The line is sent as an
     * event to the supplied event listener.
     * 
     * @param nLineNumber
     *            The current line number while parsing.
     * @param reader
     *            The reader to read characters from.
     * @param listener
     *            The listener to generate call-backs to.
     * @return true if a line was found. false if end-of-file was found while reading the input.
     * @throws IOException
     * @throws JSaParException
     */
    boolean parse(long nLineNumber, Reader reader, ParsingEventListener listener) throws IOException, JSaParException {

	Line line = new Line(getLineType(), getSchemaCells().size());
	for (FixedWidthSchemaCell schemaCell : getSchemaCells()) {
	    if (schemaCell.isIgnoreRead()) {
		long nSkipped = reader.skip(schemaCell.getLength());
		if (nSkipped != schemaCell.getLength())
		    break;
	    } else {
		try {
		    Cell cell = schemaCell.build(reader, isTrimFillCharacters(), getFillCharacter());
		    if (cell == null)
			break;
		    line.addCell(cell);
		} catch (ParseException pe) {
		    pe.getCellParseError().setLineNumber(nLineNumber);
		    listener.lineErrorErrorEvent(new LineErrorEvent(this, pe.getCellParseError()));
		}
	    }
	}
	if (line.getNumberOfCells() <= 0)
	    return false;
	else
	    listener.lineParsedEvent(new LineParsedEvent(this, line, nLineNumber));

	return true;
    }

    /**
     * Writes a line to the writer. Each cell is identified from the schema by the name of the cell.
     * If the schema-cell has no name, the cell at the same position in the line is used under the
     * condition that it also lacks name.
     * 
     * If the schema-cell has a name the cell with the same name is used. If no such cell is found
     * and the cell att the same position lacks name, it is used instead.
     * 
     * If no corresponding cell is found for a schema-cell, the positions are filled with the schema
     * fill character.
     * 
     * @param line
     *            The line to write to the writer
     * @param writer
     *            The writer to write to.
     * @throws IOException
     * @throws JSaParException
     */
    @Override
    public void output(Line line, Writer writer) throws IOException, JSaParException {
	Iterator<FixedWidthSchemaCell> iter = getSchemaCells().iterator();

	// Iterate all schema cells.
	for (int i = 0; iter.hasNext(); i++) {
	    FixedWidthSchemaCell schemaCell = iter.next();
	    Cell cell = findCell(line, schemaCell, i);
	    if (cell == null)
		schemaCell.outputEmptyCell(writer, getFillCharacter());
	    else
		schemaCell.output(cell, writer, getFillCharacter());
	}
    }

    /**
     * @param line
     * @param writer
     * @param schema
     * @throws IOException
     * @throws JSaParException
     */
    void outputByIndex(Line line, Writer writer, FixedWidthSchema schema) throws IOException, JSaParException {
	Iterator<FixedWidthSchemaCell> iter = getSchemaCells().iterator();
	for (int i = 0; iter.hasNext(); i++) {
	    iter.next().output(line.getCell(i), writer, getFillCharacter());
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public FixedWidthSchemaLine clone() throws CloneNotSupportedException {
	FixedWidthSchemaLine line = (FixedWidthSchemaLine) super.clone();

	line.schemaCells = new java.util.LinkedList<FixedWidthSchemaCell>();
	for (FixedWidthSchemaCell cell : this.schemaCells) {
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
	sb.append(" trimFillCharacters=");
	sb.append(this.trimFillCharacters);
	if (this.trimFillCharacters) {
	    sb.append(" fillCharacter='");
	    sb.append(this.fillCharacter);
	    sb.append("'");
	}
	sb.append(" schemaCells=");
	sb.append(this.schemaCells);
	return sb.toString();
    }

    /**
     * @return the trimFillCharacters
     */
    public boolean isTrimFillCharacters() {
	return trimFillCharacters;
    }

    /**
     * @param trimFillCharacters
     *            the trimFillCharacters to set
     */
    public void setTrimFillCharacters(boolean trimFillCharacters) {
	this.trimFillCharacters = trimFillCharacters;
    }

    /**
     * @return the fillCharacter
     */
    public char getFillCharacter() {
	return fillCharacter;
    }

    /**
     * @param fillCharacter
     *            the fillCharacter to set
     */
    public void setFillCharacter(char fillCharacter) {
	this.fillCharacter = fillCharacter;
    }
}
