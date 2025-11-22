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

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.RIGHT;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutViewModel extends AbstractAreaViewModel {

    private final ReadOnlyObjectWrapper<SplitSpaceViewModel> root = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<AbstractAreaViewModel> main = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarViewModel> rightBar = new ReadOnlyObjectWrapper<>();

    private final ObjectProperty<SideBarPolicy> rightBarPolicy =
            new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

    private final ReadOnlyObjectWrapper<SideBarViewModel> bottomBar = new ReadOnlyObjectWrapper<>();

    private final ObjectProperty<SideBarPolicy> bottomBarPolicy =
            new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

    private final ReadOnlyObjectWrapper<SideBarViewModel> leftBar = new ReadOnlyObjectWrapper<>();

    private final ObjectProperty<SideBarPolicy> leftBarPolicy =
            new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

    private final DockLayoutHistory<?> history;

    public DockLayoutViewModel(DockLayoutHistory<?> history) {
        this.history = history;
    }

    public final SplitSpaceViewModel getRoot() {
        return root.get();
    }

    public final ReadOnlyObjectProperty<SplitSpaceViewModel> rootProperty() {
        return root.getReadOnlyProperty();
    }

    public final AbstractAreaViewModel getMain() {
        return main.get();
    }

    public final ReadOnlyObjectProperty<AbstractAreaViewModel> mainProperty() {
        return main.getReadOnlyProperty();
    }

    public final SideBarViewModel getRightBar() {
        return rightBar.get();
    }

    public final ReadOnlyObjectProperty<SideBarViewModel> rightBarProperty() {
        return rightBar.getReadOnlyProperty();
    }

    public ObjectProperty<SideBarPolicy> rightBarPolicyProperty() {
        return rightBarPolicy;
    }

    public SideBarPolicy getRightBarPolicy() {
        return rightBarPolicy.get();
    }

    public void setRightBarPolicy(SideBarPolicy policy) {
        rightBarPolicy.set(policy);
    }

    public final SideBarViewModel getBottomBar() {
        return bottomBar.get();
    }

    public final ReadOnlyObjectProperty<SideBarViewModel> bottomBarProperty() {
        return bottomBar.getReadOnlyProperty();
    }

    public ObjectProperty<SideBarPolicy> bottomBarPolicyProperty() {
        return bottomBarPolicy;
    }

    public SideBarPolicy getBottomBarPolicy() {
        return bottomBarPolicy.get();
    }

    public void setBottomBarPolicy(SideBarPolicy policy) {
        bottomBarPolicy.set(policy);
    }

    public final SideBarViewModel getLeftBar() {
        return leftBar.get();
    }

    public ObjectProperty<SideBarPolicy> leftBarPolicyProperty() {
        return leftBarPolicy;
    }

    public SideBarPolicy getLeftBarPolicy() {
        return leftBarPolicy.get();
    }

    public void setLeftBarPolicy(SideBarPolicy policy) {
        leftBarPolicy.set(policy);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponentNames.DOCK_LAYOUT);
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
        SideBarHistory<?> sideBarHistory = null;
        switch (side) {
            case RIGHT -> sideBarHistory = this.history.getOrCreateRightSideBar();
            case LEFT -> sideBarHistory = this.history.getOrCreateLeftSideBar();
            case TOP, BOTTOM -> {
                side = Side.BOTTOM;
                sideBarHistory = this.history.getOrCreateBottomSideBar();
            }
            default -> throw new AssertionError();
        }
        var sideBar = new SideBarViewModel(side, sideBarHistory);
        return sideBar;
    }

    void setRoot(SplitSpaceViewModel value) {
        root.set(value);
    }

    void setMain(AbstractAreaViewModel value) {
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
