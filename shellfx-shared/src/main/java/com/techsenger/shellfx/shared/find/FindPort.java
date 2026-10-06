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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the component's client API.
 *
 * @param <R> the kind of {@link FindResult} this component reports
 * @author Pavel Castornii
 */
public interface FindPort<R extends FindResult> extends AreaPort, ResultFindPort<R> {

    /**
     * Returns the text currently displayed in the find field, including whatever the user is still typing.
     */
    String getEditedFindText();

    void setEditedFindText(String editedFindText);

    StringProperty editedFindTextProperty();

    @Unmodifiable ObservableList<String> getFindTexts();

    /**
     * Whether the component is allowed to show {@code clearVisible} at all. Set per component depending on
     * whether it has room to display a clear button.
     */
    boolean isShowClear();

    void setShowClear(boolean showClear);

    BooleanProperty showClearProperty();

    boolean isClearVisible();

    ReadOnlyBooleanProperty clearVisibleProperty();

    /**
     * Whether the component is allowed to show {@code matchesText}/{@code matchesVisible} at all. Set per
     * component depending on whether it has room to display a match count.
     */
    boolean isShowMatches();

    void setShowMatches(boolean showMatches);

    BooleanProperty showMatchesProperty();

    String getMatchesText();

    ReadOnlyStringProperty matchesTextProperty();

    boolean isMatchesVisible();

    ReadOnlyBooleanProperty matchesVisibleProperty();

    boolean isNotFound();

    ReadOnlyBooleanProperty notFoundProperty();

    boolean isMatchCaseSelected();

    void setMatchCaseSelected(boolean matchCaseSelected);

    BooleanProperty matchCaseSelectedProperty();

    boolean isMatchCaseDisabled();

    void setMatchCaseDisabled(boolean matchCaseDisabled);

    BooleanProperty matchCaseDisabledProperty();
}
