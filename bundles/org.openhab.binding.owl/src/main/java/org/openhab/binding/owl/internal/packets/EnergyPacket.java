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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The {@link EnergyPacket} class parses and provides data
 * from an energy xml data packet.
 * 
 * @author Martin Ebeling - Initial contribution
 */
@NonNullByDefault
public class EnergyPacket extends AbstractPacket {

    /**
     * Internal class for energy and power
     * representation of a single phase.
     * 
     * @author Martin Ebeling - Initial contribution
     */
    @NonNullByDefault
    public class EnergyPacketPhase {
        private double energy = 0.0;
        private double power = 0.0;

        public EnergyPacketPhase() {
        }

        public EnergyPacketPhase(double power, double energy) {
            this.energy = energy;
            this.power = power;
        }

        public QuantityType<?> getEnergy() {
            return new QuantityType<>(energy, SmartHomeUnits.WATT_HOUR);
        }

        public QuantityType<?> getPower() {
            return new QuantityType<>(power, SmartHomeUnits.WATT);
        }
    }

    /**
     * extracted data from the packet
     */
    private String id = "";
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
    
    @Override
    protected void parsePacket(final String packetData) throws PacketParseException {
        try {
            final Document doc = convertStringToDocument(packetData);
            final Element rootElement = getElementByPath(doc, "/electricity");
            final Element chan0Curr = getElementByPath(doc, "/electricity/chan[@id='0']/curr");
            final Element chan0Day = getElementByPath(doc, "/electricity/chan[@id='0']/day");
            final Element chan1Curr = getElementByPath(doc, "/electricity/chan[@id='1']/curr");
            final Element chan1Day = getElementByPath(doc, "/electricity/chan[@id='1']/day");
            final Element chan2Curr = getElementByPath(doc, "/electricity/chan[@id='2']/curr");
            final Element chan2Day = getElementByPath(doc, "/electricity/chan[@id='2']/day");

            id = rootElement.getAttribute("id");

            // unit creation, not useful because unit is displayed with wrong letter case...
            // assume units will be W and Wh always...
            // String unitString = chan0Curr.getAttribute("units");
            // Unit<?> unit = UnitUtils.parseUnit("0 " + unitString.toUpperCase());

            phase_1 = new EnergyPacketPhase(stringToDouble(chan0Curr.getTextContent()), stringToDouble(chan0Day.getTextContent()));
            phase_2 = new EnergyPacketPhase(stringToDouble(chan1Curr.getTextContent()), stringToDouble(chan1Day.getTextContent()));
            phase_3 = new EnergyPacketPhase(stringToDouble(chan2Curr.getTextContent()), stringToDouble(chan2Day.getTextContent()));
        } catch (final Exception e) {
            throw new PacketParseException("Failed to parse energy packet", e);
        }
    }

    @Override
    protected boolean checkExpected(String packetData) throws PacketParseException {
        try {
            final Document doc = convertStringToDocument(packetData);
            final Element rootElement = getElementByPath(doc, "/electricity");
            return (rootElement != null);
        } catch (final Exception e) {
            throw new PacketParseException("Failed to identify packet", e);
        }
    }

}
