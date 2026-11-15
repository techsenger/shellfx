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

package com.techsenger.shellfx.storage.system;

import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.storage.base.FileStorageType;
import com.techsenger.shellfx.storage.base.StorageIconProvider;
import com.techsenger.shellfx.storage.system.style.SystemStorageIcons;

/**
 * Gives a storage the icon of its type. It is the default of the storages of the local machine.
 *
 * @author Pavel Castornii
 */
public final class DefaultStorageIconProvider implements StorageIconProvider {

    private final FontIcon<?> baseIcon;

    /**
     * Creates a provider.
     *
     * @param type the type of the storage
     */
    public DefaultStorageIconProvider(FileStorageType type) {
        this.baseIcon = switch (type) {
            case BASE, VIRTUAL -> SystemStorageIcons.BASE_STORAGE;
            case HDD -> SystemStorageIcons.HDD_STORAGE;
            case SSD -> SystemStorageIcons.SSD_STORAGE;
            case REMOVABLE -> SystemStorageIcons.REMOVABLE_STORAGE;
            case NETWORK -> SystemStorageIcons.NETWORK_STORAGE;
            case FLOPPY -> SystemStorageIcons.FLOPPY_STORAGE;
            case OPTICAL -> SystemStorageIcons.OPTICAL_STORAGE;
        };
    }

    @Override
    public FontIcon<?> getBaseIcon() {
        return baseIcon;
    }
}
