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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.owl.internal.packets.EnergyPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwlEnergyHandler} is responsible for handling commands, which are
 * sent to one of the channels of the OWL energy meter CMR180(i)
 *
 * @author Martin Ebeling - Initial contribution
 */
@NonNullByDefault
public class OwlEnergyHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OwlEnergyHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable OwlConfiguration config;

    public OwlEnergyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }
    
    @Override
    public void initialize() {
        config = getConfigAs(OwlConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        // schedule an init job, which does nothing to initialize the sheduler
        scheduler.submit(() -> {
        });
        // create polling job for periodically update data received thru bridge
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 1, OwlBindingConstants.DEFAULT_POLLING_TIME, TimeUnit.SECONDS);
        logger.info("Receive polling job started for '{}'", getThing().getUID());
        updateStatus(ThingStatus.OFFLINE);
    }
    
    @Override
    public void dispose() {
        // stop waiting for multicasts
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        logger.info("Handler '{}' disposed", getThing().getUID());
    }

    private void updateData() {
        // check if bridge has a valid energy packet for us
        OwlBridgeHandler handler = (OwlBridgeHandler) getBridge().getHandler();
        EnergyPacket packet = handler.getEnergyPacket();
        if (packet != null) {
            logger.info("Packet processed...");
            // update the data for the thing
            updateState(OwlBindingConstants.CHANNEL_POWER_PHASE_1, new DecimalType(packet.getPhase1().getPower()));
            updateState(OwlBindingConstants.CHANNEL_POWER_PHASE_2, new DecimalType(packet.getPhase2().getPower()));
            updateState(OwlBindingConstants.CHANNEL_POWER_PHASE_3, new DecimalType(packet.getPhase3().getPower()));
            updateState(OwlBindingConstants.CHANNEL_ENERGY_PHASE_1, new DecimalType(packet.getPhase1().getEnergy()));
            updateState(OwlBindingConstants.CHANNEL_ENERGY_PHASE_2, new DecimalType(packet.getPhase2().getEnergy()));
            updateState(OwlBindingConstants.CHANNEL_ENERGY_PHASE_3, new DecimalType(packet.getPhase3().getEnergy()));
            // if we are not online already, we are now
            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }
}
