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

import com.techsenger.toolkit.core.function.Factory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Pavel Castornii
 */
public class UnixFileStorage<T extends StorageFile> extends AbstractSystemFileStorage<T> {

    /**
     * Discovers and returns all system storages available on the current Unix machine.
     *
     * <p>Each root directory reported by the default {@link FileSystem} is wrapped in a
     * {@link UnixFileStorage} instance with type {@link FileStorageType#BASE}. The display name
     * is obtained from {@link FileSystemView#getSystemDisplayName(java.io.File)}.
     *
     * @param fileFactory the factory used to create file entries, must not be {@code null}
     * @return a mutable list of system storages, never {@code null}, may be empty
     */
    public static List<FileStorage<StorageFile>> createSystemStorages(
            Factory<? extends DefaultStorageFile> fileFactory) {
        List<FileStorage<StorageFile>> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        FileSystems.getDefault().getRootDirectories().forEach(rootPath -> {
            @SuppressWarnings("unchecked")
            var storage = (FileStorage<StorageFile>) (FileStorage<?>) new UnixFileStorage<>(
                    FileStorageType.BASE,
                    fsv.getSystemDisplayName(rootPath.toFile()),
                    rootPath.toUri(),
                    fileFactory);
            result.add(storage);
        });
        return result;
    }

    private final boolean posixSupported;

    public UnixFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<? extends DefaultStorageFile> fileFactory) {
        super(type, displayName, rootUri, fileFactory);
        this.posixSupported = Paths.get(rootUri).getFileSystem().supportedFileAttributeViews().contains("posix");
    }

    @Override
    public boolean refersToStorage(URI uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }

    @Override
    BasicFileAttributes readAttributes(Path entryPath) throws IOException {
        // asking for the basic attributes would return a wrapper without the permissions; a file system that has
        // no POSIX attributes (e.g. an in-memory one) can only give the basic ones
        if (posixSupported) {
            return Files.readAttributes(entryPath, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }
        return super.readAttributes(entryPath);
    }
}
