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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link GenericFile}.
 *
 * <p>Instances are created by {@link FileStorage} implementations via their factory methods. This class is designed to
be subclassed when additional application-level metadata needs to be attached to a file entry (e.g. a semantic
content entryType for UI purposes).

<p>Navigation helpers ({@link #getParent()}, {@link #getParents()}, {@link #getChild}) construct virtual entries
 * derived from this file's URI and storage without performing any I/O.
 *
 * @author Pavel Castornii
 */
public class DefaultGenericFile implements GenericFile {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGenericFile.class);

    private FileStorage storage;

    private FileEntryType entryType;

    private URI uri;

    private @Nullable Long size;

    private String name;

    private @Nullable Long lastModified;

    private boolean hidden;

    private boolean virtual;

    /**
     * Constructs an empty {@code DefaultGenericFile}. Fields should be populated by the
     * {@link FileStorage} factory method immediately after construction.
     */
    public DefaultGenericFile() {
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
    public @Nullable Long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean isVirtual() {
        return virtual;
    }

    /**
     * Returns the immediate parent directory of this file, or the root directory if this file is
     * a direct child of the root.
     *
     * @return the parent entry, never {@code null}
     */
    public DefaultGenericFile getParent() {
        return buildParents(1).get(0);
    }

    /**
     * Returns all parent directories from the immediate parent up to and including the root.
     *
     * <p>Example: for a file with URI {@code /home/user/foo/bar}, returns:
     * <pre>
     * /home/user/foo/
     * /home/user/
     * /home/
     * /  (root)
     * </pre>
     *
     * @return ordered list of parents from immediate parent to root, never {@code null}, never empty
     */
    public List<DefaultGenericFile> getParents() {
        return buildParents(Integer.MAX_VALUE);
    }

    /**
     * Creates a virtual child entry of this directory with the given name and entry type.
     *
     * <p>The child URI is resolved by appending {@code childName} to this entry's URI.
     * The returned entry is always {@link GenericFile#isVirtual() virtual}.
     *
     * @param childName the name of the child entry
     * @param childEntryType the structural entryType of the child entry
     * @return a new virtual child entry, never {@code null}
     */
    public DefaultGenericFile getChild(String childName, FileEntryType childEntryType) {
        var child = new DefaultGenericFile();
        child.storage = this.storage;
        child.entryType = childEntryType;
        child.name = childName;
        child.uri = UriUtils.resolvePath(this.uri, childName);
        child.virtual = true;
        return child;
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
        final DefaultGenericFile other = (DefaultGenericFile) obj;
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
        return "DefaultGenericFile[" + "storage=" + storage + ", entryType=" + entryType + ", uri=" + uri
                + ", size=" + size + ", name=" + name + ", lastModified=" + lastModified + ", hidden=" + hidden
                + ", virtual=" + virtual + ']';
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
     * @param lastModified milliseconds since the Unix epoch, or {@code null} if unavailable
     */
    protected void setLastModified(@Nullable Long lastModified) {
        this.lastModified = lastModified;
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

    private List<DefaultGenericFile> buildParents(int limit) {
        var rootUri = storage.getUri();
        var segments = UriUtils.getPathSegments(rootUri, this.uri);
        var parents = new ArrayList<DefaultGenericFile>(Math.min(segments.size(), limit));
        for (int i = segments.size() - 1; i >= 1 && parents.size() < limit; i--) {
            var parentUri = UriUtils.resolvePath(rootUri, String.join("/", segments.subList(0, i)));
            var parent = new DefaultGenericFile();
            parent.storage = this.storage;
            parent.entryType = FileEntryType.DIRECTORY;
            parent.name = segments.get(i - 1);
            parent.uri = parentUri;
            parent.virtual = true;
            parents.add(parent);
        }
        if (parents.size() < limit) {
            parents.add((DefaultGenericFile) storage.getRootDirectory());
        }
        return parents;
    }
}
