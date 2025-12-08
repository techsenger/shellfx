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

    static final double MIN_SIZE = 100; // temp

    private final DockLayoutView<?> layout;

    private final FontIconView dragIconView = new FontIconView(CoreIcons.DRAG_VERTICAL);

    private final HBox tabHeaderFirstBox = new HBox();

    private final Button minimizeButton = new Button(null, new FontIconView(CoreIcons.REMOVE));

    private final HBox tabHeaderLastBox = new HBox(minimizeButton);

    protected TabDockView(DockLayoutView<?> layout, T viewModel) {
        super(viewModel);
        this.layout = layout;
    }

    @Override
    public AbstractTabDockComposer<?> getComposer() {
        return (AbstractTabDockComposer<?>) super.getComposer();
    }

    @Override
    protected AbstractTabDockComposer<?> createComposer() {
        return (AbstractTabDockComposer<?>) super.createComposer();
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);
        tabPane.setDragAndDropContext(this.layout.getDragAndDropContext());

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
        tabHeaderArea.setTabDragContentFactory(this.layout::createTabDragContent);
        tabHeaderArea.setTabDragScrollStep(10.0);

    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        ValueUtils.callAndAddListener(viewModel.draggableProperty(), (ov, oldV, newV) -> {
            if (newV) {
                dragIconView.setOnDragDetected(e -> this.layout.handleDockDragDetected(this, dragIconView, e));
                dragIconView.setOnMouseDragged(e -> this.layout.handleDockMouseDragged(this, dragIconView, e));
                dragIconView.setOnMouseReleased(e -> this.layout.handleDockMouseReleased(this, dragIconView, e));
                tabHeaderFirstBox.getChildren().add(0, dragIconView);
            } else {
                tabHeaderFirstBox.getChildren().remove(dragIconView);
            }
        });
        var tabPane = getNode();
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (tabPane.getTabs().isEmpty() && viewModel.getMinimizedPosition() == null) {
                this.layout.processEmptyTabPane(tabPane);
            }
        });
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        minimizeButton.setOnAction(e -> this.layout.minimizeTabDock(this));
        var tabPane = getNode();
        tabPane.addTabDragHandler(tab -> this.layout.handleTabDrag((ComponentTab) tab));
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addTabDropHandler((tab, s) -> this.layout.handleTabDrop((ComponentTab) tab));
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> this.layout.handleTabHeaderAreaMouseDragOver(tabPane, e));
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
