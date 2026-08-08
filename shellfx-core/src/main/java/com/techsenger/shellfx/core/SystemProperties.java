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

package com.techsenger.shellfx.core;

/**
 * Names of JVM system properties recognized by shellfx-based applications.
 *
 * @author Pavel Castornii
 */
public final class SystemProperties {

    /**
     * When set to {@code true} (via {@code -D} or {@link System#setProperty}), components may build extra,
     * developer-only tooling (e.g. a dialog picker for styling work) into their UI. Read once when a component
     * is built, not watched live: toggling it does not affect components already built, only ones built
     * afterwards.
     */
    public static final String DEV_MODE = "com.techsenger.shellfx.core.devMode";

    private SystemProperties() {
        // empty
    }
}
