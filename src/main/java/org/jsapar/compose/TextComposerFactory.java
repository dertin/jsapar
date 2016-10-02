package org.jsapar.compose;

import org.jsapar.JSaParException;
import org.jsapar.compose.csv.CsvComposer;
import org.jsapar.compose.fixed.FixedWidthComposer;
import org.jsapar.schema.CsvSchema;
import org.jsapar.schema.FixedWidthSchema;
import org.jsapar.schema.Schema;

import java.io.Writer;

/**
 * Created by stejon0 on 2016-01-24.
 */
public class TextComposerFactory implements ComposerFactory {

    public Composer makeComposer(Schema schema, Writer writer) throws JSaParException {
        if(schema instanceof CsvSchema){
//            if(schema instanceof CsvControlCellSchema){
//                return new CsvControlCellComposer(writer, (CsvControlCellSchema)schema);
//            }
            return new CsvComposer(writer, (CsvSchema)schema);
        }
        if(schema instanceof FixedWidthSchema){
//            if(schema instanceof FixedWidthControlCellSchema){
//                return new FixedWidthControlCellComposer(writer, (FixedWidthControlCellSchema)schema);
//            }
            return new FixedWidthComposer(writer, (FixedWidthSchema)schema);
        }
//        if(schema instanceof XmlSchema){
//            return new XmlComposer(reader, (XmlSchema) schema);
//        }

        throw new JSaParException("Unknown schema type. Unable to create parser class for it.");
    }

}