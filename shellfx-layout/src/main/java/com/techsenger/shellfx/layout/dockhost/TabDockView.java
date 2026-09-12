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

package com.techsenger.shellfx.layout.dockhost;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.layout.style.LayoutIcons;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.tabpanepro.core.TabEvent;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseDragEvent;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockView<VM extends TabDockViewModel<?>> extends TabHostView<VM> {

    static final double MIN_SIZE = 100; // TEMP TEMP TEMP

    public class Composer extends TabHostView<VM>.Composer implements TabDockComposer {

        private final TabDockView<VM> view = TabDockView.this;

        private final ObjectProperty<SpaceResolver> spaceResolver = new SimpleObjectProperty<>(SpaceResolver.nearest());

        private DockHostView<?> dockHost;

        public ObjectProperty<SpaceResolver> spaceResolverProperty() {
            return spaceResolver;
        }

        public SpaceResolver getSpaceResolver() {
            return spaceResolver.get();
        }

        public void setSpaceResolver(SpaceResolver resolver) {
            spaceResolver.set(resolver);
        }

        @Override
        public void removeTab(TabView<?> tab) {
            super.removeTab(tab);
            if (view.getNode().getTabs().isEmpty()
                    && getViewModel().getTransitionState() != TabDockTransitionState.TO_MINIMIZED) {
                view.getViewModel().close();
            }
        }

        @Override
        public void close() {
            dockHost.getComposer().closeTabDock(TabDockView.this);
        }

        public void setDockHost(DockHostView<?> dockHost) {
            if (this.dockHost == null) {
                this.dockHost = dockHost;
                getNode().setDragAndDropContext(dockHost.getDragAndDropContext());
            }
        }

        @Override
        protected @Unmodifiable List<? extends TabView<?>> getDetachedTabs() {
            return super.getDetachedTabs();
        }

        protected DockHostView<?> getDockHost() {
            return dockHost;
        }
    }

    private final FontIconView dragIconView = new FontIconView(LayoutIcons.DRAG_VERTICAL);

    private final Button minimizeButton = new Button(null, new FontIconView(LayoutIcons.REMOVE));

    private final Button closeButton = new Button(null, new FontIconView(LayoutIcons.CLOSE));

    protected TabDockView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new TabDockView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var tabPane = getNode();
        tabPane.setTabDragEnabled(true);
        tabPane.setTabDropEnabled(true);

        this.dragIconView.getStyleClass().add(StyleClasses.SIZE_S);
        minimizeButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        minimizeButton.setTooltip(new Tooltip("Minimize"));
        closeButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        closeButton.setTooltip(new Tooltip("Close"));

        this.getNode().getStyleClass().add("tab-dock");

        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.setTabDragCursor(Cursor.CLOSED_HAND);
        tabHeaderArea.setTabDragContentFactory((s) -> getComposer().getDockHost()
                .getDragAndDropHandler().createTabDragContent(s));
        tabHeaderArea.setTabDragScrollStep(10.0);

    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(viewModel.draggableProperty(), (ov, oldV, newV) -> updateDraggable(newV));
        ValueUtils.callAndAddListener(viewModel.minimizableProperty(), (ov, oldV, newV) -> updateMinimizable(newV));
        ValueUtils.callAndAddListener(viewModel.closableProperty(), (ov, oldV, newV) -> updateClosable(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        dragIconView.setOnDragDetected(e -> provideDragAndDropHandler().onDockDragDetected(this, dragIconView, e));
        dragIconView.setOnMouseDragged(e -> provideDragAndDropHandler().onDockMouseDragged(this, dragIconView, e));
        dragIconView.setOnMouseReleased(e -> provideDragAndDropHandler().onDockMouseReleased(this, dragIconView, e));
        minimizeButton.setOnAction(e -> {
            getViewModel().onMinimize();
            getComposer().getDockHost().getComposer().minimizeTabDock(this);
            getViewModel().onMinimized();
        });
        closeButton.setOnAction(e -> getViewModel().onCloseRequest());

        var tabPane = getNode();
        tabPane.addEventHandler(TabEvent.TAB_DRAG_STARTED, (e) -> {
            if (e.getTarget() == getNode()) {
                provideDragAndDropHandler().onTabDrag(e.getTab());
                e.consume();
            }
        });
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addEventHandler(TabEvent.TAB_DROPPED, (e) -> {
            if (e.getTarget() == getNode()) {
                provideDragAndDropHandler().onTabDrop(e.getTab());
                e.consume();
            }
        });
        TabPaneProSkin.TabHeaderArea tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER,
                e -> provideDragAndDropHandler().onTabHeaderAreaMouseDragOver(tabPane, e));
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

    private void updateDraggable(boolean value) {
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

    private void updateMinimizable(boolean value) {
        if (value) {
            if (this.minimizeButton.getParent() == null) {
                getTabHeaderLastBox().getChildren().add(getMinimizeButtonIndex(), this.minimizeButton);
            }
        } else {
            if (this.minimizeButton.getParent() != null) {
                getTabHeaderLastBox().getChildren().remove(this.minimizeButton);
            }
        }
    }

    private void updateClosable(boolean value) {
        if (value) {
            if (this.closeButton.getParent() == null) {
                getTabHeaderLastBox().getChildren().add(getCloseButtonIndex(), this.closeButton);
            }
        } else {
            if (this.closeButton.getParent() != null) {
                getTabHeaderLastBox().getChildren().remove(this.closeButton);
            }
        }
    }

    private DockHostView.DragAndDropHandler provideDragAndDropHandler() {
        var dockHost = getComposer().getDockHost();
        Objects.requireNonNull(dockHost, "TabDock is not attached to a DockHost");
        return dockHost.getDragAndDropHandler();
    }
}
