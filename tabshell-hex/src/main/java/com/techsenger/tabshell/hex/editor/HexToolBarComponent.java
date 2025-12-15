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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.hex.HexComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class HexToolBarComponent<T extends HexToolBarView<?, ?>> extends AbstractAreaComponent<T> {

    public HexToolBarComponent(T view) {
        super(view);
    }

    @Override
    protected Mediator createMediator() {
        return new AbstractAreaComponent.Mediator() { };
    }

    @Override
    public ComponentName getName() {
        return HexComponentNames.HEX_TOOL_BAR;
    }

}
