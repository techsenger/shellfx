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

import java.net.URI;
import java.util.Optional;

/**
 * Finds the storage that holds a URI, so a storage can reach a file that lies in another one, e.g. the target of a
 * link.
 *
 * @param <T> the type of the files the storages produce
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface StorageFinder<T extends StorageFile> {

    /**
     * Finds the storage that holds the URI.
     *
     * @param uri the URI to look for
     * @return the storage, or empty if no storage known to the finder holds it
     */
    Optional<? extends FileStorage<T>> find(URI uri);
}
