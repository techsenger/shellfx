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

package com.techsenger.tabshell.terminal.toolbar;

import com.techsenger.tabshell.core.area.AreaHistory;
import com.techsenger.tabshell.terminal.TerminalPaletteType;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarHistory extends AreaHistory {

    private TerminalPaletteType paletteType;

    public TerminalPaletteType getPaletteType() {
        return paletteType;
    }

    public void setPaletteType(TerminalPaletteType paletteType) {
        this.paletteType = paletteType;
    }
}
