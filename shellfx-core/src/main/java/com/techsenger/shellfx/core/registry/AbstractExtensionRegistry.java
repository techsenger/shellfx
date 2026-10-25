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
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Base of the extension registries: keeps the identifier of the state. A subclass calls {@link #renewState()} after
 * every change of what it holds.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractExtensionRegistry implements ExtensionRegistry {

    private final ReadOnlyObjectWrapper<UUID> stateUuid = new ReadOnlyObjectWrapper<>(UUID.randomUUID());

    @Override
    public UUID getStateUuid() {
        return stateUuid.get();
    }

    @Override
    public ReadOnlyObjectProperty<UUID> stateUuidProperty() {
        return stateUuid.getReadOnlyProperty();
    }

    /**
     * Replaces the identifier of the state with a new one, so the listeners of the property are notified. It must be
     * called in the JavaFX Application Thread.
     */
    protected void renewState() {
        stateUuid.set(UUID.randomUUID());
    }
}
