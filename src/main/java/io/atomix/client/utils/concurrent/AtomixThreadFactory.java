// Copyright 2022-present Open Networking Foundation
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.utils.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 * Named thread factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AtomixThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        return new AtomixThread(r);
    }
}
