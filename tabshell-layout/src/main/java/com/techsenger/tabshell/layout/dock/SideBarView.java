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

import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.toolkit.fx.collections.ListSynchronizer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarView<T extends SideBarViewModel> extends AbstractPaneView<T> {

    private final ObservableList<TabDockView<?>> tabDocks = FXCollections.observableArrayList();

    private final TabPanePro tabPane = new TabPanePro();

    private ListSynchronizer<TabDockView<?>, TabDockViewModel> listSynchronizer;

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
     * Returns a modifiable list of minimized tab docks.
     *
     * @return
     */
    public ObservableList<TabDockView<?>> getTabDocks() {
        return tabDocks;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        tabPane.setSide(viewModel.getSide());
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        listSynchronizer = new ListSynchronizer<TabDockView<?>, TabDockViewModel>(tabDocks,
                viewModel.getModifiableTabDocks(), v -> v.getViewModel());
        tabDocks.addListener((ListChangeListener<TabDockView<?>>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (var tabDock : change.getAddedSubList()) {
                        for (var tab : tabDock.getNode().getTabs()) {
                            var t = createTabFor(tab);
                            this.tabPane.getTabs().add(t);
                        }
                    }
                }
            }
        });
    }

    protected Tab createTabFor(Tab tab) {
        var t = new Tab(tab.getText(), tab.getGraphic());
        t.setClosable(false);
        return t;
    }
}
