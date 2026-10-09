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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIconView;
import javafx.scene.control.Labeled;

/**
 * Utilities for the cells that show files.
 *
 * @author Pavel Castornii
 */
public final class FileCellUtils {

    private static final String ORIGINAL_STYLE_KEY = "originalStyle";

    /**
     * Keeps the current styles of the cells in their properties. Call it for all cells of a view when the view gets
     * a style resolver, so {@link #restoreStyles(Iterable)} can bring the styles back when the resolver is removed.
     *
     * @param cells all cells of the view
     */
    public static void saveStyles(Iterable<? extends Labeled> cells) {
        for (var cell : cells) {
            cell.getProperties().put(ORIGINAL_STYLE_KEY, cell.getStyle());
        }
    }

    /**
     * Gives the cells the styles {@link #saveStyles(Iterable)} kept, and clears the style of their icons. A cell
     * without a kept style, which appeared while the resolver was set, gets no style.
     *
     * @param cells all cells of the view
     */
    public static void restoreStyles(Iterable<? extends Labeled> cells) {
        for (var cell : cells) {
            cell.setStyle((String) cell.getProperties().remove(ORIGINAL_STYLE_KEY));
            if (cell.getGraphic() instanceof FontIconView iconView) {
                iconView.setIconStyle(null);
            }
        }
    }

    /**
     * Applies the styles the resolver gives for the file to the cell and its icon, and clears them if there are none.
     * Call it only when the view has a resolver: without one the styles belong to the developer.
     *
     * @param cell the cell whose texts to style
     * @param iconView the icon of the cell, or {@code null} if the cell has no icon
     * @param file the file shown in the cell, or {@code null} if the cell is empty
     * @param resolver the resolver of the view
     */
    public static <F extends GenericFile> void updateStyles(Labeled cell, @Nullable FontIconView iconView,
            @Nullable F file, FileStyleResolver<F> resolver) {
        FileStyle fileStyle = file != null ? resolver.resolve(file) : null;
        cell.setStyle(fileStyle != null ? fileStyle.textStyle() : null);
        if (iconView != null) {
            iconView.setIconStyle(fileStyle != null ? fileStyle.iconStyle() : null);
        }
    }

    private FileCellUtils() {
        // empty
    }
}
