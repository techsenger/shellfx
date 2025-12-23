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
import com.techsenger.tabpanepro.core.skin.TabHeaderAreaPolicy;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.area.AbstractAreaView;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarView<T extends SideBarViewModel<?>, S extends SideBarComponent<?>> extends AbstractAreaView<T, S> {

    private static final class BarRestoreTab extends Tab {

    }

    private static final class BarTab extends Tab {

        private final ComponentTab minimizedTab;

        private final boolean minimizedTabClosable;

        private boolean shownInPopup;

        BarTab(ComponentTab minimizedTab) {
            this.minimizedTab = minimizedTab;
            this.minimizedTabClosable = this.minimizedTab.isClosable();
            this.minimizedTab.getProperties().put(BAR_TAB_KEY, this);
        }

        public ComponentTab getMinimizedTab() {
            return minimizedTab;
        }

        public boolean isMinimizedTabClosable() {
            return minimizedTabClosable;
        }

        public boolean isShownInPopup() {
            return shownInPopup;
        }

        public void setShownInPopup(boolean shownInPopup) {
            this.shownInPopup = shownInPopup;
        }

        public void deinit() {
            this.minimizedTab.getView().getViewModel().setClosable(this.minimizedTabClosable);
            this.minimizedTab.getProperties().remove(BAR_TAB_KEY);
        }
    }

    private final class BarRestoreTabHeaderSkin extends TabPaneProSkin.TabHeaderSkin {

        BarRestoreTabHeaderSkin(TabPaneProSkin.TabHeaderContext context) {
            super(context);
        }
    }

    private final class BarTabHeaderSkin extends TabPaneProSkin.TabHeaderSkin {

        BarTabHeaderSkin(TabPaneProSkin.TabHeaderContext context) {
            super(context);
            addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> {
                if (isReady()) {
                    var barTab = ((BarTab) context.getTab());
                    if (!barTab.isShownInPopup()) {
                        if (getComponent().getPopup() == null) {
                            getComponent().addPopupToLayout(getViewModel().getSide());
                        }
                        openTabInPopup(barTab);
                    }
                }

            });
            addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
                var popup = getComponent().getPopup();
                if (isReady() && popup != null && popup.getTabs().size() > 1) {
                    closeLastTabInPopup();
                }
            });
            addEventFilter(MouseEvent.MOUSE_PRESSED, (e) -> {
                BarTab barTab = (BarTab) getContext().getTab();
                if (barTab.isSelected()) {
                    // there is one open tabs
                    tabPane.getSelectionModel().selectFirst();
                    e.consume();
                } else {
                    if (getComponent().getPopup() == null) {
                        getComponent().addPopupToLayout(getViewModel().getSide());
                        openTabInPopup(barTab);
                    } else {
                        // there are two open tabs
                        closeOtherTabInPopup(barTab.getMinimizedTab());
                    }
                }
            });
        }

        private boolean isReady() {
            return this.getAnimationState() == TabPaneProSkin.TabAnimationState.NONE;
        }
    }

    private static final Object BAR_TAB_KEY = new Object();

    private final TabPanePro tabPane = new TabPanePro();

    public SideBarView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public TabPanePro getNode() {
        return tabPane;
    }

    /**
     * Returns the last area from {@link TabPanePro}.
     *
     * @return
     */
    public StackPane getLastArea() {
        TabPaneProSkin tabPaneSkin = (TabPaneProSkin) tabPane.getSkin();
        return tabPaneSkin.getTabHeaderArea().getLastArea();
    }

    @Override
    protected void build() {
        super.build();
        tabPane.setSide(getViewModel().getSide());
        tabPane.getStyleClass().addAll(TabPanePro.STYLE_CLASS_FLOATING, "side-bar");
        TabPaneProSkin tabPaneSkin = (TabPaneProSkin) tabPane.getSkin();
        tabPaneSkin.getTabHeaderArea().setTabHeaderFactory((c) -> {
            if (c.getTab().getClass() == BarRestoreTab.class) {
                return new BarRestoreTabHeaderSkin(c);
            } else {
                return new BarTabHeaderSkin(c);
            }
        });
        tabPaneSkin.getTabHeaderArea().setPolicy(TabHeaderAreaPolicy.ALWAYS_VISIBLE);

        var css = SideBarView.class.getResource("side-bar.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        tabPane.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
            if (getComponent().getPopup() != null && !hasMouseMovedToPopup(e) && !containsSelectedTab()) {
                removePopup();
            }
        });
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getComponent().getTabDocks().addListener((ListChangeListener<TabDockComponent<?>>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    addTabDocks(change.getAddedSubList());
                }
            }
        });
    }

    protected Tab createRestoreTab() {
        var tab = new BarRestoreTab();
        var button = new Button(null, new FontIconView(SharedIcons.RESTORE_WINDOW));
        button.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        button.setOnAction(e -> {
            removePopup();
            handleRestoreButtonAction(tab);
        });
        tab.setGraphic(button);
        tab.setClosable(false);
        tab.getStyleClass().add("restore");
        return tab;
    }

    protected BarTab createTab(ComponentTab tab) {
        var t = new BarTab(tab);
        t.setText(tab.getText());
        t.setGraphic(tab.getGraphic());
        t.setClosable(false);
        return t;
    }

    protected void handleRestoreButtonAction(Tab tab) {
        var restoreTabIndex = tabPane.getTabs().indexOf(tab);
        var tabDock = removeTabDock(restoreTabIndex);
        if (getComponent().getTabDocks().isEmpty()) {
            getComponent().getLayout().removeSideBar(getViewModel().getSide());
        }
        getComponent().getLayout().getView().restoreTabDock(tabDock);
    }

    protected void removePopup() {
        if (getComponent().getPopup() != null) {
            getComponent().removePopupFromLayout();
            tabPane.getSelectionModel().selectFirst(); // resetting selection
        }
    }

    protected void openTabInPopup(BarTab barTab) {
        barTab.setShownInPopup(true);
        var minimizedTab = barTab.getMinimizedTab();
        minimizedTab.getView().getViewModel().setClosable(false);
        var popupTabPane = getComponent().getPopup().getView().getTabPane();
        var tabs = popupTabPane.getTabs();
        if (tabs.size() == 1) {
            var otherBarTab = getBarTab(((ComponentTab) tabs.get(0)));
            if (!otherBarTab.isSelected()) {
                closeTabInPopup(otherBarTab.getMinimizedTab());
            }
        } else if (tabs.size() == 2) {
            closeTabInPopup((ComponentTab) tabs.get(1));
        }
        tabs.add(minimizedTab);
        popupTabPane.getSelectionModel().select(tabs.size() - 1);
    }

    protected void closeLastTabInPopup() {
        var tp = getComponent().getPopup().getView().getTabPane();
        if (!tp.getTabs().isEmpty()) {
            var tab = tp.getTabs().get(tp.getTabs().size() - 1);
            closeTabInPopup((ComponentTab) tab);
        }
    }

    protected void closeOtherTabInPopup(ComponentTab tab) {
        var tp = getComponent().getPopup().getView().getTabPane();
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
        var popupTabPane = getComponent().getPopup().getView().getTabPane();
        popupTabPane.getTabs().remove(tab);
        var barTab = getBarTab(tab);
        barTab.setShownInPopup(false);
    }

    Dimension2D getCenterDimension() {
        return getComponent().getLayout().getView().getCenterDimension();
    }

    void updatePopupSize(double width, double height) {
        if (getComponent().getPopup() != null) {
            getComponent().getPopup().getView().updateSize(width, height);
        }
    }

    boolean containsSelectedTab() {
        var index = this.tabPane.getSelectionModel().getSelectedIndex();
        return index > 0;
    }

    private void addTabDocks(List<? extends TabDockComponent<?>> tabDocks) {
        for (var tabDock : tabDocks) {
            this.tabPane.getTabs().add(createRestoreTab());
            for (var tab : tabDock.getView().getDetachedTabs()) {
                var t = createTab((ComponentTab) tab.getNode());
                this.tabPane.getTabs().add(t);
            }
        }
    }

    private TabDockView<?, ?> removeTabDock(int restoreTabIndex) {
        var dockIndex = findTabDockIndex(restoreTabIndex);
        var tabDock = getComponent().removeTabDock(dockIndex);
        // removing restoreTab and dock tabs from side bar
        var list = tabPane.getTabs().subList(restoreTabIndex,
                restoreTabIndex + tabDock.getView().getDetachedTabs().size() + 1);
        for (var i = 1; i < list.size(); i++) { // excluding restore tab
            BarTab barTab = (BarTab) list.get(i);
            barTab.deinit();
        }
        list.clear();
        return tabDock.getView();
    }

    private BarTab getBarTab(ComponentTab minimizedTab) {
        BarTab barTab = (BarTab) minimizedTab.getProperties().get(BAR_TAB_KEY);
        return barTab;
    }

    /**
     * Finds the tab dock index by the index of the tab with the restore button.
     *
     * @param restoreTab
     * @return
     */
    private int findTabDockIndex(int restoreTabIndex) {
        var index = -1;
        for (var i = 0; i < getComponent().getTabDocks().size(); i++) {
            index += 1; // restore button
            if (index == restoreTabIndex) {
                return i;
            }
            var tabDock = getComponent().getTabDocks().get(i);
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
