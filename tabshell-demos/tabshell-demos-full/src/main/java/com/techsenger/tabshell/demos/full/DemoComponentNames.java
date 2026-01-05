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

import com.techsenger.patternfx.core.Name;

/**
 * Keys are used to identify components at they are always accessible and are kept in API. Classes cannot
 * be used because they are hidden in the modules impl packages.
 *
 * @author Pavel Castornii
 */
public interface DemoComponentNames {

    Name DEMO_EDITOR_TAB = new Name("DemoEditorTab");

    Name DEMO_HEX_EDITOR_TAB = new Name("DemoHexEditorTab");

    Name DEMO_HEX_TOOL_BAR = new Name("DemoHexToolBar");

    Name DEMO_DOCK_LAYOUT_TAB = new Name("DemoDockLayoutTab");

    Name DEMO_DOCKABLE_TAB = new Name("DemoDockableTab");

    Name DEMO_TEXT_VIEWER = new Name("DemoTextViewer");

    Name DEMO_DIALOGS_DIALOG = new Name("DemoDialogsDialog");

    Name DEMO_THEME_DIALOG = new Name("DemoThemeDialog");
}
