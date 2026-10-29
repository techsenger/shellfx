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

import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.UriUtils;
import com.techsenger.shellfx.storage.view.FileCellUpdater;
import com.techsenger.shellfx.storage.view.DecorationCellUtils;
import java.util.function.Consumer;
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
public class LocationCell<F extends StorageFile> extends ListCell<F> {

    private final Label label = new Label();

    private final FontIconView iconView = new FontIconView();

    private final HBox box = new HBox();

    private final boolean valueCell;

    private final Consumer<? super F> decorate;

    private final FileCellUpdater<F> updater;

    /**
     * Creates a cell.
     *
     * @param valueCell whether the cell shows the chosen location rather than an item of the list
     * @param decorate gives a location its decoration, which the cell shows while it is decorated
     */
    public LocationCell(boolean valueCell, Consumer<? super F> decorate) {
        this.valueCell = valueCell;
        this.decorate = decorate;
        this.updater = new FileCellUpdater<>(file -> DecorationCellUtils.updateDecoration(label, iconView, file));
        this.box.setAlignment(Pos.CENTER_LEFT);
        this.box.getChildren().setAll(this.iconView, this.label);
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
            updater.unobserve();
            DecorationCellUtils.updateStyles(label, iconView, null);
            setGraphic(null);
        } else {
            if (!this.valueCell) {
                var level = item.isRoot() ? 0
                        : UriUtils.getPathSegments(item.getStorage().getUri(), item.getUri()).size();
                this.box.setPadding(new Insets(0, 0, 0, level * Spacing.getHorizontal()));
            }
            this.label.setText(item.getName());
            showFile(item);
        }
    }

    private void showFile(F item) {
        decorate.accept(item);
        updater.observe(item, StorageFile::decorationProperty);
        if (getGraphic() != this.box) {
            setGraphic(this.box);
        }
    }
}
