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
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.FileStorageType;

/**
 * Gives a storage the icon of its type and paints the icon and the texts with the default foreground color of the
 * theme. It is the default of the storages of the local machine.
 *
 * @author Pavel Castornii
 */
public final class DefaultStorageDecorator implements StorageDecorator {

    private final FileStorageType type;

    /**
     * Creates a decorator.
     *
     * @param type the type of the storage
     */
    public DefaultStorageDecorator(FileStorageType type) {
        this.type = type;
    }

    @Override
    public IconStyleDecoration decorate(Theme theme) {
        return new IconStyleDecoration(resolveIcon(), IconTextStyle.foreground(theme));
    }

    private FontIcon<?> resolveIcon() {
        return switch (type) {
            case BASE, VIRTUAL -> StorageIcons.BASE_STORAGE;
            case HDD -> StorageIcons.HDD_STORAGE;
            case SSD -> StorageIcons.SSD_STORAGE;
            case REMOVABLE -> StorageIcons.REMOVABLE_STORAGE;
            case NETWORK -> StorageIcons.NETWORK_STORAGE;
            case FLOPPY -> StorageIcons.FLOPPY_STORAGE;
            case OPTICAL -> StorageIcons.OPTICAL_STORAGE;
        };
    }
}
