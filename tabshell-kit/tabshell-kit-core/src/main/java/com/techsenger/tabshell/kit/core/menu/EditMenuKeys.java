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

package com.techsenger.tabshell.kit.core.menu;

import com.techsenger.tabshell.material.menu.MenuGroupKey;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;

/**
 *
 * @author Pavel Castornii
 */
public interface EditMenuKeys {

    MenuKey EDIT = new MenuKey();

    MenuGroupKey HISTORY = new MenuGroupKey();

    MenuGroupKey CLIPBOARD = new MenuGroupKey();

    MenuGroupKey FIND_REPLACE = new MenuGroupKey();

    MenuGroupKey DEFAULT = new MenuGroupKey();

    MenuItemKey UNDO = new MenuItemKey();

    MenuItemKey REDO = new MenuItemKey();

    MenuItemKey CUT = new MenuItemKey();

    MenuItemKey COPY = new MenuItemKey();

    MenuItemKey PASTE = new MenuItemKey();

    MenuItemKey FIND = new MenuItemKey();

    MenuItemKey REPLACE = new MenuItemKey();

    MenuItemKey FIND_SELECTION = new MenuItemKey();

    MenuItemKey FIND_NEXT = new MenuItemKey();

    MenuItemKey FIND_PREVIOUS = new MenuItemKey();

    MenuItemKey GO_TO_LINE = new MenuItemKey();
}
