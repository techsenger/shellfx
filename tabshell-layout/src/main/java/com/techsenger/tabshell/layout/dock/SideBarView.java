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
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.collections.ListSynchronizer;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import static javafx.geometry.Side.RIGHT;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarView<T extends SideBarViewModel> extends AbstractPaneView<T> {

    private static class TabInfo {

        private final SideBarTab sideBarTab;

        private final boolean closable;

        TabInfo(SideBarTab sideBarTab, boolean closable) {
            this.sideBarTab = sideBarTab;
            this.closable = closable;
        }

        public SideBarTab getSideBarTab() {
            return sideBarTab;
        }

        public boolean isClosable() {
            return closable;
        }
    }

    private static class SideBarTab extends Tab {

        private final ComponentTab minimizedTab;

        private boolean shownInPopup;

        SideBarTab(ComponentTab minimizedTab) {
            this.minimizedTab = minimizedTab;
        }

        public ComponentTab getMinimizedTab() {
            return minimizedTab;
        }

        public boolean isShownInPopup() {
            return shownInPopup;
        }

        public void setShownInPopup(boolean shownInPopup) {
            this.shownInPopup = shownInPopup;
        }
    }

    private static final class RestoreButton extends Button {

        RestoreButton(String string, Node node) {
            super(string, node);
        }
    }

    private static final Object STATE_KEY = new Object();

    private class SideTabHeaderSkin extends TabPaneProSkin.TabHeaderSkin {

        SideTabHeaderSkin(TabPaneProSkin.TabHeaderContext context) {
            super(context);
            addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> {
                if (!isRestoreTab(this) && isReady()) {
                    var sideBarTab = ((SideBarTab) context.getTab());
                    if (!sideBarTab.isShownInPopup()) {
                        if (popup == null) {
                            showPopup();
                        }
                        openTabInPopup(sideBarTab);
                    }
                }

            });
            addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
                if (!isRestoreTab(this) && isReady() && popup != null && popup.getTabPane().getTabs().size() > 1) {
                    closeLastTabInPopup();
                }
            });
            addEventFilter(MouseEvent.MOUSE_PRESSED, (e) -> {
                if (!isRestoreTab(this)) {
                    SideBarTab sideBarTab = (SideBarTab) getContext().getTab();
                    if (sideBarTab.isSelected()) {
                        // there is one open tabs
                        tabPane.getSelectionModel().selectFirst();
                        e.consume();
                    } else {
                        if (popup == null) {
                            showPopup();
                            openTabInPopup(sideBarTab);
                        } else {
                            // there are two open tabs
                            closeOtherTabInPopup(sideBarTab.getMinimizedTab());
                        }
                    }
                }
            });
        }

        private boolean isReady() {
            return this.getAnimationState() == TabPaneProSkin.TabAnimationState.NONE;
        }
    }

    private final DockLayoutView<?> layout;

    private final ObservableList<TabDockView<?>> tabDocks = FXCollections.observableArrayList();

    private final TabPanePro tabPane = new TabPanePro();

    private final ListSynchronizer<TabDockView<?>, TabDockViewModel> tabDocksSynchronizer;

    private TabPopupView<?> popup;

    public SideBarView(DockLayoutView<?> layout, T viewModel) {
        super(viewModel);
        this.layout = layout;
        this.tabDocksSynchronizer = new ListSynchronizer<TabDockView<?>, TabDockViewModel>(tabDocks,
                viewModel.getModifiableTabDocks(), v -> v.getViewModel());
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
            if (this.popup != null && !hasMouseMovedToPopup(e) && !containsSelectedTab()) {
                hidePopup();
            }
        });
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
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
        var button = new RestoreButton(null, new FontIconView(CoreIcons.RESTORE_WINDOW));
        button.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        button.setOnAction(e -> {
            hidePopup();
            handleRestoreButtonAction(tab);
        });
        tab.setGraphic(button);
        tab.setClosable(false);
        tab.getStyleClass().add("restore");
        return tab;
    }

    protected boolean isRestoreTab(TabPaneProSkin.TabHeaderSkin tabHeader) {
        var tab = tabHeader.getContext().getTab();
        if (tab.getGraphic() != null && tab.getGraphic().getClass() == RestoreButton.class) {
            return true;
        }
        return false;
    }

    protected SideBarTab createTab(ComponentTab tab) {
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

    protected void showPopup() {
        this.popup = createPopup();
        this.layout.showTabPopup(popup);
    }

    protected void hidePopup() {
        if (this.popup != null) {
            this.layout.hideTabPopup(this.popup);
            this.popup.deinitialize();
            getViewModel().setPopup(null);
            this.popup = null;
            tabPane.getSelectionModel().selectFirst(); // resetting selection
        }
    }

    protected void openTabInPopup(SideBarTab sideBarTab) {
        sideBarTab.setShownInPopup(true);
        var minimizedTab = sideBarTab.getMinimizedTab();
        saveState(sideBarTab);
        minimizedTab.setClosable(false);
        var tabs = this.popup.getTabPane().getTabs();
        if (tabs.size() == 1) {
            var otherSideBarTab = getState(((ComponentTab) tabs.get(0))).getSideBarTab();
            if (!otherSideBarTab.isSelected()) {
                closeTabInPopup(otherSideBarTab.getMinimizedTab());
            }
        } else if (tabs.size() == 2) {
            closeTabInPopup((ComponentTab) tabs.get(1));
        }
        tabs.add(minimizedTab);
        this.popup.getTabPane().getSelectionModel().select(tabs.size() - 1);
    }

    protected void closeLastTabInPopup() {
        var tp = this.popup.getTabPane();
        if (!tp.getTabs().isEmpty()) {
            var tab = tp.getTabs().get(tp.getTabs().size() - 1);
            closeTabInPopup((ComponentTab) tab);
        }
    }

    protected void closeOtherTabInPopup(ComponentTab tab) {
        var tp = this.popup.getTabPane();
        if (tp.getTabs().size() > 1) {
            var tab0 = tp.getTabs().get(0);
            if (tab0 != tab) {
                closeTabInPopup((ComponentTab) tab0);
            } else {
                closeTabInPopup((ComponentTab) tp.getTabs().get(1));
            }
        }
    }

    protected void closeTabInPopup(ComponentTab tab) {
        this.popup.getTabPane().getTabs().remove(tab);
        var sideBarTab = restoreState(tab);
        sideBarTab.setShownInPopup(false);
    }

    protected TabPopupView<?> createPopup() {
        var vm = getViewModel().createPopup();
        getViewModel().setPopup(vm);
        var popup = new TabPopupView(this, vm);
        popup.initialize();
        return popup;
    }

    Dimension2D getCenterDimension() {
        return this.layout.getCenterDimension();
    }

    void updatePopupSize() {
        if (this.popup != null) {
            this.popup.updateSize();
        }
    }

    boolean containsSelectedTab() {
        var index = this.tabPane.getSelectionModel().getSelectedIndex();
        return index > 0;
    }

    private void addTabDocks(List<? extends TabDockView<?>> tabDocks) {
        for (var tabDock : tabDocks) {
            this.tabPane.getTabs().add(createRestoreTab());
            for (var tab : tabDock.getDetachedTabs()) {
                var t = createTab((ComponentTab) tab.getNode());
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

    private void saveState(SideBarTab sideBarTab) {
        var minimizedTab = sideBarTab.getMinimizedTab();
        var state = new TabInfo(sideBarTab, minimizedTab.isClosable());
        minimizedTab.getProperties().put(STATE_KEY, state);
    }

    private SideBarTab restoreState(ComponentTab minimizedTab) {
        TabInfo state = (TabInfo) minimizedTab.getProperties().remove(STATE_KEY);
        minimizedTab.setClosable(state.isClosable());
        return state.getSideBarTab();
    }

    private TabInfo getState(ComponentTab minimizedTab) {
        TabInfo state = (TabInfo) minimizedTab.getProperties().get(STATE_KEY);
        return state;
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
