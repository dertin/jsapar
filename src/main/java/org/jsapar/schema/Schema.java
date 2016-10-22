package org.jsapar.schema;

import org.jsapar.model.Document;
import org.jsapar.parse.ParseSchema;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

/**
 * Abstract base class for all type of jsapar schemata. A schema describes how the buffer should be
 * parsed or how the lines of a {@link Document} should be written. Usually the parse and output methods are
 * called from one of the in, out or io classes.
 * 
 * @see Xml2SchemaBuilder
 * @see Schema2XmlExtractor
 * 
 * @author Jonas
 * 
 */
public abstract class Schema implements Cloneable, ParseSchema {


    public abstract boolean isEmpty();
    private Locale locale = Locale.getDefault();
    private String lineSeparator = System.getProperty("line.separator");


    /**
     * @return the lineSeparator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the line separator string. Default value is the system default (Retrieved by
     * System.getProperty("line.separator")).
     * 
     * @param lineSeparator
     *            the lineSeparator to set.
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }


    public Schema clone(){
        try {
            return (Schema) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * @return list of all schema lines.
     */
    protected abstract List<? extends SchemaLine> getSchemaLines();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return " lineSeparator=" +
                this.lineSeparator +
                " locale=" +
                this.locale;
    }

    /**
     * @param lineNumber
     * @return The line-schema for supplied line number.
     */
    public SchemaLine getSchemaLine(long lineNumber) {
        long nLineMax = 0;
        for (Object oSchemaLine : this.getSchemaLines()) {
            SchemaLine schemaLine = (SchemaLine) oSchemaLine;
            if (schemaLine.isOccursInfinitely())
                return schemaLine;
            nLineMax += (long) schemaLine.getOccurs();
            if (lineNumber <= nLineMax)
                return schemaLine;
        }
        return null;
    }

    /**
     * @param lineType
     * @return The schema line with the supplied line type name. Null if no such schema line was
     *         found.
     */
    public abstract SchemaLine getSchemaLine(String lineType);


    /**
     * @return Number of schema lines of this schema.
     */
    public abstract int size();

    /**
     * @param index
     * @return The schema line with the specified index.
     */
    public abstract SchemaLine getSchemaLineAt(int index);

    /**
     * @param line
     *            The line to find index for.
     * @return The index (starting from 0) of which the supplied line have within the schema. -1 if
     *         no such line is found.
     */
    public int getIndexOf(SchemaLine line) {
        for (int i = 0; i < size(); i++) {
            if (line.equals(getSchemaLineAt(i))) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Loads a schema instance from an xml that is read from the supplied reader. The xml needs to comply to the
     * JSaParSchema.xsd otherwise a SchemaException is thrown.
     *
     * @param reader
     *            The reader to read the xml from. Caller is responsible for closing the reader.
     * @return A new schema instance created based on the xml.
     * @throws SchemaException
     *             if the supplied xml does not comply to the JSaParSchema.xsd or if there is any other error while
     *             loading a schema.
     */
    public static Schema importFromXml(Reader reader) throws SchemaException, IOException {
        Xml2SchemaBuilder schemaBuilder = new Xml2SchemaBuilder();
        return schemaBuilder.build(reader);
    }

    /**
     * Exports this schema instance as xml that complies to the JSaParSchema.xsd.
     *
     * @param writer
     *            The writer where the xml is written. Caller is responsible for closing the writer.
     * @throws SchemaException
     *             if there is an error while writing the xml.
     */
    public void exportToXml(Writer writer) throws SchemaException {
        Schema2XmlExtractor extractor = new Schema2XmlExtractor();
        extractor.extractXml(writer, this);
    }
}
