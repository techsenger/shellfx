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

import com.techsenger.drivekit.core.DriveDetector;
import com.techsenger.shellfx.storage.style.DefaultStorageStyleResolver;
import com.techsenger.shellfx.storage.style.StorageIconResolver;
import com.techsenger.shellfx.storage.style.StorageStyleResolver;
import com.techsenger.shellfx.storage.style.DefaultStorageIconResolver;
import com.techsenger.toolkit.core.function.Factory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Pavel Castornii
 */
public class WindowsFileStorage<T extends StorageFile> extends AbstractSystemFileStorage<T> {

    /**
     * The FILE_ATTRIBUTE_DIRECTORY and FILE_ATTRIBUTE_REPARSE_POINT bits of the Windows attributes mask.
     */
    private static final int DIRECTORY_REPARSE_POINT = 0x10 | 0x400;

    /**
     * Discovers and returns all system storages available on the current Windows machine.
     *
     * <p>Each drive found by {@link DriveDetector#detect()} is wrapped in a {@link WindowsFileStorage} instance. The
     * storage type is determined by the Windows drive type. The display name is obtained from
     * {@link FileSystemView#getSystemDisplayName(java.io.File)}.
     *
     * @param fileFactory the factory used to create file entries, must not be {@code null}
     * @return a mutable list of system storages, never {@code null}, may be empty
     */
    public static List<FileStorage<StorageFile>> createSystemStorages(
            Factory<? extends DefaultStorageFile> fileFactory) {
        List<FileStorage<StorageFile>> result = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        for (var drive : DriveDetector.detect()) {
            if (!DriveUtils.isShown(drive)) {
                continue;
            }
            var type = DriveUtils.toStorageType(drive.getType());
            FileStorage<StorageFile> storage = new WindowsFileStorage<>(
                    type,
                    fsv.getSystemDisplayName(drive.getRoot().toFile()),
                    drive.getRoot().toUri(),
                    drive.getId(),
                    fileFactory,
                    new DefaultStorageIconResolver(type),
                    new DefaultStorageStyleResolver());
            result.add(storage);
        }
        return result;
    }

    public WindowsFileStorage(FileStorageType type, String name, URI rootUri, String id,
            Factory<? extends DefaultStorageFile> fileFactory, StorageIconResolver iconResolver,
            StorageStyleResolver styleResolver) {
        super(type, name, rootUri, id, fileFactory, iconResolver, styleResolver);
    }

    @Override
    public boolean refersToStorage(URI uri) {
        return FileStorageUtils.isWithin(getUri(), uri, true);
    }

    @Override
    boolean isLink(BasicFileAttributes ownAttributes) {
        // Older JDKs report a directory junction as a directory that is also isOther() (any reparse point is);
        // newer ones report it as neither, see resolveEntryType().
        return super.isLink(ownAttributes) || (ownAttributes.isDirectory() && ownAttributes.isOther());
    }

    @Override
    FileEntryType resolveEntryType(Path entryPath, BasicFileAttributes attrs) {
        var type = super.resolveEntryType(entryPath, attrs);
        // a directory junction isn't a symbolic link for Java and isn't even a directory, so it can only be told
        // from the other special entries (e.g. a socket) by the attributes mask, which is read only for those
        if (type == FileEntryType.OTHER && isDirectoryReparsePoint(entryPath)) {
            return FileEntryType.LINK;
        }
        return type;
    }

    @Override
    boolean isHidden(Path entryPath, BasicFileAttributes attrs) {
        return attrs instanceof DosFileAttributes dosAttributes && dosAttributes.isHidden();
    }

    /**
     * Tells whether the entry is a directory that is also a reparse point. The mask is not a part of the Java SE
     * specification, so an entry is considered not to be one if the JDK doesn't provide it.
     */
    private boolean isDirectoryReparsePoint(Path entryPath) {
        try {
            var mask = Files.getAttribute(entryPath, "dos:attributes", LinkOption.NOFOLLOW_LINKS);
            return mask instanceof Integer value && (value & DIRECTORY_REPARSE_POINT) == DIRECTORY_REPARSE_POINT;
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException ex) {
            return false;
        }
    }
}
