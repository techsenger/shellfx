/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.kit.terminal;

import com.techsenger.tabshell.core.tab.AbstractShellTabHistory;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalHistory extends AbstractShellTabHistory<TerminalTabViewModel> {

    private TerminalPaletteType paletteType;

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        paletteType = TerminalPaletteType.THEME_32_LC;
    }

    @Override
    public void saveAppearance(TerminalTabViewModel viewModel) {
        super.saveAppearance(viewModel);
        this.paletteType = viewModel.paletteTypeProperty().get();
    }

    @Override
    public void restoreAppearance(TerminalTabViewModel viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.paletteTypeProperty().set(paletteType);
    }
}
