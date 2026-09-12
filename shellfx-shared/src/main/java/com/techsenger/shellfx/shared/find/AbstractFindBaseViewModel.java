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

package com.techsenger.shellfx.shared.find;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.history.HistoryUtils;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindBaseViewModel<C extends ChildComposer> extends AbstractAreaViewModel<C>
        implements FullFindBasePort {

    private final StringProperty findText = new SimpleStringProperty();

    private final ObservableList<String> modifiableFindTexts = FXCollections.observableArrayList();

    private final ObservableList<String> findTexts =
            FXCollections.unmodifiableObservableList(modifiableFindTexts);

    private final BooleanProperty notFound = new SimpleBooleanProperty();

    private final BooleanProperty matchCaseSelected = new SimpleBooleanProperty();

    private final BooleanProperty matchCaseDisabled = new SimpleBooleanProperty();

    private final StringProperty matchesText = new SimpleStringProperty();

    private final BooleanProperty matchesVisible = new SimpleBooleanProperty();

    private final BooleanProperty clearVisible = new SimpleBooleanProperty();

    private final BooleanProperty findNextDisabled = new SimpleBooleanProperty();

    private final BooleanProperty findPreviousDisabled = new SimpleBooleanProperty();

    private final BooleanProperty wholeWordSelected = new SimpleBooleanProperty();

    private final BooleanProperty wholeWordDisabled = new SimpleBooleanProperty();

    private final BooleanProperty regExpSelected = new SimpleBooleanProperty();

    private final BooleanProperty regExpDisabled = new SimpleBooleanProperty();

    private final BooleanProperty highlightSelected = new SimpleBooleanProperty();

    private final BooleanProperty highlightDisabled = new SimpleBooleanProperty();

    public AbstractFindBaseViewModel(AreaParams params) {
        super(params);
        findText.addListener((obs, oldV, newV) -> onFindTextChanged(newV));
    }

    @Override
    public String getFindText() {
        return findText.get();
    }

    @Override
    public void setFindText(String findText) {
        this.findText.set(findText);
    }

    @Override
    public StringProperty findTextProperty() {
        return findText;
    }

    @Override
    public @Unmodifiable ObservableList<String> getFindTexts() {
        return findTexts;
    }

    @Override
    public boolean isNotFound() {
        return notFound.get();
    }

    @Override
    public void setNotFound(boolean notFound) {
        this.notFound.set(notFound);
    }

    @Override
    public BooleanProperty notFoundProperty() {
        return notFound;
    }

    @Override
    public boolean isMatchCaseSelected() {
        return matchCaseSelected.get();
    }

    @Override
    public void setMatchCaseSelected(boolean matchCaseSelected) {
        this.matchCaseSelected.set(matchCaseSelected);
    }

    @Override
    public BooleanProperty matchCaseSelectedProperty() {
        return matchCaseSelected;
    }

    @Override
    public boolean isMatchCaseDisabled() {
        return matchCaseDisabled.get();
    }

    @Override
    public void setMatchCaseDisabled(boolean matchCaseDisabled) {
        this.matchCaseDisabled.set(matchCaseDisabled);
    }

    @Override
    public BooleanProperty matchCaseDisabledProperty() {
        return matchCaseDisabled;
    }

    @Override
    public String getMatchesText() {
        return matchesText.get();
    }

    @Override
    public void setMatchesText(String matchesText) {
        this.matchesText.set(matchesText);
    }

    @Override
    public StringProperty matchesTextProperty() {
        return matchesText;
    }

    @Override
    public boolean isMatchesVisible() {
        return matchesVisible.get();
    }

    @Override
    public void setMatchesVisible(boolean matchesVisible) {
        this.matchesVisible.set(matchesVisible);
    }

    @Override
    public BooleanProperty matchesVisibleProperty() {
        return matchesVisible;
    }

    @Override
    public boolean isClearVisible() {
        return clearVisible.get();
    }

    @Override
    public void setClearVisible(boolean clearVisible) {
        this.clearVisible.set(clearVisible);
    }

    @Override
    public BooleanProperty clearVisibleProperty() {
        return clearVisible;
    }

    @Override
    public boolean isFindNextDisabled() {
        return findNextDisabled.get();
    }

    @Override
    public void setFindNextDisabled(boolean findNextDisabled) {
        this.findNextDisabled.set(findNextDisabled);
    }

    @Override
    public BooleanProperty findNextDisabledProperty() {
        return findNextDisabled;
    }

    @Override
    public boolean isFindPreviousDisabled() {
        return findPreviousDisabled.get();
    }

    @Override
    public void setFindPreviousDisabled(boolean findPreviousDisabled) {
        this.findPreviousDisabled.set(findPreviousDisabled);
    }

    @Override
    public BooleanProperty findPreviousDisabledProperty() {
        return findPreviousDisabled;
    }

    @Override
    public boolean isWholeWordSelected() {
        return wholeWordSelected.get();
    }

    @Override
    public void setWholeWordSelected(boolean wholeWordSelected) {
        this.wholeWordSelected.set(wholeWordSelected);
    }

    @Override
    public BooleanProperty wholeWordSelectedProperty() {
        return wholeWordSelected;
    }

    @Override
    public boolean isWholeWordDisabled() {
        return wholeWordDisabled.get();
    }

    @Override
    public void setWholeWordDisabled(boolean wholeWordDisabled) {
        this.wholeWordDisabled.set(wholeWordDisabled);
    }

    @Override
    public BooleanProperty wholeWordDisabledProperty() {
        return wholeWordDisabled;
    }

    @Override
    public boolean isRegExpSelected() {
        return regExpSelected.get();
    }

    @Override
    public void setRegExpSelected(boolean regExpSelected) {
        this.regExpSelected.set(regExpSelected);
    }

    @Override
    public BooleanProperty regExpSelectedProperty() {
        return regExpSelected;
    }

    @Override
    public boolean isRegExpDisabled() {
        return regExpDisabled.get();
    }

    @Override
    public void setRegExpDisabled(boolean regExpDisabled) {
        this.regExpDisabled.set(regExpDisabled);
    }

    @Override
    public BooleanProperty regExpDisabledProperty() {
        return regExpDisabled;
    }

    @Override
    public boolean isHighlightSelected() {
        return highlightSelected.get();
    }

    @Override
    public void setHighlightSelected(boolean highlightSelected) {
        this.highlightSelected.set(highlightSelected);
    }

    @Override
    public BooleanProperty highlightSelectedProperty() {
        return highlightSelected;
    }

    @Override
    public boolean isHighlightDisabled() {
        return highlightDisabled.get();
    }

    @Override
    public void setHighlightDisabled(boolean highlightDisabled) {
        this.highlightDisabled.set(highlightDisabled);
    }

    @Override
    public BooleanProperty highlightDisabledProperty() {
        return highlightDisabled;
    }

    /**
     * Shows search result information in the Find component using total match count only.
     *
     * <p>This method serves as a primary high-level entry point for presenting search result information. It
     * allows updating the Find component without interacting with low-level view APIs directly.
     *
     * <p>Used when search navigation is not enabled. The Find component displays the total number of matches
     * (e.g. "[ 10 ]"). If {@code totalMatches} is {@code 0}, the component reflects the "not found" state.</p>
     *
     * @param totalMatches the total number of matches found
     */
    @Override
    public void showFindResultInfo(int totalMatches) {
        setMatchesVisible(true);
        setMatchesText("[" + totalMatches + "]");
        setNotFound(totalMatches == 0);
    }

    /**
     * Shows search result information in the Find component including current position.
     *
     * <p>This method serves as a primary high-level entry point for presenting search result information. It
     * encapsulates all low-level view updates required to display positional match information.</p>
     *
     * <p>Used when search navigation is enabled. The Find component displays the current match index and total
     * number of matches (e.g. "[ 1 / 10 ]"). If {@code totalMatches} is {@code 0}, the component reflects the
     * "not found" state.</p>
     *
     * @param currentMatch the currently selected match (1-based index)
     * @param totalMatches the total number of matches found
     */
    @Override
    public void showFindResultInfo(int currentMatch, int totalMatches) {
        setMatchesVisible(true);
        setMatchesText("[" + currentMatch + " / " + totalMatches + "]");
        setNotFound(totalMatches == 0);
    }

    /**
     * Hides search result information in the Find component.
     *
     * <p>This method acts as a high-level API for clearing result presentation state without requiring direct access
     * to low-level view operations.</p>
     */
    @Override
    public void hideFindResultInfo() {
        setMatchesVisible(false);
        setNotFound(false);
    }

    /**
     * Reacts to {@link #findTextProperty()} changing, regardless of whether the change came from the View
     * (user typing) or programmatically. The default implementation shows/hides the clear button and clears
     * any stale result info once the text becomes empty; subclasses may override to layer on extra behavior,
     * calling {@code super.onFindTextChanged(text)} to keep the default reaction.
     *
     * @param text the current find text
     */
    protected void onFindTextChanged(String text) {
        if (text == null || text.isEmpty()) {
            setClearVisible(false);
            hideFindResultInfo();
        } else {
            setClearVisible(true);
        }
    }

    protected void onFindNext() {

    }

    protected void onFindPrevious() {

    }

    protected abstract void onFind();

    protected abstract void onFindCleared();

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var h = getHistory();
        setMatchCaseSelected(h.isMatchCaseSelected());
        setWholeWordSelected(h.isWholeWordSelected());
        setRegExpSelected(h.isRegExpSelected());
        setHighlightSelected(h.isHighlightSelected());
        modifiableFindTexts.setAll(h.getFindTexts());
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        var h = getHistory();
        h.setMatchCaseSelected(isMatchCaseSelected());
        h.setWholeWordSelected(isWholeWordSelected());
        h.setRegExpSelected(isRegExpSelected());
        h.setHighlightSelected(isHighlightSelected());
        h.setFindTexts(new ArrayList<>(getFindTexts()));
    }

    protected void saveFindTextToHistory() {
        HistoryUtils.addFirst(modifiableFindTexts, getFindText());
    }

    @Override
    protected FindBaseHistory getHistory() {
        return (FindBaseHistory) super.getHistory();
    }

    protected ObservableList<String> getModifiableFindTexts() {
        return modifiableFindTexts;
    }

    protected void onClearFindText() {
        setFindText(null);
    }
}
