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

package com.techsenger.tabshell.shared.find;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.history.HistoryUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindBasePresenter<V extends FindBaseView>
        extends AbstractAreaPresenter<V> implements ResultFindPort {

    private final Set<FindFeature> features;

    private String findText;

    private List<String> findTexts = Collections.emptyList();

    private boolean notFound = false;

    private boolean matchCaseSelected;

    private boolean matchCaseDisabled;

    private String matchesText;

    private boolean matchesVisible;

    private boolean clearVisible;

    private boolean findNextDisabled;

    private boolean findPreviousDisabled;

    private boolean wholeWordSelected;

    private boolean wholeWordDisabled;

    private boolean regExpSelected;

    private boolean regExpDisabled;

    private boolean highlightSelected;

    private boolean highlightDisabled;

    public AbstractFindBasePresenter(V view, FindFeature... features) {
        super(view);
        this.features = Set.of(features);
    }

    /**
     * Returns an unmodifiable set of features.
     *
     * @return
     */
    public @Unmodifiable Set<FindFeature> getFeatures() {
        return features;
    }

    public String getFindText() {
        return findText;
    }

    public void setFindText(String findText) {
        this.findText = findText;
        getView().setFindText(findText);
    }

    public List<String> getFindTexts() {
        return findTexts;
    }

    public void setFindTexts(List<String> findTexts) {
        this.findTexts = findTexts;
        getView().setFindTexts(findTexts);
    }

    protected void onClearFindText() {
        getView().setFindText(null);
    }

    public boolean isNotFound() {
        return notFound;
    }

    public void setNotFound(boolean notFound) {
        this.notFound = notFound;
        getView().setNotFound(notFound);
    }

    public boolean isMatchCaseSelected() {
        return matchCaseSelected;
    }

    public void setMatchCaseSelected(boolean matchCaseSelected) {
        this.matchCaseSelected = matchCaseSelected;
        getView().setMatchCaseSelected(matchCaseSelected);
    }

    public boolean isMatchCaseDisabled() {
        return matchCaseDisabled;
    }

    public void setMatchCaseDisabled(boolean matchCaseDisabled) {
        this.matchCaseDisabled = matchCaseDisabled;
        getView().setMatchCaseDisabled(matchCaseDisabled);
    }

    public String getMatchesText() {
        return matchesText;
    }

    public void setMatchesText(String matchesText) {
        this.matchesText = matchesText;
        getView().setMatchesText(matchesText);
    }

    public boolean isMatchesVisible() {
        return matchesVisible;
    }

    public void setMatchesVisible(boolean matchesVisible) {
        this.matchesVisible = matchesVisible;
        getView().setMatchesVisible(matchesVisible);
    }

    protected void onMatchCase(boolean selected) {
        this.matchCaseSelected = selected;
    }

    public boolean isClearVisible() {
        return clearVisible;
    }

    public void setClearVisible(boolean clearVisible) {
        this.clearVisible = clearVisible;
        getView().setClearVisible(clearVisible);
    }

    public boolean isFindNextDisabled() {
        return findNextDisabled;
    }

    public void setFindNextDisabled(boolean findNextDisabled) {
        this.findNextDisabled = findNextDisabled;
        getView().setFindNextDisabled(findNextDisabled);
    }

    public boolean isFindPreviousDisabled() {
        return findPreviousDisabled;
    }

    public void setFindPreviousDisabled(boolean findPreviousDisabled) {
        this.findPreviousDisabled = findPreviousDisabled;
        getView().setFindPreviousDisabled(findPreviousDisabled);
    }

    public boolean isWholeWordSelected() {
        return wholeWordSelected;
    }

    public void setWholeWordSelected(boolean wholeWordSelected) {
        this.wholeWordSelected = wholeWordSelected;
        getView().setWholeWordSelected(wholeWordSelected);
    }

    public boolean isWholeWordDisabled() {
        return wholeWordDisabled;
    }

    public void setWholeWordDisabled(boolean wholeWordDisabled) {
        this.wholeWordDisabled = wholeWordDisabled;
        getView().setWholeWordDisabled(wholeWordDisabled);
    }

    public boolean isRegExpSelected() {
        return regExpSelected;
    }

    public void setRegExpSelected(boolean regExpSelected) {
        this.regExpSelected = regExpSelected;
        getView().setRegExpSelected(regExpSelected);
    }

    public boolean isRegExpDisabled() {
        return regExpDisabled;
    }

    public void setRegExpDisabled(boolean regExpDisabled) {
        this.regExpDisabled = regExpDisabled;
        getView().setRegExpDisabled(regExpDisabled);
    }

    public boolean isHighlightSelected() {
        return highlightSelected;
    }

    public void setHighlightSelected(boolean highlightSelected) {
        this.highlightSelected = highlightSelected;
        getView().setHighlightSelected(highlightSelected);
    }

    public boolean isHighlightDisabled() {
        return highlightDisabled;
    }

    public void setHighlightDisabled(boolean highlightDisabled) {
        this.highlightDisabled = highlightDisabled;
        getView().setHighlightDisabled(highlightDisabled);
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

    protected void onFindNext() {

    }

    protected void onFindPrevious() {

    }

    protected void onWholeWord(boolean selected) {
        this.wholeWordSelected = selected;
    }

    protected void onRegExp(boolean selected) {
        this.regExpSelected = selected;
    }

    protected void onHighlight(boolean selected) {
        this.highlightSelected = selected;
    }

    protected abstract void onFind();

    protected abstract void onFindCleared();

    protected void onFindTextChanged(String text) {
        this.findText = text;
        if (text == null || text.isEmpty()) {
            setClearVisible(false);
            hideFindResultInfo();
        } else {
            setClearVisible(true);
        }
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        for (var f : features) {
            switch (f) {
                case MATCH_CASE -> getView().setupMatchCase();
                case FIND_NEXT -> getView().setupFindNext();
                case FIND_PREVIOUS -> getView().setupFindPrevious();
                case WHOLE_WORD -> getView().setupWholeWord();
                case REG_EXP -> getView().setupRegExp();
                case HIGHLIGHT -> getView().setupHighlight();
                default -> throw new AssertionError();
            }
        }
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        for (var f : features) {
            switch (f) {
                case MATCH_CASE -> setMatchCaseSelected(h.isMatchCaseSelected());
                case WHOLE_WORD -> setWholeWordSelected(h.isWholeWordSelected());
                case REG_EXP -> setRegExpSelected(h.isRegExpSelected());
                case HIGHLIGHT -> setHighlightSelected(h.isHighlightSelected());
            }
        }
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        for (var f : features) {
            switch (f) {
                case MATCH_CASE -> h.setMatchCaseSelected(isMatchCaseSelected());
                case WHOLE_WORD -> h.setWholeWordSelected(isWholeWordSelected());
                case REG_EXP -> h.setRegExpSelected(isRegExpSelected());
                case HIGHLIGHT -> h.setHighlightSelected(isHighlightSelected());
            }
        }
    }

    @Override
    protected void restoreData() {
        super.restoreData();
        var h = getHistory();
        setFindTexts(h.getFindTexts());
    }

    @Override
    protected void saveData() {
        super.saveData();
        var h = getHistory();
        h.setFindTexts(new ArrayList<>(getFindTexts()));
    }

    protected void saveFindTextToHistory() {
        var texts = getFindTexts();
        HistoryUtils.addFirst(texts, getFindText());
        getView().setFindTexts(texts);
    }

    @Override
    protected FindBaseHistory getHistory() {
        return (FindBaseHistory) super.getHistory();
    }
}
