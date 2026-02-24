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

package com.techsenger.tabshell.terminal;

import com.techsenger.patternfx.core.ComponentName;

/**
 *
 * @author Pavel Castornii
 */
public final class TerminalComponents {

    public static final ComponentName TERMINAL_TAB = new ComponentName("Terminal.Tab");

    public static final ComponentName TOOL_BAR = new ComponentName("Terminal.ToolBar");

    public static final ComponentName MAIN_AREA = new ComponentName("Terminal.MainArea");

    public static final ComponentName FIND_PANEL = new ComponentName("Terminal.FindPanel");

    private TerminalComponents() {
        // empty
    }
}
