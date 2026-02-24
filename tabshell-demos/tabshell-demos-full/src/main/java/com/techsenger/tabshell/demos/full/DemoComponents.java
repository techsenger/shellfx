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

package com.techsenger.tabshell.demos.full;

import com.techsenger.patternfx.core.ComponentName;

/**
 * Keys are used to identify components at they are always accessible and are kept in API. Classes cannot
 * be used because they are hidden in the modules impl packages.
 *
 * @author Pavel Castornii
 */
public final class DemoComponents {

    public static final ComponentName EDITOR_TAB = new ComponentName("Demo.EditorTab");

    public static final ComponentName HEX_EDITOR_TAB = new ComponentName("Demo.HexEditorTab");

    public static final ComponentName HEX_TOOL_BAR = new ComponentName("Demo.HexToolBar");

    public static final ComponentName DOCK_LAYOUT_TAB = new ComponentName("Demo.DockLayoutTab");

    public static final ComponentName DOCKABLE_TAB = new ComponentName("Demo.DockableTab");

    public static final ComponentName TEXT_VIEWER = new ComponentName("Demo.TextViewer");

    public static final ComponentName DIALOGS_DIALOG = new ComponentName("Demo.DialogsDialog");

    public static final ComponentName THEME_DIALOG = new ComponentName("Demo.ThemeDialog");

    public static final ComponentName PAGE_0 = new ComponentName("Demo.Page0");

    public static final ComponentName PAGE_1 = new ComponentName("Demo.Page1");

    public static final ComponentName PAGE_2 = new ComponentName("Demo.Page2");

    public static final ComponentName PAGED_TAB = new ComponentName("Demo.PagedTab");

    public static final ComponentName PAGED_DIALOG = new ComponentName("Demo.PagedDialog");

    private DemoComponents() {
        // empty
    }
}
