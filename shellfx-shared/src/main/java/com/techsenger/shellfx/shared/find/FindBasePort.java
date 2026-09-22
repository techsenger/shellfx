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
import com.techsenger.shellfx.core.area.AreaPort;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;

/**
 * Provides minimal, read-only access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FindBasePort extends AreaPort {

    String getFindText();

    ReadOnlyStringProperty findTextProperty();

    @Unmodifiable ObservableList<String> getFindTexts();

    boolean isNotFound();

    ReadOnlyBooleanProperty notFoundProperty();

    boolean isMatchCaseSelected();

    ReadOnlyBooleanProperty matchCaseSelectedProperty();

    boolean isMatchCaseDisabled();

    ReadOnlyBooleanProperty matchCaseDisabledProperty();

    String getMatchesText();

    ReadOnlyStringProperty matchesTextProperty();

    boolean isMatchesVisible();

    ReadOnlyBooleanProperty matchesVisibleProperty();

    boolean isClearVisible();

    ReadOnlyBooleanProperty clearVisibleProperty();

    boolean isFindNextDisabled();

    ReadOnlyBooleanProperty findNextDisabledProperty();

    boolean isFindPreviousDisabled();

    ReadOnlyBooleanProperty findPreviousDisabledProperty();
}
