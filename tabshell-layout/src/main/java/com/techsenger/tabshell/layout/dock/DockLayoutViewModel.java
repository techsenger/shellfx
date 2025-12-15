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

import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutViewModel<T extends DockLayoutMediator> extends AbstractAreaViewModel<T> {

    private final ObjectProperty<SideBarPolicy> rightBarPolicy =
            new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

    private final ObjectProperty<SideBarPolicy> bottomBarPolicy =
            new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

    private final ObjectProperty<SideBarPolicy> leftBarPolicy =
            new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

    public DockLayoutViewModel() {

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

    public ObjectProperty<SideBarPolicy> bottomBarPolicyProperty() {
        return bottomBarPolicy;
    }

    public SideBarPolicy getBottomBarPolicy() {
        return bottomBarPolicy.get();
    }

    public void setBottomBarPolicy(SideBarPolicy policy) {
        bottomBarPolicy.set(policy);
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
    protected void initialize() {
        super.initialize();
//        if (getRightBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
//            getComponent().addSideBar(RIGHT);
//        }
//        if (viewModel.getBottomBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
//            getComponent().addSideBar(BOTTOM);
//        }
//        if (viewModel.getLeftBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
//            getComponent().addSideBar(LEFT);
//        }
//
//        addListenerForSideBar(rightBar, viewModel.rightBarPolicyProperty(), RIGHT);
//        addListenerForSideBar(bottomBar, viewModel.bottomBarPolicyProperty(), BOTTOM);
//        addListenerForSideBar(leftBar, viewModel.leftBarPolicyProperty(), LEFT);
    }

//    private void addListenerForSideBar(ReadOnlyObjectWrapper<SideBarView<?, ?>> view,
//            ObjectProperty<SideBarPolicy> policy, Side side) {
//        policy.addListener((ov, oldV, newV) -> {
//            if (newV == SideBarPolicy.EXISTS_ALWAYS) {
//                if (view.get() == null) {
//                    addSideBar(side);
//                }
//            } else if (newV == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
//                if (view.get() != null) {
//                    removeSideBarIfRequired(view.get());
//                }
//            } else {
//                throw new AssertionError();
//            }
//        });
//    }


    void removeSideBarIfRequired(SideBarViewModel<?> sideBar) {
//        switch (sideBar.getSide()) {
//            case RIGHT:
//                if (getViewModel().getRightBarPolicy() == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
//                    setRightBar(null);
//                    this.node.setRight(null);
//                    sideBar.deinitialize();
//                }
//                break;
//            case BOTTOM:
//                if (getViewModel().getBottomBarPolicy() == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
//                    setBottomBar(null);
//                    this.node.setBottom(null);
//                    sideBar.deinitialize();
//                }
//                break;
//            case LEFT:
//                if (getViewModel().getLeftBarPolicy() == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
//                    setLeftBar(null);
//                    this.node.setLeft(null);
//                    sideBar.deinitialize();
//                }
//                break;
//            default:
//                throw new AssertionError();
//        }
    }
}
