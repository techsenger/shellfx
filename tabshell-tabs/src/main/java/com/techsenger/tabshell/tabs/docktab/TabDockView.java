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

package com.techsenger.tabshell.tabs.docktab;

import atlantafx.base.theme.Styles;
import com.techsenger.mvvm4fx.core.ParentView;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.tabs.TabHostView;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockView<T extends TabDockViewModel> extends TabHostView<T> {

    private final AbstractDockTabView<?> dockTab;

    private final Button minimizeButton = new Button(null, new FontIconView(CoreIcons.REMOVE));

    private final HBox controlContainer = new HBox(minimizeButton);

    protected TabDockView(AbstractDockTabView<?> dockTab, T viewModel) {
        super(viewModel);
        this.dockTab = dockTab;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);

        minimizeButton.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        controlContainer.setAlignment(Pos.CENTER_LEFT);
        controlContainer.getStyleClass().add("control-container");
        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.getLastArea().getChildren().add(controlContainer);

        var css = TabDockView.class.getResource("tabdock.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
        this.getNode().getStyleClass().add("tab-dock");
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        ValueUtils.callAndAddListener(viewModel.draggableProperty(), (ov, oldV, newV) -> {
            var tabHeaderArea = getTabHeaderArea();
            tabHeaderArea.getFirstArea().getChildren().clear();
            if (newV != null) {
                var iconView = new FontIconView(CoreIcons.DRAG_VERTICAL);
                iconView.setOnDragDetected(e -> this.dockTab.handleDragDetectedOnDock(this, iconView, e));
                iconView.setOnMouseDragged(e -> this.dockTab.handleMouseDraggedOnDock(this, iconView, e));
                iconView.setOnMouseReleased(e -> this.dockTab.handleMouseReleasedOnDock(this, iconView, e));
                tabHeaderArea.getFirstArea().getChildren().add(iconView);
            }
        });
    }

    /**
     * Provide access to {@link AbstractDockTabView}.
     *
     * @param parent
     */
    @Override
    protected void setParent(ParentView<?> parent) {
        super.setParent(parent);
    }

    protected TabPaneProSkin.TabHeaderArea getTabHeaderArea() {
        var tabPane = getNode();
        TabPaneProSkin sourceSkin = (TabPaneProSkin) tabPane.getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
        return tabHeaderArea;
    }

}
