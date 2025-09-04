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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.history.DefaultClassHistoryProvider;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.material.textarea.TextAreaStyle;
import com.techsenger.tabshell.shared.find.AbstractFindPaneViewModel;
import com.techsenger.tabshell.text.TextComponentKeys;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;
import org.fxmisc.richtext.model.RichTextChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State control without highlighting. 1) user enters word and starts search; 2) program finds ranges and keep them;
 * 3) if user modifies text, then а) if modification covers a range this range is removed b) if not then all positions
 * of all ranges after modification position are updated 4) if user does undo/redo, all ranges are reset.
 * 5) when replace is used then program modifies ranges positions; 6) when replace all is used then program uses multi
 * change and passes origin ranges.
 *
 * <p>Highlighting works this this way - a) in step #3-a the highlighting for modified ranges is removed b)
 * in step #3-b nothing happens. On reset all highlighting removed
 *
 * @author Pavel Castornii
 */
class DefaultFindPaneViewModel extends AbstractFindPaneViewModel implements FindPaneViewModel {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFindPaneViewModel.class);

    private final ObjectProperty<Integer> textLength = new SimpleObjectProperty<>();

    private final BooleanProperty textAreaEditable = new SimpleBooleanProperty();

    private final ObjectProperty<Integer> caretPosition = new SimpleObjectProperty<>();

    private final BooleanProperty replaceMode = new SimpleBooleanProperty();

    private final StringProperty replaceText = new SimpleStringProperty();

    private final ObservableList<String> replaceTexts = FXCollections.observableArrayList();

    private boolean replacingIsDone = false;

    private final MatchFinder finder;

    private final ObjectProperty<IndexRange> selection = new SimpleObjectProperty<>();

    private final BooleanProperty highlightActive = new SimpleBooleanProperty(false);

    DefaultFindPaneViewModel(FindMatchesResetPolicy resetPolicy, HistoryManager historyManager) {
        super();
        this.finder = new MatchFinder(resetPolicy);
        regExpSelectedProperty().addListener((ov, oldV, newV) -> wholeWordDisableProperty().set(newV));
        highlightSelectedProperty().addListener((ov, oldV, newV) -> {
            if (newV && !this.finder.getMatchRanges().isEmpty()) {
                highlightActive.set(true);
            } else {
                highlightActive.set(false);
            }
        });
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(new DefaultClassHistoryProvider<>(historyManager, FindPaneHistory.class,
                FindPaneHistory::new));
    }

    @Override
    public PaneKey getKey() {
        return TextComponentKeys.FIND_PANE;
    }

    public BooleanProperty replaceModeProperty() {
        return replaceMode;
    }

    public boolean isReplaceMode() {
        return replaceMode.get();
    }

    public void setReplaceMode(boolean value) {
        this.replaceMode.set(value);
    }

    @Override
    public boolean hasNextMatch() {
        return this.finder.hasNextMatch();
    }

    @Override
    public boolean hasPreviousMatch() {
        return this.finder.hasPreviousMatch();
    }

    @Override
    public void findPrevious() {
        if (this.finder.matchesResetProperty().get()) {
            find();
        }
        moveToPreviousRange();
    }

    @Override
    public void findNext() {
        if (this.finder.matchesResetProperty().get()) {
            find();
        }
        moveToNextRange();
    }

    @Override
    public void resetMatches() {
        if (this.finder.resetMatches()) {
            notFoundProperty().set(false);
            highlightActive.set(false);
            resultTextVisibleProperty().set(false);
        }
    }

    public BooleanProperty highlightActiveProperty() {
        return highlightActive;
    }

    public boolean isHighlightActive() {
        return highlightActive.get();
    }

    public void setHighlightActive(boolean value) {
        this.highlightActive.set(value);
    }

    StringProperty replaceTextProperty() {
        return this.replaceText;
    }

    String getReplaceText() {
        return this.replaceText.get();
    }

    void setReplaceText(String value) {
        this.replaceText.set(value);
    }

    BooleanProperty textAreaEditableProperty() {
        return textAreaEditable;
    }

    boolean isTextAreaEditable() {
        return textAreaEditable.get();
    }

    void setTextAreaEditable(boolean value) {
        this.textAreaEditable.set(value);
    }

    ObjectProperty<Integer> caretPositionProperty() {
        return caretPosition;
    }

    Integer getCaretPosition() {
        return caretPosition.get();
    }

    void setCaretPosition(Integer value) {
        this.caretPosition.set(value);
    }

    ObjectProperty<Integer> textLengthProperty() {
        return textLength;
    }

    Integer getTextLength() {
        return textLength.get();
    }

    void setTextLength(Integer value) {
        this.textLength.set(value);
    }

    ObjectProperty<IndexRange> selectionProperty() {
        return selection;
    }

    IndexRange getSelection() {
        return selection.get();
    }

    void setSelection(IndexRange value) {
        this.selection.set(value);
    }

    /**
     * Method contains main logic for finding text.
     * @param text
     * @param wholeText
     */
    void find() {
        if (highlightSelectedProperty().get()) {
            setHighlightActive(false);
        }
        var wholeText = textProperty().get();
        var findText = findTextProperty().get();
        if (findText.isBlank()) {
            return;
        }
        this.addFindText();
        this.finder.find(wholeText, findText, regExpSelectedProperty().get(),
                wholeWordSelectedProperty().get(), caseSelectedProperty().get());
        //we find all ranges, but we start not from the beginning of the file, but from caret position
        if (!this.finder.getMatchRanges().isEmpty()) {
            notFoundProperty().set(false);
            if (isHighlightSelected()) {
                setHighlightActive(true);
            }
        } else {
            notFoundProperty().set(true);
        }
    }

    boolean moveToPreviousRange() {
        int position = this.caretPosition.get();
        //for non editable after reset we start from the end
        if (!this.textAreaEditable.get() && this.finder.matchRangeProperty().get() == null) {
            position = this.textLength.get();
        }
        var newRange = this.finder.resolvePreviousRange(position);
        if (newRange != null) {
            this.moveToRange(newRange);
            return true;
        } else {
            return false;
        }
    }

    boolean moveToNextRange() {
        int position = this.caretPosition.get();
        //for non editable after reset we start from the beginning
        if (!this.textAreaEditable.get() && this.finder.matchRangeProperty().get() == null) {
            position = 0;
        }
        var newRange = this.finder.resolveNextRange(this.selection.get(), position);
        if (newRange != null) {
            this.moveToRange(newRange);
            return true;
        } else {
            return false;
        }
    }

    void moveToRange(MatchRange range) {
        this.finder.matchRangeProperty().set(range);
        this.updateInfoLabel(range);
        logger.debug("Current range: " + range);
    }

    void setReplacingIsDone(boolean replacingIsDone) {
        this.replacingIsDone = replacingIsDone;
        if (replacingIsDone) {
            this.addReplaceText();
        }
    }

    void updateNextRangesOnReplace() {
        this.finder.updateNextRangesOnReplace(replaceText.get());
    }

    void updateMatchesOnTextChange(RichTextChange<Collection<String>, String, Collection<TextAreaStyle>> c) {
        if (this.finder.updateMatchesOnTextChange(c, replacingIsDone)) {
            this.updateInfoLabel(this.finder.matchRangeProperty().get());
        }
    }

    ObservableList<String> getReplaceTexts() {
        return replaceTexts;
    }

    List<MatchRange> getMatchRanges() {
        return finder.getMatchRanges();
    }

    MatchRange getMatchRange() {
        return finder.matchRangeProperty().get();
    }

    BooleanProperty matchesResetProperty() {
        return finder.matchesResetProperty();
    }

    boolean isMatchesReset() {
        return matchesResetProperty().get();
    }

    void setMatchesReset(boolean value) {
        matchesResetProperty().set(value);
    }

    private void updateInfoLabel(MatchRange range) {
        if (range != null) {
            resultTextProperty().set((range.getIndex() + 1) + " / " + this.finder.getMatchRanges().size());
            resultTextVisibleProperty().set(true);
        } else {
            resultTextProperty().set(null);
            resultTextVisibleProperty().set(false);
        }
    }

    private void addReplaceText() {
        var replaceText = this.replaceText.get();
        if (!replaceText.isBlank()) {
            historyUpdatedProperty().set(true);
            HistoryUtils.addFirst(replaceTexts, replaceText);
            this.replaceText.set(replaceText);
            historyUpdatedProperty().set(false);
        }
    }
}
