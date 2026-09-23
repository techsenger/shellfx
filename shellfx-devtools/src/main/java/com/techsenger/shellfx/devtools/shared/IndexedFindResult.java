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

package com.techsenger.shellfx.devtools.shared;

import com.techsenger.shellfx.shared.find.NavigableFindResult;
import java.util.function.IntSupplier;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/**
 * A {@link NavigableFindResult} adapter over an existing 0-based, index-into-a-list search already maintained by
 * its owner, so the owner does not have to duplicate its next/previous wrap-around logic a second time just to
 * satisfy this contract.
 *
 * @author Pavel Castornii
 */
public final class IndexedFindResult implements NavigableFindResult {

    private final IntSupplier totalMatches;

    private final IntSupplier currentMatch;

    private final Runnable nextAction;

    private final Runnable previousAction;

    private final ReadOnlyIntegerWrapper currentMatchWrapper = new ReadOnlyIntegerWrapper();

    /**
     * @param totalMatches returns the current number of matches
     * @param currentMatch returns the 0-based index of the currently selected match, or {@code -1} if there are
     *     none
     * @param nextAction moves the owner's selection to the next match, wrapping around after the last one
     * @param previousAction moves the owner's selection to the previous match, wrapping around before the first
     *     one
     */
    public IndexedFindResult(IntSupplier totalMatches, IntSupplier currentMatch, Runnable nextAction,
            Runnable previousAction) {
        this.totalMatches = totalMatches;
        this.currentMatch = currentMatch;
        this.nextAction = nextAction;
        this.previousAction = previousAction;
        this.currentMatchWrapper.set(currentMatch.getAsInt());
    }

    @Override
    public int getTotalMatches() {
        return totalMatches.getAsInt();
    }

    @Override
    public int getCurrentMatch() {
        return currentMatch.getAsInt();
    }

    @Override
    public ReadOnlyIntegerProperty currentMatchProperty() {
        return currentMatchWrapper.getReadOnlyProperty();
    }

    @Override
    public void nextMatch() {
        nextAction.run();
        currentMatchWrapper.set(currentMatch.getAsInt());
    }

    @Override
    public void previousMatch() {
        previousAction.run();
        currentMatchWrapper.set(currentMatch.getAsInt());
    }
}
