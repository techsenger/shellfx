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
public class UnixFileStorage<T extends GenericFile> extends AbstractDefaultFileStorage<T> {

    /**
     * Discovers and returns all default storages available on the current Unix machine.
     *
     * <p>Each root directory reported by the default {@link FileSystem} is wrapped in a
     * {@link UnixFileStorage} instance with type {@link FileStorageType#BASE}. The display name
     * is obtained from {@link FileSystemView#getSystemDisplayName(java.io.File)}.
     *
     * @param fileFactory the factory used to create file entries, must not be {@code null}
     * @return a mutable list of default storages, never {@code null}, may be empty
     */
    public static List<FileStorage<GenericFile>> createDefaultStorages(
            Factory<? extends DefaultGenericFile> fileFactory) {
        List<FileStorage<GenericFile>> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        FileSystems.getDefault().getRootDirectories().forEach(rootPath -> {
            @SuppressWarnings("unchecked")
            var storage = (FileStorage<GenericFile>) (FileStorage<?>) new UnixFileStorage<>(
                    FileStorageType.BASE,
                    fsv.getSystemDisplayName(rootPath.toFile()),
                    rootPath.toUri(),
                    fileFactory);
            result.add(storage);
        });
        return result;
    }

    public UnixFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<? extends DefaultGenericFile> fileFactory) {
        super(type, displayName, rootUri, fileFactory);
    }

    @Override
    public boolean refersToStorage(URI uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }
}
