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

package com.techsenger.shellfx.demo;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.registry.AbstractSlotRegistrar;

/**
 * Builds the tree of the slots of the demo shell: which menus the menu bar has, and which groups each
 * menu has. A menu or a group nobody puts controls into is left out of the result by the builder.
 *
 * @author Pavel Castornii
 */
public class SlotRegistrar extends AbstractSlotRegistrar {

    public SlotRegistrar(ShellView<?> shell) {
        super(shell.getContext().getSlotRegistry());
    }

    @Override
    public void register() {
        register(Slots.MAIN_MENU, 0, Slots.FileMenu.MENU);
        register(Slots.MAIN_MENU, 100, Slots.WindowMenu.MENU);
        register(Slots.MAIN_MENU, 200, Slots.ExtraMenu.MENU);

        register(Slots.FileMenu.MENU, 0, Slots.FileMenu.DEMO_GROUP);
        register(Slots.FileMenu.MENU, 100, Slots.FileMenu.APPEARANCE_GROUP);
        register(Slots.FileMenu.MENU, 200, Slots.FileMenu.LAST_GROUP);

        register(Slots.WindowMenu.MENU, 0, Slots.WindowMenu.DEFAULT_GROUP);
        register(Slots.WindowMenu.MENU, 100, Slots.WindowMenu.ARRANGEMENT_GROUP);

        register(Slots.ExtraMenu.MENU, 0, Slots.ExtraMenu.FOO_GROUP);
        register(Slots.ExtraMenu.MENU, 100, Slots.ExtraMenu.BAR_GROUP);
    }
}
