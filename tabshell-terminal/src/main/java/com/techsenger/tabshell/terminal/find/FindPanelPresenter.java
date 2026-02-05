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

package com.techsenger.tabshell.terminal.find;

import com.techsenger.jeditermfx.core.StyledTextConsumer;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.model.CharBuffer;
import com.techsenger.jeditermfx.core.model.SubCharBuffer;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.ui.FindResult;
import com.techsenger.jeditermfx.ui.SubstringFinder;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.shared.find.AbstractFullFindPanelPresenter;
import com.techsenger.tabshell.shared.find.FullFindPanelHistory;
import com.techsenger.tabshell.terminal.TerminalComponents;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelPresenter<V extends FindPanelView, C extends AreaComposer>
        extends AbstractFullFindPanelPresenter<V, C> {

    protected class Port extends AbstractFullFindPanelPresenter.Port implements FindPanelPort {

        @Override
        public void setFindText(String text) {
            getView().setFindText(text);
        }
    }

    private final TerminalTextBuffer textBuffer;

    private final Runnable closeHandler;

    public FindPanelPresenter(V view, HistoryProvider<FullFindPanelHistory> historyProvider,
            TerminalTextBuffer textBuffer, Runnable closeHandler) {
        super(view);
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(historyProvider);
        this.textBuffer = textBuffer;
        this.closeHandler = closeHandler;
    }

    @Override
    public FindPanelPort getPort() {
        return (FindPanelPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new FindPanelPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(TerminalComponents.FIND_PANEL);
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        getView().setWholeWordDisable(true);
        getView().setRegExpDisable(true);
        getView().setMatchesVisible(false);
    }

    protected void find() {
        var pattern = getView().getFindText();
        if (pattern.isEmpty()) {
            handleResetMatches();
            getView().setMatchesVisible(false);
            return;
        }
        final SubstringFinder finder = new SubstringFinder(pattern, !getView().isMatchCaseSelected());
        textBuffer.processHistoryAndScreenLines(-textBuffer.getHistoryLinesCount(), -1, new StyledTextConsumer() {

            @Override
            public void consume(int x, int y, TextStyle style, CharBuffer characters, int startRow) {
                int offset = 0;
                int length = characters.length();
                if (characters instanceof SubCharBuffer) {
                    SubCharBuffer subCharBuffer = (SubCharBuffer) characters;
                    characters = subCharBuffer.getParent();
                    offset = subCharBuffer.getOffset();
                }
                for (int i = offset; i < offset + length; i++) {
                    finder.nextChar(x, y - startRow, characters, i);
                }
            }

            @Override
            public void consumeNul(int x, int y, int nulIndex, TextStyle style,
                      CharBuffer characters, int startRow) { }

            @Override
            public void consumeQueue(int x, int y, int nulIndex, int startRow) { }
        });
        var result = finder.getResult();
        updateResultText(result);
        if (result != null && !result.getMatches().isEmpty()) {
             saveFindTextToHistory();
        }
        getView().setResult(result);
    }

    protected void handleTextInput(String text) {
        if (getView().getResult() == null) {
            find();
        } else {
            handleFindNext();
        }
    }

    @Override
    protected void handleClose() {
        closeHandler.run();
    }

    @Override
    protected void handleFindPrevious() {
        getView().selectPrevMatch();
        updateResultText(getView().getResult());
    }

    @Override
    protected void handleFindNext() {
        getView().selectNextMatch();
        updateResultText(getView().getResult());
    }

    @Override
    protected void handleResetMatches() {
        getView().setResult(null);
        getView().setNotFound(false);
    }

    protected void updateResultText(FindResult r) {
        if (r == null || r.getMatches().isEmpty()) {
            getView().setMatchesVisible(false);
            getView().setNotFound(true);
        } else {
            getView().setMatchesVisible(true);
            getView().setMatchesText(r.selectedMatch().getIndex() + " / " + r.getMatches().size());
        }
    }
}
