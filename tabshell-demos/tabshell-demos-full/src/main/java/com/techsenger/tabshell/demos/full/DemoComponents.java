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
public interface DemoComponents {

    ComponentName DEMO_EDITOR_TAB = new ComponentName("DemoEditorTab");

    ComponentName DEMO_HEX_EDITOR_TAB = new ComponentName("DemoHexEditorTab");

    ComponentName DEMO_HEX_TOOL_BAR = new ComponentName("DemoHexToolBar");

    ComponentName DEMO_DOCK_LAYOUT_TAB = new ComponentName("DemoDockLayoutTab");

    ComponentName DEMO_DOCKABLE_TAB = new ComponentName("DemoDockableTab");

    ComponentName DEMO_TEXT_VIEWER = new ComponentName("DemoTextViewer");

    ComponentName DEMO_DIALOGS_DIALOG = new ComponentName("DemoDialogsDialog");

    ComponentName DEMO_THEME_DIALOG = new ComponentName("DemoThemeDialog");

    ComponentName DEMO_PAGE_0 = new ComponentName("DemoPage0");

    ComponentName DEMO_PAGE_1 = new ComponentName("DemoPage1");

    ComponentName DEMO_PAGE_2 = new ComponentName("DemoPage2");

    ComponentName DEMO_PAGED_TAB = new ComponentName("DemoPagedTab");

    ComponentName DEMO_PAGED_DIALOG = new ComponentName("DemoPagedDialog");
}
