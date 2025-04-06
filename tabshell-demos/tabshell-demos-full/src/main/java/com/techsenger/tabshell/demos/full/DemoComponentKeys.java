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

package com.techsenger.tabshell.demos.full;

import com.techsenger.tabshell.core.dialog.DialogKey;
import com.techsenger.tabshell.core.tab.ShellTabKey;

/**
 * Keys are used to identify components at they are always accessible and are kept in API. Classes cannot
 * be used because they are hidden in the modules impl packages.
 *
 * @author Pavel Castornii
 */
public final class DemoComponentKeys {

    public static final ShellTabKey EDITOR_TAB = new ShellTabKey("Demo Editor Tab");

    public static final DialogKey THEME_DIALOG = new DialogKey("Theme Dialog");

    private DemoComponentKeys() {
        //empty
    }
}
