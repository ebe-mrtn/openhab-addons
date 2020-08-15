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
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
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
        final EnergyPacket packet = new EnergyPacket();
        assertEquals(true, packet.read(validPacket));
        assertEquals(true, packet.isExpected());
        assertEquals(true, packet.isParsed());
        assertEquals("AA37190017BB", packet.getId());
        assertEquals(32.0, packet.getPhase1().getPower().doubleValue(), 0.0001);
        assertEquals(1157.67, packet.getPhase1().getEnergy().doubleValue(), 0.0001);
        assertEquals(370.0, packet.getPhase2().getPower().doubleValue(), 0.0001);
        assertEquals(2852.27, packet.getPhase2().getEnergy().doubleValue(), 0.0001);
        assertEquals(80.0, packet.getPhase3().getPower().doubleValue(), 0.0001);
        assertEquals(2318.14, packet.getPhase3().getEnergy().doubleValue(), 0.0001);
    }
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void ParseEmptyPacket()  throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket();
        try {
            assertEquals(false, packet.read(emptyPacket));
        } catch (PacketParseException e) {
        } catch (Exception e) {
            assertTrue("Expected PacketParseException", false);
        }
        assertEquals(false, packet.isExpected());
        assertEquals(false, packet.isParsed());
    }

    @Test
    public void ParseOtherPacket() throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket();
        assertEquals(false, packet.read(otherPacket)); // should not throw exception!
        assertEquals(false, packet.isExpected());
        assertEquals(false, packet.isParsed());
    }

    @Test
    public void ParseIncompletePacket() throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket();
        try {
            assertEquals(false, packet.read(incompletePacket));
        } catch (PacketParseException e) {
        } catch (Exception e) {
            assertTrue("Expected PacketParseException", false);
        }
        assertEquals(true, packet.isExpected());
        assertEquals(false, packet.isParsed());
        assertEquals("AABB", packet.getId());
        assertNotEquals(null, packet.getPhase1());
        assertNotEquals(null, packet.getPhase2());
        assertNotEquals(null, packet.getPhase3());
        assertEquals(0, packet.getPhase1().getEnergy().intValue());
        assertEquals(0, packet.getPhase1().getPower().intValue());
        assertEquals(0, packet.getPhase2().getEnergy().intValue());
        assertEquals(0, packet.getPhase2().getPower().intValue());
        assertEquals(0, packet.getPhase3().getEnergy().intValue());
        assertEquals(0, packet.getPhase3().getPower().intValue());
    }

    @Test
    public void ReadMultiple() throws PacketParseException {
        final EnergyPacket packet = new EnergyPacket();
        try {
            assertEquals(false, packet.read(incompletePacket));
        } catch (PacketParseException e) {
        } catch (Exception e) {
            assertTrue("Expected PacketParseException", false);
        }
        assertEquals(true, packet.isExpected());
        assertEquals(false, packet.isParsed());

        assertEquals(true, packet.read(validPacket));
        assertEquals(true, packet.isExpected());
        assertEquals(true, packet.isParsed());
    }
}