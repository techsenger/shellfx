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
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabHeaderAreaPolicy;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarFxView<P extends SideBarPresenter<?>> extends AbstractAreaFxView<P> implements SideBarView {

    private static final class BarRestoreTab extends Tab {

    }

    private static final class BarTab extends Tab {

        private final Tab minimizedTab;

        private final boolean minimizedTabClosable;

        private boolean shownInPopup;

        BarTab(Tab minimizedTab) {
            this.minimizedTab = minimizedTab;
            this.minimizedTabClosable = this.minimizedTab.isClosable();
            this.minimizedTab.getProperties().put(BAR_TAB_KEY, this);
        }

        public Tab getMinimizedTab() {
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
            TabFxView<?> tabFxView = (TabFxView<?>) FxViewUtils.getView(this.minimizedTab);
            tabFxView.getPresenter().setClosable(this.minimizedTabClosable);
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
                        if (getComposer().getPopup() == null) {
                            getComposer().addPopupToLayout(getPresenter().getSide());
                        }
                        openTabInPopup(barTab);
                    }
                }

            });
            addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
                var popup = getComposer().getPopup();
                if (isReady() && popup != null && popup.getComposer().getTabs().size() > 1) {
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
                    if (getComposer().getPopup() == null) {
                        getComposer().addPopupToLayout(getPresenter().getSide());
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

    public class Composer extends AbstractAreaFxView<P>.Composer implements SideBarView.Composer {

        private final SideBarFxView<P> view = SideBarFxView.this;

        private DockHostFxView<?> dockHost;

        // todo: do we need this collection?
        private final ObservableList<TabDockFxView<?>> modifiableTabDocks = FXCollections.observableArrayList();

        private final @Unmodifiable ObservableList<TabDockFxView<?>> tabDocks =
                FXCollections.unmodifiableObservableList(modifiableTabDocks);

        /**
         * Returns an unmodifiable list of minimized tab docks.
         *
         * @return
         */
        @Override
        public @Unmodifiable List<TabDockPort> getTabDockPorts() {
            return getTabDocks().stream()
                    .map(t -> (TabDockPort) t.getPresenter())
                    .toList();
        }

        public TabPopupPort getPopupPort() {
            var popup = getPopup();
            if (popup != null) {
                return popup.getPresenter();
            } else {
                return null;
            }
        }

        public DockHostPort getLayoutPort() {
            return this.dockHost == null ? null : this.dockHost.getPresenter();
        }

        protected DockHostFxView<?> getDockHost() {
            return dockHost;
        }

        public @Unmodifiable ObservableList<TabDockFxView<?>> getTabDocks() {
            return tabDocks;
        }

        private TabPopupFxView<?> getPopup() {
            return dockHost.getPopup(getPresenter().getSide());
        }

        void addTabDock(TabDockFxView<?> tabDock) {
            modifiableTabDocks.add(tabDock);
            getModifiableChildren().add(tabDock);
        }

        void removeTabDock(TabDockFxView<?> tabDock) {
            modifiableTabDocks.remove(tabDock);
            getModifiableChildren().remove(tabDock);
        }

        TabDockFxView<?> removeTabDock(int index) {
            TabDockFxView<?> tabDock = modifiableTabDocks.remove(index);
            getModifiableChildren().remove(tabDock);
            return tabDock;
        }

        void addPopupToLayout(Side side) {
            var history = view.getPresenter().getHistory();
            var v = new TabPopupFxView<>(view, dockHost.getCenterDimension());
            var p = new TabPopupPresenter<>(v, side, () -> history.getOrCreatePopup());
            p.initialize();
            dockHost.getComposer().addTabPopup(v);
        }

        void removePopupFromLayout() {
            if (getPopupPort() != null) {
                dockHost.getComposer().removeTabPopup(view.getPresenter().getSide());
            }
        }
    }

    private final TabPanePro tabPane = new TabPanePro();

    public SideBarFxView(DockHostFxView<?> dockHost) {
        getComposer().dockHost = dockHost;
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
        tabPane.setSide(getPresenter().getSide());
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

        var css = SideBarFxView.class.getResource("side-bar.css").toExternalForm();
        this.getNode().getStylesheets().add(css);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        tabPane.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
            if (getComposer().getPopupPort() != null && !hasMouseMovedToPopup(e) && !containsSelectedTab()) {
                removePopup();
            }
        });
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getComposer().tabDocks.addListener((ListChangeListener<TabDockFxView<?>>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    addTabDocks(change.getAddedSubList());
                }
            }
        });
    }

    protected Tab createRestoreTab() {
        var tab = new BarRestoreTab();
        var button = new Button(null, new FontIconView(LayoutIcons.RESTORE_WINDOW));
        button.getStyleClass().addAll(StyleClasses.ICON_BUTTON, StyleClasses.COMPACT, Styles.FLAT);
        button.setOnAction(e -> {
            removePopup();
            onRestoreButtonAction(tab);
        });
        tab.setGraphic(button);
        tab.setClosable(false);
        tab.getStyleClass().add("restore");
        return tab;
    }

    protected BarTab createTab(Tab tab) {
        var t = new BarTab(tab);
        t.setText(tab.getText());
        t.setGraphic(tab.getGraphic());
        t.setClosable(false);
        return t;
    }

    protected void onRestoreButtonAction(Tab tab) {
        var restoreTabIndex = tabPane.getTabs().indexOf(tab);
        var tabDock = removeTabDock(restoreTabIndex);
        var side = getPresenter().getSide();
        var composer = getComposer();
        if (composer.tabDocks.isEmpty()
                && composer.dockHost.getComposer().getBarPolicy(side) != SideBarPolicy.EXISTS_ALWAYS) {
            composer.dockHost.getComposer().removeBar(side);
        }
        composer.dockHost.getComposer().restoreTabDock(tabDock);
    }

    protected void removePopup() {
        if (getComposer().getPopupPort() != null) {
            getComposer().removePopupFromLayout();
            tabPane.getSelectionModel().selectFirst(); // resetting selection
        }
    }

    protected void openTabInPopup(BarTab barTab) {
        barTab.setShownInPopup(true);
        var minimizedTab = barTab.getMinimizedTab();
        TabFxView<?> tabFxView = (TabFxView<?>) FxViewUtils.getView(minimizedTab);
        tabFxView.getPresenter().setClosable(false);
        var popupTabPane = getComposer().getPopup().getTabPane();
        var tabs = popupTabPane.getTabs();
        if (tabs.size() == 1) {
            var otherBarTab = getBarTab((tabs.get(0)));
            if (!otherBarTab.isSelected()) {
                closeTabInPopup(otherBarTab.getMinimizedTab());
            }
        } else if (tabs.size() == 2) {
            closeTabInPopup(tabs.get(1));
        }
        tabs.add(minimizedTab);
        popupTabPane.getSelectionModel().select(tabs.size() - 1);
    }

    protected void closeLastTabInPopup() {
        var tp = getComposer().getPopup().getTabPane();
        if (!tp.getTabs().isEmpty()) {
            var tab = tp.getTabs().get(tp.getTabs().size() - 1);
            closeTabInPopup(tab);
        }
    }

    protected void closeOtherTabInPopup(Tab tab) {
        var tp = getComposer().getPopup().getTabPane();
        if (tp.getTabs().size() > 1) {
            var tab0 = tp.getTabs().get(0);
            if (tab0 != tab) {
                closeTabInPopup(tab0);
            } else {
                closeTabInPopup(tp.getTabs().get(1));
            }
        }
    }

    protected void closeTabInPopup(Tab tab) {
        var popupTabPane = getComposer().getPopup().getTabPane();
        popupTabPane.getTabs().remove(tab);
        var barTab = getBarTab(tab);
        barTab.setShownInPopup(false);
    }

    boolean containsSelectedTab() {
        var index = this.tabPane.getSelectionModel().getSelectedIndex();
        return index > 0;
    }

    private void addTabDocks(List<? extends TabDockFxView<?>> tabDocks) {
        for (var tabDock : tabDocks) {
            this.tabPane.getTabs().add(createRestoreTab());
            for (var tab : tabDock.getComposer().getDetachedTabs()) {
                var t = createTab(tab.getNode());
                this.tabPane.getTabs().add(t);
            }
        }
    }

    private TabDockFxView<?> removeTabDock(int restoreTabIndex) {
        var dockIndex = findTabDockIndex(restoreTabIndex);
        var tabDock = getComposer().removeTabDock(dockIndex);
        // removing restoreTab and dock tabs from side bar
        var list = tabPane.getTabs().subList(restoreTabIndex,
                restoreTabIndex + tabDock.getComposer().getDetachedTabs().size() + 1);
        for (var i = 1; i < list.size(); i++) { // excluding restore tab
            BarTab barTab = (BarTab) list.get(i);
            barTab.deinit();
        }
        list.clear();
        return tabDock;
    }

    private BarTab getBarTab(Tab minimizedTab) {
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
        for (var i = 0; i < getComposer().tabDocks.size(); i++) {
            index += 1; // restore button
            if (index == restoreTabIndex) {
                return i;
            }
            var tabDock = getComposer().tabDocks.get(i);
            index += tabDock.getComposer().getTabPorts().size();
        }
        return -1;
    }

    private boolean hasMouseMovedToPopup(MouseEvent e) {
        switch (getPresenter().getSide()) {
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
