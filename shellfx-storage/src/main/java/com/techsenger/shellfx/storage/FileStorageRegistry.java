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

package com.techsenger.shellfx.storage;

import com.techsenger.annotations.Unmodifiable;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * A registry of available {@link FileStorage file storages}.
 *
 * <p>The registry manages both: default storages discovered from the operating system, and custom storages registered
 * at runtime. Custom storages may be added and removed dynamically using registrations.
 *
 * @author Pavel Castornii
 */
public interface FileStorageRegistry {
    /**
     * Represents a handle for a registered contribution.
     *
     * <p>The holder of this handle is responsible for unregistering the associated contribution.
     */
    interface Registration {
        /**
         * Removes the associated contribution from the registry.
         */
        void unregister();
    }

    /**
     * Forces rediscovery of default storages available on the current machine.
     *
     * <p>Call this method before reading default or all storages when up-to-date information is required.
     * Implementations may otherwise return a cached result.
     */
    void refreshDefaultStorages();

    /**
     * Returns all default file storages available on the current machine.
     *
     * <p>Implementations may cache the discovered storages between calls. Call {@link #refreshDefaultStorages()}
     * first to ensure the list is up to date.
     *
     * @return an unmodifiable list of default storages
     */
    @Unmodifiable List<FileStorage> getDefaultStorages();

    /**
     * Returns the primary default storage.
     *
     * <p>The primary storage is the first storage with {@link FileStorageType#BASE} type.
     * Call {@link #refreshDefaultStorages()} first to ensure the result is up to date.
     *
     * @return an optional containing the primary storage, or an empty optional if no primary storage exists
     */
    Optional<FileStorage> getPrimaryStorage();

    /**
     * Returns all registered custom storages.
     *
     * <p>Custom storages are registered at runtime and are not discovered from the operating system.
     *
     * @return an unmodifiable list of custom storages
     */
    @Unmodifiable List<FileStorage> getCustomStorages();

    /**
     * Registers a custom storage.
     *
     * @param storage the storage to register
     * @return a registration handle that can be used to unregister the storage
     */
    Registration registerCustomStorage(FileStorage storage);

    /**
     * Returns all available storages, combining both default and custom storages.
     *
     * <p>Call {@link #refreshDefaultStorages()} first to ensure default storages are up to date.
     *
     * @return an unmodifiable list of all storages
     */
    @Unmodifiable List<FileStorage> getAllStorages();

    /**
     * Returns the storage responsible for the given URI.
     *
     * @param uri the URI to resolve
     * @return an optional containing the matching storage, or an empty optional if no storage matches the URI
     */
    Optional<FileStorage> getStorage(URI uri);
}
