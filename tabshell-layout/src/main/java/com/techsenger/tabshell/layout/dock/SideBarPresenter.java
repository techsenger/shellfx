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

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.List;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarPresenter<V extends SideBarView, C extends SideBarComposer> extends AbstractAreaPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements SideBarPort {

        @Override
        public List<? extends TabDockPort> getTabDocks() {
            return getComposer().getTabDocks();
        }
    }

    private final SideBarHistory history;

    private final Side side;

    public SideBarPresenter(V view, SideBarHistory history, Side side) {
        super(view);
        this.history = history;
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

    public SideBarHistory getHistory() {
        return history;
    }

    @Override
    public SideBarPort getPort() {
        return (SideBarPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new SideBarPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponentNames.SIDE_BAR);
    }
}
