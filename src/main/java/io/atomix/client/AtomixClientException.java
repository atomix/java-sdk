// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client;

/**
 * Client exception.
 */
public class AtomixClientException extends RuntimeException {
    public AtomixClientException() {
    }

    public AtomixClientException(String message) {
        super(message);
    }

    public AtomixClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public AtomixClientException(Throwable cause) {
        super(cause);
    }
}
