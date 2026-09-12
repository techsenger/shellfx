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
import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.tab.ContainerTabPort;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.tabpanepro.core.TabEvent;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabHeaderAreaPolicy;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TabHost is the generic class for components that have TabPane and every tab is a separate component.
 *
 * @author Pavel Castornii
 */
public class TabHostView<VM extends TabHostViewModel<?>> extends AbstractAreaView<VM>
        implements TabContainerView<VM> {

    static final Object TAB_KEY = new Object();

    private static Tab getTab(ContextMenu menu) {
        WeakReference<Tab> ref = (WeakReference<Tab>) menu.getProperties().get(TAB_KEY);
        return ref.get();
    }

    private static Tab getTab(TabPaneProSkin.TabHeaderSkin skin) {
        return (Tab) skin.getProperties().get(TAB_KEY);
    }

    public class Composer extends AbstractAreaView<VM>.Composer implements
            AreaView.Composer, TabContainerView.Composer, TabHostComposer {

        private final TabHostView<VM> view = TabHostView.this;

        private final ReadOnlyObjectWrapper<@Nullable TabView<?>> selectedTab = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<@Nullable ContainerTabPort> selectedTabPort =
                new ReadOnlyObjectWrapper<>();

        private List<? extends TabView<?>> detachedTabs = Collections.emptyList();

        private final ReadOnlyIntegerWrapper tabCount =  new ReadOnlyIntegerWrapper();

        private boolean tabsDetached;

        private int savedSelectedIndex;

        public Composer() {
            this.selectedTab.addListener((obs, oldView, newView) -> {
                this.selectedTabPort.set(newView != null ? newView.getViewModel() : null);
                if (oldView != null) {
                    oldView.getViewModel().setSelected(false);
                }
                if (newView != null) {
                    newView.getViewModel().setSelected(true);
                }
            });
        }

        @Override
        public void compose() {
            super.compose();
            tabCount.bind(Bindings.size(view.tabPane.getTabs()));
            view.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
                TabView<?> tab = null;
                if (newV != null) {
                    tab = (TabView<?>) ViewUtils.getView(newV);
                }
                selectedTab.set(tab);
            });
        }

        @Override
        public List<? extends TabView<?>> getTabs() {
            return view.tabPane.getTabs().stream().map(t -> (TabView<?>) ViewUtils.getView(t)).toList();
        }

        @Override
        public @Unmodifiable List<? extends ContainerTabPort> getTabPorts() {
            return view.getNode().getTabs().stream()
                    .map(t -> ((TabView<?>) ViewUtils.getView(t)).getViewModel())
                    .toList();
        }

        @Override
        public int getSelectedTabIndex() {
            return view.tabPane.getSelectionModel().getSelectedIndex();
        }

        @Override
        public ReadOnlyIntegerProperty selectedTabIndexProperty() {
            return view.tabPane.getSelectionModel().selectedIndexProperty();
        }

        @Override
        public int getTabCount() {
            return view.tabPane.getTabs().size();
        }

        @Override
        public ReadOnlyIntegerProperty tabCountProperty() {
            return tabCount.getReadOnlyProperty();
        }

        @Override
        public @Nullable TabView<?> getSelectedTab() {
            return this.selectedTab.get();
        }

        @Override
        public ReadOnlyObjectProperty<TabView<?>> selectedTabProperty() {
            return this.selectedTab.getReadOnlyProperty();
        }

        @Override
        public ContainerTabPort getSelectedTabPort() {
            var tab = getSelectedTab();
            return tab != null ? tab.getViewModel() : null;
        }

        @Override
        public ReadOnlyObjectProperty<ContainerTabPort> selectedTabPortProperty() {
            return this.selectedTabPort.getReadOnlyProperty();
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
                view.tabPane.getSelectionModel().select(savedSelectedIndex);
                this.detachedTabs = Collections.emptyList();
                this.tabsDetached = false;
                logger.debug("{} Attached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void detachTabs() {
            if (!this.tabsDetached) {
                this.savedSelectedIndex = view.tabPane.getSelectionModel().getSelectedIndex();
                this.detachedTabs = view.tabPane.getTabs()
                        .stream().map(t -> (TabView<?>) ViewUtils.getView(t)).collect(Collectors.toList());
                view.tabPane.getTabs().clear();
                this.tabsDetached = true;
                logger.debug("{} Detached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void addTab(TabView<?> tab) {
            view.tabPane.getTabs().add(tab.getNode());
            getModifiableChildren().add(tab);
        }

        @Override
        public void removeTab(TabView<?> tab) {
            view.tabPane.getTabs().remove(tab.getNode());
            getModifiableChildren().remove(tab);
        }

        @Override
        public void closeTab(TabView<?> tab) {
            removeTab(tab);
            tab.getViewModel().requestDeinitializeTree();
        }

        protected @Unmodifiable List<? extends TabView<?>> getDetachedTabs() {
            return detachedTabs;
        }

        @Override
        protected ObservableList<ChildView<?>> getModifiableChildren() {
            return super.getModifiableChildren();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TabHostView.class);

    private final TabPanePro tabPane = new TabPanePro();

    private final HBox tabHeaderFirstBox = new HBox();

    private final HBox tabHeaderLastBox = new HBox();

    private final EventHandler<Event> eventBlocker = event -> event.consume();

    public TabHostView(VM viewModel) {
        super(viewModel);
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
    public Composer getComposer() {
        return (Composer) super.getComposer();
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
        return new TabHostView.Composer();
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
        this.tabPane.getStylesheets().add(TabHostView.class.getResource("tab-host.css").toExternalForm());
        this.tabPane.getStyleClass().add(Styles.DENSE);
        // Tab header should always be visible so that its visibility can be controlled manually via a style class.
        getTabHeaderArea().setPolicy(TabHeaderAreaPolicy.ALWAYS_VISIBLE);
        getTabHeaderArea().getFirstArea().getChildren().add(tabHeaderFirstBox);
        getTabHeaderArea().getLastArea().getChildren().add(tabHeaderLastBox);
        VBox.setVgrow(this.tabPane, Priority.ALWAYS);
        getTabHeaderArea().setTabHeaderFactory(c -> {
            var header = new TabPaneProSkin.TabHeaderSkin(c);
            header.getProperties().put(TAB_KEY, c.getTab());
            return header;
        });
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) (change)  -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (var tab : change.getAddedSubList()) {
                        tab.setOnCloseRequest((e) -> {
                            var fxView = (TabView<?>) ViewUtils.getView(tab);
                            viewModel.onCloseTab(fxView.getViewModel());
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
        ValueUtils.callAndAddListener(viewModel.tabHeaderVisibleProperty(),
                (ov, oldV, newV) -> updateTabHeaderVisible(newV));
        viewModel.selectTabSource().addListener((index) -> tabPane.getSelectionModel().select(index));
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
                    var tabView = (TabView<?>) ViewUtils.getView(e.getTab());
                    getComposer().getModifiableChildren().remove(tabView);
                    TabHostView<?> newTabHost = ViewUtils.findView(e.getTab().getTabPane(), TabHostView.class);
                    if (newTabHost != null) {
                        newTabHost.getComposer().getModifiableChildren().add(tabView);
                        logger.debug("{} Tab {} was moved from {} to {}",
                                getDescriptor().getLogPrefix(), tabView.getViewModel().getDescriptor().getFullName(),
                                getDescriptor().getFullName(), newTabHost.getDescriptor().getFullName());
                    } else {
                        logger.debug("{} Tab {} was moved to a TabPane that is outside of TabHost",
                                getDescriptor().getLogPrefix(), tabView.getViewModel().getDescriptor().getFullName());
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

    protected ContextMenu createTabContextMenu(ObservableList<Tab> tabs, Tab tab) {
        ContextMenu contextMenu = new ContextMenu();
        // we use a weak reference as a workaround for JDK-8283449
        contextMenu.getProperties().put(TAB_KEY, new WeakReference<Tab>(tab));

        MenuItem close = new MenuItem("Close", new Label(" "));
        close.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabView<?>) ViewUtils.getView(t);
                getViewModel().onCloseTab(tabFxView.getViewModel());
            }
        });
        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction((e) -> {
            getViewModel().onCloseAllTabs();
        });
        MenuItem closeOther = new MenuItem("Close Other");
        closeOther.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabView<?>) ViewUtils.getView(t);
                getViewModel().onCloseOtherTabs(tabFxView.getViewModel());
            }
        });
        MenuItem closeRight = new MenuItem("Close to the Right");
        closeRight.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabView<?>) ViewUtils.getView(t);
                getViewModel().onCloseRightTabs(tabFxView.getViewModel());
            }
        });
        MenuItem closeLeft = new MenuItem("Close to the Left");
        closeLeft.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                var tabFxView = (TabView<?>) ViewUtils.getView(t);
                getViewModel().onCloseLeftTabs(tabFxView.getViewModel());
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

    private void updateTabHeaderVisible(boolean value) {
        if (value) {
            this.tabPane.getStyleClass().remove(StyleClasses.HIDDEN_TABS);
        } else {
            this.tabPane.getStyleClass().add(StyleClasses.HIDDEN_TABS);
        }
    }
}
