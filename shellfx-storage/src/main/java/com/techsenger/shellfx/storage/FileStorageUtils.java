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
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.toolkit.core.os.OsUtils;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
     * The priority of the local storages by their type: the lower the number, the earlier the storage is listed.
     */
    private static final Map<FileStorageType, Integer> prioritiesByType = Map.of(
            FileStorageType.FLOPPY, 0,
            FileStorageType.HDD, 1,
            FileStorageType.SSD, 2,
            FileStorageType.BASE, 3,
            FileStorageType.REMOVABLE, 4,
            FileStorageType.OPTICAL, 5,
            FileStorageType.VIRTUAL, 6);

    /**
     * Returns all local disk storages among {@code storages} - the system ones, backed by the local file system,
     * excluding network shares.
     *
     * <p>The result is ordered by the type of the medium: {@link FileStorageType#FLOPPY}, {@link FileStorageType#HDD},
     * {@link FileStorageType#SSD}, {@link FileStorageType#BASE}, {@link FileStorageType#REMOVABLE},
     * {@link FileStorageType#OPTICAL}, {@link FileStorageType#VIRTUAL}. The storages of one type keep the order they
     * were given in.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @return an unmodifiable list, never {@code null}, may be empty
     */
    public static <T extends StorageFile> @Unmodifiable List<FileStorage<T>>
            findLocal(List<? extends FileStorage<T>> storages) {
        var result = new ArrayList<FileStorage<T>>();
        for (var s : storages) {
            if (s.getType() != FileStorageType.NETWORK) {
                result.add(s);
            }
        }
        result.sort(Comparator.comparingInt(s -> prioritiesByType.get(s.getType())));
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the main local storage: the one that holds the root of the operating system ({@code /} or the system
     * drive). If it is not found, the first of the local storages is returned.
     *
     * @param storages the list of storages to search, must contain a local storage
     * @param <T>      the concrete file entry type produced by the storages
     * @return the storage, never {@code null}
     * @throws IllegalStateException if there is no local storage
     */
    public static <T extends StorageFile> FileStorage<T> getMain(
            List<? extends FileStorage<T>> storages) {
        var local = findLocal(storages);
        var systemRoot = findSystemRoot();
        if (systemRoot != null) {
            var storage = findByUri(local, systemRoot.toUri());
            if (storage.isPresent()) {
                return storage.get();
            }
        }
        return local.stream().findFirst().orElseThrow(() -> new IllegalStateException("There is no local storage"));
    }

    /**
     * Returns the storage that owns the given URI. Roots of storages may be nested, as a volume is mounted into
     * a directory of another one, so the one with the most specific (the longest) root wins.
     *
     * @param storages the list of storages to search, must not be {@code null}
     * @param uri      the URI to resolve, must not be {@code null}
     * @param <T>      the concrete file entry type produced by the storages
     * @return an {@link Optional} containing the matching storage, or empty if none matches
     */
    public static <T extends StorageFile> Optional<FileStorage<T>> findByUri(
            List<? extends FileStorage<T>> storages, URI uri) {
        @Nullable FileStorage<T> result = null;
        for (var s : storages) {
            // roots that both contain the URI lie one within the other, so the longer one is the more specific
            if (s.refersToStorage(uri) && (result == null
                    || s.getUri().toString().length() > result.getUri().toString().length())) {
                result = s;
            }
        }
        return Optional.ofNullable(result);
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

    private static @Nullable Path findSystemRoot() {
        if (!OsUtils.isWindows()) {
            return Paths.get("/");
        }
        var systemRoot = System.getenv("SystemRoot");
        return systemRoot == null ? null : Paths.get(systemRoot).getRoot();
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
