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

package com.techsenger.shellfx.core.registries;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.registry.AbstractItemRegistry;
import com.techsenger.shellfx.core.registry.Registration;
import com.techsenger.shellfx.material.style.Stylesheet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of the application-wide stylesheets that every top-level window applies: the default ones, the icon
 * stylesheets of the platform and of plugins. It starts empty, and the application decides what to register. Windows
 * read it when they are created and listen to it while they are open, so stylesheets can be registered and
 * unregistered at any time and from any thread: the windows apply the changes in the JavaFX Application Thread.
 *
 * <p>A stylesheet registered more than once is applied once and stays until all its registrations are undone.
 * Stylesheets are compared with {@link Stylesheet#equals}.
 *
 * @author Pavel Castornii
 */
public final class StylesheetRegistry extends AbstractItemRegistry<Stylesheet> {

    private final class StylesheetRegistration implements Registration {

        private final List<Stylesheet> stylesheets;

        StylesheetRegistration(List<Stylesheet> stylesheets) {
            this.stylesheets = stylesheets;
        }

        @Override
        public void unregister() {
            update(() -> registrations.remove(this));
        }
    }

    private final List<StylesheetRegistration> registrations = new CopyOnWriteArrayList<>();

    /**
     * Registers a stylesheet.
     *
     * @param stylesheet the stylesheet, never {@code null}
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public Registration register(Stylesheet stylesheet) {
        return register(List.of(Objects.requireNonNull(stylesheet)));
    }

    /**
     * Registers stylesheets as one contribution, undone as a whole.
     *
     * @param stylesheets the stylesheets, never {@code null}
     * @return a {@link Registration} that can be used to undo this contribution
     */
    public Registration register(List<Stylesheet> stylesheets) {
        var registration = new StylesheetRegistration(List.copyOf(Objects.requireNonNull(stylesheets)));
        // the listeners are told after the registration is undoable, so a listener may undo it
        update(() -> registrations.add(registration));
        return registration;
    }

    /**
     * Returns the registered stylesheets in the order of registration, each of them once.
     *
     * @return an unmodifiable snapshot of the registry, never {@code null}
     */
    public @Unmodifiable List<Stylesheet> getStylesheets() {
        var result = new LinkedHashSet<Stylesheet>();
        for (var registration : registrations) {
            result.addAll(registration.stylesheets);
        }
        return List.copyOf(result);
    }

    /**
     * Applies a change of the registrations and tells the listeners which stylesheets it has added to and removed from
     * {@link #getStylesheets()}, so a stylesheet that another registration still holds is neither added nor removed.
     */
    private void update(Runnable change) {
        var before = getStylesheets();
        change.run();
        var after = getStylesheets();
        notifyChanged(after.stream().filter(s -> !before.contains(s)).toList(),
                before.stream().filter(s -> !after.contains(s)).toList());
    }
}
