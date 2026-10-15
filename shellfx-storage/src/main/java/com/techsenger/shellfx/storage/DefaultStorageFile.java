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
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Default implementation of {@link StorageFile}.
 *
 * <p>Instances are created by {@link FileStorage} implementations via their factory methods. This class is designed to
 * be subclassed when additional application-level metadata needs to be attached to a file entry (e.g. a semantic
 * content entryType for UI purposes).
 *
 * @author Pavel Castornii
 */
public class DefaultStorageFile implements StorageFile {

    private FileStorage storage;

    private FileEntryType entryType;

    private URI uri;

    private @Nullable Long size;

    private String name;

    private @Nullable Long modifiedTime;

    private @Nullable Long createdTime;

    private boolean hidden;

    private boolean virtual;

    private boolean root;

    private @Nullable EntryLink link;

    private final ReadOnlyObjectWrapper<FontIcon<?>> icon = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<IconTextStyle> style = new ReadOnlyObjectWrapper<>();

    // written by the second loading phase on a background thread
    private volatile FileState state = FileState.READY;

    /**
     * Constructs an empty {@code DefaultStorageFile}. Fields should be populated by the
     * {@link FileStorage} factory method immediately after construction.
     */
    public DefaultStorageFile() {
    }

    @Override
    public FileStorage<?> getStorage() {
        return storage;
    }

    @Override
    public FileEntryType getEntryType() {
        return entryType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public @Nullable Long getSize() {
        return size;
    }

    @Override
    public @Nullable Long getModifiedTime() {
        return modifiedTime;
    }

    public @Nullable Long getCreatedTime() {
        return createdTime;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean isVirtual() {
        return virtual;
    }

    @Override
    public boolean isRoot() {
        return root;
    }

    @Override
    public @Nullable EntryLink getLink() {
        return link;
    }

    @Override
    public @Nullable FontIcon<?> getIcon() {
        return icon.get();
    }

    @Override
    public ReadOnlyObjectProperty<FontIcon<?>> iconProperty() {
        return icon.getReadOnlyProperty();
    }

    @Override
    public @Nullable IconTextStyle getStyle() {
        return style.get();
    }

    @Override
    public ReadOnlyObjectProperty<IconTextStyle> styleProperty() {
        return style.getReadOnlyProperty();
    }

    @Override
    public FileState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.storage);
        hash = 29 * hash + Objects.hashCode(this.entryType);
        hash = 29 * hash + Objects.hashCode(this.uri);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultStorageFile other = (DefaultStorageFile) obj;
        if (!Objects.equals(this.storage, other.storage)) {
            return false;
        }
        if (this.entryType != other.entryType) {
            return false;
        }
        return Objects.equals(this.uri, other.uri);
    }

    @Override
    public String toString() {
        return "DefaultStorageFile[" + "storage=" + storage + ", entryType=" + entryType + ", uri=" + uri
                + ", size=" + size + ", name=" + name + ", modifiedTime=" + modifiedTime
                + ", createdTime=" + createdTime + ", hidden=" + hidden + ", virtual=" + virtual + ']';
    }

    /**
     * Sets the storage that owns this entry.
     *
     * @param storage the storage, must not be {@code null}
     */
    protected void setStorage(FileStorage<?> storage) {
        this.storage = storage;
    }

    /**
     * Sets the structural entry entryType.
     *
     * @param type the entry entryType, must not be {@code null}
     */
    protected void setEntryType(FileEntryType type) {
        this.entryType = type;
    }

    /**
     * Sets the name of this entry.
     *
     * @param name the entry name, must not be {@code null}
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the URI of this entry.
     *
     * @param uri the URI, must not be {@code null}
     */
    protected void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Sets the size of this entry in bytes.
     *
     * @param size size in bytes, or {@code null} if not applicable
     */
    protected void setSize(@Nullable Long size) {
        this.size = size;
    }

    /**
     * Sets the last-modified timestamp in milliseconds since the Unix epoch.
     *
     * @param modifiedTime milliseconds since the Unix epoch, or {@code null} if unavailable
     */
    protected void setModifiedTime(@Nullable Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    /**
     * Sets the creation timestamp of this entry in milliseconds since the Unix epoch (January 1, 1970, 00:00:00 UTC).
     *
     * @param createdTime creation time in milliseconds since the Unix epoch,
     *        or {@code null} if the value is unavailable
     */
    protected void setCreatedTime(@Nullable Long createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * Sets whether this entry is hidden.
     *
     * @param hidden {@code true} if this entry is hidden
     */
    protected void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Sets whether this entry is virtual.
     *
     * @param virtual {@code true} if this entry has no real backing on the storage
     */
    protected void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    /**
     * Sets whether this entry is the root directory of its storage.
     *
     * @param root {@code true} if this entry is the root directory
     */
    protected void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * Sets what this entry points to.
     *
     * @param link the target of a link entry, or {@code null} if this is not a link or the path it points
     *     to is unknown
     */
    protected void setLink(@Nullable EntryLink link) {
        this.link = link;
    }

    protected void setIcon(@Nullable FontIcon<?> icon) {
        this.icon.set(icon);
    }

    /**
     * Sets the style this file is shown with.
     *
     * @param style the style, or {@code null} to clear it
     */
    protected void setStyle(@Nullable IconTextStyle style) {
        this.style.set(style);
    }

    protected void setState(FileState state) {
        this.state = state;
    }
}
