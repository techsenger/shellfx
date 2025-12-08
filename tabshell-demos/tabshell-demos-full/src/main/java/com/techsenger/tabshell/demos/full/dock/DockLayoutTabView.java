/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.layout.dock.TabDockView;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import com.techsenger.tabshell.shared.style.SharedIcons;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutTabView extends AbstractShellTabView<DockLayoutTabViewModel> {

    private final DockLayoutView<?> layout;

    private final TextViewerView textViewer;

    public DockLayoutTabView(ShellView<?> shell, DockLayoutTabViewModel viewModel) {
        super(shell, viewModel);
        this.layout = new DockLayoutView<>(viewModel.getLayout());
        this.textViewer = new TextViewerView(viewModel.getTextViewer());
    }

    @Override
    public void requestFocus() {

    }

    public DockLayoutView<?> getLayout() {
        return layout;
    }

    @Override
    protected void preInitialize(DockLayoutTabViewModel viewModel) {
        super.preInitialize(viewModel);
        textViewer.initialize();

        this.layout.initialize();
        this.layout.setMain(this.textViewer);

        var splitSpaceView = layout.createSplitSpace(Orientation.HORIZONTAL);
        layout.setRoot(splitSpaceView);
        splitSpaceView.getChildren().add(textViewer);

        var tabDockView = layout.createTabDock();
        fillTabs(tabDockView);
        splitSpaceView.getChildren().add(tabDockView);
    }

    @Override
    protected void build(DockLayoutTabViewModel viewModel) {
        super.build(viewModel);
        var removeButton = new Button(null, new FontIconView(SharedIcons.REMOVE));
        var addButton = new Button(null, new FontIconView(SharedIcons.ADD));
        addButton.setOnAction((e) -> {
            removeButton.setVisible(!removeButton.isVisible());
        });
        var toolbar = new ToolBar(removeButton, addButton);
        toolbar.getStyleClass().add(StyleClasses.BLEND);
        getContentPane().getChildren().addAll(toolbar, layout.getNode());


        var lastArea = this.layout.getBottomBar().getLastArea();
        var hBox = new HBox(new Label("Label 1"), new Separator(Orientation.VERTICAL),
                new Label("Label 2"));
        hBox.setRotate(-180);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 10, 0, 0));
        lastArea.getChildren().add(hBox);
    }

    @Override
    protected void postDeinitialize(DockLayoutTabViewModel viewModel) {
        super.postDeinitialize(viewModel);
        this.textViewer.deinitialize();
        this.layout.deinitialize();
    }

    private void fillTabs(TabDockView<?> tabDock) {
        for (var i = 0; i < 10; i++) {
            var tabViewModel = new DockableTabViewModel(i);
            var tabView = new DockableTabView(tabViewModel);
            tabView.initialize();
            tabDock.openTab(tabView);
        }
    }
}
