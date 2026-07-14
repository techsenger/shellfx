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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.area.AbstractAreaPresenter;
import com.techsenger.shellfx.layout.LayoutComponents;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarPresenter<V extends SideBarView> extends AbstractAreaPresenter<V> implements SideBarPort {

    private final Side side;

    public SideBarPresenter(V view, SideBarParams params) {
        super(view, params);
        this.side = params.getSide();
    }

    public Side getSide() {
        return side;
    }

    @Override
    public SideBarHistory getHistory() {
        return (SideBarHistory) super.getHistory();
    }

    @Override
    public ViewAccess getViewAccess() {
        return getView();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.SIDE_BAR);
    }
}
