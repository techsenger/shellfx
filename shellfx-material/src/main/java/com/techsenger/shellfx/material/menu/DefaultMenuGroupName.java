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

import com.techsenger.patternfx.core.AbstractIdentityName;
import com.techsenger.patternfx.mvp.ParentFxView;

/**
 *
 * @param <V> the view type of the component this group belongs to
 * @author Pavel Castornii
 */
public class DefaultMenuGroupName<V extends ParentFxView<?>> extends AbstractIdentityName implements MenuGroupName<V> {

    private final Class<?> componentClass;

    public DefaultMenuGroupName(Class<?> componentClass, String text) {
        super(text);
        this.componentClass = componentClass;
    }

    @Override
    public Class<?> getComponentClass() {
        return componentClass;
    }
}
