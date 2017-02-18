package org.jsapar.model;

import java.text.Format;

/**
 * {@link Cell} implementation carrying a string value of a cell.
 *
 */
public class StringCell extends Cell {

    private static final long serialVersionUID = -2776042954053921679L;

    /**
     * The string representation of the stringValue of this cell.
     */
    private final String stringValue;


    /**
     * Creates a string cell with the supplied name and value.
     * 
     * @param name The name of the cell
     * @param value The value. Cannot be null. Use {@link EmptyCell} for empty values.
     */
    public StringCell(String name, String value) {
        super(name, CellType.STRING);
        assert value != null : "Cell value cannot be null, use EmptyCell for empty values.";
        this.stringValue = value;
    }


    /**
     * Creates a string cell with the supplied name and value.
     *
     * @param name The name of the cell
     * @param value The value
     */
    public StringCell(String name, char value) {
        super(name, CellType.STRING);
        this.stringValue = String.valueOf(value);
    }


    /**
     * @return the stringValue as an Object.
     */
    @Override
    public Object getValue() {
	return this.stringValue;
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.jsapar.model.Cell#getStringValue()
     */
    @Override
    public String getStringValue() {
	return this.stringValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jsapar.model.Cell#getStringValue(java.text.Format)
     */
    @Override
    public String getStringValue(Format format) throws IllegalArgumentException {
        if (format != null) {
            return format.format(this.stringValue);
        } else
            return this.stringValue;
    }


    @Override
    public int compareValueTo(Cell right) {
        return getStringValue().compareTo(right.getStringValue());
    }
}
