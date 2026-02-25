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
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
public class TabDockFxView<P extends TabDockPresenter<?, ?>> extends TabHostFxView<P> implements TabDockView {

    static final double MIN_SIZE = 100; // temp

    private final FontIconView dragIconView = new FontIconView(LayoutIcons.DRAG_VERTICAL);

    private final HBox tabHeaderFirstBox = new HBox();

    private final Button minimizeButton = new Button(null, new FontIconView(LayoutIcons.REMOVE));

    private final HBox tabHeaderLastBox = new HBox(minimizeButton);

    private final BooleanProperty draggable = new SimpleBooleanProperty(true);

    private DockLayoutFxView<?> layout;

    protected TabDockFxView() {
        super();
    }

    @Override
    public boolean isDraggable() {
        return draggable.get();
    }

    @Override
    public void setDraggable(boolean value) {
        this.draggable.set(value);
    }

    @Override
    protected List<? extends TabFxView<?>> getDetachedTabs() {
        return super.getDetachedTabs();
    }

    @Override
    protected void build() {
        super.build();
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);

        tabHeaderFirstBox.getStyleClass().add("tab-header-first-box");
        minimizeButton.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        tabHeaderLastBox.getStyleClass().add("tab-header-last-box");

        var css = TabDockFxView.class.getResource("tab-dock.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
        this.getNode().getStyleClass().add("tab-dock");

        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.getFirstArea().getChildren().add(tabHeaderFirstBox);
        tabHeaderArea.getLastArea().getChildren().add(tabHeaderLastBox);
        tabHeaderArea.setTabDragCursor(Cursor.CLOSED_HAND);
        tabHeaderArea.setTabDragContentFactory((s) -> layout.createTabDragContent(s));
        tabHeaderArea.setTabDragScrollStep(10.0);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(draggable, (ov, oldV, newV) -> {
            if (newV) {
                dragIconView.setOnDragDetected(e -> layout.onDockDragDetected(this, dragIconView, e));
                dragIconView.setOnMouseDragged(e -> layout.onDockMouseDragged(this, dragIconView, e));
                dragIconView.setOnMouseReleased(e -> layout.onDockMouseReleased(this, dragIconView, e));
                tabHeaderFirstBox.getChildren().add(0, dragIconView);
            } else {
                tabHeaderFirstBox.getChildren().remove(dragIconView);
            }
        });
        var tabPane = getNode();
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (tabPane.getTabs().isEmpty() && getPresenter().getMinimizedPosition() == null) {
                layout.processEmptyTabPane(tabPane);
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        minimizeButton.setOnAction(e -> layout.minimizeTabDock(this));
        var tabPane = getNode();
        tabPane.addTabDragHandler(tab -> layout.onTabDrag((ComponentTab) tab));
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addTabDropHandler((tab, s) -> layout.onTabDrop((ComponentTab) tab));
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> layout.onTabHeaderAreaMouseDragOver(tabPane, e));
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

    protected DockLayoutFxView<?> getLayout() {
        return layout;
    }

    protected void setLayout(DockLayoutFxView<?> layout) {
        if (this.layout == null) {
            this.layout = layout;
            getNode().setDragAndDropContext(layout.getDragAndDropContext());
        }
    }
}
