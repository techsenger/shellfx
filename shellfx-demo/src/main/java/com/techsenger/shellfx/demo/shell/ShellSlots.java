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
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import javafx.scene.control.MenuItem;

/**
 * The slots - the menu bar, its menus, and their groups - the demo application's shell offers.
 *
 * @author Pavel Castornii
 */
public final class ShellSlots {

    public static final class FileMenu {

        public static final MenuSlot<ShellView<?>> MENU = new MenuSlot<>(ShellView.class, "File");

        public static final GroupSlot<ShellView<?>, MenuItem> DEMO_GROUP = new GroupSlot<>(ShellView.class, "Demo");

        public static final GroupSlot<ShellView<?>, MenuItem> APPEARANCE_GROUP =
                new GroupSlot<>(ShellView.class, "Settings");

        public static final GroupSlot<ShellView<?>, MenuItem> LAST_GROUP = new GroupSlot<>(ShellView.class, "Last");

        private FileMenu() {
            // empty
        }
    }

    public static final class WindowMenu {

        public static final MenuSlot<ShellView<?>> MENU = new MenuSlot<>(ShellView.class, "Window");

        public static final GroupSlot<ShellView<?>, MenuItem> DEFAULT_GROUP =
                new GroupSlot<>(ShellView.class, "Default");

        public static final GroupSlot<ShellView<?>, MenuItem> ARRANGEMENT_GROUP =
                new GroupSlot<>(ShellView.class, "Arrangement");

        private WindowMenu() {
            // empty
        }
    }

    public static final class ExtraMenu {

        public static final MenuSlot<ShellView<?>> MENU = new MenuSlot<>(ShellView.class, "Extra");

        public static final GroupSlot<ShellView<?>, MenuItem> FOO_GROUP = new GroupSlot<>(ShellView.class, "Foo");

        public static final GroupSlot<ShellView<?>, MenuItem> BAR_GROUP = new GroupSlot<>(ShellView.class, "Bar");

        private ExtraMenu() {
            // empty
        }
    }

    /**
     * The menu bar of the shell; the File/Window/Extra menus are put into it.
     */
    public static final MenuBarSlot<ShellView<?>> MAIN_MENU = new MenuBarSlot<>(ShellView.class, "MainMenu");

    private ShellSlots() {
        // empty
    }
}
