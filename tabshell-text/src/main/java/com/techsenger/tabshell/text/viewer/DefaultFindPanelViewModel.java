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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.area.AreaMediator;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.material.textarea.TextAreaStyle;
import com.techsenger.tabshell.shared.find.AbstractFullFindPanelViewModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
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
public class DefaultFindPanelViewModel<T extends AreaMediator> extends AbstractFullFindPanelViewModel<T>
        implements FindPanelViewModel {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFindPanelViewModel.class);

    private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper();

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

    DefaultFindPanelViewModel(FindMatchesResetPolicy resetPolicy, HistoryManager historyManager) {
        super();
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> historyManager.getOrCreateHistory(FindPanelHistory.class,
                FindPanelHistory::new));
        this.finder = new MatchFinder(resetPolicy);
        getRegExp().selectedProperty().addListener((ov, oldV, newV) -> getWholeWord().disableProperty().set(newV));
        getHighlight().selectedProperty().addListener((ov, oldV, newV) -> {
            if (newV && !this.finder.getMatchRanges().isEmpty()) {
                highlightActive.set(true);
            } else {
                highlightActive.set(false);
            }
        });
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
            setNotFound(false);
            highlightActive.set(false);
            setMatchesVisible(false);
        }
    }

    public ReadOnlyStringProperty textProperty() {
        return this.text.getReadOnlyProperty();
    }

    public String getText() {
        return this.text.get();
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

    @Override
    protected FindPanelHistory getHistory() {
        return (FindPanelHistory) super.getHistory();
    }

    @Override
    protected void restoreData() {
        super.restoreData();
        var h = getHistory();
        getReplaceTexts().addAll(h.getReplaceTexts());
    }

    @Override
    protected void saveData() {
        super.saveData();
        var h = getHistory();
        h.setReplaceTexts(new ArrayList<>(getReplaceTexts()));
    }

    ReadOnlyStringWrapper getTextWrapper() {
        return this.text;
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
        if (getHighlight().isSelected()) {
            setHighlightActive(false);
        }
        var wholeText = textProperty().get();
        var findText = findTextProperty().get();
        if (findText.isBlank()) {
            return;
        }
        this.saveFindTextToHistory();
        this.finder.find(wholeText, findText, getRegExp().isSelected(),
                getWholeWord().isSelected(), getMatchCase().isSelected());
        //we find all ranges, but we start not from the beginning of the file, but from caret position
        if (!this.finder.getMatchRanges().isEmpty()) {
            setNotFound(false);
            if (getHighlight().isSelected()) {
                setHighlightActive(true);
            }
        } else {
            setNotFound(true);
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
        logger.debug("{} Current range: {}", getMediator().getLogPrefix(), range);
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
            setMatchesText((range.getIndex() + 1) + " / " + this.finder.getMatchRanges().size());
            setMatchesVisible(true);
        } else {
            setMatchesText(null);
            setMatchesVisible(false);
        }
    }

    private void addReplaceText() {
        var replaceText = this.replaceText.get();
        if (!replaceText.isBlank()) {
            HistoryUtils.addFirst(replaceTexts, replaceText);
            this.replaceText.set(replaceText);
        }
    }
}
