/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.owl.internal.packets;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link AbstractPacket} is a abstract base class 
 * to parse XML data packages from the network owl.
 * 
 * @author Martin Ebeling - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractPacket {
    
    private boolean isExpectedPacket = false;
    private boolean isParsedPacket = false;

    /**
     * Check if data was expeted for parsing by
     * this class, after called read()
     * @return true if data seemed to be of type for expected package parsing
     */
    public boolean isExpected() {
        return isExpectedPacket;
    }

    /**
     * Check if data has been successfully
     * parsed by the read() function
     * @return true if data has been parsed
     */
    public boolean isParsed() {
        return isParsedPacket;
    }

    /**
     * Read the string and check if it may be a expected packet 
     * by checking the root node within derived class.
     * If the data is expected, try to read and parse the values.
     * @param packetData : Data representing the packet
     * @return true if the data was expected and has been parsed
     * @throws PacketParseException
     */
    public boolean read(final String packetData) throws PacketParseException {
        isExpectedPacket = checkExpected(packetData);
        isParsedPacket = false;

        if (isExpectedPacket) {
            parsePacket(packetData);
            isParsedPacket = true;
        }

        return (isExpectedPacket && isParsedPacket);
    }

    /**
     * Parse string data to an packet
     * @param packetData
     * @return new T if parsing succeeded
     * @throws PacketParseException if something went wrong parsing the string to packet data
     */
    protected abstract void parsePacket(final String packetData) throws PacketParseException;

    /**
     * Check if the packetData contains something
     * which should be identify an expected packet.
     * @param packetData
     * @return true if string is XML document should be packet for our type
     */
    protected abstract boolean checkExpected(final String packetData) throws PacketParseException;

    /**
     * Parse double values from string
     * with correct locale format (value uses point as decimal separator)
     * @param string
     * @return
     * @throws ParseException
     */
    protected static double stringToDouble(String string) throws ParseException {
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        Number n = df.parse(string);
        return n.doubleValue();
    }

    /**
     * Get an element from the DOM document by path within 
     * the XML structure.
     * @param doc
     * @param path XPath expression
     * @return
     * @throws XPathExpressionException
     */
    protected static Element getElementByPath(Document doc, String path) 
            throws XPathExpressionException {
        final XPathFactory xpf = XPathFactory.newInstance();
        final XPath xpath = xpf.newXPath();
        final Element root = (Element) xpath.evaluate(path, doc, XPathConstants.NODE);
        return root;
    }

    /**
     * Convert a string to a DOM document 
     * for reading with XPath
     */
    protected static Document convertStringToDocument(final String xmlStr)
            throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
        return doc;
    }

}
