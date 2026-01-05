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

import com.techsenger.patternfx.core.GroupName;
import com.techsenger.patternfx.core.Name;

/**
 *
 * @author Pavel Castornii
 */
public interface HexComponentNames {

    Name HEX_TOOL_BAR = new Name("HexToolBar");

    Name HEX_AREA = new Name("HexArea");

    Name HEX_EDITOR_TAB = new Name("HexEditorTab");

    Name HEX_EDITOR_CARET = new Name("HexEditorCaret");

    Name DATA_INSPECTOR_TAB = new Name("DataInspectorTab");

    GroupName TOOLS = new GroupName("Tools");
}
