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

/**
 * A registry of items that tells its listeners which items have been added and removed.
 *
 * @param <T> the type of the registered items
 * @author Pavel Castornii
 */
public interface ItemRegistry<T> extends Registry {

    /**
     * Adds a listener to be told about every change of the items.
     *
     * @param listener the listener to add
     */
    void addListener(ItemRegistryListener<T> listener);

    /**
     * Removes a listener; does nothing if it was not added.
     *
     * @param listener the listener to remove
     */
    void removeListener(ItemRegistryListener<T> listener);
}
