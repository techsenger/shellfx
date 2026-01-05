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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.core.Name;
import com.techsenger.patternfx.mvvmx.BindingUtils;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.core.area.AreaComponent;
import com.techsenger.tabshell.core.area.AreaViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import static javafx.geometry.Side.TOP;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutComponent<T extends DockLayoutView<?, ?>> extends AbstractAreaComponent<T> {

    protected class Mediator extends AbstractAreaComponent.Mediator implements DockLayoutMediator {

        private final ReadOnlyObjectWrapper<SplitSpaceViewModel> root = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<AreaViewModel<?>> main = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarViewModel<?>> rightSideBar = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarViewModel<?>> bottomSideBar = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarViewModel<?>> leftSideBar = new ReadOnlyObjectWrapper<>();

        private final DockLayoutComponent<?> component = DockLayoutComponent.this;

        public Mediator() {
            BindingUtils.bind(root, component.root, c -> c.getView().getViewModel());
            BindingUtils.bind(main, component.main, c -> c.getView().getViewModel());
            BindingUtils.bind(rightSideBar, component.rightSideBar, c -> c.getView().getViewModel());
            BindingUtils.bind(bottomSideBar, component.bottomSideBar, c -> c.getView().getViewModel());
            BindingUtils.bind(leftSideBar, component.leftSideBar, c -> c.getView().getViewModel());
        }

        @Override
        public ReadOnlyObjectProperty<SplitSpaceViewModel> rootProperty() {
            return root.getReadOnlyProperty();
        }

        @Override
        public SplitSpaceViewModel getRoot() {
            return this.root.get();
        }

        @Override
        public ReadOnlyObjectProperty<AreaViewModel<?>> mainProperty() {
            return main.getReadOnlyProperty();
        }

        @Override
        public AreaViewModel<?> getMain() {
            return this.main.get();
        }

        @Override
        public ReadOnlyObjectProperty<SideBarViewModel<?>> rightSideBarProperty() {
            return rightSideBar.getReadOnlyProperty();
        }

        @Override
        public SideBarViewModel<?> getRightSideBar() {
            return this.rightSideBar.get();
        }

        @Override
        public ReadOnlyObjectProperty<SideBarViewModel<?>> bottomSideBarProperty() {
            return bottomSideBar.getReadOnlyProperty();
        }

        @Override
        public SideBarViewModel<?> getBottomSideBar() {
            return this.bottomSideBar.get();
        }

        @Override
        public ReadOnlyObjectProperty<SideBarViewModel<?>> leftSideBarProperty() {
            return leftSideBar.getReadOnlyProperty();
        }

        @Override
        public SideBarViewModel<?> getLeftSideBar() {
            return this.leftSideBar.get();
        }

        @Override
        public void addSideBar(Side side) {
            component.addSideBar(side);
        }

        @Override
        public void removeSideBar(Side side) {
            component.removeSideBar(side);
        }
    }

    private final PlaceholderComponent placeholder = createPlaceholder();

    private final ObjectProperty<SplitSpaceComponent<?>> root = new SimpleObjectProperty<>();

    private final ObjectProperty<AreaComponent<?>> main = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<SideBarComponent<?>> rightSideBar = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarComponent<?>> bottomSideBar = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarComponent<?>> leftSideBar = new ReadOnlyObjectWrapper<>();

    public DockLayoutComponent(T view) {
        super(view);
        // the root is a child, the main one is not
        root.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                getModifiableChildren().remove(oldV);
            }
            if (newV != null) {
                getModifiableChildren().add(newV);
            }
        });
    }

    @Override
    public Name getName() {
        return LayoutComponentNames.DOCK_LAYOUT;
    }

    public ObjectProperty<SplitSpaceComponent<?>> rootProperty() {
        return root;
    }

    public SplitSpaceComponent<?> getRoot() {
        return root.get();
    }

    public void setRoot(SplitSpaceComponent<?> root) {
        this.root.set(root);
    }

    public AreaComponent<?> getMain() {
        return main.get();
    }

    public void setMain(AreaComponent<?> component) {
        main.set(component);
    }

    public ObjectProperty<AreaComponent<?>> mainProperty() {
        return main;
    }

    public ReadOnlyObjectProperty<SideBarComponent<?>> rightSideBarProperty() {
        return rightSideBar.getReadOnlyProperty();
    }

    public SideBarComponent<?> getRightSideBar() {
        return rightSideBar.get();
    }

    public ReadOnlyObjectProperty<SideBarComponent<?>> bottomSideBarProperty() {
        return bottomSideBar.getReadOnlyProperty();
    }

    public SideBarComponent<?> getBottomSideBar() {
        return bottomSideBar.get();
    }

    public ReadOnlyObjectProperty<SideBarComponent<?>> leftSideBarProperty() {
        return leftSideBar.getReadOnlyProperty();
    }

    public SideBarComponent<?> getLeftSideBar() {
        return leftSideBar.get();
    }

    public SplitSpaceComponent<?> createSplitSpace(Orientation orientation) {
        var vm = new SplitSpaceViewModel(orientation);
        var v = new SplitSpaceView<>(vm);
        var c = new SplitSpaceComponent<>(v, this);
        return c;
    }

    public TabDockComponent<?> createTabDock() {
        var vm = new TabDockViewModel<>();
        var v = new TabDockView<>(vm);
        var c = new TabDockComponent<>(v, this);
        return c;
    }

    public void addTabDock(TabDockComponent<?> dock, Side side, double size) {
        getView().addTabDock(dock.getView(), side, size);
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        this.placeholder.initialize();
        // placeholder is not added to children
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        this.placeholder.deinitialize();
    }

    protected SideBarComponent<?> createSideBar(Side side, SideBarHistory history) {
        var vm = new SideBarViewModel<>(history, side);
        var v = new SideBarView<>(vm);
        var c = new SideBarComponent<>(v, this);
        return c;
    }

    SideBarComponent<?> addSideBar(Side side) {
        var layoutHistory = getView().getViewModel().getHistory();
        SideBarHistory barHistory = null;
        switch (side) {
            case RIGHT -> {
                barHistory = layoutHistory.getOrCreateRightSideBar();
                return addSideBar(side, barHistory, rightSideBar, v -> getView().getNode().setRight(v.getNode()));
            }
            case LEFT -> {
                barHistory = layoutHistory.getOrCreateLeftSideBar();
                return addSideBar(side, barHistory, leftSideBar, v -> getView().getNode().setLeft(v.getNode()));
            }
            case TOP, BOTTOM -> {
                side = Side.BOTTOM;
                barHistory = layoutHistory.getOrCreateBottomSideBar();
                return addSideBar(side, barHistory, bottomSideBar, v -> getView().getNode().setBottom(v.getNode()));
            }
            default -> throw new AssertionError();
        }
    }

    void removeSideBar(Side side) {
        switch (side) {
            case RIGHT -> {
                getView().getNode().setRight(null);
                var sideBar = getRightSideBar();
                rightSideBar.set(null);
                sideBar.deinitialize();
                getModifiableChildren().remove(sideBar);
            }
            case BOTTOM -> {
                getView().getNode().setBottom(null);
                var sideBar = getBottomSideBar();
                bottomSideBar.set(null);
                sideBar.deinitialize();
                getModifiableChildren().remove(sideBar);
            }
            case LEFT -> {
                getView().getNode().setLeft(null);
                var sideBar = getLeftSideBar();
                leftSideBar.set(null);
                sideBar.deinitialize();
                getModifiableChildren().remove(sideBar);
            }
            default -> throw new AssertionError();
        }
    }

    void addTabPopup(TabPopupComponent<?> popup) {
        getModifiableChildren().add(popup);
        getView().addTabPopup(popup.getView());
    }

    void removeTabPopup(TabPopupComponent<?> popup) {
        getView().removeTabPopup(popup.getView());
        getModifiableChildren().remove(popup);
        popup.deinitialize();
    }

    PlaceholderComponent getPlaceholder() {
        return placeholder;
    }

    private SideBarComponent<?> addSideBar(Side side, SideBarHistory sideBarHistory,
        ReadOnlyObjectWrapper<SideBarComponent<?>> wrapper, Consumer<SideBarView<?, ?>> viewAdder) {
        if (wrapper.get() != null) {
            return wrapper.get();
        }
        var sideBar = createSideBar(side, sideBarHistory);
        sideBar.initialize();
        getModifiableChildren().add(sideBar);
        wrapper.set(sideBar);
        viewAdder.accept(sideBar.getView());
        return sideBar;
    }

    private PlaceholderComponent createPlaceholder() {
        var vm = new PlaceholderViewModel();
        var v = new PlaceholderView(vm);
        var c = new PlaceholderComponent(v, this);
        return c;
    }

}
