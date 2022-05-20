// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.grpc;

import io.atomix.client.grpc.PrimitiveService;
import io.atomix.client.grpc.ServiceConfigBuilder;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Tests for service config builder.
 */
public class ServiceConfigBuilderTest {

    @Test
    public void testDefault() {
        Map<String, ?> generatedServiceConfig = new ServiceConfigBuilder().build();
        List<?> methodConfigs = (List<?>) generatedServiceConfig.get(ServiceConfigBuilder.METHOD_CONFIG);
        assertNotNull(methodConfigs);

        Map<?, ?> methodConfig = (Map<?, ?>) methodConfigs.get(0);
        assertNotNull(methodConfigs);
        assertEquals(methodConfigs.size(), PrimitiveService.values().length);

        List<Map<String, ?>> name = (List<Map<String, ?>>) methodConfig.get(ServiceConfigBuilder.NAME);
        assertNotNull(name);

        Map<?, ?> retryPolicy = (Map<?, ?>) methodConfig.get(ServiceConfigBuilder.RETRY_POLICY);
        assertNotNull(retryPolicy);
        assertThat(retryPolicy.get(ServiceConfigBuilder.MAX_ATTEMPTS), is(5.0));
        assertEquals(retryPolicy.get(ServiceConfigBuilder.INITIAL_BACKOFF), "0.5s");
        assertEquals(retryPolicy.get(ServiceConfigBuilder.MAX_BACKOFF), "30s");
        assertThat(retryPolicy.get(ServiceConfigBuilder.BACKOFF_MULTIPLIER), is(2.0));
        assertEquals(retryPolicy.get(ServiceConfigBuilder.RETRYABLE_STATUS_CODES), List.of("UNAVAILABLE"));
    }

    @Test
    public void testNonDefault() {
        Map<String, ?> generatedServiceConfig = new ServiceConfigBuilder()
                .withMaxAttempts(1)
                .withInitialBackoff("1.0s")
                .withMaxBackoff("3.0s")
                .withBackoffMultiplier(3.0)
                .withRetryableStatusCodes(List.of("DATA_LOSS"))
                .build();
        List<?> methodConfigs = (List<?>) generatedServiceConfig.get(ServiceConfigBuilder.METHOD_CONFIG);
        assertNotNull(methodConfigs);

        Map<?, ?> methodConfig = (Map<?, ?>) methodConfigs.get(0);
        assertNotNull(methodConfigs);
        assertEquals(methodConfigs.size(), PrimitiveService.values().length);

        List<Map<String, ?>> name = (List<Map<String, ?>>) methodConfig.get(ServiceConfigBuilder.NAME);
        assertNotNull(name);

        Map<?, ?> retryPolicy = (Map<?, ?>) methodConfig.get(ServiceConfigBuilder.RETRY_POLICY);
        assertNotNull(retryPolicy);
        assertThat(retryPolicy.get(ServiceConfigBuilder.MAX_ATTEMPTS), is(1.0));
        assertEquals(retryPolicy.get(ServiceConfigBuilder.INITIAL_BACKOFF), "1.0s");
        assertEquals(retryPolicy.get(ServiceConfigBuilder.MAX_BACKOFF), "3.0s");
        assertThat(retryPolicy.get(ServiceConfigBuilder.BACKOFF_MULTIPLIER), is(3.0));
        assertEquals(retryPolicy.get(ServiceConfigBuilder.RETRYABLE_STATUS_CODES), List.of("DATA_LOSS"));
    }
}
