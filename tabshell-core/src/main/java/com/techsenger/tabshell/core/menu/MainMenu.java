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

package com.techsenger.tabshell.core.menu;

import com.techsenger.tabshell.material.menu.DefaultMenuGroupName;
import com.techsenger.tabshell.material.menu.MenuGroupName;

/**
 * Describes the main menu of the application, which is a {@code MenuBar} located in the {@link ShellFxView}.
 * All menus intended for the main menu must be registered under {@link #GROUP} so that they can be distinguished
 * from other menus that may also be present in the {@link ShellFxView}, such as those belonging to a
 * {@code MenuButton} or other controls.
 *
 * @author Pavel Castornii
 */
public interface MainMenu {

    /**
     * The group under which all main menu entries must be registered.
     */
    MenuGroupName GROUP = new DefaultMenuGroupName("DefaultGroup");
}
