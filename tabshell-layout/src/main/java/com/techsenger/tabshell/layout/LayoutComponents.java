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

package com.techsenger.tabshell.layout;

import com.techsenger.patternfx.core.ComponentName;

/**
 *
 * @author Pavel Castornii
 */
public final class LayoutComponents {

    public static final ComponentName TAB_HOST = new ComponentName("Layout.TabHost");

    public static final ComponentName DOCK_LAYOUT = new ComponentName("Layout.DockLayout");

    public static final ComponentName SPLIT_SPACE = new ComponentName("Layout.SplitSpace");

    public static final ComponentName TAB_DOCK = new ComponentName("Layout.TabDock");

    public static final ComponentName SIDE_BAR = new ComponentName("Layout.SideBar");

    public static final ComponentName TAB_POPUP = new ComponentName("Layout.TabPopup");

    public static final ComponentName PLACEHOLDER = new ComponentName("Layout.Placeholder");

    public static final ComponentName PAGE_HOST = new ComponentName("Layout.PageHost");

    private LayoutComponents() {
        // empty
    }
}
