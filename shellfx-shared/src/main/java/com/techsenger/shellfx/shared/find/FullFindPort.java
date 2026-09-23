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

import com.techsenger.shellfx.material.RequestSetter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * Provides full access to the component's client API.
 *
 * @param <R> the kind of {@link FindResult} this component reports
 * @author Pavel Castornii
 */
public interface FullFindPort<R extends FindResult> extends FindPort<R> {

    void setEditedFindText(String editedFindText);

    @Override
    StringProperty editedFindTextProperty();

    /**
     * Requests {@code findText} to change to the given value — the actual value is decided by the widget backing
     * it, not written directly; observe {@link #findTextProperty()} for what was actually applied.
     *
     * @param findText the requested find text
     */
    @RequestSetter
    void setFindText(String findText);

    void setShowClear(boolean showClear);

    @Override
    BooleanProperty showClearProperty();

    void setShowMatches(boolean showMatches);

    @Override
    BooleanProperty showMatchesProperty();

    void setMatchCaseSelected(boolean matchCaseSelected);

    @Override
    BooleanProperty matchCaseSelectedProperty();

    void setMatchCaseDisabled(boolean matchCaseDisabled);

    @Override
    BooleanProperty matchCaseDisabledProperty();
}
