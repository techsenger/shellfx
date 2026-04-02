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

package com.techsenger.tabshell.layout.tabhost;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabpanepro.core.TabEvent;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.FxViewUtils;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.core.tab.TabPort;
import com.techsenger.tabshell.layout.LayoutView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TabHost is the generic class for components that have TabPane and every tab is a separate component.
 *
 * @author Pavel Castornii
 */
public class TabHostFxView<P extends TabHostPresenter<?, ?>> extends AbstractAreaFxView<P>
        implements TabContainerFxView<P>, TabHostView, LayoutView {

    private static final Object TAB_KEY = new Object();

    private static ComponentTab getTab(ContextMenu menu) {
        WeakReference<ComponentTab> ref = (WeakReference<ComponentTab>) menu.getProperties().get(TAB_KEY);
        return ref.get();
    }

    public class Composer extends AbstractAreaFxView<P>.Composer implements TabHostComposer,
            TabContainerFxView.Composer {

        private final TabHostFxView<P> view = TabHostFxView.this;

        private List<? extends TabFxView<?>> detachedTabs = Collections.emptyList();

        private boolean tabsDetached;

        @Override
        public @Unmodifiable List<? extends TabPort> getTabPorts() {
            return view.getNode().getTabs().stream()
                    .map(t -> ((ComponentTab) t).getView().getPresenter())
                    .toList();
        }

        @Override
        public TabFxView<?> getSelectedTab() {
            var tab = view.tabPane.getSelectionModel().getSelectedItem();
            if (tab != null) {
                return ((ComponentTab) tab).getView();
            } else {
                return null;
            }
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
                        .stream().map(t -> ((ComponentTab) t).getView()).collect(Collectors.toList());
                view.tabPane.getTabs().clear();
                this.tabsDetached = true;
                logger.debug("{} Detached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void addTab(TabFxView<?> tab) {
            view.tabPane.getTabs().add(tab.getNode());
            view.getModifiableChildren().add(tab);
        }

        @Override
        public void removeTab(TabFxView<?> tab) {
            view.tabPane.getTabs().remove(tab.getNode());
            view.getModifiableChildren().remove(tab);
            tab.getPresenter().deinitializeTree();
        }

        protected @Unmodifiable List<? extends TabFxView<?>> getDetachedTabs() {
            return detachedTabs;
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
    protected Composer createComposer() {
        return new TabHostFxView.Composer();
    }

    protected BooleanProperty tabHeaderAutoHideProperty() {
        return tabHeaderAutoHide;
    }

    protected BooleanProperty tabHeaderVisibleProperty() {
        return tabHeaderVisible;
    }

    @Override
    protected void build() {
        super.build();
        this.tabPane.getStylesheets().add(TabHostFxView.class.getResource("tab-host.css").toExternalForm());
        this.tabPane.getStyleClass().add(Styles.DENSE);
        VBox.setVgrow(this.tabPane, Priority.ALWAYS);
        if (this.workspace) {
            buildWorkspace();
        }
    }

    protected void buildWorkspace() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.getStyleClass().addAll("workspace-tab-pane", Styles.DENSE);
        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.setTabHeaderFactory(c -> new SlantedTabHeaderSkin(c));
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
                    for (var t : change.getAddedSubList()) {
                        ComponentTab tab = (ComponentTab) t;
                        tab.setOnCloseRequest((e) -> {
                            getPresenter().onCloseTab(tab.getView().getPresenter());
                            e.consume();
                        });
                        //tabs can be added only by one
                        tabPane.getSelectionModel().select(t);
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
                    var tabView = ((ComponentTab) e.getTab()).getView();
                    getModifiableChildren().remove(tabView);
                    TabHostFxView<?> newTabHost = (TabHostFxView<?>) FxViewUtils.getComponent(e.getTab().getTabPane());
                    if (newTabHost != null) {
                        newTabHost.getModifiableChildren().add(tabView);
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
        var tabPane = getNode();
        TabPaneProSkin sourceSkin = (TabPaneProSkin) tabPane.getSkin();
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

    protected ContextMenu createTabContextMenu(ObservableList<ComponentTab> tabs, ComponentTab tab) {
        ContextMenu contextMenu = new ContextMenu();
        // we use a weak reference as a workaround for JDK-8283449
        contextMenu.getProperties().put(TAB_KEY, new WeakReference<ComponentTab>(tab));

        MenuItem close = new MenuItem("Close", new Label(" "));
        close.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                getPresenter().onCloseTab(t.getView().getPresenter());
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
                getPresenter().onCloseOtherTabs(t.getView().getPresenter());
            }
        });
        MenuItem closeRight = new MenuItem("Close to the Right");
        closeRight.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                getPresenter().onCloseRightTabs(t.getView().getPresenter());
            }
        });
        MenuItem closeLeft = new MenuItem("Close to the Left");
        closeLeft.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                getPresenter().onCloseLeftTabs(t.getView().getPresenter());
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
