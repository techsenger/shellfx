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

import com.techsenger.shellfx.core.close.CloseRequestPort;
import javafx.beans.property.BooleanProperty;

/**
 * Provides full access to the component's client API.
 *
 * @param <R> the kind of {@link NavigableFindResult} this component reports
 * @author Pavel Castornii
 */
public interface FindPanelPort<R extends NavigableFindResult> extends NavigableFindPort<R>, CloseRequestPort {

    boolean isWholeWordSelected();

    void setWholeWordSelected(boolean wholeWordSelected);

    BooleanProperty wholeWordSelectedProperty();

    boolean isWholeWordDisabled();

    void setWholeWordDisabled(boolean wholeWordDisabled);

    BooleanProperty wholeWordDisabledProperty();

    boolean isRegExpSelected();

    void setRegExpSelected(boolean regExpSelected);

    BooleanProperty regExpSelectedProperty();

    boolean isRegExpDisabled();

    void setRegExpDisabled(boolean regExpDisabled);

    BooleanProperty regExpDisabledProperty();

    boolean isHighlightSelected();

    void setHighlightSelected(boolean highlightSelected);

    BooleanProperty highlightSelectedProperty();

    boolean isHighlightDisabled();

    void setHighlightDisabled(boolean highlightDisabled);

    BooleanProperty highlightDisabledProperty();
}
