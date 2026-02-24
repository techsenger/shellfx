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
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxViewUtils;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.core.tab.TabPort;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TabHost is the generic class for components that have TabPane and every tab is a separate component.
 *
 * @author Pavel Castornii
 */
public class TabHostFxView<P extends TabHostPresenter<?, ?>> extends AbstractAreaFxView<P>
        implements TabContainerFxView<TabFxView<?>>, TabHostView {

    public class Composer extends AbstractAreaFxView.Composer implements TabHostComposer,
            TabContainerFxView.Composer<TabFxView<?>> {

        private final TabHostFxView<?> view = TabHostFxView.this;

        @Override
        public List<? extends TabPort> getTabs() {
            return view.getNode().getTabs().stream()
                    .map(t -> ((ComponentTab) t).getView().getPresenter().getPort())
                    .toList();
        }

        @Override
        public TabPort getSelectedTab() {
            var tab = view.getSelectedTab();
            if (tab != null) {
                return tab.getPresenter().getPort();
            } else {
                return null;
            }
        }

        @Override
        public boolean areTabsDetached() {
            return view.tabsDetached;
        }

        @Override
        public void attachTabs() {
            if (tabsDetached) {
                var tabs = view.detachedTabs.stream().map(t -> t.getNode()).collect(Collectors.toList());
                view.root.getTabs().addAll(tabs);
                view.root.getSelectionModel().select(selectedIndex);
                view.detachedTabs = Collections.emptyList();
                view.tabsDetached = false;
                logger.debug("{} Attached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void detachTabs() {
            if (!tabsDetached) {
                view.selectedIndex = view.root.getSelectionModel().getSelectedIndex();
                view.detachedTabs = view.root.getTabs()
                        .stream().map(t -> ((ComponentTab) t).getView()).collect(Collectors.toList());
                view.root.getTabs().clear();
                view.tabsDetached = true;
                logger.debug("{} Detached tabs", getDescriptor().getLogPrefix());
            }
        }

        public void addTab(TabFxView<?> tab) {
            view.root.getTabs().add(tab.getNode());
            view.getModifiableChildren().add(tab);
        }

        public void removeTab(TabFxView<?> tab) {
            view.root.getTabs().remove(tab.getNode());
            view.getModifiableChildren().remove(tab);
            tab.getPresenter().deinitializeTree();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TabHostFxView.class);

    private final TabPanePro root = new TabPanePro();

    private List<? extends TabFxView<?>> detachedTabs = Collections.emptyList();

    private int selectedIndex;

    /**
     * If true then tab header is hidden when tab count is 1.
     */
    private final BooleanProperty tabHeaderAutoHide = new SimpleBooleanProperty(false);

    private final BooleanProperty tabHeaderVisible = new SimpleBooleanProperty(true);

    private boolean tabsDetached;

    public TabHostFxView() {
        super();
    }

    @Override
    public TabFxView<?> getSelectedTab() {
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
    public boolean isTabHeaderAutoHide() {
        return tabHeaderAutoHide.get();
    }

    @Override
    public void setTabHeaderAutoHide(boolean value) {
        this.tabHeaderAutoHide.set(value);
    }

    @Override
    public boolean isTabHeaderVisible() {
        return tabHeaderVisible.get();
    }

    @Override
    public void setTabHeaderVisible(boolean value) {
        this.tabHeaderVisible.set(value);
    }

    @Override
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex <= this.root.getTabs().size() - 1) {
            this.root.getSelectionModel().select(tabIndex);
        }
    }

    @Override
    public int getSelectedTabIndex() {
        return this.root.getSelectionModel().getSelectedIndex();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
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

    protected List<? extends TabFxView<?>> getDetachedTabs() {
        return detachedTabs;
    }

    @Override
    protected void build() {
        super.build();
        TabContainerFxViewUtils.initTabPane(root, getPresenter());
        this.root.getStyleClass().add(Styles.DENSE);
        VBox.setVgrow(this.root, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        tabHeaderVisibleProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.root.getStyleClass().remove(StyleClasses.HIDDEN_TABS);
            } else {
                this.root.getStyleClass().add(StyleClasses.HIDDEN_TABS);
            }
        });
        this.tabHeaderAutoHide.addListener((ov, oldV, newV) -> {
            resolveTabHeaderVisibility();
        });
        this.root.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) ->
                getPresenter().onSelectedTabChanged(newV.intValue()));
        this.root.getTabs().addListener((ListChangeListener<? super Tab>) (change) -> {
            resolveTabHeaderVisibility();
        });
    }

    protected TabPaneProSkin.TabHeaderArea getTabHeaderArea() {
        var tabPane = getNode();
        TabPaneProSkin sourceSkin = (TabPaneProSkin) tabPane.getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
        return tabHeaderArea;
    }

    private void resolveTabHeaderVisibility() {
        if (this.tabHeaderAutoHide.get() && this.root.getTabs().size() == 1) {
            setTabHeaderVisible(false);
        } else {
            setTabHeaderVisible(true);
        }
    }
}
