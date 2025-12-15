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

package com.techsenger.tabshell.terminal;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.tab.AbstractShellTabComponent;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabComponent<T extends TerminalTabView<?, ?>> extends AbstractShellTabComponent<T> {

    protected class Mediator extends AbstractShellTabComponent.Mediator implements TerminalTabMediator {

        @Override
        public void addFindPane(FindPaneViewModel vm) {
            var v = new FindPaneView(vm, getView().getWidget());
            findPane = new FindPaneComponent<>(v, TerminalTabComponent.this.getShell().getHistoryManager());
            findPane.initialize();
            getModifiableChildren().add(findPane);
            getView().showFind(v);
        }

        @Override
        public void removeFindPane() {
            getView().hideFind(findPane.getView());
            getModifiableChildren().remove(findPane);
            findPane.deinitialize();
            findPane = null;
        }

    }

    private FindPaneComponent<?> findPane;

    public TerminalTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> shell.getHistoryManager().getOrCreateHistory(TerminalHistory.class,
                TerminalHistory::new));
    }

    @Override
    public ComponentName getName() {
        return TerminalComponentNames.TERMINAL_TAB;
    }

    protected FindPaneComponent<?> getFindPane() {
        return findPane;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

}
