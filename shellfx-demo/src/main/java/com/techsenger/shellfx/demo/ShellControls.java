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

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.material.menu.DefaultMenuGroupName;
import com.techsenger.shellfx.material.menu.DefaultMenuName;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.menu.MenuName;

/**
 * The menus and menu groups the demo application's shell menu bar offers.
 *
 * @author Pavel Castornii
 */
public final class ShellControls {

    public static final class FileMenu {

        public static final MenuName<ShellFxView<?>> NAME = createName();

        public static final MenuGroupName<ShellFxView<?>> DEMO_GROUP = createGroupName("Demo");

        public static final MenuGroupName<ShellFxView<?>> APPEARANCE_GROUP = createGroupName("Settings");

        public static final MenuGroupName<ShellFxView<?>> LAST_GROUP = createGroupName("Last");

        private FileMenu() {
            // empty
        }
    }

    public static final class WindowMenu {

        public static final MenuName<ShellFxView<?>> NAME = createName();

        public static final MenuGroupName<ShellFxView<?>> DEFAULT_GROUP = createGroupName("Default");

        public static final MenuGroupName<ShellFxView<?>> ARRANGEMENT_GROUP = createGroupName("Arrangement");

        private WindowMenu() {
            // empty
        }
    }

    public static final class ExtraMenu {

        public static final MenuName<ShellFxView<?>> NAME = createName();

        public static final MenuGroupName<ShellFxView<?>> FOO_GROUP = createGroupName("Foo");

        public static final MenuGroupName<ShellFxView<?>> BAR_GROUP = createGroupName("Bar");

        private ExtraMenu() {
            // empty
        }
    }

    /**
     * The group File/Window/Extra menus register into, and that {@link com.techsenger.shellfx.core.DefaultShellFxView}
     * treats as the top-level group of its own menu bar.
     */
    public static final MenuGroupName<ShellFxView<?>> MAIN_MENU_GROUP = createGroupName("MainMenuGroup");

    private static MenuName<ShellFxView<?>> createName() {
        return new DefaultMenuName<>(ShellFxView.class);
    }

    private static MenuGroupName<ShellFxView<?>> createGroupName(String text) {
        return new DefaultMenuGroupName<>(ShellFxView.class, text);
    }

    private ShellControls() {
        // empty
    }
}
