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

package com.techsenger.tabshell.hex;

import com.techsenger.patternfx.core.Name;

/**
 *
 * @author Pavel Castornii
 */
public final class HexComponentNames {

    public static final Name TOOL_BAR = new Name("Hex.ToolBar");

    public static final Name MAIN_AREA = new Name("Hex.MainArea");

    public static final Name EDITOR_TAB = new Name("Hex.EditorTab");

    public static final Name EDITOR_CARET = new Name("Hex.EditorCaret");

    public static final Name DATA_INSPECTOR_TAB = new Name("Hex.DataInspectorTab");

    public static final GroupName TOOLS = new GroupName("Tools");

    private HexComponentNames() {
        // empty
    }


}
