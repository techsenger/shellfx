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
import com.techsenger.shellfx.storage.FileCellObserver;
import com.techsenger.shellfx.storage.FileCellUtils;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.UriUtils;
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

    private final HBox box = new HBox();

    private final boolean valueCell;

    private final FileCellObserver observer = new FileCellObserver(() -> {
        var item = getItem();
        if (item != null && !isEmpty()) {
            showFile(item);
        }
    });

    private @Nullable FontIconView iconView;

    public LocationCell(boolean valueCell) {
        this.valueCell = valueCell;
        this.box.setAlignment(Pos.CENTER_LEFT);
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
            observer.unobserve();
            this.iconView = null;
            setGraphic(null);
            FileCellUtils.updateStyles(label, null, null);
        } else {
            observer.observe(item.iconProperty(), item.styleProperty());
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
        var icon = item.getIcon();
        if (this.iconView == null || this.iconView.getIcon() != icon) {
            this.iconView = new FontIconView(icon);
            this.box.getChildren().setAll(this.iconView, this.label);
        }
        if (getGraphic() != this.box) {
            setGraphic(this.box);
        }
        FileCellUtils.updateStyles(label, iconView, item);
    }
}
