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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link EnergyPacket} class parses and provides data
 * from an energy xml data packet.
 * 
 * @author Martin Ebeling - Initial contribution
 */
public class EnergyPacket {

    public class EnergyPacketPhase {
        private double energy;
        private double power;

        public double getEnergy() {
            return energy;
        }

        public double getPower() {
            return power;
        }
    }

    /**
     * extracted data from the packet
     */
    private String id;
    private EnergyPacketPhase phase_1 = new EnergyPacketPhase();
    private EnergyPacketPhase phase_2 = new EnergyPacketPhase();
    private EnergyPacketPhase phase_3 = new EnergyPacketPhase();

    public String getId() {
        return id;
    }
    
    public EnergyPacketPhase getPhase1() {
        return phase_1;
    }

    public EnergyPacketPhase getPhase2() {
        return phase_2;
    }

    public EnergyPacketPhase getPhase3() {
        return phase_3;
    }
    
    /**
     * Parse string data to an energy packet
     * @param packetData
     * @return new EnergyPacket if parsing succeeded, null otherwise
     */
    public static EnergyPacket parsePacket(final String packetData) {
        try {
            final Document doc = convertStringToDocument(packetData);
            final Element rootElement = getElementByPath(doc, "/electricity");
            final Element chan0Curr = getElementByPath(doc, "/electricity/chan[@id='0']/curr");
            final Element chan0Day = getElementByPath(doc, "/electricity/chan[@id='0']/day");
            final Element chan1Curr = getElementByPath(doc, "/electricity/chan[@id='1']/curr");
            final Element chan1Day = getElementByPath(doc, "/electricity/chan[@id='1']/day");
            final Element chan2Curr = getElementByPath(doc, "/electricity/chan[@id='2']/curr");
            final Element chan2Day = getElementByPath(doc, "/electricity/chan[@id='2']/day");

            EnergyPacket result = new EnergyPacket();
            result.id = rootElement.getAttribute("id");
            result.phase_1.power = stringToDouble(chan0Curr.getTextContent());
            result.phase_1.energy = stringToDouble(chan0Day.getTextContent());
            result.phase_2.power = stringToDouble(chan1Curr.getTextContent());
            result.phase_2.energy = stringToDouble(chan1Day.getTextContent());
            result.phase_3.power = stringToDouble(chan2Curr.getTextContent());
            result.phase_3.energy = stringToDouble(chan2Day.getTextContent());
            return result;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse double values from string
     * with correct locale format (value uses point as decimal separator)
     * @param string
     * @return
     * @throws ParseException
     */
    private static double stringToDouble(String string) throws ParseException {
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        Number n = df.parse(string);
        return n.doubleValue();
    }

    /**
     * Check if the packetData contains something
     * which should be an electritity packet.
     * @param packetData
     * @return true if string is XML document with root node 'electricity'
     */
    public static boolean isEnergyPacket(final String packetData) {
        try {
            final Document doc = convertStringToDocument(packetData);
            final Element rootElement = getElementByPath(doc, "/electricity");
            return (rootElement != null);
        } catch (final Exception e) {}
        return false;
    }

    /**
     * Get an element from the DOM document by path within 
     * the XML structure.
     * @param doc
     * @param path XPath expression
     * @return
     * @throws XPathExpressionException
     */
    private static Element getElementByPath(Document doc, String path) 
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
    private static Document convertStringToDocument(final String xmlStr)
            throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
        return doc;
    }

}
