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

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Files are always immutable.
 *
 * @author Pavel Castornii
 */
public final class GenericFile {

    /**
     * A reusable builder.
     */
    public static class Builder {

        private FileStorage storage;

        private FileType type;

        private URI uri;

        private Long size;

        private String name;

        private Long lastModified;

        private boolean virtual;

        public Builder() {

        }

        public Builder storage(FileStorage storage) {
            this.storage = storage;
            return this;
        }

        public Builder type(FileType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        public Builder lastModified(Long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder virtual(boolean virtual) {
            this.virtual = virtual;
            return this;
        }

        public GenericFile build() {
            var file = new GenericFile(this);
            return file;
        }

        /**
         * Use this method to create a copy with or without modifications.
         *
         * @param file
         */
        public Builder setAllFrom(GenericFile file) {
            this.storage = file.storage;
            this.type = file.type;
            this.uri = file.uri;
            this.size = file.size;
            this.name = file.name;
            this.lastModified = file.lastModified;
            this.virtual = file.virtual;
            return this;
        }

        public Builder reset() {
            this.storage = null;
            this.type = null;
            this.uri = null;
            this.size = null;
            this.name = null;
            this.lastModified = null;
            this.virtual = false;
            return this;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(GenericFile.class);

    /**
     * Returns the immediate parent of the given file, or the root directory if the file is a direct child of the root.
     *
     * @param child the file whose parent to retrieve
     * @return the parent {@link GenericFile}, never null
     */
    public static GenericFile getParent(GenericFile child) {
        return buildParents(child, 1).get(0);
    }

    /**
     * Returns all parent directories from the immediate parent up to and including the root.
     *
     * <p>Example: for a file with URI {@code /home/user/foo/bar}, returns:
     * <pre>
     * /home/user/foo/
     * /home/use/
     * /home/
     * /  (root)
     * </pre>
     *
     * @param child the file whose parents to collect
     * @return ordered list of parents from immediate parent to root, never null, never empty
     */
    public static List<GenericFile> getParents(GenericFile child) {
        return buildParents(child, Integer.MAX_VALUE);
    }

    /**
     * Creates a virtual child file.
     *
     * @param parent
     * @param childName
     * @param childType
     * @return
     */
    public static GenericFile getChild(GenericFile parent, String childName, FileType childType) {
        var builder = new Builder();
        builder.storage(parent.storage);
        builder.type(childType);
        builder.name(childName);
        var uri = UriUtils.resolvePath(parent.getUri(), childName);
        builder.uri(uri);
        builder.virtual(true);
        var result = builder.build();
        return result;
    }

    /**
     * Converts a local file to GenericFile.
     *
     * @param file
     * @param storages
     * @return
     */
    public static GenericFile convert(File file, FileStorageRegistry registry) throws InvalidFileException {
        var path = file.toPath();
        var uri = path.toUri(); // it is faster than file.toURI.
        var storage = registry.getStorage(uri);
        if (storage.isEmpty()) {
            throw new IllegalArgumentException("Couldn't find storage for " + uri);
        }
        var builder = new Builder();
        var result = createFile(builder, path, uri, storage.get());
        return result;
    }

    static GenericFile createFile(GenericFile.Builder builder, Path path, URI uri, FileStorage storage)
            throws InvalidFileException {
        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return createFile(builder, path, attrs, uri, storage);
        } catch (Exception ex) {
            throw new InvalidFileException(ex);
        }
    }

    static GenericFile createFile(GenericFile.Builder builder, Path path, BasicFileAttributes attrs,
            URI uri, FileStorage storage) {
        builder.storage(storage);
        builder.name(path.getFileName().toString());
        builder.uri(uri);
        builder.lastModified(attrs.lastModifiedTime().toMillis());
        if (attrs.isDirectory()) {
            builder.type(FileType.DIRECTORY);
        } else {
            var fileType = FileType.FILE;
            if (attrs.isSymbolicLink()) {
                fileType = FileType.SYMBOLIC_LINK;
            }
            builder.type(fileType);
            builder.size(attrs.size());
        }
        builder.virtual(false);
        return builder.build();
    }

    private static List<GenericFile> buildParents(GenericFile child, int limit) {
        var storage = child.getStorage();
        var rootUri = storage.getRootUri();
        var segments = UriUtils.getPathSegments(rootUri, child.getUri());

        var parents = new ArrayList<GenericFile>(Math.min(segments.size(), limit));
        for (int i = segments.size() - 1; i >= 1 && parents.size() < limit; i--) {
            var parentUri = UriUtils.resolvePath(rootUri, String.join("/", segments.subList(0, i)));
            var builder = new Builder();
            builder.storage(storage);
            builder.type(FileType.DIRECTORY);
            builder.name(segments.get(i - 1));
            builder.uri(parentUri);
            builder.virtual(true);
            parents.add(builder.build());
        }
        if (parents.size() < limit) {
            parents.add(storage.getRoot());
        }
        return parents;
    }

    private final FileStorage storage;

    private final FileType type;

    private final URI uri;

    private final Long size;

    private final String name;

    private final Long lastModified;

    private final boolean virtual;

    private GenericFile(Builder builder) {
        this.storage = builder.storage;
        this.type = builder.type;
        this.uri = builder.uri;
        this.size = builder.size;
        this.name = builder.name;
        this.lastModified = builder.lastModified;
        this.virtual = builder.virtual;
    }

    public FileStorage getStorage() {
        return storage;
    }

    public FileType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    /**
     * Returns the size of the file or null.
     *
     * @return
     */
    public Long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return this.type == FileType.DIRECTORY;
    }

    public boolean isFile() {
        return this.type == FileType.FILE;
    }

    public boolean isSymbolicLink() {
        return this.type == FileType.SYMBOLIC_LINK;
    }

    /**
     * Returns the last modified time of the file or null. The time is represented as a {@code Long} value, where the
     * value is the number of milliseconds since the Unix epoch (January 1, 1970, 00:00:00 UTC).
     *
     * @return a {@code Long} representing the last modified time in milliseconds since the Unix epoch.
     */
    public Long getLastModified() {
        return lastModified;
    }

    /**
     * Checks if this file is virtual (i.e., created manually without a real corresponding file or directory on
     * the underlying storage). Virtual files are used as placeholders — for example, a parent or child inferred from
     * a path structure, or a root directory that does not physically exist.
     *
     * @return {@code true} if the file is virtual (no real file or directory backing it),
     *         {@code false} if it was loaded from a storage backend and corresponds to an actual entry.
     */
    public boolean isVirtual() {
        return virtual;
    }

    /**
     * Checks if this file represents the root directory of its {@link FileStorage}.
     *
     * @return {@code true} if this file's URI matches the root URI of its storage,
     *         {@code false} otherwise.
     */
    public boolean isRoot() {
        return isDirectory() && Objects.equals(uri, storage.getRootUri());
    }
}
