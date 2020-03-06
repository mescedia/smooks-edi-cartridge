package org.milyn.edifact.edg;

import org.milyn.edifact.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.milyn.edifact.edg.template.MessagesTemplate;
import org.milyn.edifact.edg.template.SegmentsTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.zip.ZipInputStream;

public final class EDIFACTDfdlSchemaGenerator {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();


    private EDIFACTDfdlSchemaGenerator() {

    }

    public static void main(final String[] args) throws Throwable {
        final String specPath = args[0];
        final String outputDirectory = args[1];
        new File(outputDirectory).mkdirs();

        final InputStream resourceAsStream = EDIFACTDfdlSchemaGenerator.class.getResourceAsStream(specPath);
        final UnEdifactSpecificationReader unEdifactSpecificationReader = new UnEdifactSpecificationReader(new ZipInputStream(resourceAsStream), true, true);

        final String[] namespace = unEdifactSpecificationReader.getDefinitionModel().getDescription().getNamespace().split(":");
        final String version = namespace[3].replace("-", "").toUpperCase();

        final String segmentsSchema = new SegmentsTemplate(version, unEdifactSpecificationReader).materialise();
        write(segmentsSchema, outputDirectory + "/EDIFACT-Segments.dfdl.xsd");

        String messagesSchema = new MessagesTemplate(version, unEdifactSpecificationReader).materialise();
        write(messagesSchema, outputDirectory + "/EDIFACT-Messages.dfdl.xsd");
    }

    private static void write(final String xml, final String fileName) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        final DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        final Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        stripWhitespaceTextNodes(document);

        final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");

        final FileWriter fileWriter = new FileWriter(fileName);
        transformer.transform(new DOMSource(document), new StreamResult(fileWriter));
    }

    private static void stripWhitespaceTextNodes(final Document document) throws XPathExpressionException {
        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
        final NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            final Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
    }
}