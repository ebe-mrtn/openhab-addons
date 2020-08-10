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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OwlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Ebeling - Initial contribution
 */
@NonNullByDefault
public class OwlBindingConstants {

    private static final String BINDING_ID = "owl";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // supported things: currently only energy meter CMR180(i)
    // TODO: support other things
    public static final ThingTypeUID THING_TYPE_CMR180 = new ThingTypeUID(BINDING_ID, "cmr180");

    // List of all Channel ids
    public static final String CHANNEL_CURR_PHASE_1 = "currPhase1";
    public static final String CHANNEL_CURR_PHASE_2 = "currPhase2";
    public static final String CHANNEL_CURR_PHASE_3 = "currPhase3";
    public static final String CHANNEL_DAY_PHASE_1 = "dayPhase1";
    public static final String CHANNEL_DAY_PHASE_2 = "dayPhase2";
    public static final String CHANNEL_DAY_PHASE_3 = "dayPhase3";

    // Bridge config properties
    public static final String UDP_HOST = "ipAddress";
    public static final String UDP_PORT = "udpPort";
    public static final String UDP_KEY = "udpKey";
    public static final String MCAST_GROUP = "mcastGroup";
    public static final String MCAST_PORT = "mcastPort";
    public static final String POLLINTERVAL = "pollingInterval";

    // Thing configuration properties
    public static final String CMR180_MODE = "mode";
}
