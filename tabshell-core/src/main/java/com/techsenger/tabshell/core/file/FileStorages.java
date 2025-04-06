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

package com.techsenger.tabshell.core.file;

import com.techsenger.toolkit.core.os.OsUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileSystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public final class FileStorages {

    private static final Logger logger = LoggerFactory.getLogger(FileStorages.class);

    private static List<FileStorage> storages = null;

    private static final Map<String, FileStorage> storagesByUri = new HashMap<>();

    /**
     * Returns unmodifiable list of all default file storage for this machine.
     *
     * @param refresh if true, or it is the first call then existing list is created/refreshed.
     * @return
     */
    public static synchronized List<FileStorage> getDefault(boolean refresh) {
        if (storages == null || refresh) {
            if (OsUtils.isWindows()) {
                storages = getWindowsStorages();
            } else {
                storages = getUnixStorages();
            }
            storages = Collections.unmodifiableList(storages);
        }
        return storages;
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
     * Finds the storages for the given uri.
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
        Set<String> networkDrives = getNetworkDrives();
        for (var rootPath : rootPaths) {
            var file = rootPath.toFile();
            var str = rootPath.toString();
            FileStorageType type = null;
            if (networkDrives.contains(str)) {
                type = FileStorageType.NETWORK;
            } else if (fsv.isFloppyDrive(file)) {
                type = FileStorageType.FLOPPY;
            } else if (isOptical(rootPath)) {
                type = FileStorageType.OPTICAL;
            } else {
                type = FileStorageType.BASE;
            }
            var storage = new WindowsFileStorage(type, fsv.getSystemDisplayName(file), rootPath.toUri());
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
            var storage = new LinuxFileStorage(FileStorageType.BASE, fsv.getSystemDisplayName(file), rootPath.toUri());
            var resultStorage = selectOldOrNew(storage);
            result.add(resultStorage);
        }
        updateStoragesByUri((List) result);
        return result;
    }

    private static void updateStoragesByUri(List<AbstractFileStorage> storages) {
        storagesByUri.clear();
        for (var s : storages) {
            storagesByUri.put(s.getRootUriAsString(), s);
        }
    }

    private static FileStorage selectOldOrNew(AbstractDefaultFileStorage newStorage) {
        var oldStorage = storagesByUri.get(newStorage.getRootUriAsString());
        if (oldStorage != null && oldStorage.equals(newStorage)) {
            return oldStorage;
        } else {
            return newStorage;
        }
    }

    private static Set<String> getNetworkDrives() {
        Set<String> driveLetters = new HashSet<>();
        Pattern pattern = Pattern.compile("\\s*([A-Z]:)\\s*");
        try {
            Process process = new ProcessBuilder("cmd", "/c", "net use").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    driveLetters.add(matcher.group(1) + "\\");
                }
            }
            process.waitFor();
        } catch (Exception ex) {
            logger.error("Error getting network drives", ex);
        }
        return driveLetters;
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
