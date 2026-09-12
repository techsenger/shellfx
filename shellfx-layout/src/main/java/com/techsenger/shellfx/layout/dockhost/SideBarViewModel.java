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

import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarViewModel<C extends SideBarComposer> extends AbstractAreaViewModel<C>
        implements FullSideBarPort {

    private final Side side;

    public SideBarViewModel(SideBarParams params) {
        super(params);
        this.side = params.getSide();
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public SideBarPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    protected SideBarHistory getHistory() {
        return (SideBarHistory) super.getHistory();
    }
}
