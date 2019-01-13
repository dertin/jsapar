package org.jsapar.parse.fixed;

import org.jsapar.parse.LineParseException;
import org.jsapar.schema.FixedWidthSchemaCell;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * Internal class that acts as a read buffer while parsing fixed width from a reader.
 */
@SuppressWarnings("Duplicates")
class ReadBuffer {
    private static final String EMPTY_STRING = "";
    private final Reader reader;
    private final LineLoader lineLoader;
    private int maxLoadSize;
    private int lineMark=0;
    private int lineEnd;
    private int nextLineBegin=0;

    private final char[] buffer;
    private int cursor=0;
    private int bufferSize=0;
    private long lineNumber=0;
    private boolean eof=false;
    /**
     * @param reader The reader to read from
     * @param bufferSize The buffer size to use.
     * @param maxLoadSize The maximum number of characters to load a a time to the buffer.
     */
    ReadBuffer(String lineSeparator, Reader reader, int bufferSize, int maxLoadSize) {
        this.reader = reader;
        this.buffer = new char[bufferSize];
        this.maxLoadSize = Math.min(maxLoadSize, bufferSize);
        this.lineLoader = makeLineLoader(lineSeparator);
        this.lineEnd = bufferSize;
    }

    private LineLoader makeLineLoader(String lineSeparator) {
        if(lineSeparator.isEmpty())
            return new LineLoaderFlat();
        if(Arrays.asList("\n", "\r\n").contains(lineSeparator)){
            return new LineLoaderCRLF();
        }
        return new LineLoaderCustom(lineSeparator);
    }

    /**
     * Loads new characters to the buffer.
     * @return The number of new characters added to the buffer. 0 if there was no room in the buffer to load. -1 if end of file was reached.
     * @throws IOException In case of underlying io error.
     * @param requested The size requested to load. Overrides the maxLoadSize.
     */
    private int load(int requested) throws IOException {
        final int remaining = bufferSize - lineMark;
        int toLoad=buffer.length - remaining;
        int maxLoad = Math.max(maxLoadSize, requested);
        if(toLoad < maxLoad) {
            if (toLoad==0){
                // Max line size reached. No more space to load.
                throw new LineParseException(lineNumber, "Line length exceeds maximum line length of " + buffer.length + " characters");
            }
            // Shift remaining to the left
            System.arraycopy(buffer, lineMark, buffer, 0, remaining);
            cursor -= lineMark;
            bufferSize -= lineMark;
            lineEnd -= lineMark;
            nextLineBegin -= lineMark;
            lineMark = 0;
        }
        else {
            if(lineMark==bufferSize){
                cursor = 0;
                bufferSize = 0;
                lineMark = 0;
            }
            toLoad = maxLoad;
        }
        final int count = reader.read(buffer, bufferSize, toLoad);
        if(count >= 0) {
            bufferSize += count;
        }
        if(count < toLoad){
            lineEnd = Math.min(bufferSize, lineEnd); // EOF
        }
        return count;
    }

    /**
     * Place a line mark.
     */
    void markLine(){
        lineMark = cursor;
    }

    /**
     * Reset cursor to last line mark.
     */
    void resetLine(){
        eof=(cursor==lineMark);
        cursor = lineMark;
    }

    /**
     * @param toSkip The number of characters to skip
     * @return The number of characters skipped within line.
     */
    int skipWithinLine(int toSkip){
        final int availableWithinLine = lineEnd - cursor;
        toSkip = Math.min(toSkip, availableWithinLine);
        cursor+=toSkip;
        return toSkip;
    }
    /**
     * @return The string value of the cell read from the reader at the position pointed to by the offset. Null if end
     * of input stream was reached.
     * @throws IOException If there is a problem while reading the input reader.
     */
    String readToString(FixedWidthSchemaCell schemaCell, int offset) throws IOException {
        int length = schemaCell.getLength(); // The actual length
        if(length == 0)
            return EMPTY_STRING;

        cursor += offset;
        int required = cursor + length - bufferSize;
        if(required > 0){
            int loaded = load(required);
            if(loaded < 0) {
                if(cursor >= bufferSize) {
                    this.eof = true;
                    return null; // EOF
                }
                length = bufferSize-cursor; // What remains in buffer.
            }
        }
        final int availableWithinLine = lineEnd - cursor;
        length = Math.min(length, availableWithinLine);
        if(length<0)
            return null; //EOL
        if(length == 0)
            return EMPTY_STRING;
        final int fieldEnd = cursor + length;
        int cellEnd = fieldEnd;
        char padCharacter = schemaCell.getPadCharacter();
        if(schemaCell.getAlignment() != FixedWidthSchemaCell.Alignment.LEFT) {
            while (cursor < cellEnd && buffer[cursor] == padCharacter) {
                cursor++;
            }
        }
        if(schemaCell.getAlignment() != FixedWidthSchemaCell.Alignment.RIGHT) {
            while (cellEnd > cursor && buffer[cellEnd - 1] == padCharacter) {
                cellEnd--;
            }
        }
        final int cellBegin = cursor;
        cursor = fieldEnd;
        if(cellEnd<=cellBegin){
            if(padCharacter == '0' && schemaCell.getCellFormat().getCellType().isNumber())
                return String.valueOf(padCharacter);
            return EMPTY_STRING;
        }
        return new String(buffer, cellBegin, cellEnd-cellBegin);
    }

    /**
     * @param allocate The number of characters to allocate for the next line. Used for performance optimization.
     * @return The length of the next line or -1 if end of file was reached.
     * @throws IOException In case of underlying io error.
     */
    int nextLine(int allocate) throws IOException {
        lineNumber++;
        return lineLoader.nextLine(allocate);
    }

    int remainsForLine() {
        return lineLoader.remainsForLine();
    }

    boolean eofReached() {
        return eof;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    interface LineLoader{
        int nextLine(int allocate) throws IOException;

        int remainsForLine();
    }

    /**
     *
     */
    class LineLoaderFlat implements LineLoader{
        @Override
        public int nextLine(int allocate) throws IOException {
            int spaceRequired = cursor+ allocate -bufferSize;
            lineMark = cursor;
            lineEnd = Integer.MAX_VALUE;
            if(spaceRequired>0) {
                final int loaded = load(spaceRequired);
                if(loaded < 0 && bufferSize==cursor)
                    return loaded;
                return bufferSize-lineMark;
            }
            return allocate;
        }

        @Override
        public int remainsForLine() {
            return 0;
        }
    }

    private class LineLoaderCRLF implements LineLoader {
        @Override
        public int nextLine(int allocate) throws IOException {
            cursor = nextLineBegin;
            lineMark = nextLineBegin;
            while(true){
                if(cursor>=bufferSize){
                    int loaded = load(1);
                    if(loaded<0) {
                        lineEnd = cursor;
                        nextLineBegin = lineEnd;
                        final int lineSize = lineMark==cursor ? loaded : cursor-lineMark;
                        cursor = lineMark;
                        return lineSize;
                    }
                }
                final char c = buffer[cursor];
                if(c=='\n'){
                    if(cursor>nextLineBegin && buffer[cursor-1]=='\r'){
                        lineEnd = cursor-1;
                    }
                    else{
                        lineEnd = cursor;
                    }
                    int lineSize = lineEnd - lineMark;
                    nextLineBegin=cursor+1;
                    cursor = lineMark;
                    return lineSize;
                }
                cursor++;
            }
        }

        @Override
        public int remainsForLine() {
            return lineEnd-cursor;
        }
    }

    private class LineLoaderCustom implements LineLoader {
        private String lineSeparator;
        private final char lastCharOfSeparator;

        LineLoaderCustom(String lineSeparator) {
            this.lineSeparator = lineSeparator;
            this.lastCharOfSeparator = lineSeparator.charAt(lineSeparator.length()-1);
        }

        @Override
        public int nextLine(int allocate) throws IOException {
            cursor = nextLineBegin;
            lineMark = nextLineBegin;
            while(true){
                if(cursor>=bufferSize){
                    int loaded = load(1);
                    if(loaded<0) {
                        lineEnd = cursor;
                        nextLineBegin = lineEnd;
                        final int lineSize = lineMark==cursor ? loaded : cursor-lineMark;
                        cursor = lineMark;
                        return lineSize;
                    }
                }
                final char c = buffer[cursor++];
                if (c == lastCharOfSeparator) {
                    if(tailOfCellMatches(cursor, lineSeparator)){
                        lineEnd = cursor-lineSeparator.length();
                        nextLineBegin = cursor;
                        int lineSize = lineEnd - lineMark;
                        cursor=lineMark;
                        return lineSize;
                    }

                }
            }
        }

        @Override
        public int remainsForLine() {
            return lineEnd-cursor;
        }
    }

    /**
     * Checks tail of current cell matches supplied string. Assumes that the current character is already checked.
     * @param toMatch The string to match
     * @return True if tail of current cell matches supplied string if the supplied character were to be added.
     */
    private boolean tailOfCellMatches(int index, String toMatch){
        int cellOffset = index -toMatch.length();
        if(cellOffset < lineMark) {
            return false;
        }
        // Scan backwards to see if characters before matches. Start at character before current.
        for(int i = toMatch.length()-2; i>=0; i--){
            if(toMatch.charAt(i) !=  buffer[cellOffset + i])
                return false;
        }
        return true;
    }

}
