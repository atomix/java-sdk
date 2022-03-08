// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

/**
 * State of distributed primitive.
 */
public enum PrimitiveState {

    /**
     * Signifies a state wherein the primitive is operating correctly and is capable of meeting the advertised
     * consistency and reliability guarantees.
     */
    CONNECTED,

    /**
     * Signifies a state wherein the primitive is temporarily incapable of providing the advertised
     * consistency properties.
     */
    SUSPENDED,

    /**
     * Signifies a state wherein the primitive's session has been expired and therefore cannot perform its functions.
     */
    EXPIRED,

    /**
     * Signifies a state wherein the primitive session has been closed and therefore cannot perform its functions.
     */
    CLOSED
}
