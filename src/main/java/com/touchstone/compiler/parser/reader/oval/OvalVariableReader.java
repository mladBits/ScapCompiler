package com.touchstone.compiler.parser.reader.oval;

import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalConstantVariable;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalExternalVariable;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalLocalVariable;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariable;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OvalVariableReader {

    private final OvalVariableComponentReader componentReader;

    public List<ParsedOvalVariable> readVariables(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final List<ParsedOvalVariable> variables = new ArrayList<>();

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                switch (localName) {
                    case "external_variable" -> variables.add(readExternalVariable(reader));
                    case "constant_variable" -> variables.add(readConstantVariable(reader));
                    case "local_variable" -> variables.add(readLocalVariable(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "variables".equals(reader.getLocalName())) {
                return variables;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading variables");
    }

    private ParsedOvalExternalVariable readExternalVariable(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalExternalVariable variable = new ParsedOvalExternalVariable();
        variable.setId(reader.getAttributeValue(null, "id"));
        variable.setDatatype(reader.getAttributeValue(null, "datatype"));

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.END_ELEMENT
                    && "external_variable".equals(reader.getLocalName())) {
                return variable;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading external_variable");
    }

    private ParsedOvalConstantVariable readConstantVariable(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalConstantVariable variable = new ParsedOvalConstantVariable();
        variable.setId(reader.getAttributeValue(null, "id"));
        variable.setDatatype(reader.getAttributeValue(null, "datatype"));

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("value".equals(reader.getLocalName())) {
                    variable.getValues().add(reader.getElementText().trim());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "constant_variable".equals(reader.getLocalName())) {
                return variable;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading constant_variable");
    }

    private ParsedOvalLocalVariable readLocalVariable(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalLocalVariable variable = new ParsedOvalLocalVariable();
        variable.setId(reader.getAttributeValue(null, "id"));
        variable.setDatatype(reader.getAttributeValue(null, "datatype"));

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                variable.setExpression(componentReader.readComponent(reader));
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "local_variable".equals(reader.getLocalName())) {
                return variable;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading local_variable");
    }
}
