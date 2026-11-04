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

package com.techsenger.shellfx.base.registry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base of the state registries: keeps the identifier of the state and the listeners. A subclass calls
 * {@link #renewState()} after every change of what it holds.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractStateRegistry implements StateRegistry {

    private final List<StateRegistryListener> listeners = new CopyOnWriteArrayList<>();

    private volatile UUID stateUuid = UUID.randomUUID();

    @Override
    public UUID getStateUuid() {
        return stateUuid;
    }

    @Override
    public void addListener(StateRegistryListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(StateRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Replaces the identifier of the state with a new one and tells the listeners, in the calling thread.
     */
    protected void renewState() {
        var newUuid = UUID.randomUUID();
        stateUuid = newUuid;
        for (var listener : listeners) {
            listener.changed(newUuid);
        }
    }
}
