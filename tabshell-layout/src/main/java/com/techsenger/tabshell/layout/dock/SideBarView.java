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
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarView<T extends SideBarViewModel> extends AbstractPaneView<T> {

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
            this.minimizedTab.setClosable(this.minimizedTabClosable);
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
                        if (popup == null) {
                            addPopup();
                        }
                        openTabInPopup(barTab);
                    }
                }

            });
            addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
                if (isReady() && popup != null && popup.getTabPane().getTabs().size() > 1) {
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
                    if (popup == null) {
                        addPopup();
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
    protected void build(T viewModel) {
        super.build(viewModel);
        tabPane.setSide(viewModel.getSide());
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
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        tabPane.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
            if (this.popup != null && !hasMouseMovedToPopup(e) && !containsSelectedTab()) {
                removePopup();
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

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        if (this.popup != null) {
            this.popup.deinitialize();
        }
    }

    protected Tab createRestoreTab() {
        var tab = new BarRestoreTab();
        var button = new Button(null, new FontIconView(CoreIcons.RESTORE_WINDOW));
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
        this.layout.restoreTabDock(this, tabDock);
    }

    protected void addPopup() {
        this.popup = createPopup();
        this.layout.addTabPopup(popup);
    }

    protected void removePopup() {
        if (this.popup != null) {
            this.layout.removeTabPopup(this.popup);
            this.popup.deinitialize();
            getViewModel().setPopup(null);
            this.popup = null;
            tabPane.getSelectionModel().selectFirst(); // resetting selection
        }
    }

    protected void openTabInPopup(BarTab barTab) {
        barTab.setShownInPopup(true);
        var minimizedTab = barTab.getMinimizedTab();
        minimizedTab.setClosable(false);
        var tabs = this.popup.getTabPane().getTabs();
        if (tabs.size() == 1) {
            var otherBarTab = getBarTab(((ComponentTab) tabs.get(0)));
            if (!otherBarTab.isSelected()) {
                closeTabInPopup(otherBarTab.getMinimizedTab());
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
        var barTab = getBarTab(tab);
        barTab.setShownInPopup(false);
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

    void updatePopupSize(double width, double height) {
        if (this.popup != null) {
            this.popup.updateSize(width, height);
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
        var list = tabPane.getTabs().subList(restoreTabIndex, restoreTabIndex + tabDock.getDetachedTabs().size() + 1);
        for (var i = 1; i < list.size(); i++) { // excluding restore tab
            BarTab barTab = (BarTab) list.get(i);
            barTab.deinit();
        }
        list.clear();
        return tabDock;
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
