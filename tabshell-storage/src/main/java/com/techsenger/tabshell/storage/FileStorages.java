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
import com.techsenger.toolkit.core.os.OsUtils;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.filechooser.FileSystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class FileStorages {

    public interface StorageFactory {

        FileStorage createFactory(FileStorageType type, String displayName, URI rootUri);
    }

    private static final Logger logger = LoggerFactory.getLogger(FileStorages.class);

    private static volatile List<FileStorage> defaultStorages = null;

    private static final List<FileStorage> customStorages = new CopyOnWriteArrayList<>();

    private static final Map<String, FileStorage> storagesByUri = new HashMap<>();

    private static volatile StorageFactory storageFactory = (FileStorageType type, String displayName, URI rootUri) -> {
        if (OsUtils.isWindows()) {
            return new WindowsFileStorage(type, displayName, rootUri);
        } else {
            return new UnixFileStorage(type, displayName, rootUri);
        }
    };

    /**
     * Returns the factory that is used for creating default factories.
     *
     * @return
     */
    public static StorageFactory getStorageFactory() {
        return storageFactory;
    }

    /**
     * Sets the factory that is used for creating default factories. Use this method if you have your custom
     * implementations of FileStorage and want to use them for the default storages.
     *
     * @param storageFactory
     */
    public static void setStorageFactory(StorageFactory storageFactory) {
        FileStorages.storageFactory = storageFactory;
    }

    /**
     * Returns an unmodifiable list of all default file storages for this machine.
     *
     * @param refresh if true, or it is the first call then existing list is created/refreshed.
     * @return
     */
    public static synchronized List<FileStorage> getDefault(boolean refresh) {
        if (defaultStorages == null || refresh) {
            if (OsUtils.isWindows()) {
                defaultStorages = getWindowsStorages();
            } else {
                defaultStorages = getUnixStorages();
            }
            defaultStorages = Collections.unmodifiableList(defaultStorages);
        }
        return defaultStorages;
    }

    /**
     * Returns a modifiable thread safe list of custom storages (which don't exist in OS).
     *
     * @return
     */
    public static List<FileStorage> getCustom() {
        return customStorages;
    }

    /**
     * Returns an unmodifiable list of all default and custom file storages.
     *
     * @return
     */
    public static List<FileStorage> getAll(boolean refreshDefault) {
        var combinedList = Stream.concat(getDefault(refreshDefault).stream(), customStorages.stream())
                .collect(Collectors.toList());
        return combinedList;
    }

    /**
     * Returns the first default storage with {@link FileStorageType#BASE} type.
     *
     * @param storages
     * @return
     */
    public static FileStorage findPrimary(List<FileStorage> storages) {
        for (var s : storages) {
            if (s.getType() == FileStorageType.BASE && s.isDefault()) {
                return s;
            }
        }
        return null;
    }

    /**
     * Finds the storage for the given uri.
     *
     * @param storages
     * @param uri
     * @return
     */
    public static FileStorage findByUri(List<FileStorage> storages, URI uri) {
        for (var s : storages) {
            if (s.refersToStorage(uri)) {
                return s;
            }
        }
        return null;
    }

    private FileStorages() {
        //empty
    }

    private static List<FileStorage> getWindowsStorages() {
        List<FileStorage> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        List<Path> rootPaths = new ArrayList<>();
        FileSystems.getDefault().getRootDirectories().forEach(r -> rootPaths.add(r));
        for (var rootPath : rootPaths) {
            var file = rootPath.toFile();
            var str = rootPath.toString();
            FileStorageType type = null;
            switch (Kernel32.INSTANCE.GetDriveType(str)) {
                case 2:
                    type = FileStorageType.FLOPPY;
                    break;
                case 4:
                    type = FileStorageType.NETWORK;
                    break;
                case 5:
                    type = FileStorageType.OPTICAL;
                    break;
                default:
                    type = FileStorageType.BASE;
            }
            var storage = storageFactory.createFactory(type, fsv.getSystemDisplayName(file), rootPath.toUri());
            var resultStorage = selectOldOrNew(storage);
            result.add(resultStorage);
        }
        updateStoragesByUri((List) result);
        return result;
    }

    private static List<FileStorage> getUnixStorages() {
        List<FileStorage> result = new ArrayList<>();
        List<Path> rootPaths = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        FileSystems.getDefault().getRootDirectories().forEach(r -> rootPaths.add(r));
        for (var rootPath : rootPaths) {
            var file = rootPath.toFile();
            var storage = storageFactory
                    .createFactory(FileStorageType.BASE, fsv.getSystemDisplayName(file), rootPath.toUri());
            var resultStorage = selectOldOrNew(storage);
            result.add(resultStorage);
        }
        updateStoragesByUri((List) result);
        return result;
    }

    private static void updateStoragesByUri(List<AbstractFileStorage> storages) {
        storagesByUri.clear();
        for (var s : storages) {
            storagesByUri.put(s.getRootUri().toString(), s);
        }
    }

    private static FileStorage selectOldOrNew(FileStorage newStorage) {
        var oldStorage = storagesByUri.get(newStorage.getRootUri().toString());
        if (oldStorage != null && oldStorage.equals(newStorage)) {
            return oldStorage;
        } else {
            return newStorage;
        }
    }

    private static boolean isOptical(Path p) {
        try {
            FileStore store = Files.getFileStore(p);
            String storeType = store.type().toUpperCase();
            if (storeType.equals("CDFS") || storeType.equals("UDF")
                    || storeType.equals("ISO9660") || storeType.equals("JOLIET")) {
                return true;
            }
        } catch (Exception ex) {
            logger.error("Error resolving file store type", ex);
        }
        return false;
    }
}
