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

package com.techsenger.tabshell.storage;

import com.sun.jna.platform.win32.Kernel32;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.toolkit.core.os.OsUtils;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Pavel Castornii
 */
public final class DefaultFileStorageRegistry implements FileStorageRegistry {

    private final List<FileStorage> customStorages = new CopyOnWriteArrayList<>();
    private volatile List<FileStorage> defaultStorages = null;
    private volatile Map<String, FileStorage> storagesByUri = Map.of();

    @Override
    public void refreshDefaultStorages() {
        synchronized (this) {
            if (OsUtils.isWindows()) {
                defaultStorages = Collections.unmodifiableList(getWindowsStorages());
            } else {
                defaultStorages = Collections.unmodifiableList(getUnixStorages());
            }
        }
    }

    @Override
    public @Unmodifiable List<FileStorage> getDefaultStorages() {
        if (defaultStorages == null) {
            refreshDefaultStorages();
        }
        return defaultStorages;
    }

    @Override
    public Optional<FileStorage> getPrimaryStorage() {
        return getDefaultStorages().stream()
                .filter(s -> s.getType() == FileStorageType.BASE && s.isDefault())
                .findFirst();
    }

    @Override
    public @Unmodifiable List<FileStorage> getCustomStorages() {
        return Collections.unmodifiableList(customStorages);
    }

    @Override
    public Registration registerCustomStorage(FileStorage storage) {
        customStorages.add(storage);
        return () -> customStorages.remove(storage);
    }

    @Override
    public @Unmodifiable List<FileStorage> getAllStorages() {
        return Stream.concat(getDefaultStorages().stream(), customStorages.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<FileStorage> getStorage(URI uri) {
        var byDefault = storagesByUri.get(uri.toString());
        if (byDefault != null) {
            return Optional.of(byDefault);
        }
        return customStorages.stream()
                .filter(s -> s.refersToStorage(uri))
                .findFirst();
    }

    private List<FileStorage> getWindowsStorages() {
        List<FileStorage> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        List<Path> rootPaths = new ArrayList<>();
        FileSystems.getDefault().getRootDirectories().forEach(rootPaths::add);
        for (var rootPath : rootPaths) {
            var file = rootPath.toFile();
            FileStorageType type = switch (Kernel32.INSTANCE.GetDriveType(rootPath.toString())) {
                case 2 -> FileStorageType.FLOPPY;
                case 4 -> FileStorageType.NETWORK;
                case 5 -> FileStorageType.OPTICAL;
                default -> FileStorageType.BASE;
            };
            var storage = createStorage(type, fsv.getSystemDisplayName(file), rootPath.toUri());
            result.add(storage);
        }
        updateStoragesByUri(result);
        return result;
    }

    private List<FileStorage> getUnixStorages() {
        List<FileStorage> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        FileSystems.getDefault().getRootDirectories().forEach(rootPath -> {
            var storage = createStorage(
                    FileStorageType.BASE,
                    fsv.getSystemDisplayName(rootPath.toFile()),
                    rootPath.toUri()
            );
            result.add(storage);
        });
        updateStoragesByUri(result);
        return result;
    }

    private FileStorage createStorage(FileStorageType type, String displayName, URI rootUri) {
        FileStorage newStorage = OsUtils.isWindows()
                ? new WindowsFileStorage(type, displayName, rootUri)
                : new UnixFileStorage(type, displayName, rootUri);
        // Reuse existing instance if it hasn't changed, to preserve identity and any associated state.
        var existing = storagesByUri.get(rootUri.toString());
        return (existing != null && existing.equals(newStorage)) ? existing : newStorage;
    }

    private void updateStoragesByUri(List<FileStorage> storages) {
        Map<String, FileStorage> newMap = new HashMap<>(storages.size() * 2);
        for (var s : storages) {
            newMap.put(s.getRootUri().toString(), s);
        }
        storagesByUri = Collections.unmodifiableMap(newMap);
    }
}
