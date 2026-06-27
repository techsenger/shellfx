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
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class FileStorageUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageUtils.class);

    /**
     * Returns the first default storage with {@link FileStorageType#BASE} type.
     *
     * @param storages
     * @return
     */
    public static Optional<FileStorage> findPrimary(List<FileStorage> storages) {
        for (var s : storages) {
            if (s.getType() == FileStorageType.BASE && s.isDefault()) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the storage for the given uri.
     *
     * @param storages
     * @param uri
     * @return
     */
    public static Optional<FileStorage> findByUri(List<FileStorage> storages, URI uri) {
        for (var s : storages) {
            if (s.refersToStorage(uri)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the file to home directory.
     *
     * @param storages
     * @return
     */
    public static Optional<GenericFile> getHome(List<FileStorage> storages) {
        var str = System.getProperty("user.home");
        if (str == null) {
            return Optional.empty();
        }
        var homeUri = Paths.get(str).toUri();
        for (var s : storages) {
            if (s.isDefault() && s.refersToStorage(homeUri)) {
                try {
                    return Optional.of(s.getFile(homeUri));
                } catch (Exception ex) {
                    logger.error("Error getting home file", ex);
                }
            }
        }
        return Optional.empty();
    }

    private FileStorageUtils() {
        // empty
    }
}
