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

import com.techsenger.tabshell.core.area.AreaView;
import com.techsenger.tabshell.terminal.TerminalPaletteType;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface TerminalToolBarView extends AreaView {

    void setPaletteTypes(List<TerminalPaletteType> types);

    TerminalPaletteType getPaletteType();

    void setPaletteType(TerminalPaletteType type);

    void setCopyDisable(boolean value);
}
