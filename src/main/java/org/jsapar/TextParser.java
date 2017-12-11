package org.jsapar;

import org.jsapar.parse.AbstractParser;
import org.jsapar.parse.LineEventListener;
import org.jsapar.parse.text.TextParseConfig;
import org.jsapar.parse.text.TextParseTask;
import org.jsapar.schema.Schema;

import java.io.IOException;
import java.io.Reader;

/**
 * This class is the starting point for parsing a text (like a text file). <br>
 * The instance of this class will produce events for each line that has been successfully parsed. <br/>
 * If you want to get the result back as a complete Document object, you should use
 * the {@link org.jsapar.parse.DocumentBuilderLineEventListener} as line event listener.
 * <br/>
 * <ol>
 * <li>First, create an instance of TextParser with the {@link Schema} that you want to use while parsing.</li>
 * <li>Call the {@link #parse(Reader, LineEventListener)} method. </li>
 * <li>The supplied {@link LineEventListener} will receive a callback event for each line that is parsed.</li>
 * </ol>
 * <br/>
 *
 * The default error handling is to throw an exception upon the first error that occurs. You can however change that
 * behavior by adding an {@link org.jsapar.error.ErrorEventListener}. There are several implementations to choose from such as
 * {@link org.jsapar.error.RecordingErrorEventListener} or
 * {@link org.jsapar.error.ThresholdRecordingErrorEventListener}, or you may implement your own.
 *
 * @see TextComposer
 * @see Text2TextConverter
 * @see TextParseTask
 *
 */
public class TextParser extends AbstractParser{

    private final Schema parseSchema;
    private TextParseConfig parseConfig = new TextParseConfig();

    public TextParser(Schema parseSchema) {
        this(parseSchema, new TextParseConfig());
    }

    public TextParser(Schema parseSchema, TextParseConfig parseConfig) {
        this.parseSchema = parseSchema;
        this.parseConfig = parseConfig;
    }

    public void parse(Reader reader, LineEventListener lineEventListener) throws IOException {
        TextParseTask parseTask = new TextParseTask(this.parseSchema, reader, parseConfig);
        execute(parseTask, lineEventListener);
    }

    public TextParseConfig getParseConfig() {
        return parseConfig;
    }

    public void setParseConfig(TextParseConfig parseConfig) {
        this.parseConfig = parseConfig;
    }
}