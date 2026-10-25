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
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * A registry of the contributions of plugins to the components, which can change at any time. The identifier of its
 * state is changed and read in the JavaFX Application Thread only.
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

    ReadOnlyObjectProperty<UUID> stateUuidProperty();
}
