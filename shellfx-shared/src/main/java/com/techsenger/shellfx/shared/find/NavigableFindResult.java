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

import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * A {@link FindResult} whose matches can be moved between one at a time — a search that jumps to the next/previous
 * occurrence, as opposed to one that only filters a list down to matches. A component that runs its own such
 * search implements this directly on top of whatever result type it already builds, then reports it to
 * {@link AbstractNavigableFindViewModel} through {@code setFindResult(NavigableFindResult)}.
 *
 * @author Pavel Castornii
 */
public interface NavigableFindResult extends FindResult {

    /**
     * Returns the 0-based index of the currently selected match, or {@code -1} if there are none.
     */
    int getCurrentMatch();

    /**
     * The read-only property for the 0-based index of the currently selected match, or {@code -1} if there are none.
     */
    ReadOnlyIntegerProperty currentMatchProperty();

    /**
     * Selects the next match, wrapping around after the last one. Read {@link #getCurrentMatch()} afterward for
     * the newly selected match.
     */
    void nextMatch();

    /**
     * Selects the previous match, wrapping around before the first one. Read {@link #getCurrentMatch()} afterward
     * for the newly selected match.
     */
    void previousMatch();
}
