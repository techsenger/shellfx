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
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.area.AbstractAreaView;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabContainerView;
import com.techsenger.tabshell.core.tab.TabContainerViewUtils;
import com.techsenger.tabshell.core.tab.TabView;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TabHost is the generic class for components that have TabPane and every tab is a separate component.
 *
 * @author Pavel Castornii
 */
public class TabHostView<T extends TabHostViewModel<?>, S extends TabHostComponent<?>>
        extends AbstractAreaView<T, S> implements MenuAware, TabContainerView<TabView<?, ?>> {

    private static final Logger logger = LoggerFactory.getLogger(TabHostView.class);

    private final TabPanePro root = new TabPanePro();

    private List<? extends TabView<?, ?>> detachedTabs = Collections.EMPTY_LIST;

    private int selectedIndex;

    public TabHostView(T viewModel) {
        super(viewModel);
    }

    @Override
    public TabView<?, ?> getSelectedTab() {
        var tab = this.root.getSelectionModel().getSelectedItem();
        return ((ComponentTab) tab).getView();
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
    public void doOnMenuShowing(MenuName menuName) {
        var tab = getSelectedTab();
        if (tab != null) {
            tab.doOnMenuShowing(menuName);
        }
    }

    @Override
    public void doOnMenuHiding(MenuName menuName) {
        var tab = getSelectedTab();
        if (tab != null) {
            tab.doOnMenuHiding(menuName);
        }
    }

    @Override
    public MenuHelper getMenuHelper(MenuName menuName) {
        var tab = getSelectedTab();
        if (tab != null) {
            return tab.getMenuHelper(menuName);
        } else {
            return null;
        }
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemName menuItemName) {
        var tab = getSelectedTab();
        if (tab != null) {
            return tab.getMenuItemHelper(menuItemName);
        } else {
            return null;
        }
    }

    /**
     * Returns the unmodifiable list of the detached tabs or empty list.
     *
     * @return
     */
    public List<? extends TabView<?, ?>> getDetachedTabs() {
        return detachedTabs;
    }

    /**
     * Attaches the detached tabs to the {@link TabPane}. The process involves several iteration loops, so it may be
     * relatively costly.
     */
    public void attachTabs() {
        if (getViewModel().areTabsDetached()) {
            var tabs = this.detachedTabs.stream().map(t -> t.getNode()).collect(Collectors.toList());
            this.root.getTabs().addAll(tabs);
            this.root.getSelectionModel().select(selectedIndex);
            this.detachedTabs = Collections.EMPTY_LIST;
            getViewModel().setDetachedTabs(Collections.EMPTY_LIST);
            getViewModel().setTabsDetached(false);
            logger.debug("{} Attached tabs", getComponent().getLogPrefix());
        }
    }

    /**
     * Detaches the tabs from the {@link TabPane}. This operation is required when the tabs need to be temporarily added
     * to other {@link TabPane}s. The process involves several iteration loops, so it may be relatively costly.
     */
    public void detachTabs() {
        if (!getViewModel().areTabsDetached()) {
            this.selectedIndex = this.root.getSelectionModel().getSelectedIndex();
            this.detachedTabs = this.root.getTabs()
                    .stream().map(t -> ((ComponentTab) t).getView()).collect(Collectors.toList());
            var detachedTabsVM = this.detachedTabs.stream().map(v -> v.getViewModel()).collect(Collectors.toList());
            getViewModel().setDetachedTabs(detachedTabsVM);
            this.root.getTabs().clear();
            getViewModel().setTabsDetached(true);
            logger.debug("{} Detached tabs", getComponent().getLogPrefix());
        }
    }

    @Override
    protected void build() {
        super.build();
        TabContainerViewUtils.initTabPane(root, getViewModel());
        this.root.getStyleClass().add(Styles.DENSE);
        VBox.setVgrow(this.root, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        this.root.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                var tab = (ComponentTab) oldV;
                tab.getView().doOnDeselected();
            }
            if (newV != null) {
                var tab = (ComponentTab) newV;
                viewModel.selectedTabWrapper().set(tab.getView().getViewModel());
                tab.getView().doOnSelected();
            } else {
                viewModel.selectedTabWrapper().set(null);
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

    protected TabPaneProSkin.TabHeaderArea getTabHeaderArea() {
        var tabPane = getNode();
        TabPaneProSkin sourceSkin = (TabPaneProSkin) tabPane.getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
        return tabHeaderArea;
    }
}
