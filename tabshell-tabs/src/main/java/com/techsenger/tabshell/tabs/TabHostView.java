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

package com.techsenger.tabshell.tabs;

import atlantafx.base.theme.Styles;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabContainerView;
import com.techsenger.tabshell.core.tab.TabContainerViewUtils;
import com.techsenger.tabshell.core.tab.TabView;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * TabDock is the generic class for components that have TabPane and every tab is a separate component.
 *
 * @author Pavel Castornii
 */
public class TabHostView<T extends TabHostViewModel> extends AbstractPaneView<T>
        implements TabContainerView<TabView<?>>, MenuAware {

    private final TabPanePro root = new TabPanePro();

    private final ReadOnlyObjectWrapper<TabView<?>> selectedTab = new ReadOnlyObjectWrapper<>();

    public TabHostView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void openTab(TabView<?> tabView) {
        root.getTabs().add(tabView.getNode());
    }

    @Override
    public void closeTab(ComponentTab tab) {
        this.closeTab(tab.getView());
    }

    @Override
    public void closeTab(TabView<?> tabView) {
        if (tabView.doOnCloseAttempt(CloseScope.TAB, () -> closeTab(tabView))) {
            root.getTabs().remove(tabView.getNode());
            tabView.deinitialize();
            var closedCallback = tabView.getViewModel().getOnClosed();
            if (closedCallback != null) {
                closedCallback.call();
            }
        }
    }

    @Override
    public TabView<?> getSelectedTab() {
        var tab = this.root.getSelectionModel().getSelectedItem();
        return ((ComponentTab) tab).getView();
    }

    public List<TabView<?>> getTabs() {
        return (List) this.root.getTabs().stream().map(e -> ((ComponentTab) e).getView()).collect(Collectors.toList());
    }

    public ReadOnlyObjectProperty<TabView<?>> selectedTabProperty() {
        return this.selectedTab.getReadOnlyProperty();
    }

    @Override
    public void requestFocus() {
        var tab = this.getSelectedTab();
        if (tab != null) {
            tab.requestFocus();
        }
    }

    @Override
    public TabPanePro getNode() {
        return this.root;
    }

    @Override
    public void doOnMenuShowing(MenuKey menuKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            tab.doOnMenuShowing(menuKey);
        }
    }

    @Override
    public void doOnMenuHiding(MenuKey menuKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            tab.doOnMenuHiding(menuKey);
        }
    }

    @Override
    public MenuHelper getMenuHelper(MenuKey menuKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            return tab.getMenuHelper(menuKey);
        } else {
            return null;
        }
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemKey menuItemKey) {
        var tab = this.selectedTab.get();
        if (tab != null) {
            return tab.getMenuItemHelper(menuItemKey);
        } else {
            return null;
        }
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        TabContainerViewUtils.initTabPane(root, this);
        this.root.getStyleClass().add(Styles.DENSE);
        VBox.setVgrow(this.root, Priority.ALWAYS);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        this.root.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                var tab = (ComponentTab) oldV;
                tab.getView().doOnDeselected();
            }
            if (newV != null) {
                var tab = (ComponentTab) newV;
                this.selectedTab.set(tab.getView());
                viewModel.selectedTabWrapper().set(tab.getView().getViewModel());
                tab.getView().doOnSelected();
            } else {
                this.selectedTab.set(null);
                viewModel.selectedTabWrapper().set(null);
            }
        });
        this.root.getTabs().addListener((ListChangeListener<? super Tab>) (change) -> {

            while (change.next()) {
                if (change.wasAdded()) {
                    for (Tab tab : change.getAddedSubList()) {
                        var tabView = ((ComponentTab) tab).getView();
                        tabView.setParent(this);
                    }
                }
                if (change.wasRemoved()) {
                    for (Tab tab : change.getRemoved()) {
                        var tabView = ((ComponentTab) tab).getView();
                        tabView.setParent(null);
                    }
                }
            }
        });
        viewModel.tabHeaderVisibleProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.root.getStyleClass().remove(StyleClasses.HIDDEN_TABS);
            } else {
                this.root.getStyleClass().add(StyleClasses.HIDDEN_TABS);
            }
        });
        viewModel.selectedTabIndexWrapper().addListener((ov, oldV, newV) ->
                this.root.getSelectionModel().select(newV.intValue()));
        this.root.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) ->
                viewModel.selectedTabIndexWrapper().set(newV.intValue()));
    }

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        for (var t : this.root.getTabs()) {
            ((ComponentTab) t).getView().deinitialize();
        }
    }
}
