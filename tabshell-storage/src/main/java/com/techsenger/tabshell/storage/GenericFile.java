/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.storage;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Files are always immutable.
 *
 * @author Pavel Castornii
 */
public final class GenericFile {

    private static final Logger logger = LoggerFactory.getLogger(GenericFile.class);

    /**
     * Tries to create parent file with minimum data - storage, type, uri, name or returns null.
     *
     * @param child
     * @return
     */
    public static GenericFile getParent(GenericFile child) {
        var segments = UriUtils.getPathSegments(child.getStorage().getRootUri(), child.getUri());
        var parentUri = UriUtils.getParentUri(child.getStorage().getRootUri(), child.getUri(), segments);
        if (parentUri == null || parentUri == child.getStorage().getRootUri()) {
            return null;
        }
        var builder = new Builder();
        builder.storage(child.storage);
        builder.type(FileType.DIRECTORY);
        builder.name(segments.get(segments.size() - 2));
        builder.uri(parentUri);
        builder.virtual(true);
        var result = builder.build();
        return result;
    }

    /**
     * Creates child file.
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
     * Returns file to home directory or null.
     *
     * @param storages
     * @return
     */
    public static GenericFile getHome(List<FileStorage> storages) {
        var str = System.getProperty("user.home");
        if (str == null) {
            return null;
        }
        var homeUri = Paths.get(str).toUri();
        FileStorage storage = null;
        for (var s : storages) {
            if (s.isDefault() && s.refersToStorage(homeUri)) {
                storage = s;
                break;
            }
        }
        if (storage != null) {
            try {
                return storage.getFile(homeUri);
            } catch (Exception ex) {
                logger.error("Error getting home file", ex);
            }
        }
        return null;
    }

    /**
     * Converts file to GenericFile.
     *
     * @param file
     * @param storages
     * @return
     */
    public static GenericFile convert(File file, List<FileStorage> storages) throws InvalidFileException {
        var uri = file.toURI();
        var storage = FileStorages.findByUri(storages, uri);
        if (storage == null) {
            throw new IllegalArgumentException("Couldn't find storage for " + uri);
        }
        var builder = new Builder();
        var result = createFile(builder, file, uri, storage);
        return result;
    }

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
            reset();
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

    static GenericFile createFile(GenericFile.Builder builder, File file, URI uri, FileStorage storage)
            throws InvalidFileException {
        try {
            var filePath = file.toPath();
            builder.storage(storage);
            builder.name(file.getName());
            builder.uri(uri);
            builder.lastModified(file.lastModified());
            if (file.isDirectory()) {
                builder.type(FileType.DIRECTORY);
            } else {
                var fileType = FileType.FILE;
                if (Files.isSymbolicLink(filePath)) {
                    fileType = FileType.SYMBOLIC_LINK;
                }
                builder.type(fileType);
                builder.size(file.length());
            }
            builder.virtual(false);
            return builder.build();
        } catch (Exception ex) {
            throw new InvalidFileException(ex);
        }
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
     * Checks if this file is virtual (i.e., created manually without full data from storage).
     * Virtual files are typically placeholders for paths that haven't been fully resolved
     * with the underlying {@link FileStorage} (e.g., parent/child inferred from path structure).
     *
     * @return {@code true} if the file is virtual (lacks full metadata),
     *         {@code false} if it was loaded from a storage backend with complete data.
     */
    public boolean isVirtual() {
        return virtual;
    }
}
