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

import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabFxView;
import com.techsenger.tabshell.layout.dock.DockLayoutFxView;
import com.techsenger.tabshell.layout.dock.DockLayoutHistory;
import com.techsenger.tabshell.layout.dock.DockLayoutPresenter;
import com.techsenger.tabshell.layout.dock.SideBarPolicy;
import com.techsenger.tabshell.layout.dock.TabDockFxView;
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
public class DockLayoutTabFxView extends AbstractShellTabFxView<DockLayoutTabPresenter> {

    private final class Composer extends AbstractShellTabFxView.Composer {

        private final DockLayoutTabFxView view = DockLayoutTabFxView.this;

        @Override
        public void compose() {
            super.compose();
            var historyManager = view.getShell().getPresenter().getHistoryManager();
            var layout = createLayout(() -> historyManager.getHistory(DockLayoutTabHistory.class).getDockLayout());
            layout.getPresenter().initialize();
            layout.getComposer().setBottomBarPolicy(SideBarPolicy.EXISTS_ALWAYS);
            view.getModifiableChildren().add(layout);
            view.layout = layout;
            view.addLayout();

            var textViewer = createTextViewer();
            textViewer.getPresenter().initialize();

            var splitSpace = layout.getComposer().createSplitSpace(Orientation.HORIZONTAL);
            splitSpace.getPresenter().initialize();
            splitSpace.getComposer().addChild(textViewer);
            layout.getComposer().setRoot(splitSpace);
            layout.getComposer().setMain(textViewer);

            var tabDock = layout.getComposer().createTabDock();
            tabDock.getPresenter().initialize();
            fillTabs(tabDock);
            splitSpace.getComposer().addChild(tabDock);
        }

        protected TextViewerFxView createTextViewer() {
            var v = new TextViewerFxView();
            var p = new TextViewerPresenter(v);
            return v;
        }

        protected DockLayoutFxView<?> createLayout(HistoryProvider<DockLayoutHistory> provider) {
            var v = new DockLayoutFxView<>();
            var p = new DockLayoutPresenter<>(v, provider);
            // vm.setBottomBarPolicy(SideBarPolicy.EXISTS_ALWAYS); todo:
            return v;
        }

        private void fillTabs(TabDockFxView<?> tabDock) {
            for (var i = 0; i < 10; i++) {
                var tabView = new DockableTabFxView();
                var tabPresenter = new DockableTabPresenter(tabView, i);
                tabPresenter.initialize();
                tabDock.getComposer().addTab(tabView);
            }
        }
    }

    private DockLayoutFxView<?> layout;

    public DockLayoutTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DockLayoutTabFxView.Composer();
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
        getContentPane().getChildren().add(toolbar);

    }

    private void addLayout() {
        getContentPane().getChildren().add(layout.getNode());
        var lastArea = layout.getBottomBar().getLastArea();
        var hBox = new HBox(new Label("Label 1"), new Separator(Orientation.VERTICAL),
                new Label("Label 2"));
        hBox.setRotate(-180);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 10, 0, 0));
        lastArea.getChildren().add(hBox);
    }
}
