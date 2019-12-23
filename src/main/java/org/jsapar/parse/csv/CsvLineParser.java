package org.jsapar.parse.csv;

import org.jsapar.error.JSaParException;
import org.jsapar.model.Cell;
import org.jsapar.model.Line;
import org.jsapar.model.StringCell;
import org.jsapar.parse.LineParseException;
import org.jsapar.parse.LineParsedEvent;
import org.jsapar.parse.cell.CellParser;
import org.jsapar.parse.line.LineDecoratorErrorConsumer;
import org.jsapar.parse.line.ValidationHandler;
import org.jsapar.schema.CsvSchemaCell;
import org.jsapar.schema.CsvSchemaLine;
import org.jsapar.text.TextParseConfig;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Responsible for parsing csv lines
 */
class CsvLineParser {

    private static final String                          EMPTY_STRING                    = "";
    private              CsvSchemaLine                   lineSchema;
    private              List<CellParser<CsvSchemaCell>> cellParsers;
    private              TextParseConfig                 config;
    private              long                            usedCount                       = 0L;
    private ValidationHandler          validationHandler          = new ValidationHandler();
    private LineDecoratorErrorConsumer lineDecoratorErrorConsumer = new LineDecoratorErrorConsumer();
    /**
     * Creates a csv line parser with the given line schema.
     *
     * @param lineSchema The line schema to use.
     */
    CsvLineParser(CsvSchemaLine lineSchema) {
        this(lineSchema, new TextParseConfig());
    }

    /**
     * Creates a csv line parser with the given line schema.
     *
     * @param lineSchema The line schema to use.
     * @param config     Configuration for parsing.
     */
    CsvLineParser(CsvSchemaLine lineSchema, TextParseConfig config) {
        this.lineSchema = lineSchema;
        this.config = config;
        cellParsers = makeCellParsers(lineSchema);
    }

    private List<CellParser<CsvSchemaCell>> makeCellParsers(CsvSchemaLine lineSchema) {
        return lineSchema.stream().map(this::makeCellParser).collect(Collectors.toList());
    }

    private CellParser<CsvSchemaCell> makeCellParser(CsvSchemaCell schemaCell) {
        return CellParser.ofSchemaCell(schemaCell, Math.min(config.getMaxCellCacheSize(), lineSchema.getOccurs() - 1));
    }

    /**
     * Parses one line from the given lineReader and sends a {@link LineParsedEvent} to the provided listener.
     *
     * @param lineReader    The line reader to read one line from.
     * @param listener      The event listener to receive events when parsing is complete.
     * @param errorListener The error event listener to which this method will send events for each error that occurs.
     * @return True if a line was parsed, false if no line could be parsed.
     * @throws IOException if an io-error occur
     */
    boolean parse(CsvLineReader lineReader, Consumer<Line> listener, Consumer<JSaParException> errorListener)
            throws IOException {

        if(lineReader.eofReached())
            return false;
        List<String> rawCells = lineReader.readLine(lineSchema.getCellSeparator(), lineSchema.getQuoteChar());

        if (rawCells.isEmpty())
            return handleEmptyLine(lineReader.currentLineNumber(), errorListener);

        if (usedCount == 0 && lineSchema.isFirstLineAsSchema()) {
            lineSchema = buildSchemaFromHeader(lineSchema, rawCells, errorListener);
            usedCount++;
            return true;
        }

        usedCount++;
        if(lineSchema.isIgnoreRead())
            return true;

        // Create with same size as schema plus 1 to handle trailing cell separator which is quite common.
        Line line = new Line(lineSchema.getLineType(), 1 + lineSchema.size());
        line.setLineNumber(lineReader.currentLineNumber());
        lineDecoratorErrorConsumer.initialize(errorListener, line);

        java.util.Iterator<CellParser<CsvSchemaCell>> itParser = cellParsers.iterator();
        for (String sCell : rawCells) {
            if (itParser.hasNext()) {
                addCellToLineBySchema(line, itParser.next(), sCell, lineDecoratorErrorConsumer);
            } else {
                if(!addCellToLineWithoutSchema(line, sCell, errorListener))
                    return true;
            }
        }
        if (line.size() <= 0)
            return false;

        // We have to fill all the default values and mandatory items for remaining cells within the schema.
        while (itParser.hasNext()) {
            if (!validationHandler.lineValidation(line.getLineNumber(), config.getOnLineInsufficient(), errorListener,
                    ()->"Insufficient number of cells could be read from the line of type " + lineSchema.getLineType())) {
                return true;
            }
            addCellToLineBySchema(line, itParser.next(), EMPTY_STRING, lineDecoratorErrorConsumer);
        }

        listener.accept( line );
        return true;
    }

    /**
     * Builds a CsvSchemaLine from a header line.
     *
     * @param masterLineSchema The base to use while creating csv schema. May add formatting, defaults etc.
     * @param asCells          An array of cells in the header line to use for building the schema.
     * @param errorListener    The error handler
     * @return A CsvSchemaLine created from the header line.
     *
     */
    private CsvSchemaLine buildSchemaFromHeader(CsvSchemaLine masterLineSchema, List<String> asCells, Consumer<JSaParException> errorListener) {

        CsvSchemaLine schemaLine = masterLineSchema.clone();
        schemaLine.clear();
        int ignoreCellCount=1;
        for (String sCell : asCells) {
            CsvSchemaCell schemaCell = masterLineSchema.getSchemaCell(sCell);
            if (schemaCell == null) {
                if(sCell.isEmpty()){
                    schemaCell = CsvSchemaCell.builder("@@"+ ignoreCellCount++ + "@@").withIgnoreRead(true).build();
                }
                else {
                    schemaCell = CsvSchemaCell.builder(sCell).build();
                }
            }
            schemaLine.addSchemaCell(schemaCell);
        }
        addMissingDefaultValuesFromMaster(schemaLine, masterLineSchema);
        checkMissingMandatoryValues(schemaLine, masterLineSchema, errorListener);
        this.cellParsers = this.makeCellParsers(schemaLine);
        return schemaLine;
    }

    private void checkMissingMandatoryValues(CsvSchemaLine schemaLine,
                                             CsvSchemaLine masterLineSchema,
                                             Consumer<JSaParException> errorListener) {
        masterLineSchema.stream().filter(schemaCell -> schemaCell.isMandatory()
                && schemaLine.getSchemaCell(schemaCell.getName()) == null).forEach(schemaCell -> errorListener
                .accept(new LineParseException(0, "Mandatory cell " + schemaCell.getName()
                        + " is missing in the header line that is used as schema for lines of type [" + schemaLine
                        .getLineType() + "].")));

    }

    /**
     * Add all missing schema cells that has a default value in the master schema last on the line with
     * ignoreRead=true so that the default values are always set.
     *
     * @param schemaLine       The schema line to add missing default values to.
     * @param masterLineSchema The master schema line to get default values from.
     */
    private void addMissingDefaultValuesFromMaster(CsvSchemaLine schemaLine, CsvSchemaLine masterLineSchema) {
        masterLineSchema.stream().filter(schemaCell -> schemaCell.isDefaultValue()
                && schemaLine.getSchemaCell(schemaCell.getName()) == null).forEach(schemaCell -> {
            CsvSchemaCell defaultCell = schemaCell.clone();
            defaultCell.setIgnoreRead(true);
            schemaLine.addSchemaCell(defaultCell);
        });
    }

    /**
     * Handles behavior of empty lines
     *
     * @param lineNumber The current line number
     * @param listener   The error event listener
     * @return Returns true (always).
     *
     */
    @SuppressWarnings("UnusedParameters")
    private boolean handleEmptyLine(long lineNumber, Consumer<JSaParException> listener) {
        return true;
    }

    /**
     * Adds a cell to the line according to the schema.
     * @param line               The line to add a cell to
     * @param cellParser         The cell parser
     * @param sCell              The string value of the cell
     * @param errorEventListener The error event listener to report errors to.
     *
     */
    @SuppressWarnings("rawtypes")
    private void addCellToLineBySchema(Line line,
                                       CellParser<CsvSchemaCell> cellParser,
                                       String sCell,
                                       Consumer<JSaParException> errorEventListener) {

        CsvSchemaCell cellSchema = cellParser.getSchemaCell();
        if (cellSchema.isIgnoreRead()) {
            if (cellSchema.isDefaultValue())
                line.addCell(cellParser.makeDefaultCell());
            return;
        }
        if (cellSchema.isMaxLength() && sCell.length() > cellSchema.getMaxLength())
            sCell = sCell.substring(0, cellSchema.getMaxLength());
        Cell cell = cellParser.parse(sCell, errorEventListener);
        if(cell != null){
            line.addCell(cell);
        }
    }

    /**
     * Adds overflowing cell to the line if there is no schema.
     *  @param line          The line to add cell to
     * @param sCell         The string value of the cell.
     * @param errorListener Error listener to send error event to if so is configured.
     *
     */
    @SuppressWarnings("rawtypes")
    private boolean addCellToLineWithoutSchema(Line line, String sCell, Consumer<JSaParException> errorListener)
            {

        if (!validationHandler.lineValidation(line.getLineNumber(), config.getOnLineOverflow(), errorListener,
                ()->"Found additional cell on the line that is not described in the line schema.")) {
            return false;
        }
        Cell cell;
        cell = new StringCell("@@cell-" + (1 + line.size()), sCell);
        line.addCell(cell);
        return true;
    }

}
