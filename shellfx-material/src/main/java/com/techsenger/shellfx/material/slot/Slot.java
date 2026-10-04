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
 * A folder of the tree other components contribute to. A slot is an immutable identity declared once as a
 * constant; it knows neither its parent nor its children, the registries do. The kind of a slot (a menu bar, a menu,
 * a group of controls, a tool bar) is its class, so the registries can accept only the combinations that make
 * sense, and the compiler rejects the others.
 *
 * <p>{@code V} is the view type of the component the slot belongs to, so a factory registered for the slot is
 * checked against it at compile time.
 *
 * @param <V> the view type of the component this slot belongs to
 * @author Pavel Castornii
 */
public interface Slot<V extends ParentView<?>> {

    /**
     * Returns the view class registrations of this slot are filed under, and that a component's own class (plus
     * its ancestors and interfaces) is matched against when its applicable registrations are resolved.
     */
    Class<? super V> getComponentClass();

    /**
     * Returns the human-readable name of this slot, used for logging and diagnostics.
     */
    String getText();
}
