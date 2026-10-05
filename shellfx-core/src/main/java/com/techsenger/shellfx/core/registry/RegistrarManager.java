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

import java.util.List;

/**
 * Manages the registrars of a plugin as a whole: {@link #registerAll()} registers them in the given order, and
 * {@link #unregisterAll()} withdraws them in the reverse order.
 *
 * @author Pavel Castornii
 */
public final class RegistrarManager {

    private final List<Registrar> registrars;

    /**
     * Creates a manager of the registrars.
     *
     * @param registrars the registrars in the order they are registered in, the slot registrars first
     */
    public RegistrarManager(List<? extends Registrar> registrars) {
        this.registrars = List.copyOf(registrars);
    }

    /**
     * Registers all the registrars in their order. If one of them fails, everything registered so far is
     * withdrawn before the failure is rethrown.
     */
    public void registerAll() {
        try {
            registrars.forEach(Registrar::register);
        } catch (RuntimeException ex) {
            unregisterAll();
            throw ex;
        }
    }

    /**
     * Withdraws all the registrars in the reverse order of their registration.
     */
    public void unregisterAll() {
        registrars.reversed().forEach(Registrar::unregister);
    }
}
