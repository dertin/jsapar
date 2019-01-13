package org.jsapar.parse.fixed;

import org.jsapar.parse.text.TextParseConfig;
import org.jsapar.schema.FixedWidthSchemaCell;
import org.jsapar.schema.FixedWidthSchemaLine;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests if next line to parse can be used for the schemaLine of this instance.
 */
class FWLineParserMatcher {
    private final FixedWidthSchemaLine schemaLine;
    private List<FWControlCell> controlCells =new ArrayList<>();
    private FixedWidthLineParser lineParser;
    private int occursLeft;
    private int maxControlEndPos;

    FWLineParserMatcher(FixedWidthSchemaLine schemaLine, TextParseConfig config) {
        this.schemaLine = schemaLine;
        this.lineParser = new FixedWidthLineParser(schemaLine, config);
        occursLeft = schemaLine.getOccurs();
        int beginPos=0;
        for (FixedWidthSchemaCell schemaCell : schemaLine.getSchemaCells()) {
            if(schemaCell.hasLineCondition()){
                controlCells.add(new FWControlCell(beginPos, schemaCell));
            }
            beginPos += schemaCell.getLength();
            maxControlEndPos = beginPos;
        }
    }
    
    LineParserMatcherResult testLineParserIfMatching(ReadBuffer lineReader) throws IOException {
        if(occursLeft <= 0)
            return LineParserMatcherResult.NO_OCCURS;
        if(!controlCells.isEmpty()) {
            // We only peek into the line to follow.
            lineReader.markLine();
            try {
                int read = 0;
                for (FWControlCell controlCell : controlCells) {
                    int offset = controlCell.beginPos - read;
                    String value = lineReader.readToString(controlCell.schemaCell, offset);
                    if (value == null)
                        return LineParserMatcherResult.EOF; // EOF reached
                    if (!controlCell.schemaCell.getLineCondition().satisfies(value))
                        return LineParserMatcherResult.NOT_MATCHING; // Not matching criteria.
                    read = controlCell.beginPos + controlCell.schemaCell.getLength();
                }
            } finally {
                lineReader.resetLine();
            }
        }
        if (!schemaLine.isOccursInfinitely())
            occursLeft--;
        return LineParserMatcherResult.SUCCESS;
    }

    FixedWidthLineParser getLineParser() {
        return lineParser;
    }

    private static class FWControlCell{
        final int beginPos;
        final FixedWidthSchemaCell schemaCell;

        FWControlCell(int beginPos, FixedWidthSchemaCell schemaCell) {
            this.beginPos = beginPos;
            this.schemaCell = schemaCell;
        }
    }

    boolean isOccursLeft() {
        return schemaLine.isOccursInfinitely() || occursLeft > 0;
    }

}
