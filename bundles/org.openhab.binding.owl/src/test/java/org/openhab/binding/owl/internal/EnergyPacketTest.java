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
package org.openhab.binding.owl.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.openhab.binding.owl.internal.packets.EnergyPacket;

/**
 * Testing parsing algorithm energy packets from the network owl
 * 
 * @author Martin Ebeling - Initial contribution
 */
public class EnergyPacketTest {

    private final String validPacket = 
    "<electricity id='AA37190017BB'>\n" +
    "    <signal rssi='-43' lqi='6'/>\n" +
    "    <battery level='100%'/>\n" +
    "    <chan id='0'>\n" +
    "       <curr units='w'>32.00</curr>\n" +
    "       <day units='wh'>1157.67</day>\n" +
    "    </chan>\n" +
    "    <chan id='1'>\n" +
    "       <curr units='w'>370.00</curr>\n" +
    "       <day units='wh'>2852.27</day>\n" +
    "    </chan>\n" +
    "    <chan id='2'>\n" +
    "       <curr units='w'>80.00</curr>\n" +
    "       <day units='wh'>2318.14</day>\n" +
    "    </chan>\n" +
    "</electricity>";
    
    private final String originalPacket = "<electricity id='AA37190017BB'><signal rssi='-36' lqi='13'/><battery level='100%'/><chan id='0'><curr units='w'>338.00</curr><day units='wh'>2783.06</day></chan><chan id='1'><curr units='w'>305.00</curr><day units='wh'>2678.55</day></chan><chan id='2'><curr units='w'>48.00</curr><day units='wh'>1017.71</day></chan></electricity>";
    
    @Test
    public void ParseValidPacket() {
        final EnergyPacket packet = EnergyPacket.parsePacket(validPacket);
        assertNotEquals(null, packet);
        assertEquals("AA37190017BB", packet.getId());
        assertEquals(32.0, packet.getPhase1().getPower(), 0.0001);
        assertEquals(1157.67, packet.getPhase1().getEnergy(), 0.0001);
        assertEquals(370.0, packet.getPhase2().getPower(), 0.0001);
        assertEquals(2852.27, packet.getPhase2().getEnergy(), 0.0001);
        assertEquals(80.0, packet.getPhase3().getPower(), 0.0001);
        assertEquals(2318.14, packet.getPhase3().getEnergy(), 0.0001);
    }

    @Test
    public void CheckValidPacket() {
        final Boolean isEnergyPacket = EnergyPacket.isEnergyPacket(validPacket);
        assertEquals(true, isEnergyPacket);
    }

    @Test
    public void CheckOriginalPacket() {
        final Boolean isEnergyPacket = EnergyPacket.isEnergyPacket(originalPacket);
        assertEquals(true, isEnergyPacket);
    }

    @Test
    public void ParseOriginalPacket() {
        final EnergyPacket packet = EnergyPacket.parsePacket(originalPacket);
        assertNotEquals(null, packet);
        assertEquals("AA37190017BB", packet.getId());
    }
}