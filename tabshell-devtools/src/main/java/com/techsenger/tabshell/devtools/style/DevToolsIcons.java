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

package com.techsenger.tabshell.devtools.style;

import com.techsenger.tabshell.material.icon.StyleFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public interface DevToolsIcons {

    StyleFontIcon SELECT = new StyleFontIcon("select-icon");

    StyleFontIcon SELECTION = new StyleFontIcon("selection-icon");

    StyleFontIcon RECORD_START = new StyleFontIcon("record-start-icon");

    StyleFontIcon RECORD_STOP = new StyleFontIcon("record-stop-icon");

    StyleFontIcon FILTER = new StyleFontIcon("filter-icon");

    StyleFontIcon SELECTED_ONLY = new StyleFontIcon("selected-only-icon");
}
