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

package com.techsenger.tabshell.layout.dock;

import atlantafx.base.theme.Styles;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.layout.tabhost.TabHostView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockView<T extends TabDockViewModel<?>, S extends TabDockComponent<?>> extends TabHostView<T, S> {

    static final double MIN_SIZE = 100; // temp

    private final FontIconView dragIconView = new FontIconView(SharedIcons.DRAG_VERTICAL);

    private final HBox tabHeaderFirstBox = new HBox();

    private final Button minimizeButton = new Button(null, new FontIconView(SharedIcons.REMOVE));

    private final HBox tabHeaderLastBox = new HBox(minimizeButton);

    protected TabDockView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        var layoutView = getComponent().getLayout().getView();
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);
        tabPane.setDragAndDropContext(layoutView.getDragAndDropContext());

        tabHeaderFirstBox.getStyleClass().add("tab-header-first-box");
        minimizeButton.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        tabHeaderLastBox.getStyleClass().add("tab-header-last-box");

        var css = TabDockView.class.getResource("tab-dock.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
        this.getNode().getStyleClass().add("tab-dock");

        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.getFirstArea().getChildren().add(tabHeaderFirstBox);
        tabHeaderArea.getLastArea().getChildren().add(tabHeaderLastBox);
        tabHeaderArea.setTabDragCursor(Cursor.CLOSED_HAND);
        tabHeaderArea.setTabDragContentFactory(layoutView::createTabDragContent);
        tabHeaderArea.setTabDragScrollStep(10.0);

    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        var layoutView = getComponent().getLayout().getView();
        ValueUtils.callAndAddListener(viewModel.draggableProperty(), (ov, oldV, newV) -> {
            if (newV) {
                dragIconView.setOnDragDetected(e -> layoutView.handleDockDragDetected(this, dragIconView, e));
                dragIconView.setOnMouseDragged(e -> layoutView.handleDockMouseDragged(this, dragIconView, e));
                dragIconView.setOnMouseReleased(e -> layoutView.handleDockMouseReleased(this, dragIconView, e));
                tabHeaderFirstBox.getChildren().add(0, dragIconView);
            } else {
                tabHeaderFirstBox.getChildren().remove(dragIconView);
            }
        });
        var tabPane = getNode();
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (tabPane.getTabs().isEmpty() && viewModel.getMinimizedPosition() == null) {
                layoutView.processEmptyTabPane(tabPane);
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        var layoutView = getComponent().getLayout().getView();
        minimizeButton.setOnAction(e -> layoutView.minimizeTabDock(this));
        var tabPane = getNode();
        tabPane.addTabDragHandler(tab -> layoutView.handleTabDrag((ComponentTab) tab));
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addTabDropHandler((tab, s) -> layoutView.handleTabDrop((ComponentTab) tab));
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> layoutView.handleTabHeaderAreaMouseDragOver(tabPane, e));
    }

    protected Button getMinimizeButton() {
        return minimizeButton;
    }

    protected FontIconView getDragIconView() {
        return dragIconView;
    }

    protected HBox getTabHeaderFirstBox() {
        return tabHeaderFirstBox;
    }

    protected HBox getTabHeaderLastBox() {
        return tabHeaderLastBox;
    }
}
