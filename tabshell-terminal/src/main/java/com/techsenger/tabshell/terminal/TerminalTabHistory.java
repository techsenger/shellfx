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

import com.techsenger.tabshell.core.shelltab.ShellTabHistory;
import com.techsenger.tabshell.shared.find.FullFindPanelHistory;
import com.techsenger.tabshell.terminal.toolbar.TerminalToolBarHistory;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabHistory extends ShellTabHistory {

    private final TerminalToolBarHistory toolBar = new TerminalToolBarHistory();

    private final FullFindPanelHistory findPanel = new FullFindPanelHistory();

    public TerminalToolBarHistory getToolBar() {
        return toolBar;
    }

    public FullFindPanelHistory getFindPanel() {
        return findPanel;
    }
}
