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

package com.techsenger.shellfx.layout.tabhost;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.tabpanepro.core.TabEvent;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.shellfx.core.area.AbstractAreaFxView;
import com.techsenger.shellfx.core.tab.TabContainerFxView;
import com.techsenger.shellfx.core.tab.TabFxView;
import com.techsenger.shellfx.core.tab.TabPort;
import com.techsenger.shellfx.layout.LayoutView;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TabHost is the generic class for components that have TabPane and every tab is a separate component.
 *
 * @author Pavel Castornii
 */
public class TabHostFxView<P extends TabHostPresenter<?>> extends AbstractAreaFxView<P>
        implements TabContainerFxView<P>, TabHostView, LayoutView {

    private static final Object TAB_KEY = new Object();

    private static Tab getTab(ContextMenu menu) {
        WeakReference<Tab> ref = (WeakReference<Tab>) menu.getProperties().get(TAB_KEY);
        return ref.get();
    }

    private static Tab getTab(TabPaneProSkin.TabHeaderSkin skin) {
        return (Tab) skin.getProperties().get(TAB_KEY);
    }

    public class Composer extends AbstractAreaFxView<P>.Composer implements TabHostView.Composer,
            TabContainerFxView.Composer {

        private final TabHostFxView<P> view = TabHostFxView.this;

        private List<? extends TabFxView<?>> detachedTabs = Collections.emptyList();

        private boolean tabsDetached;

        @Override
        public @Unmodifiable List<? extends TabPort> getTabPorts() {
            return view.getNode().getTabs().stream()
                    .map(t -> ((TabFxView<?>) FxViewUtils.getView(t)).getPresenter())
                    .toList();
        }

        @Override
        public TabFxView<?> getSelectedTab() {
            var tab = view.tabPane.getSelectionModel().getSelectedItem();
            if (tab != null) {
                return (TabFxView<?>) FxViewUtils.getView(tab);
            } else {
                return null;
            }
        }

        @Override
        public List<? extends TabFxView<?>> getTabs() {
            return view.tabPane.getTabs().stream().map(t -> (TabFxView<?>) FxViewUtils.getView(t)).toList();
        }

        @Override
        public TabPort getSelectedTabPort() {
            var tab = getSelectedTab();
            if (tab != null) {
                return tab.getPresenter();
            } else {
                return null;
            }
        }

        @Override
        public boolean areTabsDetached() {
            return tabsDetached;
        }

        @Override
        public void attachTabs() {
            if (this.tabsDetached) {
                var tabs = this.detachedTabs.stream().map(t -> t.getNode()).collect(Collectors.toList());
                view.tabPane.getTabs().addAll(tabs);
                view.tabPane.getSelectionModel().select(selectedIndex);
                this.detachedTabs = Collections.emptyList();
                this.tabsDetached = false;
                logger.debug("{} Attached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void detachTabs() {
            if (!this.tabsDetached) {
                view.selectedIndex = view.tabPane.getSelectionModel().getSelectedIndex();
                this.detachedTabs = view.tabPane.getTabs()
                        .stream().map(t -> (TabFxView<?>) FxViewUtils.getView(t)).collect(Collectors.toList());
                view.tabPane.getTabs().clear();
                this.tabsDetached = true;
                logger.debug("{} Detached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void addTab(TabFxView<?> tab) {
            view.tabPane.getTabs().add(tab.getNode());
            getModifiableChildren().add(tab);
        }

        @Override
        public void removeTab(TabFxView<?> tab) {
            view.tabPane.getTabs().remove(tab.getNode());
            getModifiableChildren().remove(tab);
        }

        @Override
        public void closeTab(TabFxView<?> tab) {
            removeTab(tab);
            tab.getPresenter().deinitializeTree();
        }

        protected @Unmodifiable List<? extends TabFxView<?>> getDetachedTabs() {
            return detachedTabs;
        }

        @Override
        protected ObservableList<ChildFxView<?>> getModifiableChildren() {
            return super.getModifiableChildren();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TabHostFxView.class);

    private final boolean workspace;

    private final TabPanePro tabPane = new TabPanePro();

    private int selectedIndex;

    /**
     * If true then tab header is hidden when tab count is 1.
     */
    private final BooleanProperty tabHeaderAutoHide = new SimpleBooleanProperty(false);

    private final BooleanProperty tabHeaderVisible = new SimpleBooleanProperty(true);

    private final HBox tabHeaderFirstBox = new HBox();

    private final HBox tabHeaderLastBox = new HBox();

    private final EventHandler<Event> eventBlocker = event -> event.consume();

    public TabHostFxView(boolean workspace) {
        super();
        this.workspace = workspace;
    }

    @Override
    public void requestFocus() {
        var tab = getComposer().getSelectedTab();
        if (tab != null) {
            tab.requestFocus();
        }
    }

    @Override
    public TabPanePro getNode() {
        return this.tabPane;
    }

    @Override
    public void setTabHeaderAutoHide(boolean value) {
        this.tabHeaderAutoHide.set(value);
    }

    @Override
    public void setTabHeaderVisible(boolean value) {
        this.tabHeaderVisible.set(value);
    }

    @Override
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex <= this.tabPane.getTabs().size() - 1) {
            this.tabPane.getSelectionModel().select(tabIndex);
        }
    }

    @Override
    public int getSelectedTabIndex() {
        return this.tabPane.getSelectionModel().getSelectedIndex();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public boolean isWorkspace() {
        return this.workspace;
    }

    @Override
    public void updateRegularFont(Font font) {
        if (this.workspace) {
            tabPane.setTabMaxWidth(font.getSize() * 15);
        }
    }

    @Override
    public void setTabHeaderBlocked(Tab tab, boolean blocked) {
        StackPane headersRegion = (StackPane) getTabHeaderArea().lookup(".headers-region");
        if (headersRegion != null) {
            for (var child : headersRegion.getChildren()) {
                if (child instanceof TabPaneProSkin.TabHeaderSkin skin) {
                    var t = getTab(skin);
                    if (t == tab) {
                        var button = child.lookup(".tab-close-button");
                        if (button != null) {
                            if (blocked) {
                                button.addEventFilter(InputEvent.ANY, eventBlocker);
                            } else {
                                button.removeEventFilter(InputEvent.ANY, eventBlocker);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected Composer createComposer() {
        return new TabHostFxView.Composer();
    }

    protected BooleanProperty tabHeaderAutoHideProperty() {
        return tabHeaderAutoHide;
    }

    protected BooleanProperty tabHeaderVisibleProperty() {
        return tabHeaderVisible;
    }

    protected HBox getTabHeaderFirstBox() {
        return tabHeaderFirstBox;
    }

    protected HBox getTabHeaderLastBox() {
        return tabHeaderLastBox;
    }

    @Override
    protected void build() {
        super.build();
        tabHeaderFirstBox.getStyleClass().add("tab-header-first-box");
        tabHeaderLastBox.getStyleClass().add("tab-header-last-box");
        this.tabPane.getStylesheets().add(TabHostFxView.class.getResource("tab-host.css").toExternalForm());
        this.tabPane.getStyleClass().add(Styles.DENSE);
        getTabHeaderArea().getFirstArea().getChildren().add(tabHeaderFirstBox);
        getTabHeaderArea().getLastArea().getChildren().add(tabHeaderLastBox);
        VBox.setVgrow(this.tabPane, Priority.ALWAYS);
        if (this.workspace) {
            buildWorkspace();
        } else {
            getTabHeaderArea().setTabHeaderFactory(c -> {
                var header = new TabPaneProSkin.TabHeaderSkin(c);
                header.getProperties().put(TAB_KEY, c.getTab());
                return header;
            });
        }
    }

    protected void buildWorkspace() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.getStyleClass().addAll("workspace", Styles.DENSE);
        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.setTabHeaderFactory(c -> {
            var header = new SlantedTabHeaderSkin(c);
            header.getProperties().put(TAB_KEY, c.getTab());
            return header;
        });
        tabHeaderArea.setTabGap(-10.0);
        // right corner is on top
        tabHeaderArea.setTabViewOrderResolver((tabHeader, index, tabCount, selected) -> {
            if (selected) {
                return  tabCount * -1.0;
            } else {
                return (tabCount - 1 - index) * -1.0;
            }
        });
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        tabHeaderVisibleProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.tabPane.getStyleClass().remove(StyleClasses.HIDDEN_TABS);
            } else {
                this.tabPane.getStyleClass().add(StyleClasses.HIDDEN_TABS);
            }
        });
        this.tabHeaderAutoHide.addListener((ov, oldV, newV) -> {
            resolveTabHeaderVisibility();
        });
        this.tabPane.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) ->
                getPresenter().onSelectedTabChanged(newV.intValue()));
        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) (change)  -> {
            resolveTabHeaderVisibility();
            while (change.next()) {
                if (change.wasAdded()) {
                    for (var tab : change.getAddedSubList()) {
                        tab.setOnCloseRequest((e) -> {
                            var fxView = (TabFxView<?>) FxViewUtils.getView(tab);
                            getPresenter().onCloseTab(fxView.getPresenter());
                            e.consume();
                        });
                        //tabs can be added only by one
                        tabPane.getSelectionModel().select(tab);
                        tab.setContextMenu(createTabContextMenu((ObservableList) tabPane.getTabs(), tab));
                    }
                } else if (change.wasRemoved()) {
                    for (var t : change.getRemoved()) {
                        t.setOnCloseRequest(null);
                        t.setContextMenu(null);
                    }
                }
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // this handler is called when mouse is over TabHeaderArea
        tabPane.addEventHandler(TabEvent.TAB_DRAG_FINISHED, (e) -> {
            if (e.getTarget() == this.tabPane) {
                if (e.getTab().getTabPane() != this.tabPane) { // If tab droped in another tabpane
                    // Moving a tab from one pane to another is handled by TabPanePro.
                    // Here we only need to synchronize the component lists.
                    var tabView = (TabFxView<?>) FxViewUtils.getView(e.getTab());
                    getComposer().getModifiableChildren().remove(tabView);
                    TabHostFxView<?> newTabHost = FxViewUtils.findView(e.getTab().getTabPane(), TabHostFxView.class);
                    if (newTabHost != null) {
                        newTabHost.getComposer().getModifiableChildren().add(tabView);
                        logger.debug("{} Tab {} was moved from {} to {}",
                                getDescriptor().getLogPrefix(), tabView.getDescriptor().getFullName(),
                                getDescriptor().getFullName(), newTabHost.getDescriptor().getFullName());
                    } else {
                        logger.debug("{} Tab {} was moved to a TabPane that is outside of TabHost",
                                getDescriptor().getLogPrefix(), tabView.getDescriptor().getFullName());
                    }
                }
                e.consume();
            }
        });
    }

    protected TabPaneProSkin.TabHeaderArea getTabHeaderArea() {
        TabPaneProSkin sourceSkin = (TabPaneProSkin) getNode().getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
        return tabHeaderArea;
    }

    @Override
    protected void unbuild() {
        super.unbuild();
        if (this.workspace) {
            unbuildWorkspace();
        }
    }

    protected void unbuildWorkspace() {

    }

    protected ContextMenu createTabContextMenu(ObservableList<Tab> tabs, Tab tab) {
        ContextMenu contextMenu = new ContextMenu();
        // we use a weak reference as a workaround for JDK-8283449
        contextMenu.getProperties().put(TAB_KEY, new WeakReference<Tab>(tab));

        MenuItem close = new MenuItem("Close", new Label(" "));
        close.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabFxView<?>) FxViewUtils.getView(t);
                getPresenter().onCloseTab(tabFxView.getPresenter());
            }
        });
        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction((e) -> {
            getPresenter().onCloseAllTabs();
        });
        MenuItem closeOther = new MenuItem("Close Other");
        closeOther.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabFxView<?>) FxViewUtils.getView(t);
                getPresenter().onCloseOtherTabs(tabFxView.getPresenter());
            }
        });
        MenuItem closeRight = new MenuItem("Close to the Right");
        closeRight.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabFxView<?>) FxViewUtils.getView(t);
                getPresenter().onCloseRightTabs(tabFxView.getPresenter());
            }
        });
        MenuItem closeLeft = new MenuItem("Close to the Left");
        closeLeft.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabFxView<?>) FxViewUtils.getView(t);
                getPresenter().onCloseLeftTabs(tabFxView.getPresenter());
            }
        });

        contextMenu.getItems().addAll(close, closeAll, closeOther, closeRight, closeLeft);
        contextMenu.setOnShowing((e) -> {
            if (tabs.size() == 0) {
                return;
            }
            if (tabs.size() == 1) {
                closeAll.setDisable(true);
                closeOther.setDisable(true);
                closeLeft.setDisable(true);
                closeRight.setDisable(true);
            } else {
                closeAll.setDisable(false);
                closeOther.setDisable(false);
                closeLeft.setDisable(false);
                closeRight.setDisable(false);
            }
            var t = getTab(contextMenu);
            if (t != null) {
                if (tabs.get(tabs.size() - 1) == t) {
                    closeRight.setDisable(true);
                } else {
                    closeRight.setDisable(false);
                }
                if (tabs.get(0) == t) {
                    closeLeft.setDisable(true);
                } else {
                    closeLeft.setDisable(false);
                }
            }
        });
        return contextMenu;
    }

    private void resolveTabHeaderVisibility() {
        if (this.tabHeaderAutoHide.get() && this.tabPane.getTabs().size() == 1) {
            setTabHeaderVisible(false);
        } else {
            setTabHeaderVisible(true);
        }
    }
}
