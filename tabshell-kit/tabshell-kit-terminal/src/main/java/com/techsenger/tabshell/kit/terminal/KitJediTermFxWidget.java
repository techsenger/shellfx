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

import com.techsenger.jeditermfx.core.model.StyleState;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import com.techsenger.jeditermfx.ui.TerminalAction;
import com.techsenger.jeditermfx.ui.TerminalPanel;
import com.techsenger.jeditermfx.ui.settings.SettingsProvider;
import java.util.List;
import javafx.scene.input.KeyCode;

/**
 *
 * @author Pavel Castornii
 */
public class KitJediTermFxWidget extends JediTermFxWidget {

    private final Runnable showFind;

    public KitJediTermFxWidget(int columns, int lines, SettingsProvider settingsProvider, Runnable showFind) {
        super(columns, lines, settingsProvider);
        this.showFind = showFind;
    }

    @Override
    public List<TerminalAction> getActions() {
        return List.of(new TerminalAction(mySettingsProvider.getFindActionPresentation(),
                keyEvent -> {
                    this.showFind.run();
                    return true;
                }).withMnemonicKey(KeyCode.F));
    }

    @Override
    protected TerminalPanel createTerminalPanel(SettingsProvider settingsProvider, StyleState styleState,
            TerminalTextBuffer terminalTextBuffer) {
        return new KitTerminalPanel(settingsProvider, terminalTextBuffer, styleState);
    }
}
