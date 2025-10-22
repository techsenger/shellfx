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
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.collections.ListSynchronizer;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import static javafx.geometry.Side.RIGHT;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarView<T extends SideBarViewModel> extends AbstractPaneView<T> {

    private static class SideBarTab extends Tab {

        private final Tab minimizedTab;

        SideBarTab(Tab minimizedTab) {
            this.minimizedTab = minimizedTab;
        }

        public Tab getMinimizedTab() {
            return minimizedTab;
        }
    }

    private static class TabState {

        private final TabPane tabPane;

        private final int index;

        private final boolean closable;

        TabState(TabPane tabPane, int index, boolean closable) {
            this.tabPane = tabPane;
            this.index = index;
            this.closable = closable;
        }

        public TabPane getTabPane() {
            return tabPane;
        }

        public int getIndex() {
            return index;
        }

        public boolean isClosable() {
            return closable;
        }
    }

    private class SideTabHeaderSkin extends TabPaneProSkin.TabHeaderSkin {

        SideTabHeaderSkin(TabPaneProSkin.TabHeaderContext context) {
            super(context);
            addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> {
                if (!isRestoreTab(this) && this.getAnimationState() == TabPaneProSkin.TabAnimationState.NONE) {
                    var minimizedTabNode = ((SideBarTab) context.getTab()).getMinimizedTab();
                    var minimizedTab = ((ComponentTab) minimizedTabNode).getView();
                    if (popup != null) {
                        if (popup.getTab() != minimizedTab) {
                            hidePopup();
                            showPopup(minimizedTab);
                        }
                    } else {
                        showPopup(minimizedTab);
                    }
                }

            });
        }
    }

    private final DockLayoutView<?> layout;

    private final ObservableList<TabDockView<?>> tabDocks = FXCollections.observableArrayList();

    private final TabPanePro tabPane = new TabPanePro();

    private ListSynchronizer<TabDockView<?>, TabDockViewModel> listSynchronizer;

    /**
     * The original position of the tab.
     */
    private TabState popupTabState;

    private TabPopupView<?> popup;

    public SideBarView(DockLayoutView<?> layout, T viewModel) {
        super(viewModel);
        this.layout = layout;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public TabPanePro getNode() {
        return tabPane;
    }

    /**
     * Returns a modifiable list of minimized tab docks.
     *
     * @return
     */
    public ObservableList<TabDockView<?>> getTabDocks() {
        return tabDocks;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        tabPane.setSide(viewModel.getSide());
        tabPane.getStyleClass().addAll(TabPanePro.STYLE_CLASS_FLOATING, "side-bar");
        TabPaneProSkin tabPaneSkin = (TabPaneProSkin) tabPane.getSkin();
        tabPaneSkin.getTabHeaderArea().setTabHeaderFactory((c) -> new SideTabHeaderSkin(c));

        var css = SideBarView.class.getResource("side-bar.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        var tabPaneSkin = (TabPaneProSkin) tabPane.getSkin();
        tabPane.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
            if (!hasMouseMovedToPopup(e)) {
                hidePopup();
            }
        });
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        listSynchronizer = new ListSynchronizer<TabDockView<?>, TabDockViewModel>(tabDocks,
                viewModel.getModifiableTabDocks(), v -> v.getViewModel());
        tabDocks.addListener((ListChangeListener<TabDockView<?>>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    addTabDocks(change.getAddedSubList());
                }
            }
        });
    }

    protected Tab createRestoreTab() {
        var tab = new Tab();
        var button = new Button(null, new FontIconView(CoreIcons.RESTORE_WINDOW));
        button.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        button.setOnAction(e -> handleRestoreButtonAction(tab));
        tab.setGraphic(button);
        tab.setClosable(false);
        tab.getStyleClass().add("restore");
        return tab;
    }

    protected boolean isRestoreTab(SideTabHeaderSkin tabHeader) {
        var tab = tabHeader.getContext().getTab();
        if (tab.getGraphic() != null && tab.getGraphic().getClass() == Button.class) {
            return true;
        }
        return false;
    }

    protected SideBarTab createTabFor(Tab tab) {
        var t = new SideBarTab(tab);
        t.setText(tab.getText());
        t.setGraphic(tab.getGraphic());
        t.setClosable(false);
        return t;
    }

    protected void handleRestoreButtonAction(Tab tab) {
        var restoreTabIndex = tabPane.getTabs().indexOf(tab);
        var tabDock = removeTabDock(restoreTabIndex);
        this.layout.restoreTabDock(this, tabDock);
    }

    protected void showPopup(TabView<?> minimizedTab) {
        saveState(minimizedTab);
        var vm = getViewModel().createPopup(minimizedTab.getViewModel());
        getViewModel().setPopup(vm);
        this.popup = new TabPopupView(this, minimizedTab, vm);
        this.popup.initialize();
        this.layout.showTabPopup(popup);
    }

    protected void hidePopup() {
        if (this.popup != null) {
            this.layout.hideTabPopup();
            var minimizedTab = this.popup.getTab();
            this.popup.deinitialize();
            getViewModel().setPopup(null);
            this.popup = null;
            restoreState(minimizedTab);
        }
    }

    Point2D resolvePositionInLayout() {
        Bounds sideBarBounds = this.tabPane.getBoundsInLocal();
        Point2D positionInScene = this.tabPane.localToScene(sideBarBounds.getMinX(), sideBarBounds.getMinY());
        StackPane layoutNode = this.layout.getNode();
        Point2D layoutPositionInScene = layoutNode.localToScene(0, 0);
        double x = positionInScene.getX() - layoutPositionInScene.getX();
        double y = positionInScene.getY() - layoutPositionInScene.getY();
        return new Point2D(x, y);
    }

    private void addTabDocks(List<? extends TabDockView<?>> tabDocks) {
        for (var tabDock : tabDocks) {
            this.tabPane.getTabs().add(createRestoreTab());
            for (var tab : tabDock.getNode().getTabs()) {
                var t = createTabFor(tab);
                this.tabPane.getTabs().add(t);
            }
        }
    }

    private TabDockView<?> removeTabDock(int restoreTabIndex) {
        var dockIndex = findTabDockIndex(restoreTabIndex);
        var tabDock = this.tabDocks.remove(dockIndex);
        // removing restoreTab and dock tabs from side bar
        tabPane.getTabs().remove(restoreTabIndex, restoreTabIndex + tabDock.getNode().getTabs().size() + 1);
        return tabDock;
    }

    private void saveState(TabView<?> minimizedTab) {
        var minimizedTabNode = minimizedTab.getNode();
        var minimizedTabPane = minimizedTabNode.getTabPane();
        this.popupTabState = new TabState(minimizedTabPane, minimizedTabPane.getTabs().indexOf(minimizedTabNode),
                minimizedTabNode.isClosable());
        this.popupTabState.getTabPane().getTabs().remove(this.popupTabState.getIndex());
    }

    private void restoreState(TabView<?> minimizedTab) {
        var minimizedTabNode = minimizedTab.getNode();
        this.popupTabState.getTabPane().getTabs().add(this.popupTabState.getIndex(), minimizedTabNode);
        minimizedTabNode.setClosable(this.popupTabState.isClosable());
        this.popupTabState = null;
    }

    /**
     * Finds the tab dock index by the index of the tab with the restore button.
     *
     * @param restoreTab
     * @return
     */
    private int findTabDockIndex(int restoreTabIndex) {
        var index = -1;
        for (var i = 0; i < this.tabDocks.size(); i++) {
            index += 1; // restore button
            if (index == restoreTabIndex) {
                return i;
            }
            var tabDock = this.tabDocks.get(i);
            index += tabDock.getTabs().size();
        }
        return -1;
    }

    private boolean hasMouseMovedToPopup(MouseEvent e) {
        switch (getViewModel().getSide()) {
            case RIGHT:
                return (e.getX() <= 0 && e.getY() <= this.tabPane.getHeight() && e.getY() >= 0);
            case BOTTOM:
                return (e.getY() <= 0 && e.getX() <= this.tabPane.getWidth() && e.getX() >= 0);
            case LEFT:
                return (e.getX() >= this.tabPane.getWidth() && e.getY() <= this.tabPane.getHeight() && e.getY() >= 0);
            default:
                throw new AssertionError();
        }
    }
}
