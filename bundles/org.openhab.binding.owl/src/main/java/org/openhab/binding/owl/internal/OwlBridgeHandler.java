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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.owl.internal.packets.EnergyPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Ebeling - Initial contribution
 */
@NonNullByDefault
public class OwlBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OwlBridgeHandler.class);
    private OwlConfiguration config = getConfigAs(OwlConfiguration.class);
    private String multicastGroup = OwlBindingConstants.DEFAULT_MCAST_GRP;
    private int multicastPort = OwlBindingConstants.DEFAULT_MCAST_PORT;
    private int timeoutMinutes = OwlBindingConstants.DEFAULT_TIMEOUT_MINS;

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable MulticastSocket multicastSocket;

    private @Nullable EnergyPacket energyPacket = null;

    public OwlBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    @Override
    public void initialize() {
        // get config parameters or defaults
        config = getConfigAs(OwlConfiguration.class);
        multicastPort = (config.mcastPort == null) ? 
                        OwlBindingConstants.DEFAULT_MCAST_PORT : config.mcastPort;
        multicastGroup = (config.mcastGroup == null) ? 
                         OwlBindingConstants.DEFAULT_MCAST_GRP : config.mcastGroup;
        timeoutMinutes = (config.timoutInterval == null) ? 
                         OwlBindingConstants.DEFAULT_TIMEOUT_MINS : config.timoutInterval;

        // set the thing status to UNKNOWN temporarily
        updateStatus(ThingStatus.UNKNOWN);

        try {
            // initialize the multicast client
            InetAddress address = InetAddress.getByName(multicastGroup);
            multicastSocket = new MulticastSocket(multicastPort);
            multicastSocket.setReuseAddress(true);
            multicastSocket.setSoTimeout(timeoutMinutes * 60 * 1000);
            multicastSocket.joinGroup(address);
            logger.info("UDP multicast socket opened on '{}:{}' with {} minutes timeout", multicastGroup, multicastPort,
                    timeoutMinutes);

            // schedule an init job, which does nothing
            // to initialize the sheduler
            scheduler.submit(() -> {
            });

            // create polling job for periodically receive multicasts
            pollingJob = scheduler.scheduleWithFixedDelay(this::receiveMcast, 1,
                    OwlBindingConstants.DEFAULT_POLLING_TIME, TimeUnit.SECONDS);
            logger.info("Receive polling job started for '{}'", getThing().getUID());

            // initialize ready, set to OFFLINE temporarily until a valid multicast has been received
            updateStatus(ThingStatus.OFFLINE);

        } catch (Exception ex) {
            // cannot create connection to Ucp broadcast
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    String.format("%s on multicast connection '%s:%d'",
                            ex.getMessage() != null ? ex.getMessage() : ex.getClass().toString(), multicastGroup,
                            multicastPort));
        }
    }

    @Override
    public void dispose() {
        // stop waiting for multicasts
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        // close multicast listener, to abort receive
        if (multicastSocket != null) {
            multicastSocket.close();
            multicastSocket = null;
        }
        // clear all packets
        energyPacket = null;
        logger.info("Handler '{}' disposed", getThing().getUID());
    }

    /**
     * Cyclically check if we can receive a multicast package.
     * If no package was received until timeout, we seem to be offline.
     */
    private synchronized void receiveMcast() {
        // receive multicasts until the handler should be disposed
        // try to receive a multicast within given timeout
        try {
            byte[] bytes = new byte[2048];
            DatagramPacket datagram = new DatagramPacket(bytes, bytes.length);
            multicastSocket.receive(datagram);
            String packetData = new String(bytes, 0, datagram.getLength());

            EnergyPacket packet = new EnergyPacket(packetData);

            // provide the data as energy packet
            logger.info("Is energy packet? '{}'", packet.isEnergyPacket());

            if (packet.isEnergyPacket()) {
                energyPacket = packet;

                /// TODO wieder raus!
                logger.info("Received energy packet from '{}'", energyPacket.getId());
            }

            /// TODO wieder raus!
            // logger.info("Received multicast with data: {}", packetData);

            /*
            String sma = new String(Arrays.copyOfRange(bytes, 0x00, 0x03));
            if (!sma.equals("SMA")) {
            throw new IOException("Not a SMA telegram." + sma);
            }
            
            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0x14, 0x18));
            serialNumber = String.valueOf(buffer.getInt());
            
            powerIn.updateValue(bytes);
            energyIn.updateValue(bytes);
            powerOut.updateValue(bytes);
            energyOut.updateValue(bytes);
            */

            // if we are still not online, we are now
            if (getThing().getStatus().equals(ThingStatus.ONLINE) == false) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception ex) {
            // timeout occurred waiting for a packet
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().toString());
            // reset received packet, connected things can check for not null
            // to determine if a valid packes has been received
            energyPacket = null;
        }
    }
    
    /**
     * Access to the received energy packet data
     * @return
     */
    @Nullable
    public EnergyPacket getEnergyPacket() {
        return energyPacket;
    }
    
}
