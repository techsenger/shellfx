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

package com.techsenger.shellfx.storage.style;

import com.techsenger.shellfx.material.icon.FontIcon;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.StorageFile;

/**
 * Gives a file the icon of its entry type, and the root of a storage the icon of the storage. A link gets the link
 * icon of the kind of the file it points to, or of a file if the target is not known. A broken link gets an error
 * icon, a file that is still to be refined a pending one. It is the fallback for the files that an application
 * has no icon of its own for.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class DefaultFileIconResolver<F extends StorageFile> implements FileIconResolver<F> {

    private enum Look {
        PENDING,
        ERROR,
        NORMAL
    }

    @Override
    public FontIcon<?> resolve(F file, FileDecorationType type) {
        if (file.isRoot()) {
            return file.getStorage().getIconResolver().resolve();
        }
        var link = file.getLink();
        var target = link == null ? null : link.getTarget();
        var look = Look.NORMAL;
        if (type == FileDecorationType.TEMP) {
            look = Look.PENDING;
        } else if (isError(file)) {
            look = Look.ERROR;
        }
        // a chain of links is followed to its end, so the target is never a link itself
        var kind = file.isLink() ? (target == null ? FileEntryType.FILE : target.getEntryType())
                : file.getEntryType();
        if (file.isLink()) {
            return switch (look) {
                case PENDING -> switch (kind) {
                    case DIRECTORY -> StorageIcons.PENDING_DIRECTORY_LINK;
                    case OTHER -> StorageIcons.PENDING_OTHER_LINK;
                    case FILE, LINK -> StorageIcons.PENDING_FILE_LINK;
                };
                case ERROR -> switch (kind) {
                    case DIRECTORY -> StorageIcons.ERROR_DIRECTORY_LINK;
                    case OTHER -> StorageIcons.ERROR_OTHER_LINK;
                    case FILE, LINK -> StorageIcons.ERROR_FILE_LINK;
                };
                case NORMAL -> switch (kind) {
                    case DIRECTORY -> StorageIcons.DIRECTORY_LINK;
                    case OTHER -> StorageIcons.OTHER_LINK;
                    case FILE, LINK -> StorageIcons.FILE_LINK;
                };
            };
        }
        return switch (look) {
            case PENDING -> switch (kind) {
                case DIRECTORY -> StorageIcons.PENDING_DIRECTORY;
                case OTHER -> StorageIcons.PENDING_OTHER;
                case FILE, LINK -> StorageIcons.PENDING_FILE;
            };
            case ERROR -> switch (kind) {
                case DIRECTORY -> StorageIcons.ERROR_DIRECTORY;
                case OTHER -> StorageIcons.ERROR_OTHER;
                case FILE, LINK -> StorageIcons.ERROR_FILE;
            };
            case NORMAL -> switch (kind) {
                case DIRECTORY -> StorageIcons.DIRECTORY;
                case OTHER -> StorageIcons.OTHER;
                case FILE, LINK -> StorageIcons.FILE;
            };
        };
    }

    /**
     * Tells whether the file is shown as erroneous, with an error icon: the default says so of a link whose target is
     * not known. An application with more kinds of errors overrides it and keeps the answer of the superclass.
     *
     * @param file the file
     * @return {@code true} if the error icon is used for the file
     */
    protected boolean isError(F file) {
        var link = file.getLink();
        return file.isLink() && (link == null || link.getTarget() == null);
    }
}
