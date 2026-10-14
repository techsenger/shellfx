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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

/**
 * Resolves a file with the first of the storages that holds its URI. The storages are asked at every resolution, so
 * storages added later are found too.
 *
 * @param <T> the type of the files
 * @author Pavel Castornii
 */
public final class DefaultFileResolver<T extends StorageFile> implements FileResolver<T> {

    private final Supplier<? extends List<? extends FileStorage<T>>> storages;

    /**
     * Creates a resolver over the storages.
     *
     * @param storages gives the storages to search, must not be {@code null}
     */
    public DefaultFileResolver(Supplier<? extends List<? extends FileStorage<T>>> storages) {
        this.storages = storages;
    }

    @Override
    public T resolve(URI uri, OperationType operationType) throws IOException {
        var storage = FileStorageUtils.findByUri(storages.get(), uri);
        if (storage.isEmpty()) {
            throw new IOException("No storage holds " + uri);
        }
        return storage.get().getFile(uri, operationType);
    }
}
