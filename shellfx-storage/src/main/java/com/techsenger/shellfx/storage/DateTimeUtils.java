/*
 * Copyright 2024-2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.shellfx.storage;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Pavel Castornii
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter CURRENT_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM dd HH:mm");

    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy");

    /**
     * Formats the timestamp using a compact representation for entries from the current year.
     *
     * <p>If the timestamp belongs to the specified year, the year is omitted from the formatted value. Otherwise,
     * the year is included.
     *
     * @param timestamp timestamp in milliseconds since the Unix epoch
     * @param currentYear year used to determine whether the year should be displayed
     * @return formatted date and time
     */
    public static String format(Long timestamp, Year currentYear) {
        var zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
        if (currentYear.getValue() == zonedDateTime.getYear()) {
            return CURRENT_YEAR_FORMATTER.format(zonedDateTime);
        }
        return FULL_FORMATTER.format(zonedDateTime);
    }

    /**
     * Formats the timestamp using a full date representation that always includes the year.
     *
     * @param timestamp timestamp in milliseconds since the Unix epoch
     * @return formatted date and time including the year
     */
    public static String formatFull(Long timestamp) {
        var zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
        return FULL_FORMATTER.format(zonedDateTime);
    }

    private DateTimeUtils() {
        // empty
    }
}
