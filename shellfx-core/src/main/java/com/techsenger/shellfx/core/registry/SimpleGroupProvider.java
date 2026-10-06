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

package com.techsenger.shellfx.core.registry;

import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.ControlGroup;

/**
 * Provides a plain {@link ControlGroup}; extend it to provide a custom group, for example one that tracks its items.
 *
 * @param <V> the view type of the component the group is built for
 * @param <C> the type of the controls the group holds
 * @author Pavel Castornii
 */
public class SimpleGroupProvider<V extends ParentView<?>, C> extends SimpleControlProvider<V, ControlGroup<C>> {

    public SimpleGroupProvider() {
        super(new ControlGroup<>());
    }
}
