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

package com.techsenger.shellfx.demo.menu.file;

import com.techsenger.shellfx.material.menu.DefaultMenuGroupName;
import com.techsenger.shellfx.material.menu.DefaultMenuName;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.menu.MenuName;

/**
 *
 * @author Pavel Castornii
 */
public final class FileMenu {

    public static final MenuName NAME = new DefaultMenuName();

    public static final MenuGroupName DEMO_GROUP = new DefaultMenuGroupName("Demo");

    public static final MenuGroupName APPEARANCE_GROUP = new DefaultMenuGroupName("Settings");

    public static final MenuGroupName LAST_GROUP = new DefaultMenuGroupName("Last");

    private FileMenu() {
        // empty
    }
}
