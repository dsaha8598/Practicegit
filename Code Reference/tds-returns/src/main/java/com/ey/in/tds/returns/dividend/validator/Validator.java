package com.ey.in.tds.returns.dividend.validator;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

public class Validator {

    public static List<Errors> validate(File xml, File xsd, String uniqueId) throws Exception {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder parser = builderFactory
                .newDocumentBuilder();
        Document document = parser.parse(new FileInputStream(xml));
        SchemaFactory factory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String basePath = xsd.getAbsolutePath().substring(0, xsd.getAbsolutePath().lastIndexOf(File.separator) + 1);
        factory.setResourceResolver(new ResourceResolver(basePath));
        Schema schema = factory.newSchema(new StreamSource(new FileInputStream(new File(xsd.getAbsolutePath()))));
        javax.xml.validation.Validator validator = schema.newValidator();
        final List<Errors> exceptions = new LinkedList<>();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) {
                Errors errors = new Errors();
                errors.setId(uniqueId);
                errors.setMessage(exception.getMessage().split(":")[0]);
                errors.setType(Errors.TYPE.WARNING);
                errors.setCompleteMessage(exception.getMessage());
                exceptions.add(errors);
            }

            @Override
            public void fatalError(SAXParseException exception) {
                Errors errors = new Errors();
                errors.setId(uniqueId);
                errors.setMessage(exception.getMessage().split(":")[0]);
                errors.setType(Errors.TYPE.FATAL_ERROR);
                errors.setCompleteMessage(exception.getMessage());
                exceptions.add(errors);
            }

            @Override
            public void error(SAXParseException exception) {
                Errors errors = new Errors();
                errors.setId(uniqueId);
                errors.setMessage(exception.getMessage().split(":")[1]);
                errors.setCompleteMessage(exception.getMessage());
                errors.setType(Errors.TYPE.ERROR);
                exceptions.add(errors);
            }
        });
        validator.validate(new DOMSource(document));
        return exceptions;
    }
}
