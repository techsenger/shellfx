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
 * Told that the state of an extension registry has changed. The registry is not synchronized and promises nothing
 * about the thread: the listener is called in the thread that changed the registry.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface RegistryStateListener {

    /**
     * Called once per change of the registrations. It does not say what changed: the listener reads the current state
     * of the registry it is added to.
     *
     * @param stateUuid the identifier of the state the listener is told about; if the registry has a different one
     *     by the time the listener reads it, the state has changed again and must be re-read
     */
    void stateChanged(UUID stateUuid);
}
