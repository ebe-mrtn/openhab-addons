/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private @Nullable ScheduledFuture<?> pollingJob = null;
    private @Nullable MulticastSocket multicastSocket = null;
    private @Nullable EnergyPacket energyPacket = null;

    public OwlBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            /// TODO wieder raus!
            logger.info("Refreshing {}", channelUID);
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
        multicastPort = config.mcastPort;
        multicastGroup = config.mcastGroup;
        timeoutMinutes = config.timoutInterval;

        // set the thing status to UNKNOWN temporarily
        updateStatus(ThingStatus.UNKNOWN);

        try {
            // initialize the multicast client
            final InetAddress address = InetAddress.getByName(multicastGroup);
            final MulticastSocket ms = new MulticastSocket(multicastPort);
            ms.setReuseAddress(true);
            ms.setSoTimeout(timeoutMinutes * 60 * 1000);
            ms.joinGroup(address);
            multicastSocket = ms;

            /// TODO wieder debug!
            logger.info("UDP multicast socket opened on '{}:{}' with {} minutes timeout", multicastGroup, multicastPort,
                    timeoutMinutes);

            // schedule an init job, which does nothing
            // to initialize the sheduler
            scheduler.submit(() -> {
            });

            // create polling job for periodically receive multicasts
            pollingJob = scheduler.scheduleWithFixedDelay(this::receiveMcast, 1,
                    OwlBindingConstants.DEFAULT_POLLING_TIME, TimeUnit.SECONDS);
            /// TODO wieder debug!
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
        final ScheduledFuture<?> pj = pollingJob;
        if (pj != null) {
            pj.cancel(true);
        }
        pollingJob = null;
        // close multicast listener, to abort receive
        final MulticastSocket ms = multicastSocket;
        if (ms != null) {
            ms.close();
        }
        multicastSocket = null;
        // clear all packets
        energyPacket = null;
        /// TODO wieder debug!
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
            final byte[] bytes = new byte[2048];
            final DatagramPacket datagram = new DatagramPacket(bytes, bytes.length);
            final MulticastSocket ms = multicastSocket;
            
            // blocking receive of multicast message until timeout
            if (ms != null) {
                /// TODO wieder raus!
                logger.info("Waiting for multicast...");

                ms.receive(datagram);

                /// TODO wieder raus!
                logger.info("... received multicast (length='{}')", datagram.getLength());
            }

            // process received data, timout on receive will directly trigger catch block
            final String packetData = new String(bytes, 0, datagram.getLength());
            final EnergyPacket packet = new EnergyPacket();
            packet.read(packetData);

            // assign received packets for getters for connected things
            if (packet.isParsed()) {
                energyPacket = packet;
            } else {
                energyPacket = null;
               /// TODO wieder debug!
               logger.info("Received unknown packet with data '{}'", packetData);
            } 

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
    public @Nullable EnergyPacket getEnergyPacket() {
        return energyPacket;
    }
    
}
