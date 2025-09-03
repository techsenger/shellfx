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

package com.techsenger.tabshell.layout.dock;

import atlantafx.base.theme.Styles;
import com.techsenger.mvvm4fx.core.ParentView;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.layout.tabhost.TabHostView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockView<T extends TabDockViewModel> extends TabHostView<T> {

    private final DockLayoutView<?> layout;

    private final Button minimizeButton = new Button(null, new FontIconView(CoreIcons.REMOVE));

    private final HBox controlContainer = new HBox(minimizeButton);

    protected TabDockView(DockLayoutView<?> layout, T viewModel) {
        super(viewModel);
        this.layout = layout;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);
        tabPane.setDragAndDropContext(this.layout.getDragAndDropContext());

        minimizeButton.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        controlContainer.setAlignment(Pos.CENTER_LEFT);
        controlContainer.getStyleClass().add("control-container");

        var css = TabDockView.class.getResource("tab-dock.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
        this.getNode().getStyleClass().add("tab-dock");

        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.getLastArea().getChildren().add(controlContainer);
        tabHeaderArea.setTabDragCursor(Cursor.CLOSED_HAND);
        tabHeaderArea.setTabDragContentFactory(this.layout::createTabDragContent);
        tabHeaderArea.setTabDragScrollStep(10.0);

    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        ValueUtils.callAndAddListener(viewModel.draggableProperty(), (ov, oldV, newV) -> {
            var tabHeaderArea = getTabHeaderArea();
            tabHeaderArea.getFirstArea().getChildren().clear();
            if (newV != null) {
                var iconView = new FontIconView(CoreIcons.DRAG_VERTICAL);
                iconView.setOnDragDetected(e -> this.layout.handleDragDetectedOnDock(this, iconView, e));
                iconView.setOnMouseDragged(e -> this.layout.handleMouseDraggedOnDock(this, iconView, e));
                iconView.setOnMouseReleased(e -> this.layout.handleMouseReleasedOnDock(this, iconView, e));
                tabHeaderArea.getFirstArea().getChildren().add(iconView);
            }
        });
        var tabPane = getNode();
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (tabPane.getTabs().isEmpty()) {
                this.layout.processEmptyTabPane(tabPane);
            }
        });
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        minimizeButton.setOnAction(e -> this.layout.minimizeTabDock(this));
        var tabPane = getNode();
        tabPane.addTabDragHandler(tab -> this.layout.handleDragOnTab((ComponentTab) tab));
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addTabDropHandler((tab, s) -> this.layout.handleDropOnTab((ComponentTab) tab));
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> this.layout.handleMouseDragOverOnTabHeaderArea(tabPane, e));
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

    protected Button getMinimizeButton() {
        return minimizeButton;
    }
}
