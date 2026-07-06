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

package com.techsenger.shellfx.demo.browser;

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.tab.AbstractHostTabFxView;
import com.techsenger.shellfx.demo.HostFactory;
import com.techsenger.shellfx.demo.main.TestInterface;
import com.techsenger.shellfx.layout.dockhost.DockHostFxView;
import com.techsenger.shellfx.layout.dockhost.TabDockFxView;
import com.techsenger.shellfx.layout.dockhost.UtilityDockContainerFxView;
import com.techsenger.shellfx.material.icon.PlainFontIcon;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
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
public class BrowserMainTabFxView extends AbstractHostTabFxView<BrowserMainTabPresenter>
        implements UtilityDockContainerFxView<BrowserMainTabPresenter> {

    private final class Composer extends AbstractHostTabFxView<BrowserMainTabPresenter>.Composer
            implements UtilityDockContainerFxView.Composer, TestInterface {

        private final BrowserMainTabFxView view = BrowserMainTabFxView.this;

        @Override
        public void compose() {
            super.compose();
            var historyManager = getShell().getPresenter().getContext().getHistoryManager();
            var dockHost = HostFactory
                    .createDockHost(getShell(),
                            () -> historyManager.getHistory(BrowserMainTabHistory.class).getDockHost());
            getModifiableChildren().add(dockHost);
            view.dockHost = dockHost;
            view.addLayout();

            var textViewer = createTextViewer();
            dockHost.getComposer().setMain(textViewer);
            dockHost.getComposer().getRoot().getComposer().addChild(textViewer);
        }

        @Override
        public void addUtilityDock(TabDockFxView<?> tabDock) {
            view.dockHost.getComposer().addTabDock(tabDock, Side.BOTTOM, 250);
        }

        protected MenuAwareAreaFxView createTextViewer() {
            var v = new MenuAwareAreaFxView(view);
            var params = new MenuAwareAreaParams(getShell().getPresenter().getContext().getSettings().getAppearance());
            var p = new MenuAwareAreaPresenter(v, params);
            p.initialize();
            return v;
        }
    }

    private DockHostFxView<?> dockHost;

    public BrowserMainTabFxView(ShellFxView<?> shell) {
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
        return new BrowserMainTabFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var removeButton = new Button(null, new FontIconView(new PlainFontIcon(983924)));
        removeButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, StyleClasses.SIZE_L);
        var addButton = new Button(null, new FontIconView(new PlainFontIcon(984085)));
        addButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, StyleClasses.SIZE_L);
        addButton.setOnAction((e) -> {
            removeButton.setVisible(!removeButton.isVisible());
        });
        var toolbar = new ToolBar(removeButton, addButton);
        toolbar.getStyleClass().add(StyleClasses.BLEND);
        getContentBox().getChildren().add(toolbar);

    }

    private void addLayout() {
        getContentBox().getChildren().add(dockHost.getNode());
        var lastArea = dockHost.getComposer().getBottomBar().getLastArea();
        var hBox = new HBox(new Label("Label 1"), new Separator(Orientation.VERTICAL),
                new Label("Label 2"));
        hBox.setRotate(-180);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0, 10, 0, 0));
        lastArea.getChildren().add(hBox);
    }
}
