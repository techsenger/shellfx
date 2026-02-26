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

package com.techsenger.tabshell.demo.dock;

import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.demo.HostFactory;
import com.techsenger.tabshell.layout.dockhost.DockHostFxView;
import com.techsenger.tabshell.layout.dockhost.TabDockFxView;
import com.techsenger.tabshell.layout.dockhost.UtilityDockContainerFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class DockHostTabFxView extends AbstractTabFxView<DockHostTabPresenter>
        implements UtilityDockContainerFxView {

    private final class Composer extends AbstractTabFxView<DockHostTabPresenter>.Composer
            implements UtilityDockContainerFxView.Composer, TestInterface {

        private final DockHostTabFxView view = DockHostTabFxView.this;

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);
            var historyManager = view.getShell().getPresenter().getHistoryManager();
            var dockHost = HostFactory
                    .createDockHost(view.getShell(),
                            () -> historyManager.getHistory(DockHostTabHistory.class).getDockHost());
            view.getModifiableChildren().add(dockHost);
            view.dockHost = dockHost;
            view.addLayout();

            var textViewer = createTextViewer();
            textViewer.getPresenter().initialize();
            dockHost.getComposer().setMain(textViewer);
            dockHost.getRoot().getComposer().addChild(textViewer);
        }

        @Override
        public void addUtilityDock(TabDockFxView<?> tabDock) {
            view.dockHost.getComposer().addTabDock(tabDock, Side.BOTTOM, 250);
        }

        protected TextViewerFxView createTextViewer() {
            var v = new TextViewerFxView();
            var p = new TextViewerPresenter(v);
            return v;
        }
    }

    private DockHostFxView<?> dockHost;

    public DockHostTabFxView(ShellFxView<?> shell) {
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
        return new DockHostTabFxView.Composer();
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
        getContentBox().getChildren().add(toolbar);

    }

    private void addLayout() {
        getContentBox().getChildren().add(dockHost.getNode());
        var lastArea = dockHost.getBottomBar().getLastArea();
        var hBox = new HBox(new Label("Label 1"), new Separator(Orientation.VERTICAL),
                new Label("Label 2"));
        hBox.setRotate(-180);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 10, 0, 0));
        lastArea.getChildren().add(hBox);
    }
}
