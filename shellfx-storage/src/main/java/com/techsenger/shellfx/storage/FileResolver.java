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
import java.nio.file.NoSuchFileException;

/**
 * Gives a file that may lie in any storage, so a storage or a component that holds a file of another storage (the
 * target of a link, a recently used file) can reach it without knowing the other storages.
 *
 * @param <T> the type of the files
 * @author Pavel Castornii
 */
public interface FileResolver<T extends StorageFile> {

    /**
     * Resolves the file at the URI with the storage that holds it.
     *
     * @param uri the URI of the file
     * @param operationType how the storage classifies the operation it reports for the file
     * @return the file, never {@code null}
     * @throws NoSuchFileException if the storage holds the URI, but there is no file at it
     * @throws IOException if no storage holds the URI, or the storage can't give the file
     */
    T resolve(URI uri, OperationType operationType) throws NoSuchFileException, IOException;
}
