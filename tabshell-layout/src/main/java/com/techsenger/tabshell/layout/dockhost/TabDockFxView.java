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

package com.techsenger.tabshell.layout.dockhost;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import java.util.List;
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

    public class Composer extends TabHostFxView<P>.Composer implements TabDockComposer {

        @Override
        public void remove() {
            dockHost.getComposer().removeTabDock(TabDockFxView.this);
        }

        @Override
        protected @Unmodifiable List<? extends TabFxView<?>> getDetachedTabs() {
            return super.getDetachedTabs();
        }
    }

    private final FontIconView dragIconView = new FontIconView(LayoutIcons.DRAG_VERTICAL);

    private final HBox tabHeaderFirstBox = new HBox();

    private final Button minimizeButton = new Button(null, new FontIconView(SharedIcons.REMOVE));

    private final HBox tabHeaderLastBox = new HBox(minimizeButton);

    private DockHostFxView<?> dockHost;

    protected TabDockFxView() {
        super(false);
    }

    @Override
    public void setDraggable(boolean value) {
        if (value) {
            if (dragIconView.getParent() == null) {
            tabHeaderFirstBox.getChildren().add(0, dragIconView);
            }
        } else {
            if (dragIconView.getParent() != null) {
                tabHeaderFirstBox.getChildren().remove(dragIconView);
            }
        }
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new TabDockFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);

        this.dragIconView.getStyleClass().add(StyleClasses.COMPACT);
        tabHeaderFirstBox.getStyleClass().add("tab-header-first-box");
        minimizeButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.COMPACT);
        tabHeaderLastBox.getStyleClass().add("tab-header-last-box");

        var css = TabDockFxView.class.getResource("tab-dock.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
        this.getNode().getStyleClass().add("tab-dock");

        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.getFirstArea().getChildren().add(tabHeaderFirstBox);
        tabHeaderArea.getLastArea().getChildren().add(tabHeaderLastBox);
        tabHeaderArea.setTabDragCursor(Cursor.CLOSED_HAND);
        tabHeaderArea.setTabDragContentFactory((s) -> dockHost.createTabDragContent(s));
        tabHeaderArea.setTabDragScrollStep(10.0);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var tabPane = getNode();
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            if (tabPane.getTabs().isEmpty()
                    && getPresenter().getTransitionState() != TabDockTransitionState.TO_MINIMIZED) {
                dockHost.getComposer().removeTabDock(this);
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        dragIconView.setOnDragDetected(e -> dockHost.onDockDragDetected(this, dragIconView, e));
        dragIconView.setOnMouseDragged(e -> dockHost.onDockMouseDragged(this, dragIconView, e));
        dragIconView.setOnMouseReleased(e -> dockHost.onDockMouseReleased(this, dragIconView, e));
        minimizeButton.setOnAction(e -> {
            getPresenter().onMinimize();
            dockHost.getComposer().minimizeTabDock(this);
            getPresenter().onMinimized();
        });
        var tabPane = getNode();
        tabPane.addTabDragHandler(tab -> dockHost.onTabDrag((ComponentTab) tab));
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addTabDropHandler((tab, s) -> dockHost.onTabDrop((ComponentTab) tab));
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> dockHost.onTabHeaderAreaMouseDragOver(tabPane, e));
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

    protected DockHostFxView<?> getDockHost() {
        return dockHost;
    }

    protected void setDockHost(DockHostFxView<?> dockHost) {
        if (this.dockHost == null) {
            this.dockHost = dockHost;
            getNode().setDragAndDropContext(dockHost.getDragAndDropContext());
        }
    }
}
