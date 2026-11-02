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
 * Told that the registrations of an extension registry have changed. The registry is not synchronized and promises
 * nothing about the thread: the listener is called in the thread that changed the registry.
 *
 * @param <T> the type of the registered items
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface ItemRegistryListener<T> {

    /**
     * Called once per change of the registrations, which may add and remove several items at once.
     *
     * @param added the items that have been added, empty if none, never {@code null}
     * @param removed the items that have been removed, empty if none, never {@code null}
     */
    void changed(List<T> added, List<T> removed);
}
