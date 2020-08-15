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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openhab.binding.owl.internal.packets.EnergyPacket;
import org.openhab.binding.owl.internal.packets.PacketParseException;

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
        
    private final String emptyPacket = "";
    private final String otherPacket = "<other id='23'></other>";
    private final String incompletePacket = "<electricity id='AABB'></electricity>";

    @Test
    public void ParseValidPacket() throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket(validPacket);
        assertNotEquals(null, packet);
        assertEquals(true, packet.isEnergyPacket());
        assertEquals("AA37190017BB", packet.getId());
        assertEquals(32.0, packet.getPhase1().getPower().doubleValue(), 0.0001);
        assertEquals(1157.67, packet.getPhase1().getEnergy().doubleValue(), 0.0001);
        assertEquals(370.0, packet.getPhase2().getPower().doubleValue(), 0.0001);
        assertEquals(2852.27, packet.getPhase2().getEnergy().doubleValue(), 0.0001);
        assertEquals(80.0, packet.getPhase3().getPower().doubleValue(), 0.0001);
        assertEquals(2318.14, packet.getPhase3().getEnergy().doubleValue(), 0.0001);
    }

    @Test
    public void CheckValidPacket()  throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket(validPacket);
        final Boolean isEnergyPacket = packet.isEnergyPacket();
        assertEquals(true, isEnergyPacket);
    }

    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void ParseEmptyPacket()  throws PacketParseException {
        exception.expect(PacketParseException.class);
        final EnergyPacket packet = new EnergyPacket(emptyPacket);
        assertEquals(null, packet);
    }

    @Test
    public void ParseOtherPacket() throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket(otherPacket);
        assertEquals(false, packet.isEnergyPacket());
        assertEquals(null, packet.getId());
        assertEquals(null, packet.getPhase1());
        assertEquals(null, packet.getPhase2());
        assertEquals(null, packet.getPhase3());
    }

    @Test
    public void ParseIncompletePacket() throws PacketParseException {
        exception.expect(PacketParseException.class);
        final EnergyPacket packet = new EnergyPacket(incompletePacket);
        assertEquals(true, packet.isEnergyPacket());
        assertEquals("AABB", packet.getId());
        assertEquals(null, packet.getPhase1());
        assertEquals(null, packet.getPhase2());
        assertEquals(null, packet.getPhase3());
    }
}