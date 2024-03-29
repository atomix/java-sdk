// SPDX-FileCopyrightText: 2018-present Open Networking Foundation
// SPDX-FileCopyrightText: 2022-present Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package io.atomix.util.misc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Timestamp printer.
 */
public class TimestampPrinter {

    /**
     * Returns a new timestamp printer.
     *
     * @param timestamp the timestamp to print
     * @return the timestamp printer
     */
    public static TimestampPrinter of(long timestamp) {
        return new TimestampPrinter(timestamp);
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss,SSS");

    private final long timestamp;

    public TimestampPrinter(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
    }
}