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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class LocationCell extends ListCell<Location> {

    private final Label label = new Label();

    private final HBox box = new HBox();

    private final boolean valueCell;

    public LocationCell(boolean valueCell) {
        this.valueCell = valueCell;
        this.box.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(Location item, boolean empty) {
        //many updates happening, resulting in visible flickering of the value cell's content
        if (valueCell && item == getItem()) {
            return;
        }
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            if (!this.valueCell) {
                this.box.setPadding(new Insets(0, 0, 0, item.getLevel() * Spacing.HORIZONTAL));
            }
            this.label.setText(item.getName());
            this.box.getChildren().clear();
            this.box.getChildren().addAll(new FontIconView(item.getIcon()), this.label);
            setGraphic(this.box);
        }
    }
}
