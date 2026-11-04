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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base of the item registries: keeps the listeners. A subclass calls {@link #notifyChanged(List, List)}
 * after every change of what it holds.
 *
 * @param <T> the type of the registered items
 * @author Pavel Castornii
 */
public abstract class AbstractItemRegistry<T> implements ItemRegistry<T> {

    private final List<ItemRegistryListener<T>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(ItemRegistryListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ItemRegistryListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Tells the listeners, in the calling thread, what has been added and removed; does nothing if both are empty.
     *
     * @param added the items that have been added, may be empty; must be unmodifiable
     * @param removed the items that have been removed, may be empty; must be unmodifiable
     */
    protected void notifyChanged(List<T> added, List<T> removed) {
        if (added.isEmpty() && removed.isEmpty()) {
            return;
        }
        for (var listener : listeners) {
            listener.changed(added, removed);
        }
    }
}
