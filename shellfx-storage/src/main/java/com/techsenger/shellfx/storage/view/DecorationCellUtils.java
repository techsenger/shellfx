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
import com.techsenger.shellfx.storage.style.IconStyleDecoration;
import javafx.scene.control.Labeled;

/**
 * Utilities for the cells that show decorated elements, such as files and storages.
 *
 * @author Pavel Castornii
 */
public final class DecorationCellUtils {

    /**
     * Sets the icon of the cell and applies the style to the cell and its icon from the decoration of the file; does
     * nothing if the decoration is outdated, which a reset leaves until the file is decorated again.
     *
     * @param cell the cell whose texts to style
     * @param iconView the icon of the cell, or {@code null} if the cell has no icon
     * @param file the file shown in the cell
     */
    public static void updateDecoration(Labeled cell, @Nullable FontIconView iconView, StorageFile file) {
        var decoration = file.getDecoration();
        if (decoration != null) {
            updateDecoration(cell, iconView, decoration);
        }
    }

    /**
     * Sets the icon of the cell and applies the style to the cell and its icon from the decoration; does nothing if
     * the decoration is outdated in its style.
     *
     * @param cell the cell whose texts to style
     * @param iconView the icon of the cell, or {@code null} if the cell has no icon
     * @param decoration the decoration of what is shown in the cell
     */
    public static void updateDecoration(Labeled cell, @Nullable FontIconView iconView, IconStyleDecoration decoration) {
        var style = decoration.style();
        if (style == null) {
            return;
        }
        cell.setStyle(style.textStyle());
        if (iconView != null) {
            iconView.setIcon(decoration.icon());
            iconView.setIconStyle(style.iconStyle());
        }
    }

    /**
     * Applies the styles of the file to the cell and its icon, and clears them if the file has none.
     *
     * @param cell the cell whose texts to style
     * @param iconView the icon of the cell, or {@code null} if the cell has no icon
     * @param file the file shown in the cell, or {@code null} if the cell is empty
     */
    public static void updateStyles(Labeled cell, @Nullable FontIconView iconView, @Nullable StorageFile file) {
        var decoration = file != null ? file.getDecoration() : null;
        var fileStyle = decoration != null ? decoration.style() : null;
        cell.setStyle(fileStyle != null ? fileStyle.textStyle() : null);
        if (iconView != null) {
            iconView.setIconStyle(fileStyle != null ? fileStyle.iconStyle() : null);
        }
    }

    private DecorationCellUtils() {
        // empty
    }
}
