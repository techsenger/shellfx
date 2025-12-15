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

import com.techsenger.jeditermfx.core.StyledTextConsumer;
import com.techsenger.jeditermfx.core.TextStyle;
import com.techsenger.jeditermfx.core.model.CharBuffer;
import com.techsenger.jeditermfx.core.model.SubCharBuffer;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.ui.FindResult;
import com.techsenger.jeditermfx.ui.SubstringFinder;
import com.techsenger.tabshell.shared.find.AbstractFindPaneViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class FindPaneViewModel extends AbstractFindPaneViewModel {

    private TerminalTextBuffer textBuffer;

    private final ObjectProperty<FindResult> result = new SimpleObjectProperty<>();

    public FindPaneViewModel(String selectedText) {
        findTextProperty().set(selectedText);
        wholeWordDisableProperty().set(true);
        regExpDisableProperty().set(true);
        result.addListener((ov, oldV, newV) -> {
            updateResultText();
            if (newV != null && !newV.getItems().isEmpty()) {
                addFindText();
            }
        });
    }

    public void find() {
        var pattern = findTextProperty().get();
        var ignoreCase = caseSelectedProperty().get();
        if (pattern.isEmpty()) {
            result.set(null);
        }
        final SubstringFinder finder = new SubstringFinder(pattern, !caseSelectedProperty().get());
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
            public void consumeNul(int x, int y, int nulIndex, TextStyle style, CharBuffer characters, int startRow) { }

            @Override
            public void consumeQueue(int x, int y, int nulIndex, int startRow) { }
        });
        result.set(finder.getResult());
    }

    public ObjectProperty<FindResult> resultProperty() {
        return result;
    }

    public FindResult getResult() {
        return result.get();
    }

    public void setResult(FindResult result) {
        this.result.set(result);
    }

    @Override
    protected void resetMatches() {
        result.set(null);
        notFoundProperty().set(false);
    }

    protected void updateResultText() {
        var r = result.get();
        if (r == null || r.getItems().isEmpty()) {
            resultTextVisibleProperty().set(false);
            notFoundProperty().set(true);
        } else {
            resultTextVisibleProperty().set(true);
            resultTextProperty().set(r.selectedItem().getIndex() + " / " + r.getItems().size());
        }
    }

    void setTextBuffer(TerminalTextBuffer textBuffer) {
        this.textBuffer = textBuffer;
    }
}
