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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
     * Returns all local disk storages among {@code storages} - those backed by the local file system (Unix or
     * Windows), excluding both network shares and any storage registered programmatically (e.g. FTP, cloud,
     * {@code Recent}), which never carry a local disk {@link FileStorageType}.
     *
     * <p>The result is ordered by media priority - all {@link FileStorageType#BASE} storages first, then
     * {@link FileStorageType#OPTICAL}, then {@link FileStorageType#FLOPPY} - so a caller picking a single
     * fallback (e.g. via {@code findLocal(storages).stream().findFirst()}) prefers a real disk over
     * removable media, rather than depending on whatever order the storages happened to be discovered in.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @return an unmodifiable list, never {@code null}, may be empty
     */
    public static <T extends StorageFile> List<FileStorage<T>> findLocal(List<? extends FileStorage<T>> storages) {
        var base = new ArrayList<FileStorage<T>>();
        var optical = new ArrayList<FileStorage<T>>();
        var floppy = new ArrayList<FileStorage<T>>();
        for (var s : storages) {
            switch (s.getType()) {
                case BASE -> base.add(s);
                case OPTICAL -> optical.add(s);
                case FLOPPY -> floppy.add(s);
                default -> { }
            }
        }
        var result = new ArrayList<FileStorage<T>>(base.size() + optical.size() + floppy.size());
        result.addAll(base);
        result.addAll(optical);
        result.addAll(floppy);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the first storage whose root URI contains the given URI.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @param uri      the URI to resolve, must not be {@code null}
     * @param <T>      the concrete file entry type produced by the storages
     * @return an {@link Optional} containing the matching storage, or empty if none matches
     */
    public static <T extends StorageFile> Optional<FileStorage<T>> findByUri(
            List<? extends FileStorage<T>> storages, URI uri) {
        for (var s : storages) {
            if (s.refersToStorage(uri)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Tells whether {@code uri} is the root URI itself or lies within it: the scheme and the authority are the
     * same, and the path starts with the path of the root at a segment boundary.
     *
     * @param root the root URI, must not be {@code null}
     * @param uri the URI to check, must not be {@code null}
     * @param ignoreCase whether the paths are compared ignoring the case, as in a case-insensitive file system
     * @return {@code true} if {@code uri} is within {@code root}
     */
    public static boolean isWithin(URI root, URI uri, boolean ignoreCase) {
        if (!equalsIgnoreCase(root.getScheme(), uri.getScheme())
                || !equalsIgnoreCase(root.getAuthority(), uri.getAuthority())) {
            return false;
        }
        var rootPath = withTrailingSlash(root);
        var path = withTrailingSlash(uri);
        return path.regionMatches(ignoreCase, 0, rootPath, 0, rootPath.length());
    }

    /**
     * Returns the file entry for the current user's home directory.
     *
     * <p>The home directory is resolved from the {@code user.home} system property. The method searches the local
     * disk storages among {@code storages} (see {@link #findLocal}) for one that covers the home URI and
     * delegates to {@link FileStorage#getFile(URI)}.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @param <T>      the concrete file entry type produced by the storages
     * @return an {@link Optional} containing the home directory entry, or empty if the
     *         {@code user.home} property is not set, no matching storage is found, or an error
     *         occurs while retrieving the entry
     */
    public static <T extends StorageFile> Optional<T> getHome(List<? extends FileStorage<T>> storages) {
        var str = System.getProperty("user.home");
        if (str == null) {
            return Optional.empty();
        }
        var homeUri = Paths.get(str).toUri();
        var storage = findByUri(findLocal(storages), homeUri);
        if (storage.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(storage.get().getFile(homeUri, OperationType.SECONDARY));
        } catch (Exception ex) {
            logger.error("Error getting home file", ex);
            return Optional.empty();
        }
    }

    private static boolean equalsIgnoreCase(@Nullable String first, @Nullable String second) {
        return first == null ? second == null : first.equalsIgnoreCase(second);
    }

    private static String withTrailingSlash(URI uri) {
        var path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
        return path.endsWith("/") ? path : path + "/";
    }

    private FileStorageUtils() {
        // empty
    }
}
