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

import com.techsenger.annotations.Nullable;
import java.net.URI;
import java.util.Objects;

/**
 * Represents a file or directory entry within a {@link FileStorage}.
 *
 * <p>A {@code GenericFile} is a storage-agnostic abstraction over a file system entry. It carries the metadata
 * provided by the underlying storage backend (name, URI, size, last modified time, entry type) without imposing any
 * assumptions about the nature of the content.
 *
 * <p>Implementations must be obtainable via the factory method of the corresponding {@link FileStorage}. The interface
 * intentionally exposes only getters; mutation, construction, and navigation helpers are the responsibility of concrete
 * implementations.
 *
 * @author Pavel Castornii
 */
public interface GenericFile {

    /**
     * Returns the {@link FileStorage} that owns this file.
     *
     * @return the storage, never {@code null}
     */
    FileStorage<?> getStorage();

    /**
     * Returns the structural type of this file system entry (regular file, directory, symbolic link, etc.).
     *
     * @return the entry type, never {@code null}
     */
    FileEntryType getEntryType();

    /**
     * Returns the name of this entry, i.e. the last segment of its path.
     *
     * <p>For the root directory the name is implementation-defined (typically an empty string or the storage label).
     *
     * @return the entry name, never {@code null}
     */
    String getName();

    /**
     * Returns the URI that uniquely identifies this entry within its {@link FileStorage}.
     *
     * @return the URI, never {@code null}
     */
    URI getUri();

    /**
     * Returns the size of this entry in bytes, or {@code null} if the size is unknown or not
     * applicable (e.g. for directories).
     *
     * @return size in bytes, or {@code null}
     */
    @Nullable Long getSize();

    /**
     * Returns the last-modified timestamp of this entry in milliseconds since the Unix epoch
     * (January 1, 1970, 00:00:00 UTC), or {@code null} if the value is unavailable.
     *
     * @return last-modified time in milliseconds since the Unix epoch, or {@code null}
     */
    @Nullable Long getLastModified();

    /**
     * Returns {@code true} if this entry is hidden, and {@code false} otherwise.
     */
    boolean isHidden();

    /**
     * Returns {@code true} if this entry is virtual, i.e. it was constructed programmatically without a corresponding
     * real entry on the underlying storage. As a result, virtual entries generally do not have all metadata
     * (such as size, hidden status, etc.).
     *
     * <p>Virtual entries are used as placeholders — for example, a parent directory inferred from a child's URI, or a
     * root entry that does not physically exist on the backend.
     *
     * @return {@code true} if virtual, {@code false} if backed by a real storage entry
     */
    boolean isVirtual();

    /**
     * Returns {@code true} if this entry is a directory.
     *
     * @return {@code true} if {@link #getEntryType()} is {@link FileEntryType#DIRECTORY}
     */
    default boolean isDirectory() {
        return getEntryType() == FileEntryType.DIRECTORY;
    }

    /**
     * Returns {@code true} if this entry is a regular file.
     *
     * @return {@code true} if {@link #getEntryType()} is {@link FileEntryType#FILE}
     */
    default boolean isFile() {
        return getEntryType() == FileEntryType.FILE;
    }

    /**
     * Returns {@code true} if this entry is a symbolic link.
     *
     * @return {@code true} if {@link #getEntryType()} is {@link FileEntryType#SYMBOLIC_LINK}
     */
    default boolean isSymbolicLink() {
        return getEntryType() == FileEntryType.SYMBOLIC_LINK;
    }

    /**
     * Returns {@code true} if this entry represents the root directory of its {@link FileStorage}.
     *
     * @return {@code true} if this entry is a directory whose URI equals the storage root URI
     */
    default boolean isRoot() {
        return isDirectory() && Objects.equals(getUri(), getStorage().getRootUri());
    }
}
