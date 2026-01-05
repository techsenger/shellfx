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

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.tab.AbstractShellTabComponent;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabComponent<T extends TerminalTabView<?, ?>> extends AbstractShellTabComponent<T> {

    protected class Mediator extends AbstractShellTabComponent.Mediator implements TerminalTabMediator {

        @Override
        public void addFindPanel(FindPanelViewModel vm) {
            var v = new FindPanelView(vm, getView().getWidget());
            findPanel = new FindPanelComponent<>(v);
            findPanel.initialize();
            getModifiableChildren().add(findPanel);
            getView().showFind(v);
        }

        @Override
        public void removeFindPanel() {
            getView().hideFind(findPanel.getView());
            getModifiableChildren().remove(findPanel);
            findPanel.deinitialize();
            findPanel = null;
        }

    }

    private FindPanelComponent<?> findPanel;

    public TerminalTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
    }

    @Override
    public Name getName() {
        return TerminalComponentNames.TERMINAL_TAB;
    }

    protected FindPanelComponent<?> getFindPanel() {
        return findPanel;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

}
