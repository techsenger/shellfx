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

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.layout.LayoutComponentKeys;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutViewModel extends AbstractPaneViewModel {

    private final ReadOnlyObjectWrapper<SplitSpaceViewModel> root = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<AbstractPaneViewModel> main = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarViewModel> rightBar = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarViewModel> bottomBar = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarViewModel> leftBar = new ReadOnlyObjectWrapper<>();

    @Override
    public PaneKey getKey() {
        return LayoutComponentKeys.DOCK_LAYOUT;
    }

    public final SplitSpaceViewModel getRoot() {
        return root.get();
    }

    public final ReadOnlyObjectProperty<SplitSpaceViewModel> rootProperty() {
        return root.getReadOnlyProperty();
    }

    public final AbstractPaneViewModel getMain() {
        return main.get();
    }

    public final ReadOnlyObjectProperty<AbstractPaneViewModel> mainProperty() {
        return main.getReadOnlyProperty();
    }

    public final SideBarViewModel getRightBar() {
        return rightBar.get();
    }

    public final ReadOnlyObjectProperty<SideBarViewModel> rightBarProperty() {
        return rightBar.getReadOnlyProperty();
    }

    public final SideBarViewModel getBottomBar() {
        return bottomBar.get();
    }

    public final ReadOnlyObjectProperty<SideBarViewModel> bottomBarProperty() {
        return bottomBar.getReadOnlyProperty();
    }

    public final SideBarViewModel getLeftBar() {
        return leftBar.get();
    }

    public final ReadOnlyObjectProperty<SideBarViewModel> leftBarProperty() {
        return leftBar.getReadOnlyProperty();
    }

    protected TabDockViewModel createTabDock() {
        var tabDock = new TabDockViewModel();
        return tabDock;
    }

    protected SplitSpaceViewModel createSplitSpace(Orientation orientation) {
        var workspace = new SplitSpaceViewModel(orientation);
        return workspace;
    }

    protected SideBarViewModel createSideBar(Side side) {
        var sideBar = new SideBarViewModel(side);
        return sideBar;
    }

    void setRoot(SplitSpaceViewModel value) {
        root.set(value);
    }

    void setMain(AbstractPaneViewModel value) {
        main.set(value);
    }

    ReadOnlyObjectWrapper<SideBarViewModel> rightBarWrapper() {
        return rightBar;
    }

    ReadOnlyObjectWrapper<SideBarViewModel> bottomBarWrapper() {
        return bottomBar;
    }

    ReadOnlyObjectWrapper<SideBarViewModel> leftBarWrapper() {
        return leftBar;
    }
}
