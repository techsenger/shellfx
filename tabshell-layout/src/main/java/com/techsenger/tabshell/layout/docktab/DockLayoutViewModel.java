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

package com.techsenger.tabshell.layout.docktab;

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.layout.LayoutComponentKeys;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutViewModel extends AbstractPaneViewModel {

    private final ReadOnlyObjectWrapper<WorkspaceViewModel> root = new ReadOnlyObjectWrapper<>();

    @Override
    public PaneKey getKey() {
        return LayoutComponentKeys.DOCK_LAYOUT;
    }

    public final WorkspaceViewModel getRoot() {
        return root.get();
    }

    public final ReadOnlyObjectProperty<WorkspaceViewModel> rootProperty() {
        return root.getReadOnlyProperty();
    }

    protected TabDockViewModel createTabDock() {
        var tabDock = new TabDockViewModel();
        return tabDock;
    }

    protected WorkspaceViewModel createWorkspace() {
        var workspace = new WorkspaceViewModel();
        return workspace;
    }

    protected SideBarViewModel createSideBar(Side side) {
        var sideBar = new SideBarViewModel(side);
        return sideBar;
    }

    void setRoot(WorkspaceViewModel value) {
        root.set(value);
    }

}
