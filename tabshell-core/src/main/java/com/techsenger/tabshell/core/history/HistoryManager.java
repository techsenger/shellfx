/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.mvvm4fx.core.ComponentHistory;
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
    <T extends ComponentHistory> T getHistory(Class<T> historyClass);

    /**
     * Stores a global history object for the specified class. This is used for components that share a single
     * history across all component instances.
     *
     * @param <T> the type of history being stored
     * @param historyClass the class to associate with the history
     * @param history the history object to store
     */
    <T extends ComponentHistory> void putHistory(Class<T> historyClass, T history);

    /**
     * Retrieves the history for a specific component instance by its UUID. This is used for components that require
     * unique history per instance.
     *
     * @param uuid the unique identifier of the component instance
     * @return the history object for the specified instance, or null if not found
     */
    ComponentHistory getHistory(UUID uuid);

    /**
     * Stores history for a specific component instance identified by its UUID. This is used for components that
     * require unique history per instance.
     *
     * @param uuid the unique identifier of the component instance
     * @param history the history object to store for this instance
     */
    void putHistory(UUID uuid, ComponentHistory history);
}
