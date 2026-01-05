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

package com.techsenger.tabshell.material.menu;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.material.Named;
import com.techsenger.tabshell.material.Positioned;

/**
 *
 * @author Pavel Castornii
 */
public interface NamedMenuElement<T extends Name> extends Named, Positioned {

    @Override
    T getName();

    /**
     * Optional menu/item can be either visible or hidden. The visibility depends on the active tab support.
     */
    boolean isOptional();

    /**
     * Visible menu/item can be either validatable or not validatable. Validatable menus are checked if they are valid.
     */
    boolean isValidatable();

    /**
     * Updatable menu/item is a menu/item which properties can be changed by active tab. Note! It is NOT the updatable
     * menu that has updatable menu/items.
     */
    boolean isUpdatable();
}
