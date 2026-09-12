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

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.tab.AbstractHostTabView;
import com.techsenger.shellfx.demo.HostFactory;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.dockhost.ModelNodeBuilder;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.dockhost.UtilityDockContainerView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.icon.PlainFontIcon;
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
public class BrowserMainTabView<VM extends BrowserMainTabViewModel<?>> extends AbstractHostTabView<VM>
        implements UtilityDockContainerView<VM> {

    private final class Composer extends AbstractHostTabView<VM>.Composer
            implements UtilityDockContainerView.Composer {

        private final BrowserMainTabView<VM> view = BrowserMainTabView.this;

        @Override
        public void compose() {
            super.compose();
            var historyManager = getShell().getViewModel().getContext().getHistoryManager();
            var dockHost = HostFactory.createDockHost(getShell(),
                    () -> historyManager.getHistory(BrowserMainTabHistory.class).getDockHost());
            getModifiableChildren().add(dockHost);
            view.dockHost = dockHost;
            view.addLayout();

            var leftTabDock = HostFactory.createLeftTabDock(getShell(), dockHost);
            var textViewer = createTextViewer();

            var modelRoot = ModelNodeBuilder.root(Orientation.HORIZONTAL, s -> s
                        .area(leftTabDock)
                        .mainArea(textViewer));

            dockHost.getComposer().applyModel(modelRoot);
        }

        @Override
        public void addUtilityDock(TabDockView<?> tabDock) {
            view.dockHost.getComposer().addTabDock(tabDock, Side.BOTTOM, 250);
        }

        protected MenuAwareAreaView<?> createTextViewer() {
            var appearance = getShell().getViewModel().getContext().getSettings().getAppearance();
            var params = new MenuAwareAreaParams(appearance);
            var viewModel = new MenuAwareAreaViewModel<>(params);
            var areaView = new MenuAwareAreaView<>(viewModel, view);
            areaView.initialize();
            return areaView;
        }
    }

    private DockHostView<?> dockHost;

    public BrowserMainTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
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
        return new BrowserMainTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var removeButton = new Button(null, new FontIconView(new PlainFontIcon(983924)));
        removeButton.getStyleClass().add(StyleClasses.SIZE_L);
        var addButton = new Button(null, new FontIconView(new PlainFontIcon(984085)));
        addButton.getStyleClass().add(StyleClasses.SIZE_L);
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
