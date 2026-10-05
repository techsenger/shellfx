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
 * The slot of a group of controls that are laid out together and set apart from the other groups of the same
 * container (for example, menu items between two separators); the controls are put into it. The control of the
 * group is a {@code ControlGroup}, created by the factory registered for the slot.
 *
 * <p>{@code C} is the type of the controls the group holds, so a factory registered for the group is checked
 * against it at compile time.
 *
 * @param <V> the view type of the component this slot belongs to
 * @param <C> the type of the controls the group holds
 * @author Pavel Castornii
 */
public final class GroupSlot<V extends ParentView<?>, C> extends AbstractSlot<V> {

    public GroupSlot(Class<? super V> componentClass, String text) {
        super(componentClass, text);
    }
}
