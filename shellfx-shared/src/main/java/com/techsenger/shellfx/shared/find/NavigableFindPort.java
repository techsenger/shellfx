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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * Provides minimal, read-only access to the client API of a find component that can move between individual
 * matches, on top of the plain {@link FindPort} contract.
 *
 * @param <R> the kind of {@link NavigableFindResult} this component reports
 * @author Pavel Castornii
 */
public interface NavigableFindPort<R extends NavigableFindResult> extends FindPort<R> {

    MatchesFormat getMatchesFormat();

    ReadOnlyObjectProperty<MatchesFormat> matchesFormatProperty();

    boolean isFindNextDisabled();

    ReadOnlyBooleanProperty findNextDisabledProperty();

    boolean isFindPreviousDisabled();

    ReadOnlyBooleanProperty findPreviousDisabledProperty();
}
