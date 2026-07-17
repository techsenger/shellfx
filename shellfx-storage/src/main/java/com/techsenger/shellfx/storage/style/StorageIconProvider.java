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

import com.techsenger.shellfx.material.icon.StyleFontIcon;
import com.techsenger.shellfx.storage.FileStorageType;
import static com.techsenger.shellfx.storage.FileStorageType.BASE;
import static com.techsenger.shellfx.storage.FileStorageType.FLOPPY;
import static com.techsenger.shellfx.storage.FileStorageType.NETWORK;
import static com.techsenger.shellfx.storage.FileStorageType.OPTICAL;
import java.util.function.Function;

/**
 *
 * @author Pavel Castornii
 */
public final class StorageIconProvider {

    public static final Function<FileStorageType, StyleFontIcon> INSTANCE = (t) -> {
        switch (t) {
            case BASE:
                return StorageIcons.BASE_DISK;
            case NETWORK:
                return StorageIcons.NETWORK_DISK;
            case FLOPPY:
                return StorageIcons.FLOPPY;
            case OPTICAL:
                return StorageIcons.DISC;
            default:
                throw new AssertionError();
        }
    };

    private StorageIconProvider() {
        // empty
    }
}
