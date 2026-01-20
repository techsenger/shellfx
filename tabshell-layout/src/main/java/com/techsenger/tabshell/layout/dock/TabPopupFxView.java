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
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.core.tab.TabPort;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A TabPopup can display either one or two tabs. Two tabs are shown when the first tab is pinned and the mouse
 * is hovered over the second tab.
 *
 * @author Pavel Castornii
 */
public class TabPopupFxView<P extends TabPopupPresenter<?, ?>> extends AbstractAreaFxView<P> implements TabPopupView {

    private static final double RESIZE_MARGIN = 2.0;

    public class Composer extends AbstractAreaFxView.Composer implements TabPopupComposer {

        private final TabPopupFxView<?> view = TabPopupFxView.this;

        @Override
        public List<? extends TabPort> getTabs() {
            return view.getTabs().stream().map(v -> v.getPresenter().getPort()).toList();
        }

        public void addTab(TabFxView<?> tab) {
            view.modifiableTabs.add(tab);
            // there can be other children, so index is not used
            view.tabPane.getTabs().add(tab.getNode());
            view.getModifiableChildren().add(tab);
        }

        public void removeTab(TabFxView<?> tab) {
            view.modifiableTabs.remove(tab);
            // there can be other children, so index is not used
            view.tabPane.getTabs().remove(tab.getNode());
            view.getModifiableChildren().remove(tab);
        }
    }

    private final TabPanePro tabPane = new TabPanePro();

    private final VBox node = new VBox(tabPane);

    private final Button closeButton = new Button();

    private double onResizeX;

    private double onResizeY;

    private double onResizeWidth;

    private double onResizeHeight;

    private boolean isResizing = false;

    private final ObservableList<TabFxView<?>> modifiableTabs = FXCollections.observableArrayList();

    private final ObservableList<TabFxView<?>> tabs = FXCollections.unmodifiableObservableList(modifiableTabs);

    private final SideBarFxView<?> sideBar;

    public TabPopupFxView(SideBarFxView<?> sideBar) {
        super();
        this.sideBar = sideBar;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Region getNode() {
        return this.node;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.node.getStyleClass().addAll("tab-popup", getPresenter().getSide().name().toLowerCase());
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.getStyleClass().add(Styles.DENSE);

        TabPaneProSkin tabPaneSkin = (TabPaneProSkin) tabPane.getSkin();
        var lastArea = tabPaneSkin.getTabHeaderArea().getLastArea();
        lastArea.getChildren().add(closeButton);
        lastArea.setPadding(new Insets(0, SizeConstants.INSET, 0, 0));
        closeButton.getStyleClass().add(StyleClasses.CROSS_BUTTON);

        setInitialSizeAndPosition();
        var css = TabPopupFxView.class.getResource("tab-popup.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var presenter = getPresenter();
        node.addEventFilter(MouseEvent.MOUSE_EXITED, (e) -> {
            if (!hasMouseMovedToSideBar(e) && !isResizing && !sideBar.containsSelectedTab()
                    && !presenter.isClosing()) {
                presenter.setClosing(true);
                sideBar.closeLastTabInPopup();
                sideBar.getComposer().removePopupFromLayout();
            }
        });
        // resizing
        node.addEventFilter(MouseEvent.MOUSE_MOVED, (e) -> {
            if (!isResizing) {
                if (isOnEdge(e.getX(), e.getY())) {
                    setResizeCursor();
                } else {
                    restoreCursor();
                }
            }
        });
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, (e) -> {
            if (isOnEdge(e.getX(), e.getY())) {
                isResizing = true;
                onResizeX = e.getSceneX();
                onResizeY = e.getSceneY();
                onResizeWidth = this.node.getWidth();
                onResizeHeight = this.node.getHeight();
                e.consume();
            }
        });
        node.addEventFilter(MouseEvent.MOUSE_DRAGGED, (e) -> {
            if (isResizing) {
                double deltaX = e.getSceneX() - onResizeX;
                double deltaY = e.getSceneY() - onResizeY;
                handleResize(deltaX, deltaY);
                e.consume();
            }
        });
        node.addEventFilter(MouseEvent.MOUSE_RELEASED, (e) -> {
            if (isResizing) {
                isResizing = false;
                restoreCursor();
                e.consume();
            }
        });
        closeButton.setOnAction(e -> {
            if (!presenter.isClosing()) {
                presenter.setClosing(true);
                sideBar.closeLastTabInPopup();
                sideBar.getComposer().removePopupFromLayout();
            }
        });
    }

    protected TabPanePro getTabPane() {
        return tabPane;
    }

    ObservableList<TabFxView<?>> getTabs() {
        return tabs;
    }

    void updateSize(double centerWidth, double centerHeight) {
        double width = this.node.getWidth();
        double height = this.node.getHeight();
        switch (getPresenter().getSide()) {
            case RIGHT:
                width = Math.min(centerWidth, width);
                height = centerHeight;
                break;
            case BOTTOM:
                height = Math.min(centerHeight, height);
                width = centerWidth;
                break;
            case LEFT:
                width = Math.min(centerWidth, width);
                height = centerHeight;
                break;
            default:
                throw new AssertionError();
        }
        node.setPrefSize(width, height);
        node.setMinSize(width, height);
        node.setMaxSize(width, height);
    }

    private void setResizeCursor() {
        switch (getPresenter().getSide()) {
            case RIGHT:
                this.node.setCursor(Cursor.W_RESIZE);
                break;
            case BOTTOM:
                this.node.setCursor(Cursor.N_RESIZE);
                break;
            case LEFT:
                this.node.setCursor(Cursor.E_RESIZE);
                break;
            default:
                throw new AssertionError();
        }
    }

    private void restoreCursor() {
        this.node.setCursor(Cursor.DEFAULT);
    }

    private void setInitialSizeAndPosition() {
        var centerDimension = sideBar.getCenterDimension();
        double width = centerDimension.getWidth();
        double height = centerDimension.getHeight();
        var presenter = getPresenter();
        switch (presenter.getSide()) {
            case RIGHT:
                width = Math.min(presenter.getOldWidth(), width);
                StackPane.setAlignment(node, Pos.TOP_RIGHT);
                break;
            case BOTTOM:
                height = Math.min(presenter.getOldHeight(), height);
                StackPane.setAlignment(node, Pos.BOTTOM_LEFT);
                break;
            case LEFT:
                width = Math.min(presenter.getOldWidth(), width);
                StackPane.setAlignment(node, Pos.TOP_LEFT);
                break;
            default:
                throw new AssertionError();
        }
        node.setPrefSize(width, height);
        node.setMinSize(width, height);
        node.setMaxSize(width, height);
    }

    private boolean hasMouseMovedToSideBar(MouseEvent e) {
        switch (getPresenter().getSide()) {
            case RIGHT:
                return (e.getX() >= this.node.getWidth() && e.getY() <= this.node.getHeight() && e.getY() >= 0);
            case BOTTOM:
                return (e.getY() >= this.node.getHeight() && e.getX() <= this.node.getWidth() && e.getX() >= 0);
            case LEFT:
                return (e.getX() <= 0 && e.getY() <= this.node.getHeight() && e.getY() >= 0);
            default:
                throw new AssertionError();
        }
    }

   private boolean isOnEdge(double x, double y) {
        switch (getPresenter().getSide()) {
            case RIGHT:
                return x <= RESIZE_MARGIN;
            case BOTTOM:
                return y <= RESIZE_MARGIN;
            case LEFT:
                return x >= node.getWidth() - RESIZE_MARGIN;
            default:
                throw new AssertionError();
        }
    }

    private void handleResize(double deltaX, double deltaY) {
        double newHeight;
        double newWidth;
        switch (getPresenter().getSide()) {
            case BOTTOM:
                newHeight = onResizeHeight - deltaY;
                this.node.setPrefHeight(newHeight);
                this.node.setMinHeight(newHeight);
                this.node.setMaxHeight(newHeight);
                break;
            case LEFT:
                newWidth = onResizeWidth + deltaX;
                this.node.setPrefWidth(newWidth);
                this.node.setMinWidth(newWidth);
                this.node.setMaxWidth(newWidth);
                break;
            case RIGHT:
                newWidth = onResizeWidth - deltaX;
                this.node.setPrefWidth(newWidth);
                this.node.setMinWidth(newWidth);
                this.node.setMaxWidth(newWidth);
                break;
        }
    }
}
