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
 * A registry of available {@link FileStorage} instances.
 *
 * <p>The registry manages two categories of storages:
 * <ul>
 *   <li><b>Default storages</b> — discovered automatically from the operating system (e.g. local
 *       drives, mount points). Refreshed on demand via {@link #refreshDefaultStorages()}.</li>
 *   <li><b>Custom storages</b> — registered programmatically at runtime (e.g. FTP servers, cloud
 *       drives). Added and removed via {@link #registerCustomStorage(FileStorage)} and
 *       {@link Registration#unregister()}.</li>
 * </ul>
 *
 * @param <T> the concrete file entry type produced by the storages in this registry
 * @author Pavel Castornii
 */
public interface FileStorageRegistry<T extends GenericFile> {

    /**
     * A handle representing a single custom storage registration.
     *
     * <p>The caller that registers a storage is responsible for unregistering it when it is no
     * longer needed by calling {@link #unregister()}.
     */
    interface Registration {

        /**
         * Removes the associated storage from the registry. Has no effect if the storage has
         * already been unregistered.
         */
        void unregister();
    }

    /**
     * Forces rediscovery of default storages available on the current machine.
     *
     * <p>Implementations are permitted to cache the discovered storages between calls. Invoke
     * this method before reading default or all storages whenever up-to-date information is
     * required (e.g. after a drive is mounted or unmounted).
     */
    void refreshDefaultStorages();

    /**
     * Returns all default storages discovered from the operating system.
     *
     * <p>The returned list reflects the last completed discovery. Call
     * {@link #refreshDefaultStorages()} first if up-to-date information is required.
     *
     * @return an unmodifiable list of default storages, never {@code null}, may be empty
     */
    @Unmodifiable List<FileStorage<T>> getDefaultStorages();

    /**
     * Returns the primary default storage, defined as the first storage whose type is
     * {@link FileStorageType#BASE}.
     *
     * <p>Call {@link #refreshDefaultStorages()} first if up-to-date information is required.
     *
     * @return an {@link Optional} containing the primary storage, or empty if none exists
     */
    Optional<FileStorage<T>> getPrimaryStorage();

    /**
     * Returns all custom storages currently registered in this registry.
     *
     * <p>Custom storages are registered programmatically and are not discovered from the
     * operating system.
     *
     * @return an unmodifiable list of custom storages, never {@code null}, may be empty
     */
    @Unmodifiable List<FileStorage<T>> getCustomStorages();

    /**
     * Registers a custom storage with this registry.
     *
     * @param storage the storage to register, must not be {@code null}
     * @return a {@link Registration} handle that can be used to remove the storage from the
     *         registry; never {@code null}
     */
    Registration registerCustomStorage(FileStorage<T> storage);

    /**
     * Returns all available storages, combining both default and custom storages.
     *
     * <p>Call {@link #refreshDefaultStorages()} first if up-to-date information about default
     * storages is required.
     *
     * @return an unmodifiable list of all storages, never {@code null}, may be empty
     */
    @Unmodifiable List<FileStorage<T>> getAllStorages();

    /**
     * Returns the storage responsible for the given URI.
     *
     * <p>A storage is considered responsible for a URI if {@link FileStorage#refersToStorage(URI)}
     * returns {@code true} for that URI.
     *
     * @param uri the URI to resolve, must not be {@code null}
     * @return an {@link Optional} containing the matching storage, or empty if no storage matches
     */
    Optional<FileStorage<T>> getStorage(URI uri);
}
