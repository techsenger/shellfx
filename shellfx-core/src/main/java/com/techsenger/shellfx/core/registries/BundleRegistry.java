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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.base.bundle.BundleName;
import com.techsenger.shellfx.base.registry.AbstractItemRegistry;
import com.techsenger.shellfx.base.registry.Registration;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of the bundles of texts. A module loads its own bundle, which only it can reach, and registers it under a
 * {@link BundleName}; controls and other modules then get the bundle by its name without knowing where it comes
 * from. The language is the one the bundles have been loaded for, so a text does not change while the application
 * runs.
 *
 * <p>The registry tells its listeners which bundles have been added and removed. It can be changed from any thread.
 *
 * @author Pavel Castornii
 */
public final class BundleRegistry extends AbstractItemRegistry<BundleName> {

    private final Map<BundleName, ResourceBundle> bundlesByName = new ConcurrentHashMap<>();

    /**
     * Registers a bundle.
     *
     * @param name the name of the bundle, unique in the application
     * @param bundle the bundle, already loaded for the language of the application
     * @return a {@link Registration} that can be used to undo this registration
     * @throws IllegalStateException if a bundle with the same name is already registered
     */
    public Registration register(BundleName name, ResourceBundle bundle) {
        if (bundlesByName.putIfAbsent(name, bundle) != null) {
            throw new IllegalStateException("Bundle is already registered: " + name);
        }
        // the listeners are told after the registration is undoable, so a listener may undo it
        var registration = (Registration) () -> {
            if (bundlesByName.remove(name, bundle)) {
                notifyChanged(List.of(), List.of(name));
            }
        };
        notifyChanged(List.of(name), List.of());
        return registration;
    }

    /**
     * Returns the bundle registered under a name.
     *
     * @param name the name of the bundle
     * @return the bundle or {@code null} if there is none, for example because its module has been unloaded
     */
    public @Nullable ResourceBundle getBundle(BundleName name) {
        return bundlesByName.get(name);
    }
}
