package io.atomix.client;

import io.atomix.client.utils.concurrent.ThreadContextFactory;

/**
 * Manages the creation of distributed primitive instances.
 * <p>
 * TODO add additional documentation
 */
public interface AtomixCloudService extends PrimitiveFactory {

    /**
     * Returns the Atomix primitive thread factory.
     *
     * @return the primitive thread context factory
     */
    ThreadContextFactory getThreadFactory();
}
