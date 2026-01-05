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

package com.techsenger.tabshell.core.history;

import com.techsenger.patternfx.core.History;
import com.techsenger.toolkit.core.function.Factory;
import java.util.UUID;

/**
 * {@link HistoryManager} is interface and there can be different implementations. For example, it is possible to use
 * different {@link HistoryFile} when one file is used for one module layer.
 *
 * @author Pavel Castornii
 */
public interface HistoryManager {

    /**
     * Retrieves the global history object associated with the specified class. This is used for components that
     * share a single history across all component instances.
     *
     * @param <T> the type of history to retrieve
     * @param historyClass the class of the history to retrieve
     * @return the history object for the given class, or null if no history exists
     */
    <T extends History> T getHistory(Class<T> historyClass);

    /**
     * Retrieves the global history object associated with the specified class, creating and storing a new one
     * if it does not already exist. This is useful for components that require a shared history instance
     * but should automatically create it on first access.
     *
     * @param <T> the type of history to retrieve
     * @param historyClass the class of the history to retrieve
     * @param factory the factory used to create a new instance if no history exists
     * @return the existing or newly created history object
     */
    <T extends History> T getOrCreateHistory(Class<T> historyClass, Factory<T> factory);

    /**
     * Stores a global history object for the specified class. This is used for components that share a single
     * history across all component instances.
     *
     * @param <T> the type of history being stored
     * @param historyClass the class to associate with the history
     * @param history the history object to store
     */
    <T extends History> void putHistory(Class<T> historyClass, T history);

    /**
     * Removes and returns the global history object associated with the specified class. This is used for components
     * that share a single history across all component instances and need to clear or replace it.
     *
     * @param <T> the type of history to remove
     * @param historyClass the class of the history to remove
     * @return the removed history object for the given class, or {@code null} if no history was associated
     */
    <T extends History> T removeHistory(Class<T> historyClass);

    /**
     * Retrieves the history for a specific component instance by its UUID. This is used for components that require
     * unique history per instance.
     *
     * @param uuid the unique identifier of the component instance
     * @return the history object for the specified instance, or null if not found
     */
    History getHistory(UUID uuid);

    /**
     * Retrieves the history associated with a specific component instance identified by its UUID, creating and storing
     * a new one if it does not already exist. This method is used for components that require a dedicated history per
     * instance and should automatically create it on first access.
     *
     * @param uuid the unique identifier of the component instance
     * @param factory the factory used to create a new {@code ComponentHistory} instance if none exists
     * @return the existing or newly created history object associated with the given UUID
     */
    History getOrCreateHistory(UUID uuid, Factory<? extends History> factory);

    /**
     * Stores history for a specific component instance identified by its UUID. This is used for components that
     * require unique history per instance.
     *
     * @param uuid the unique identifier of the component instance
     * @param history the history object to store for this instance
     */
    void putHistory(UUID uuid, History history);

    /**
     * Removes and returns the history associated with a specific component instance identified by its UUID.
     * This is used for components that require unique history per instance and need to clear or replace it.
     *
     * @param uuid the unique identifier of the component instance
     * @return the removed history object for the specified instance, or {@code null} if no history was associated
     */
    History removeHistory(UUID uuid);
}
