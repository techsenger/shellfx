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
import com.techsenger.tabpanepro.core.TabEvent;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.List;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseDragEvent;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockFxView<P extends TabDockPresenter<?>> extends TabHostFxView<P> implements TabDockView {

    static final double MIN_SIZE = 100; // temp

    public class Composer extends TabHostFxView<P>.Composer implements TabDockView.Composer {

        private final TabDockFxView<P> view = TabDockFxView.this;

        private DockHostFxView<?> dockHost;

        @Override
        public void removeTab(TabFxView<?> tab) {
            super.removeTab(tab);
            if (view.getNode().getTabs().isEmpty()
                    && getPresenter().getTransitionState() != TabDockTransitionState.TO_MINIMIZED) {
                dockHost.getComposer().closeTabDock(view);
            }
        }

        @Override
        public void close() {
            dockHost.getComposer().closeTabDock(TabDockFxView.this);
        }

        @Override
        protected @Unmodifiable List<? extends TabFxView<?>> getDetachedTabs() {
            return super.getDetachedTabs();
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

    private final FontIconView dragIconView = new FontIconView(LayoutIcons.DRAG_VERTICAL);

    private final Button minimizeButton = new Button(null, new FontIconView(LayoutIcons.REMOVE));

    private final Button closeButton = new Button(null, new FontIconView(LayoutIcons.CLOSE));

    protected TabDockFxView() {
        super(false);
    }

    @Override
    public void setDraggable(boolean value) {
        if (value) {
            if (dragIconView.getParent() == null) {
                getTabHeaderFirstBox().getChildren().add(getDragIconViewIndex(), dragIconView);
            }
        } else {
            if (dragIconView.getParent() != null) {
                getTabHeaderLastBox().getChildren().remove(dragIconView);
            }
        }
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        if (minimizable) {
            if (this.minimizeButton.getParent() == null) {
                getTabHeaderLastBox().getChildren().add(getMinimizeButtonIndex(), this.minimizeButton);
            }
        } else {
            if (this.minimizeButton.getParent() != null) {
                getTabHeaderLastBox().getChildren().remove(this.minimizeButton);
            }
        }
    }

    @Override
    public void setClosable(boolean closable) {
        if (closable) {
            if (this.closeButton.getParent() == null) {
                getTabHeaderLastBox().getChildren().add(getCloseButtonIndex(), this.closeButton);
            }
        } else {
            if (this.closeButton.getParent() != null) {
                getTabHeaderLastBox().getChildren().remove(this.closeButton);
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
        minimizeButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.COMPACT);
        minimizeButton.setTooltip(new Tooltip("Minimize"));
        closeButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.COMPACT);
        closeButton.setTooltip(new Tooltip("Close"));

        this.getNode().getStyleClass().add("tab-dock");

        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.setTabDragCursor(Cursor.CLOSED_HAND);
        tabHeaderArea.setTabDragContentFactory((s) -> getComposer().getDockHost().createTabDragContent(s));
        tabHeaderArea.setTabDragScrollStep(10.0);

    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        dragIconView.setOnDragDetected(e -> getComposer().getDockHost().onDockDragDetected(this, dragIconView, e));
        dragIconView.setOnMouseDragged(e -> getComposer().getDockHost().onDockMouseDragged(this, dragIconView, e));
        dragIconView.setOnMouseReleased(e -> getComposer().getDockHost().onDockMouseReleased(this, dragIconView, e));
        minimizeButton.setOnAction(e -> {
            getPresenter().onMinimize();
            getComposer().getDockHost().getComposer().minimizeTabDock(this);
            getPresenter().onMinimized();
        });
        closeButton.setOnAction(e -> getPresenter().requestClose());

        var tabPane = getNode();
        tabPane.addEventHandler(TabEvent.TAB_DRAG_STARTED, (e) -> {
            if (e.getTarget() == getNode()) {
                getComposer().getDockHost().onTabDrag(e.getTab());
                e.consume();
            }
        });
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addEventHandler(TabEvent.TAB_DROPPED, (e) -> {
            if (e.getTarget() == getNode()) {
                getComposer().getDockHost().onTabDrop(e.getTab());
                e.consume();
            }
        });
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> getComposer().getDockHost().onTabHeaderAreaMouseDragOver(tabPane, e));
    }

    protected Button getMinimizeButton() {
        return minimizeButton;
    }

    protected int getMinimizeButtonIndex() {
        var size = getTabHeaderLastBox().getChildren().size();
        if (this.closeButton.getParent() == null) {
            return size;
        } else {
            return size - 1;
        }
    }

    protected Button getCloseButton() {
        return closeButton;
    }

    protected int getCloseButtonIndex() {
        return getTabHeaderLastBox().getChildren().size();
    }

    protected FontIconView getDragIconView() {
        return dragIconView;
    }

    protected int getDragIconViewIndex() {
        return 0;
    }
}
