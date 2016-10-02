package org.jsapar.parse;

import java.util.EventObject;

public final class LineErrorEvent extends EventObject {
    final LineParseError parseError;

    public LineErrorEvent(Object source, LineParseError parseError) {
        super(source);
        this.parseError = parseError;
    }

    /**
     * @return the parseError
     */
    public LineParseError getParseError() {
        return parseError;
    }

}