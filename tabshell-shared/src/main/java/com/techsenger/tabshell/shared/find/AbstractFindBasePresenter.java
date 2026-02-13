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

import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.core.history.HistoryUtils;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindBasePresenter<V extends FindBaseView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    private final Set<FindFeature> features;

    public AbstractFindBasePresenter(V view, FindFeature... features) {
        super(view);
        this.features = Set.of(features);
    }

    /**
     * Returns an unmodifiable set of features.
     *
     * @return
     */
    public Set<FindFeature> getFeatures() {
        return features;
    }

    protected void handleClearFindText() {
        getView().setFindText(null);
    }

    protected void handleMatchCase() {

    }

    protected void handleFindNext() {

    }

    protected void handleFindPrevious() {

    }

    protected void handleWholeWord() {

    }

    protected void handleRegExp() {

    }

    protected void handleHighlight() {

    }

    protected abstract void handleFind();

    protected abstract void handleFindCleared();

    protected void handleFindTextEdited(String text) {
        if (text == null || text.isEmpty()) {
            getView().setClearVisible(false);
            hideFindResultInfo();
        } else {
            getView().setClearVisible(true);
        }
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
    protected void showFindResultInfo(int totalMatches) {
        getView().setMatchesVisible(true);
        getView().setMatchesText("[ " + totalMatches + " ]");
        getView().setNotFound(totalMatches == 0);
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
    protected void showFindResultInfo(int currentMatch, int totalMatches) {
        getView().setMatchesVisible(true);
        getView().setMatchesText("[ " + currentMatch + " / " + totalMatches + " ]");
        getView().setNotFound(totalMatches == 0);
    }

    /**
     * Hides search result information in the Find component.
     *
     * <p>This method acts as a high-level API for clearing result presentation state without requiring direct access
     * to low-level view operations.</p>
     */
    protected void hideFindResultInfo() {
        getView().setMatchesVisible(false);
        getView().setNotFound(false);
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
        var v = getView();
        for (var f : features) {
            switch (f) {
                case MATCH_CASE -> v.setMatchCaseSelected(h.isMatchCaseSelected());
                case WHOLE_WORD -> v.setWholeWordSelected(h.isWholeWordSelected());
                case REG_EXP -> v.setRegExpSelected(h.isRegExpSelected());
                case HIGHLIGHT -> v.setHighlightSelected(h.isHighlightSelected());
            }
        }
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        var v = getView();
        for (var f : features) {
            switch (f) {
                case MATCH_CASE -> h.setMatchCaseSelected(v.isMatchCaseSelected());
                case WHOLE_WORD -> h.setWholeWordSelected(v.isWholeWordSelected());
                case REG_EXP -> h.setRegExpSelected(v.isRegExpSelected());
                case HIGHLIGHT -> h.setHighlightSelected(v.isHighlightSelected());
            }
        }
    }

    @Override
    protected void restoreData() {
        super.restoreData();
        var h = getHistory();
        var v = getView();
        v.setFindTexts(h.getFindTexts());
    }

    @Override
    protected void saveData() {
        super.saveData();
        var h = getHistory();
        var v = getView();
        h.setFindTexts(new ArrayList<>(v.getFindTexts()));
    }

    protected void saveFindTextToHistory() {
        var texts = getView().getFindTexts();
        HistoryUtils.addFirst(texts, getView().getFindText());
        getView().setFindTexts(texts);
    }

    @Override
    protected FindBaseHistory getHistory() {
        return (FindBaseHistory) super.getHistory();
    }
}
