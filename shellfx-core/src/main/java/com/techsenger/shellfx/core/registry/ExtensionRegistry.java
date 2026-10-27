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

import java.util.UUID;

/**
 * A registry of the contributions of plugins to the components, which can change at any time. It is not synchronized
 * and promises nothing about threads: its listeners are called in the thread that changed it.
 *
 * @author Pavel Castornii
 */
public interface ExtensionRegistry {

    /**
     * Returns an identifier of the current state of the registrations that is replaced with a new one on every
     * change: a registration added or removed. Only a different value matters, so it is compared with the one seen
     * before.
     *
     * @return the identifier of the state, never {@code null}
     */
    UUID getStateUuid();

    /**
     * Adds a listener to be told about every change of the state.
     *
     * @param listener the listener to add
     */
    void addListener(RegistryStateListener listener);

    /**
     * Removes a listener; does nothing if it was not added.
     *
     * @param listener the listener to remove
     */
    void removeListener(RegistryStateListener listener);
}
