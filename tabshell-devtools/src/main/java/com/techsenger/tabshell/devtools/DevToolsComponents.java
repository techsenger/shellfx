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

package com.techsenger.tabshell.devtools;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.DefaultComponentName;

/**
 *
 * @author Pavel Castornii
 */
public final class DevToolsComponents {

    public static final ComponentName WINDOW = new DefaultComponentName("DevTools.Window");

    public static final ComponentName TAB_DOCK = new DefaultComponentName("DevTools.TabDock");

    public static final ComponentName TOOL_BAR = new DefaultComponentName("DevTools.ToolBar");

    public static final ComponentName COMPONENT_TAB = new DefaultComponentName("DevTools.ComponentTab");

    public static final ComponentName NODE_TAB = new DefaultComponentName("DevTools.NodeTab");

    public static final ComponentName PROPERTY_DIALOG = new DefaultComponentName("DevTools.PropertyDialog");

    public static final ComponentName EVENT_TAB = new DefaultComponentName("DevTools.EventTab");

    public static final ComponentName EVENT_TOOL_BAR = new DefaultComponentName("DevTools.EventToolBar");

    public static final ComponentName STYLESHEET_TAB = new DefaultComponentName("DevTools.StylesheetTab");

    public static final ComponentName ENVIRONMENT_TAB = new DefaultComponentName("DevTools.EnvironmentTab");

    private DevToolsComponents() {
        // empty
    }
}
