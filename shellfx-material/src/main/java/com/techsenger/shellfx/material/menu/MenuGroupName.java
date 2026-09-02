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

package com.techsenger.shellfx.material.menu;

import com.techsenger.patternfx.core.Name;
import com.techsenger.patternfx.mvp.ParentFxView;

/**
 * {@code V} is the view type a {@link com.techsenger.shellfx.core.registry.ControlFactory} registering a menu or
 * an item into this group is invoked with, so a mismatched factory is rejected at compile time.
 *
 * @param <V> the view type of the component this group belongs to
 * @author Pavel Castornii
 */
public interface MenuGroupName<V extends ParentFxView<?>> extends Name {

    /**
     * Returns the view class registrations targeting this group are filed under, and that a component's own class
     * (plus its ancestors and interfaces) is matched against when its applicable controls are resolved.
     */
    Class<?> getComponentClass();
}
