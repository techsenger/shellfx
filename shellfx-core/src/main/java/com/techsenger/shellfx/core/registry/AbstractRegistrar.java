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

package com.techsenger.shellfx.core.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Base of the registrars of a registry, be it the {@link ControlRegistry} or the {@link SlotRegistry}:
 * it remembers every registration made through {@link #addRegistration(Registration)} and undoes them all in
 * {@link #unregister()}, which also forgets them, so a repeated call does nothing.
 *
 * @param <R> the type of the registry this registrar contributes to
 * @author Pavel Castornii
 */
public abstract class AbstractRegistrar<R extends ExtensionRegistry> implements Registrar {

    private final R registry;

    private final List<Registration> registrations = new ArrayList<>();

    public AbstractRegistrar(R registry) {
        this.registry = registry;
    }

    @Override
    public void unregister() {
        registrations.forEach(r -> r.unregister());
        registrations.clear();
    }

    protected R getRegistry() {
        return registry;
    }

    protected List<Registration> getRegistrations() {
        return registrations;
    }

    protected void addRegistration(Registration reg) {
        this.registrations.add(reg);
    }
}
