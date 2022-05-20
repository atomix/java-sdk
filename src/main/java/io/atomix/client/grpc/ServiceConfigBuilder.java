// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.client.grpc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;


/**
 * Service config builder used to configure the internal retry
 * mechanism of the gRPC services.
 */
public class ServiceConfigBuilder {
    public static final String MAX_ATTEMPTS = "maxAttempts";
    public static final String INITIAL_BACKOFF = "initialBackoff";
    public static final String MAX_BACKOFF = "maxBackoff";
    public static final String BACKOFF_MULTIPLIER = "backoffMultiplier";
    public static final String RETRYABLE_STATUS_CODES = "retryableStatusCodes";
    public static final String SERVICE = "service";
    public static final String NAME = "name";
    public static final String RETRY_POLICY = "retryPolicy";
    public static final String METHOD_CONFIG = "methodConfig";

    private Double maxAttempts = 5.0;
    private String initialBackoff = "0.5s";
    private String maxBackoff = "30s";
    private Double backoffMultiplier = 2.0;
    private List<String> retryableStatusCodes = List.of("UNAVAILABLE");

    /**
     * Set the maximum number of attempts for retries
     *
     * @param maxAttempts max attempts to be performed
     * @return this
     */
    public ServiceConfigBuilder withMaxAttempts(double maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * Set the initial backoff for retries
     *
     * @param initialBackoff the initial backoff
     * @return this
     */
    public ServiceConfigBuilder withInitialBackoff(String initialBackoff) {
        this.initialBackoff = initialBackoff;
        return this;
    }

    /**
     * Set the maximum backoff for retries
     *
     * @param maxBackoff the maximum backoff
     * @return this
     */
    public ServiceConfigBuilder withMaxBackoff(String maxBackoff) {
        this.maxBackoff = maxBackoff;
        return this;
    }

    /**
     * Set the backoff multiplier for retries
     *
     * @param backoffMultiplier the backoff multiplier
     * @return this
     */
    public ServiceConfigBuilder withBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
        return this;
    }

    /**
     * Define the retryable status codes.
     *
     * @param retryableStatusCodes the retryable status codes
     * @return this
     */
    public ServiceConfigBuilder withRetryableStatusCodes(List<String> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
        return this;
    }

    /**
     * Builds the band.
     *
     * @return a band
     */
    public Map<String, ?> build() {
        // For further infos look at service_config.proto in the grpc repo
        // https://github.com/grpc/proposal/blob/master/A6-client-retries.md#detailed-design
        Map<String, Object> methodConfig = Maps.newHashMap();
        // method config is the overarching map
        List<Map<String, Object>> methodConfigs = Lists.newArrayList();
        for (PrimitiveService primitiveService : PrimitiveService.values()) {
            // retryPolicy opaque map
            Map<String, Object> retryPolicy = Maps.newHashMap();
            retryPolicy.put(MAX_ATTEMPTS, maxAttempts);
            retryPolicy.put(INITIAL_BACKOFF, initialBackoff);
            retryPolicy.put(MAX_BACKOFF, maxBackoff);
            retryPolicy.put(BACKOFF_MULTIPLIER, backoffMultiplier);
            retryPolicy.put(RETRYABLE_STATUS_CODES, retryableStatusCodes);

            // name defines the grpc services affected by the retry policy
            // we do that for all the primitives we want to support
            List<Map<String, Object>> name = Lists.newArrayList();

            Map<String, Object> serviceName = Maps.newHashMap();
            serviceName.put(SERVICE, primitiveService.getServiceName());
            name.add(serviceName);

            Map<String, Object> serviceConfig = Maps.newHashMap();
            serviceConfig.put(NAME, name);
            serviceConfig.put(RETRY_POLICY, retryPolicy);
            methodConfigs.add(serviceConfig);
        }
        methodConfig.put(METHOD_CONFIG, methodConfigs);
        return methodConfig;
    }

}
