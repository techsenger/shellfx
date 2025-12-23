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

import com.techsenger.patternfx.core.ComponentState;
import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;

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
        addListenerForSideBar(getMediator().rightSideBarProperty(), rightBarPolicyProperty(), RIGHT);
        addListenerForSideBar(getMediator().bottomSideBarProperty(), bottomBarPolicyProperty(), BOTTOM);
        addListenerForSideBar(getMediator().leftSideBarProperty(), leftBarPolicyProperty(), LEFT);

        getMediator().stateProperty().addListener((ov, oldV, newV) -> {
            if (newV == ComponentState.INITIALIZED) {
                if (getRightBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                    getMediator().addSideBar(Side.RIGHT);
                }
                if (getBottomBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                    getMediator().addSideBar(Side.BOTTOM);
                }
                if (getLeftBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                    getMediator().addSideBar(Side.LEFT);
                }
            }
        });
    }

    void removeSideBarIfRequired(SideBarViewModel<?> sideBar) {
        switch (sideBar.getSide()) {
            case RIGHT -> {
                if (getRightBarPolicy() == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                    getMediator().removeSideBar(RIGHT);
                }
            }
            case BOTTOM -> {
                if (getBottomBarPolicy() == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                    getMediator().removeSideBar(BOTTOM);
                }
            }
            case LEFT -> {
                if (getLeftBarPolicy() == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                    getMediator().removeSideBar(LEFT);
                }
            }
            default -> throw new AssertionError();
        }
    }

    private void addListenerForSideBar(ReadOnlyObjectProperty<SideBarViewModel<?>> viewModel,
            ObjectProperty<SideBarPolicy> policy, Side side) {
        policy.addListener((ov, oldV, newV) -> {
            if (newV == SideBarPolicy.EXISTS_ALWAYS) {
                if (viewModel.get() == null) {
                    getMediator().addSideBar(side);
                }
            } else if (newV == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                if (viewModel.get() != null) {
                    removeSideBarIfRequired(viewModel.get());
                }
            } else {
                throw new AssertionError();
            }
        });
    }
}
