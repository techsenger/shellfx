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

package com.techsenger.shellfx.material.style;

import com.techsenger.annotations.Unmodifiable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry of icon stylesheets used internally by the ShellFX platform. This includes icons for built-in components
 * such as default dialogs, devtools, and other platform components. This registry is not intended for
 * application-level icons — those should be managed by the application itself.
 *
 * <p>Duplicate stylesheets are ignored — equality is determined by {@link Stylesheet#equals}. Stylesheets should be
 * configured before the UI is initialized.
 *
 * @author Pavel Castornii
 */
public final class IconStylesheets {

    private static final Logger logger = LoggerFactory.getLogger(IconStylesheets.class);

    private static final Set<Stylesheet> stylesheets = new LinkedHashSet<>();

    /**
     * Adds the given stylesheet to the registry. Duplicates are ignored.
     *
     * @param stylesheet the stylesheet to add, must not be {@code null}
     */
    public static void add(Stylesheet stylesheet) {
        if (stylesheets.add(Objects.requireNonNull(stylesheet))) {
            logStylesheets();
        }
    }

    /**
     * Adds all stylesheets from the given collection to the registry. Duplicates are ignored.
     *
     * @param stylesheets the stylesheets to add, must not be {@code null}
     */
    public static void addAll(Collection<? extends Stylesheet> stylesheets) {
        var changed = false;
        for (var s : Objects.requireNonNull(stylesheets)) {
            changed = IconStylesheets.stylesheets.add(Objects.requireNonNull(s)) || changed;
        }
        if (changed) {
            logStylesheets();
        }
    }

    /**
     * Adds all given stylesheets to the registry. Duplicates are ignored.
     *
     * @param stylesheets the stylesheets to add, must not be {@code null}
     */
    public static void addAll(Stylesheet... stylesheets) {
        var changed = false;
        for (var s : Objects.requireNonNull(stylesheets)) {
            changed = IconStylesheets.stylesheets.add(Objects.requireNonNull(s)) || changed;
        }
        if (changed) {
            logStylesheets();
        }
    }

    /**
     * Removes the given stylesheet from the registry. Does nothing if not present.
     *
     * @param stylesheet the stylesheet to remove, must not be {@code null}
     */
    public static void remove(Stylesheet stylesheet) {
        if (stylesheets.remove(Objects.requireNonNull(stylesheet))) {
            logStylesheets();
        }
    }

    /**
     * Removes all stylesheets in the given collection from the registry.
     * Stylesheets not present in the registry are ignored.
     *
     * @param stylesheets the stylesheets to remove, must not be {@code null}
     */
    public static void removeAll(Collection<? extends Stylesheet> stylesheets) {
        var changed = false;
        for (var s : Objects.requireNonNull(stylesheets)) {
            changed = IconStylesheets.stylesheets.remove(Objects.requireNonNull(s)) || changed;
        }
        if (changed) {
            logStylesheets();
        }
    }

    /**
     * Removes all given stylesheets from the registry.
     * Stylesheets not present in the registry are ignored.
     *
     * @param stylesheets the stylesheets to remove, must not be {@code null}
     */
    public static void removeAll(Stylesheet... stylesheets) {
        var changed = false;
        for (var s : Objects.requireNonNull(stylesheets)) {
            changed = IconStylesheets.stylesheets.remove(Objects.requireNonNull(s)) || changed;
        }
        if (changed) {
            logStylesheets();
        }
    }

    /**
     * Returns an unmodifiable list of all registered stylesheets in insertion order.
     *
     * @return an unmodifiable list of stylesheets, never {@code null}
     */
    public static @Unmodifiable List<Stylesheet> getAll() {
        return List.copyOf(stylesheets);
    }

    private static void logStylesheets() {
        if (logger.isDebugEnabled()) {
            if (stylesheets.isEmpty()) {
                logger.debug("Icon stylesheets updated. No icon stylesheets registered");
            } else {
                var sb = new StringBuilder();
                sb.append("Icon stylesheets updated. Current icon stylesheets:");
                for (var s : stylesheets) {
                    sb.append(System.lineSeparator());
                    sb.append("    ");
                    sb.append(s.getUrl());
                }
                logger.debug(sb.toString());
            }
        }
    }

    private IconStylesheets() {
        //empty
    }
}
