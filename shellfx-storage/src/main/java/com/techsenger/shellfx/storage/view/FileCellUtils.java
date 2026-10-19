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

package com.techsenger.shellfx.storage.view;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.storage.StorageFile;
import javafx.scene.control.Labeled;

/**
 * Utilities for the cells that show files.
 *
 * @author Pavel Castornii
 */
public final class FileCellUtils {

    /**
     * Applies the styles of the file to the cell and its icon, and clears them if the file has none.
     *
     * @param cell the cell whose texts to style
     * @param iconView the icon of the cell, or {@code null} if the cell has no icon
     * @param file the file shown in the cell, or {@code null} if the cell is empty
     */
    public static void updateStyles(Labeled cell, @Nullable FontIconView iconView, @Nullable StorageFile file) {
        var fileStyle = file != null ? file.getStyle() : null;
        cell.setStyle(fileStyle != null ? fileStyle.textStyle() : null);
        if (iconView != null) {
            iconView.setIconStyle(fileStyle != null ? fileStyle.iconStyle() : null);
        }
    }

    private FileCellUtils() {
        // empty
    }
}
