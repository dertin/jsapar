package org.jsapar.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.*;

/**
 * Class that can be used to parse and format <a href="https://www.ibm.com/support/knowledgecenter/en/SSLVMB_24.0.0/spss/base/syn_data_list_implied_decimal_positions.html">implied decimals</a>.
 * The text representation is always an integer but when parsing the decimal point is shifted left and when composing it is shifted right.
 */
public class ImpliedDecimalFormat extends Format {

    private final int decimals;
    private final DecimalFormat integerFormat = new DecimalFormat("0");

    /**
     * @param decimals Number of decimals to imply
     */
    public ImpliedDecimalFormat(int decimals) {
        this.decimals = decimals;
    }

    public int getDecimals() {
        return decimals;
    }

    @Override
    public StringBuffer format(Object o, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        BigDecimal value;
        if(o instanceof BigDecimal){
            value = (BigDecimal) o;
        }
        else if (o instanceof BigInteger){
            value = new BigDecimal((BigInteger) o);
        }
        else if (o instanceof Number){
            value = BigDecimal.valueOf(((Number) o).doubleValue());
        }
        else if (o instanceof String){
            value = new BigDecimal((String)o);
        }
        else{
            throw new IllegalArgumentException("Unable to format an implied decimal value from " + o + ". Unsupported type.");
        }
        value = value.movePointRight(decimals);
        return integerFormat.format(value, stringBuffer, fieldPosition);
    }



    @Override
    public Object parseObject(String s, ParsePosition parsePosition) {
        Number v = integerFormat.parse(s, parsePosition);
        return BigDecimal.valueOf(v.longValue()).movePointLeft(decimals);
    }

    @Override
    public Object parseObject(String source) {
        return parse(source);
    }

    public BigDecimal parse(String source){
        return new BigDecimal(source).movePointLeft(decimals);
    }


}
