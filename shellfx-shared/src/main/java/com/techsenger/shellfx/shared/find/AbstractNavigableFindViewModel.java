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

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.area.AreaParams;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A {@link AbstractFindViewModel} whose matches can be moved between one at a time — adds match-position display
 * format and next/previous navigation on top of the plain find contract.
 *
 * @param <C> the composer type
 * @param <R> the kind of {@link NavigableFindResult} this component reports
 * @author Pavel Castornii
 */
public abstract class AbstractNavigableFindViewModel<C extends ChildComposer, R extends NavigableFindResult>
        extends AbstractFindViewModel<C, R> implements FullNavigableFindPort<R> {

    private final ObjectProperty<MatchesFormat> matchesFormat =
            new SimpleObjectProperty<>(MatchesFormat.CURRENT_AND_TOTAL);

    private final ReadOnlyBooleanWrapper findNextDisabled = new ReadOnlyBooleanWrapper(true);

    private final ReadOnlyBooleanWrapper findPreviousDisabled = new ReadOnlyBooleanWrapper(true);

    public AbstractNavigableFindViewModel(AreaParams params, FindTrigger findTrigger) {
        super(params, findTrigger);
    }

    @Override
    public MatchesFormat getMatchesFormat() {
        return matchesFormat.get();
    }

    @Override
    public void setMatchesFormat(MatchesFormat matchesFormat) {
        this.matchesFormat.set(matchesFormat);
    }

    @Override
    public ObjectProperty<MatchesFormat> matchesFormatProperty() {
        return matchesFormat;
    }

    @Override
    public boolean isFindNextDisabled() {
        return findNextDisabled.get();
    }

    @Override
    public ReadOnlyBooleanProperty findNextDisabledProperty() {
        return findNextDisabled.getReadOnlyProperty();
    }

    @Override
    public boolean isFindPreviousDisabled() {
        return findPreviousDisabled.get();
    }

    @Override
    public ReadOnlyBooleanProperty findPreviousDisabledProperty() {
        return findPreviousDisabled.getReadOnlyProperty();
    }

    @Override
    protected void onFindNext() {
        applyNextMatch();
    }

    @Override
    protected void onFindPrevious() {
        applyPreviousMatch();
    }

    protected void setFindNextDisabled(boolean findNextDisabled) {
        this.findNextDisabled.set(findNextDisabled);
    }

    protected void setFindPreviousDisabled(boolean findPreviousDisabled) {
        this.findPreviousDisabled.set(findPreviousDisabled);
    }

    /**
     * Shows search result information including current position.
     *
     * <p>The Find component displays the current match index and total number of matches (e.g. "[ 1 / 10 ]"). If
     * {@code totalMatches} is {@code 0}, the component reflects the "not found" state.</p>
     *
     * @param currentMatch the currently selected match (1-based index)
     * @param totalMatches the total number of matches found
     */
    protected void showFindResultInfo(int currentMatch, int totalMatches) {
        setMatchesVisible(isShowMatches());
        if (isShowMatches()) {
            setMatchesText("[" + currentMatch + " / " + totalMatches + "]");
        }
        setNotFound(totalMatches == 0);
    }

    @Override
    protected void hideFindResultInfo() {
        super.hideFindResultInfo();
        setFindNextDisabled(true);
        setFindPreviousDisabled(true);
    }

    /**
     * Selects the next match in the current {@link #getFindResult()} and reports the updated position; does
     * nothing if there is no current result.
     */
    protected void applyNextMatch() {
        var result = getFindResult();
        if (result != null) {
            result.nextMatch();
            reportResultInfo();
        }
    }

    /**
     * Selects the previous match in the current {@link #getFindResult()} and reports the updated position; does
     * nothing if there is no current result.
     */
    protected void applyPreviousMatch() {
        var result = getFindResult();
        if (result != null) {
            result.previousMatch();
            reportResultInfo();
        }
    }

    /**
     * Reports the current {@code findResult}, converting its 0-based {@code currentMatch} (or {@code -1} for none)
     * into the 1-based match number {@code showFindResultInfo} displays, before delegating to whichever overload
     * {@code matchesFormat} calls for, and updates whether next/previous navigation is disabled.
     */
    @Override
    protected void reportResultInfo() {
        var result = getFindResult();
        if (result == null) {
            return;
        }
        setFindNextDisabled(result.getTotalMatches() == 0);
        setFindPreviousDisabled(result.getTotalMatches() == 0);
        if (matchesFormat.get() == MatchesFormat.CURRENT_AND_TOTAL) {
            showFindResultInfo(result.getCurrentMatch() + 1, result.getTotalMatches());
        } else {
            showFindResultInfo(result.getTotalMatches());
        }
    }
}
