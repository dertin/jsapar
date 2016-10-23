/** 
 * Copyright: Jonas Stenberg
 */
package org.jsapar.model;

/**
 * Float cell contains a double precision float number. Single precision float
 * values are converted into double precision values.
 * 
 * @author Jonas Stenberg
 * 
 */
public class FloatCell extends NumberCell implements Comparable<FloatCell> {

    private static final long serialVersionUID = 2102712515168714171L;

    /**
     * Creates a float number cell with supplied name. Converts the float value
     * into a double precision float value.
     * 
     * @param name
     * @param value
     */
    public FloatCell(String name, Float value) {
	super(name, value, CellType.FLOAT);
    }

    /**
     * Creates a float number cell with supplied name.
     * 
     * @param name
     * @param value
     */
    public FloatCell(String name, Double value) {
	super(name, value, CellType.FLOAT);
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FloatCell right) {
	return Double.compare(this.getNumberValue().doubleValue(), right
		.getNumberValue().doubleValue());
    }
}
