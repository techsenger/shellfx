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
 * @author Pavel Castornii
 */
public interface FullFindPanelPort extends FindPanelPort, FullFindBasePort, CloseRequestPort {

    void setWholeWordSelected(boolean wholeWordSelected);

    @Override
    BooleanProperty wholeWordSelectedProperty();

    void setWholeWordDisabled(boolean wholeWordDisabled);

    @Override
    BooleanProperty wholeWordDisabledProperty();

    void setRegExpSelected(boolean regExpSelected);

    @Override
    BooleanProperty regExpSelectedProperty();

    void setRegExpDisabled(boolean regExpDisabled);

    @Override
    BooleanProperty regExpDisabledProperty();

    void setHighlightSelected(boolean highlightSelected);

    @Override
    BooleanProperty highlightSelectedProperty();

    void setHighlightDisabled(boolean highlightDisabled);

    @Override
    BooleanProperty highlightDisabledProperty();
}
