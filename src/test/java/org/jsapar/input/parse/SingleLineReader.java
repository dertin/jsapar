package org.jsapar.input.parse;

import java.io.IOException;

import org.jsapar.JSaParException;

public class SingleLineReader implements LineReader {
    private String line;

    public SingleLineReader(String line) {
        super();
        this.line = line;
    }

    @Override
    public String readLine() throws IOException, JSaParException {
        try {
            return line;
        } finally {
            line = null;
        }
    }

    @Override
    public String getLineSeparator() {
        return "|";
    }

}