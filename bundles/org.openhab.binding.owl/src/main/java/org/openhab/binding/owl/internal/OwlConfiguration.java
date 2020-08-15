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

/**
 * The {@link OwlConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin Ebeling - Initial contribution
 */
@NonNullByDefault
public class OwlConfiguration {

    /**
     * Configuration parameter of the Network OWL bridge interface.
     */
    public String ipAddress = "";
    public Integer udpPort = 0;
    public String udpKey = "";
    public String mcastGroup = "";
    public Integer mcastPort = 0;
    public Integer timoutInterval = 0;
}
