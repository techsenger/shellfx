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

package com.techsenger.tabshell.core.registry;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractControlRegistrar implements ControlRegistrar {

    private final ControlRegistry registry;

    private final List<ControlRegistry.Registration> registrations = new ArrayList<>();

    public AbstractControlRegistrar(ControlRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void unregister() {
        registrations.forEach(r -> r.unregister());
    }

    protected ControlRegistry getRegistry() {
        return registry;
    }

    protected List<ControlRegistry.Registration> getRegistrations() {
        return registrations;
    }

    protected void addRegistration(ControlRegistry.Registration reg) {
        this.registrations.add(reg);
    }
}
