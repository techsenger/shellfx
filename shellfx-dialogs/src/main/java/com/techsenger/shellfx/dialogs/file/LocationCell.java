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

package com.techsenger.shellfx.dialogs.file;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.storage.FileCellUtils;
import com.techsenger.shellfx.storage.FileStyleResolver;
import com.techsenger.shellfx.storage.GenericFile;
import com.techsenger.shellfx.storage.UriUtils;
import java.util.List;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 * A cell of the location combo box that shows a directory of the hierarchy, indented by its depth.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class LocationCell<F extends GenericFile> extends ListCell<F> {

    private final Label label = new Label();

    private final HBox box = new HBox();

    private final boolean valueCell;

    private final Supplier<@Nullable FileStyleResolver<F>> styleResolver;

    private @Nullable FontIconView iconView;

    public LocationCell(boolean valueCell, Supplier<@Nullable FileStyleResolver<F>> styleResolver) {
        this.valueCell = valueCell;
        this.styleResolver = styleResolver;
        this.box.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Keeps the current styles of the cell, so {@link #restoreStyles()} can bring them back. Call it when the
     * view gets a style resolver.
     */
    public void saveStyles() {
        FileCellUtils.saveStyles(List.of(label));
    }

    /**
     * Gives the cell the styles {@link #saveStyles()} kept. Call it when the view loses its style resolver.
     */
    public void restoreStyles() {
        FileCellUtils.restoreStyles(List.of(label));
        if (iconView != null) {
            iconView.setIconStyle(null);
        }
    }

    /**
     * Applies the styles the style resolver gives for the shown file; does nothing without a resolver, as then the
     * styles belong to the developer.
     */
    public void updateStyles() {
        var resolver = styleResolver.get();
        if (resolver != null) {
            FileCellUtils.updateStyles(label, iconView, getItem(), resolver);
        }
    }

    @Override
    protected void updateItem(F item, boolean empty) {
        //many updates happening, resulting in visible flickering of the value cell's content
        if (valueCell && item == getItem()) {
            return;
        }
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            this.iconView = null;
            setGraphic(null);
            updateStyles();
        } else {
            if (!this.valueCell) {
                var level = item.isRoot() ? 0
                        : UriUtils.getPathSegments(item.getStorage().getUri(), item.getUri()).size();
                this.box.setPadding(new Insets(0, 0, 0, level * Spacing.getHorizontal()));
            }
            this.label.setText(item.getName());
            var icon = item.isRoot() ? item.getStorage().getIcon() : item.getIcon();
            if (this.iconView == null || this.iconView.getIcon() != icon) {
                this.iconView = new FontIconView(icon);
                this.box.getChildren().setAll(this.iconView, this.label);
            }
            if (getGraphic() != this.box) {
                setGraphic(this.box);
            }
            updateStyles();
        }
    }
}
