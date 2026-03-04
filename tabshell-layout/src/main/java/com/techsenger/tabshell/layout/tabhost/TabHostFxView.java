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
import com.techsenger.tabshell.layout.LayoutView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
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

    public class Composer extends AbstractAreaFxView<P>.Composer implements TabHostComposer,
            TabContainerFxView.Composer {

        private final TabHostFxView<P> view = TabHostFxView.this;

        @Override
        public List<? extends TabPort> getTabs() {
            return view.getNode().getTabs().stream()
                    .map(t -> ((ComponentTab) t).getView().getPresenter())
                    .toList();
        }

        @Override
        public TabPort getSelectedTab() {
            var tab = view.getSelectedTab();
            if (tab != null) {
                return tab.getPresenter();
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
                view.tabPane.getTabs().addAll(tabs);
                view.tabPane.getSelectionModel().select(selectedIndex);
                view.detachedTabs = Collections.emptyList();
                view.tabsDetached = false;
                logger.debug("{} Attached tabs", getDescriptor().getLogPrefix());
            }
        }

        @Override
        public void detachTabs() {
            if (!tabsDetached) {
                view.selectedIndex = view.tabPane.getSelectionModel().getSelectedIndex();
                view.detachedTabs = view.tabPane.getTabs()
                        .stream().map(t -> ((ComponentTab) t).getView()).collect(Collectors.toList());
                view.tabPane.getTabs().clear();
                view.tabsDetached = true;
                logger.debug("{} Detached tabs", getDescriptor().getLogPrefix());
            }
        }

        public void addTab(TabFxView<?> tab) {
            view.tabPane.getTabs().add(tab.getNode());
            view.getModifiableChildren().add(tab);
        }

        public void removeTab(TabFxView<?> tab) {
            view.tabPane.getTabs().remove(tab.getNode());
            view.getModifiableChildren().remove(tab);
            tab.getPresenter().deinitializeTree();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TabHostFxView.class);

    private final boolean workspace;

    private final TabPanePro tabPane = new TabPanePro();

    private List<? extends TabFxView<?>> detachedTabs = Collections.emptyList();

    private int selectedIndex;

    /**
     * If true then tab header is hidden when tab count is 1.
     */
    private final BooleanProperty tabHeaderAutoHide = new SimpleBooleanProperty(false);

    private final BooleanProperty tabHeaderVisible = new SimpleBooleanProperty(true);

    private boolean tabsDetached;

    public TabHostFxView(boolean workspace) {
        super();
        this.workspace = workspace;
    }

    @Override
    public TabFxView<?> getSelectedTab() {
        var tab = this.tabPane.getSelectionModel().getSelectedItem();
        if (tab != null) {
            return ((ComponentTab) tab).getView();
        } else {
            return null;
        }
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

    protected List<? extends TabFxView<?>> getDetachedTabs() {
        return detachedTabs;
    }

    @Override
    protected void build() {
        super.build();
        TabContainerFxViewUtils.initTabPane(tabPane, getPresenter());
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
        TabContainerFxViewUtils.initTabPane(tabPane, getPresenter());
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
        this.tabPane.getTabs().addListener((ListChangeListener<? super Tab>) (change) -> {
            resolveTabHeaderVisibility();
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

    private void resolveTabHeaderVisibility() {
        if (this.tabHeaderAutoHide.get() && this.tabPane.getTabs().size() == 1) {
            setTabHeaderVisible(false);
        } else {
            setTabHeaderVisible(true);
        }
    }
}
