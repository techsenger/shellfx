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

import com.sun.jna.platform.win32.Kernel32;
import com.techsenger.toolkit.core.function.Factory;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Pavel Castornii
 */
public class WindowsFileStorage<T extends GenericFile> extends AbstractDefaultFileStorage<T> {

    /**
     * Discovers and returns all default storages available on the current Windows machine.
     *
     * <p>Each root directory reported by the default {@link FileSystem} is wrapped in a
     * {@link WindowsFileStorage} instance. The storage type is determined by the Windows drive
     * type via {@code Kernel32.GetDriveType}:
     * <ul>
     *   <li>{@code 2} → {@link FileStorageType#FLOPPY}</li>
     *   <li>{@code 4} → {@link FileStorageType#NETWORK}</li>
     *   <li>{@code 5} → {@link FileStorageType#OPTICAL}</li>
     *   <li>any other value → {@link FileStorageType#BASE}</li>
     * </ul>
     * The display name is obtained from {@link FileSystemView#getSystemDisplayName(java.io.File)}.
     *
     * @param fileFactory the factory used to create file entries, must not be {@code null}
     * @return a mutable list of default storages, never {@code null}, may be empty
     */
    public static List<FileStorage<GenericFile>> createDefaultStorages(
            Factory<? extends DefaultGenericFile> fileFactory) {
        List<FileStorage<GenericFile>> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        FileSystems.getDefault().getRootDirectories().forEach(rootPath -> {
            FileStorageType type = switch (Kernel32.INSTANCE.GetDriveType(rootPath.toString())) {
                case 2 -> FileStorageType.FLOPPY;
                case 4 -> FileStorageType.NETWORK;
                case 5 -> FileStorageType.OPTICAL;
                default -> FileStorageType.BASE;
            };
            @SuppressWarnings("unchecked")
            var storage = (FileStorage<GenericFile>) (FileStorage<?>) new WindowsFileStorage<>(
                    type,
                    fsv.getSystemDisplayName(rootPath.toFile()),
                    rootPath.toUri(),
                    fileFactory);
            result.add(storage);
        });
        return result;
    }

    public WindowsFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<? extends DefaultGenericFile> fileFactory) {
        super(type, displayName, rootUri, fileFactory);
    }

    @Override
    public boolean refersToStorage(URI uri) {
        String givenRoot = uri.getPath().substring(0, Math.min(uri.getPath().length(), 3));
        String storageRoot = getUri().getPath().substring(0, 3);
        return givenRoot.equalsIgnoreCase(storageRoot);
    }
}
