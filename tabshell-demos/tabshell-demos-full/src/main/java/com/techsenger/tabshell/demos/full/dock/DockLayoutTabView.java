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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutTabView extends AbstractShellTabView<DockLayoutTabViewModel, DockLayoutTabComponent> {

    public DockLayoutTabView(DockLayoutTabViewModel viewModel) {
        super(viewModel);

    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        var removeButton = new Button(null, new FontIconView(SharedIcons.REMOVE));
        var addButton = new Button(null, new FontIconView(SharedIcons.ADD));
        addButton.setOnAction((e) -> {
            removeButton.setVisible(!removeButton.isVisible());
        });
        var toolbar = new ToolBar(removeButton, addButton);
        toolbar.getStyleClass().add(StyleClasses.BLEND);
        var layout = getComponent().getLayout();
        getContentPane().getChildren().addAll(toolbar, layout.getView().getNode());

        var lastArea = layout.getBottomSideBar().getView().getLastArea();
        var hBox = new HBox(new Label("Label 1"), new Separator(Orientation.VERTICAL),
                new Label("Label 2"));
        hBox.setRotate(-180);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 10, 0, 0));
        lastArea.getChildren().add(hBox);
    }

    void addLayout(DockLayoutView<?, ?> layout) {

    }
}
