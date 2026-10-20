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
import com.techsenger.shellfx.material.icon.FontIcon;
import com.techsenger.shellfx.storage.style.IconTextStyle;
import java.net.URI;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * Represents a file or directory entry within a {@link FileStorage}.
 *
 * <p>A {@code StorageFile} is a storage-agnostic abstraction over a file system entry. It carries the metadata
 * provided by the underlying storage backend (name, URI, size, last modified time, entry type) without imposing any
 * assumptions about the nature of the content.
 *
 * <p>Implementations must be obtainable via the factory method of the corresponding {@link FileStorage}. The interface
 * intentionally exposes only getters; mutation, construction, and navigation helpers are the responsibility of concrete
 * implementations.
 *
 * @author Pavel Castornii
 */
public interface StorageFile {

    /**
     * Returns the {@link FileStorage} that owns this file.
     *
     * @return the storage, never {@code null}
     */
    FileStorage<?> getStorage();

    /**
     * Returns the structural type of this file system entry (regular file, directory, link, etc.).
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
     * Returns the size as a property, which the second loading phase may change on the UI thread.
     *
     * @return the property of the size
     */
    ReadOnlyObjectProperty<@Nullable Long> sizeProperty();

    /**
     * Returns the last-modified timestamp of this entry in milliseconds since the Unix epoch
     * (January 1, 1970, 00:00:00 UTC), or {@code null} if the value is unavailable.
     *
     * @return last-modified time in milliseconds since the Unix epoch, or {@code null}
     */
    @Nullable Long getModifiedTime();

    /**
     * Returns the modified time as a property, which the second loading phase may change on the UI thread.
     *
     * @return the property of the modified time
     */
    ReadOnlyObjectProperty<@Nullable Long> modifiedTimeProperty();

    /**
     * Returns the creation timestamp of this entry in milliseconds since the Unix epoch
     * (January 1, 1970, 00:00:00 UTC), or {@code null} if the value is unavailable.
     *
     * <p>Note that creation time is not supported by all file systems. If unavailable,
     * this method returns {@code null}.
     *
     * @return creation time in milliseconds since the Unix epoch, or {@code null}
     */
    @Nullable Long getCreatedTime();

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
     * Returns the icon representing this entry in the UI. The icon is set by whoever creates the file (for example,
     * a file decorator); the storage does not provide one.
     *
     * @return the icon, or {@code null} if the creator of the file has not set one
     */
    @Nullable FontIcon<?> getIcon();

    /**
     * Returns the property of the icon, which changes when the second loading phase rewrites it. It is changed on
     * the UI thread once the file is shown, so the elements that show the file can observe it.
     */
    ReadOnlyObjectProperty<FontIcon<?>> iconProperty();

    /**
     * Returns the styles this entry is shown with. The style is set by whoever creates the file and by the client
     * that restyles it, using the resolver of the storage of the file.
     *
     * @return the style, or {@code null} if the file has none, which clears the styles of the elements that show it
     */
    @Nullable IconTextStyle getStyle();

    /**
     * Returns the property of the style, which changes when the second loading phase or the client rewrites it. It
     * is changed on the UI thread once the file is shown, so the elements that show the file can observe it.
     */
    ReadOnlyObjectProperty<IconTextStyle> styleProperty();

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
     * Returns {@code true} if this entry is a link: a symbolic link on any system, or a directory junction on
     * Windows. It is the link itself that this entry describes; see {@link #getLink()} for what it points to.
     * Code that walks a directory tree must not descend into a link, see {@link FileEntryType#LINK}.
     *
     * @return {@code true} if {@link #getEntryType()} is {@link FileEntryType#LINK}
     */
    default boolean isLink() {
        return getEntryType() == FileEntryType.LINK;
    }

    /**
     * Returns what this link points to, or {@code null} if this entry is not a {@link #isLink() link} or the path
     * the link points to couldn't be determined. A broken link still has a target - its path is known, only
     * {@link EntryLink#getTarget()} is {@code null}.
     *
     * @return the target of the link, or {@code null}
     */
    @Nullable EntryLink getLink();

    /**
     * Returns {@code true} if this entry is a link whose target exists and is a directory, which can be listed and
     * opened like a directory although the link itself is not one. A broken link is not.
     *
     * @return {@code true} if this is a {@link #isLink() link} to a directory
     */
    default boolean isDirectoryLink() {
        var target = getLink();
        var file = target == null ? null : target.getTarget();
        return file != null && file.isDirectory();
    }

    /**
     * Returns {@code true} if this entry represents the root directory of its {@link FileStorage}. The flag is set
     * by whoever creates the file, and it is the storage that creates its root.
     *
     * @return {@code true} if this entry is the root directory of its storage
     */
    boolean isRoot();
}
