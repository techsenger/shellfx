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

package com.techsenger.shellfx.material.slot;

import com.techsenger.patternfx.mvvm.ParentView;

/**
 * The slot of a context menu; groups of menu items are put into it. The context menu itself is a control
 * registered for the slot.
 *
 * @param <V> the view type of the component this slot belongs to
 * @author Pavel Castornii
 */
public final class ContextMenuSlot<V extends ParentView<?>> extends AbstractSlot<V> {

    public ContextMenuSlot(Class<? super V> componentClass, String text) {
        super(componentClass, text);
    }
}
