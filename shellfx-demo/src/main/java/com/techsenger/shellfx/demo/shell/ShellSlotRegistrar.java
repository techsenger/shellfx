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

package com.techsenger.shellfx.demo.shell;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.registry.AbstractSlotRegistrar;

/**
 * Builds the tree of the slots of the demo shell: which menus the menu bar has, and which groups each
 * menu has. A menu or a group nobody puts controls into is left out of the result by the builder.
 *
 * @author Pavel Castornii
 */
public class ShellSlotRegistrar extends AbstractSlotRegistrar {

    public ShellSlotRegistrar(ShellView<?> shell) {
        super(shell.getContext().getSlotRegistry());
    }

    @Override
    public void register() {
        register(ShellSlots.MAIN_MENU, 0, ShellSlots.FileMenu.MENU);
        register(ShellSlots.MAIN_MENU, 100, ShellSlots.WindowMenu.MENU);
        register(ShellSlots.MAIN_MENU, 200, ShellSlots.ExtraMenu.MENU);

        register(ShellSlots.FileMenu.MENU, 0, ShellSlots.FileMenu.DEMO_GROUP);
        register(ShellSlots.FileMenu.MENU, 100, ShellSlots.FileMenu.APPEARANCE_GROUP);
        register(ShellSlots.FileMenu.MENU, 200, ShellSlots.FileMenu.LAST_GROUP);

        register(ShellSlots.WindowMenu.MENU, 0, ShellSlots.WindowMenu.DEFAULT_GROUP);
        register(ShellSlots.WindowMenu.MENU, 100, ShellSlots.WindowMenu.ARRANGEMENT_GROUP);

        register(ShellSlots.ExtraMenu.MENU, 0, ShellSlots.ExtraMenu.FOO_GROUP);
        register(ShellSlots.ExtraMenu.MENU, 100, ShellSlots.ExtraMenu.BAR_GROUP);
    }
}
