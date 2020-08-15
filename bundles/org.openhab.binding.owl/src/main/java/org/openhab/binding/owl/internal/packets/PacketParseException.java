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

/**
 * 
 * @author Martin Ebeling - Initial contribution
 */
@SuppressWarnings("serial")
public class PacketParseException extends Exception {
    public PacketParseException() {
        super();
    }

    public PacketParseException(String message) {
        super(message);
    }
    
    public PacketParseException(String message, Exception e) {
        super(message, e);
    }
}