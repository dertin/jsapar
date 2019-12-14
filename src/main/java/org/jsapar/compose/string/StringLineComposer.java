package org.jsapar.compose.string;

import org.jsapar.model.Line;
import org.jsapar.schema.SchemaLine;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StringLineComposer {
    private final SchemaLine schemaLine;
    private final List<StringCellComposer> cellComposers;

    StringLineComposer(SchemaLine schemaLine) {
        this.schemaLine = schemaLine;
        cellComposers = schemaLine.stream().map(StringCellComposer::new).collect(Collectors.toList());
    }

    Stream<String> composeStringLine(Line line) {
        return cellComposers.stream().map(f -> f.compose(line.getCell(f.getName()).orElse(f.makeEmptyCell())));
    }

    boolean isIgnoreWrite(){
        return schemaLine.isIgnoreWrite();
    }
}