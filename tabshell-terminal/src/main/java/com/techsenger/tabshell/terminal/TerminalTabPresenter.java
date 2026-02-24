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

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.area.AreaPort;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabPresenter;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.tabshell.terminal.toolbar.ToolBarPort;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabPresenter<V extends TerminalTabView, C extends TerminalTabComposer>
        extends AbstractShellTabPresenter<V, C> {

    protected class Port extends AbstractShellTabPresenter.Port implements TerminalTabPort {

        private final TerminalTabPresenter<?, ?> presenter = TerminalTabPresenter.this;

        @Override
        public String getDirectory() {
            return presenter.directory;
        }

        @Override
        public ToolBarPort getToolBar() {
            return getComposer().getToolBar();
        }

        @Override
        public AreaPort getArea() {
            return getComposer().getArea();
        }

        @Override
        public TerminalTabHistory getHistory() {
            return presenter.getHistory();
        }

    }

    private final String directory;

    public TerminalTabPresenter(V view, String directory, HistoryManager historyManager) {
        super(view);
        this.directory = directory;
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> historyManager
                .getOrCreateHistory(TerminalTabHistory.class, TerminalTabHistory::new));
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TerminalTabPort getPort() {
        return (TerminalTabPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new TerminalTabPresenter.Port();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var view = getView();
        view.setIcon(TerminalIcons.TERMINAL);
        view.setTitle("Terminal");
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(TerminalComponents.TERMINAL_TAB);
    }

    @Override
    protected TerminalTabHistory getHistory() {
        return (TerminalTabHistory) super.getHistory();
    }

    protected String getDirectory() {
        return directory;
    }

    @Override
    public void onSelected(boolean selected) {
        if (selected) {
            getComposer().getArea().requestFocus();
        }
    }
}
