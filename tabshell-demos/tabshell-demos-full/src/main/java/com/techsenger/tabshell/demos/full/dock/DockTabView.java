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
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.layout.dock.SpaceReceiver;
import com.techsenger.tabshell.layout.dock.TabDockView;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

/**
 *
 * @author Pavel Castornii
 */
public class DockTabView extends AbstractShellTabView<DockTabViewModel> {

    private final DockLayoutView<?> layout;

    private final TextViewerView textViewer;

    public DockTabView(ShellView<?> shell, DockTabViewModel viewModel) {
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
    protected void build(DockTabViewModel viewModel) {
        super.build(viewModel);
        var removeButton = new Button(null, new FontIconView(CoreIcons.REMOVE));
        var addButton = new Button(null, new FontIconView(CoreIcons.ADD));
        var toolbar = new ToolBar(removeButton, addButton);
        toolbar.getStyleClass().add(StyleClasses.BLEND);
        getContentPane().getChildren().addAll(toolbar, layout.getNode());
    }

    @Override
    protected void postInitialize(DockTabViewModel viewModel) {
        super.postInitialize(viewModel);
        textViewer.initialize();

        this.layout.initialize();
        this.layout.setMain(this.textViewer);

        var splitSpaceView = layout.createSplitSpace();
        layout.setRoot(splitSpaceView);
        splitSpaceView.getChildren().add(textViewer);
        
        var tabDockView = layout.createTabDock();
        fillTabs(tabDockView);
        tabDockView.getViewModel().setSpaceReceiver(SpaceReceiver.NEXT);
        splitSpaceView.getChildren().add(tabDockView);
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
