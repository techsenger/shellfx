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

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.MediatorBindings;
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
            MediatorBindings.bind(root, component.root, c -> c.getView().getViewModel());
            MediatorBindings.bind(main, component.main, c -> c.getView().getViewModel());
            MediatorBindings.bind(rightSideBar, component.rightSideBar, c -> c.getView().getViewModel());
            MediatorBindings.bind(bottomSideBar, component.bottomSideBar, c -> c.getView().getViewModel());
            MediatorBindings.bind(leftSideBar, component.leftSideBar, c -> c.getView().getViewModel());
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
        public SideBarViewModel<?> getRightBar() {
            return this.rightSideBar.get();
        }

        @Override
        public ReadOnlyObjectProperty<SideBarViewModel<?>> bottomSideBarProperty() {
            return bottomSideBar.getReadOnlyProperty();
        }

        @Override
        public SideBarViewModel<?> getBottomBar() {
            return this.bottomSideBar.get();
        }

        @Override
        public ReadOnlyObjectProperty<SideBarViewModel<?>> leftSideBarProperty() {
            return leftSideBar.getReadOnlyProperty();
        }

        @Override
        public SideBarViewModel<?> getLeftBar() {
            return this.leftSideBar.get();
        }
    }

    private final ObjectProperty<SplitSpaceComponent<?>> root = new SimpleObjectProperty<>();

    private final ObjectProperty<AreaComponent<?>> main = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<SideBarComponent<?>> rightSideBar = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarComponent<?>> bottomSideBar = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<SideBarComponent<?>> leftSideBar = new ReadOnlyObjectWrapper<>();

    private final DockLayoutHistory<?> history;

    public DockLayoutComponent(T view, DockLayoutHistory<?> history) {
        super(view);
        this.history = history;
        root.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                getModifiableChildren().remove(oldV);
            }
            if (newV != null) {
                getModifiableChildren().add(newV);
            }
        });
        main.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                getModifiableChildren().remove(oldV);
            }
            if (newV != null) {
                getModifiableChildren().add(newV);
            }
        });
    }

    @Override
    public ComponentName getName() {
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

    protected SideBarComponent<?> createSideBar(Side side, SideBarHistory<?> history) {
        var vm = new SideBarViewModel<>(side);
        var v = new SideBarView<>(vm);
        var c = new SideBarComponent<>(v, history, this);
        return c;
    }

    SideBarComponent<?> addSideBar(Side side) {
        SideBarHistory<?> history = null;
        switch (side) {
            case RIGHT -> {
                history = this.history.getOrCreateRightSideBar();
                return addSideBar(side, history, rightSideBar, v -> getView().getNode().setRight(v.getNode()));
            }
            case LEFT -> {
                history = this.history.getOrCreateLeftSideBar();
                return addSideBar(side, history, leftSideBar, v -> getView().getNode().setLeft(v.getNode()));
            }
            case TOP, BOTTOM -> {
                side = Side.BOTTOM;
                history = this.history.getOrCreateBottomSideBar();
                return addSideBar(side, history, bottomSideBar, v -> getView().getNode().setBottom(v.getNode()));
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

    private SideBarComponent<?> addSideBar(Side side, SideBarHistory<?> sideBarHistory,
        ReadOnlyObjectWrapper<SideBarComponent<?>> wrapper, Consumer<SideBarView<?, ?>> viewAdder) {
        if (wrapper.get() != null) {
            return null;
        }
        var sideBar = createSideBar(side, sideBarHistory);
        sideBar.initialize();
        getModifiableChildren().add(sideBar);
        wrapper.set(sideBar);
        viewAdder.accept(sideBar.getView());
        return sideBar;
    }

}
