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
     * Returns the first storage with {@link FileStorageType#BASE} type that is marked as default.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @return an {@link Optional} containing the primary storage, or empty if none matches
     */
    public static <T extends GenericFile> Optional<FileStorage<T>> findPrimary(
            List<? extends FileStorage<T>> storages) {
        for (var s : storages) {
            if (s.getType() == FileStorageType.BASE && s.isDefault()) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the first storage whose root URI contains the given URI.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @param uri      the URI to resolve, must not be {@code null}
     * @param <T>      the concrete file entry type produced by the storages
     * @return an {@link Optional} containing the matching storage, or empty if none matches
     */
    public static <T extends GenericFile> Optional<FileStorage<T>> findByUri(
            List<? extends FileStorage<T>> storages, URI uri) {
        for (var s : storages) {
            if (s.refersToStorage(uri)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the file entry for the current user's home directory.
     *
     * <p>The home directory is resolved from the {@code user.home} system property. The method searches the given
     * storages for a default storage that covers the home URI and delegates to {@link FileStorage#getFile(URI)}.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @param <T>      the concrete file entry type produced by the storages
     * @return an {@link Optional} containing the home directory entry, or empty if the
     *         {@code user.home} property is not set, no matching storage is found, or an error
     *         occurs while retrieving the entry
     */
    public static <T extends GenericFile> Optional<T> getHome(List<? extends FileStorage<T>> storages) {
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
