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

import com.techsenger.drivekit.core.Drive;
import com.techsenger.drivekit.core.DriveType;
import java.nio.file.Path;
import java.util.Set;

/**
 * Utilities for turning the drives of the machine into storages.
 *
 * @author Pavel Castornii
 */
public final class DriveUtils {

    /**
     * The roots of the drives that the system keeps for itself, such as the partition the machine boots from.
     */
    private static final Set<Path> HIDDEN_ROOTS = Set.of(Path.of("/boot"), Path.of("/boot/efi"), Path.of("/efi"));

    /**
     * Tells whether a drive is shown as a storage. A drive that the system keeps for itself, such as the boot
     * partition, is not shown; the root of the file system is always shown.
     *
     * @param drive the drive to check
     * @return {@code true} if the drive is shown
     */
    public static boolean isShown(Drive drive) {
        return !HIDDEN_ROOTS.contains(drive.getRoot());
    }

    /**
     * Returns the type of the storage on a drive. A drive that is a layer over other drives or whose type is
     * unknown gives {@link FileStorageType#BASE}.
     *
     * @param type the type of the drive
     * @return the type of the storage, never {@code null}
     */
    public static FileStorageType toStorageType(DriveType type) {
        return switch (type) {
            case HDD -> FileStorageType.HDD;
            case SSD -> FileStorageType.SSD;
            case REMOVABLE -> FileStorageType.REMOVABLE;
            case FLOPPY -> FileStorageType.FLOPPY;
            case OPTICAL -> FileStorageType.OPTICAL;
            case NETWORK -> FileStorageType.NETWORK;
            case VIRTUAL -> FileStorageType.VIRTUAL;
            case LVM, RAID, UNKNOWN -> FileStorageType.BASE;
        };
    }

    private DriveUtils() {
        // empty
    }
}
