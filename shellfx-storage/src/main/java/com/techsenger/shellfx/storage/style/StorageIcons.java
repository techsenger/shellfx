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
import com.techsenger.shellfx.material.icon.StyleFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public interface StorageIcons {

    FontIcon<?> OPTICAL_STORAGE = new StyleFontIcon("optical-storage-icon");

    FontIcon<?> FLOPPY_STORAGE = new StyleFontIcon("floppy-storage-icon");

    FontIcon<?> BASE_STORAGE = new StyleFontIcon("base-storage-icon");

    FontIcon<?> HDD_STORAGE = new StyleFontIcon("hdd-storage-icon");

    FontIcon<?> SSD_STORAGE = new StyleFontIcon("ssd-storage-icon");

    FontIcon<?> REMOVABLE_STORAGE = new StyleFontIcon("removable-storage-icon");

    FontIcon<?> NETWORK_STORAGE = new StyleFontIcon("network-storage-icon");

    FontIcon<?> DIRECTORY = new StyleFontIcon("directory-icon");

    FontIcon<?> FILE = new StyleFontIcon("file-icon");

    FontIcon<?> OTHER = new StyleFontIcon("other-icon");

    FontIcon<?> DIRECTORY_LINK = new StyleFontIcon("directory-link-icon");

    FontIcon<?> FILE_LINK = new StyleFontIcon("file-link-icon");

    FontIcon<?> OTHER_LINK = new StyleFontIcon("other-link-icon");

    FontIcon<?> ERROR_DIRECTORY = new StyleFontIcon("error-directory-icon");

    FontIcon<?> ERROR_FILE = new StyleFontIcon("error-file-icon");

    FontIcon<?> ERROR_OTHER = new StyleFontIcon("error-other-icon");

    FontIcon<?> ERROR_DIRECTORY_LINK = new StyleFontIcon("error-directory-link-icon");

    FontIcon<?> ERROR_FILE_LINK = new StyleFontIcon("error-file-link-icon");

    FontIcon<?> ERROR_OTHER_LINK = new StyleFontIcon("error-other-link-icon");

    FontIcon<?> PENDING_DIRECTORY = new StyleFontIcon("pending-directory-icon");

    FontIcon<?> PENDING_FILE = new StyleFontIcon("pending-file-icon");

    FontIcon<?> PENDING_OTHER = new StyleFontIcon("pending-other-icon");

    FontIcon<?> PENDING_DIRECTORY_LINK = new StyleFontIcon("pending-directory-link-icon");

    FontIcon<?> PENDING_FILE_LINK = new StyleFontIcon("pending-file-link-icon");

    FontIcon<?> PENDING_OTHER_LINK = new StyleFontIcon("pending-other-link-icon");
}
