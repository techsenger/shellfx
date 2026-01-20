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

import com.techsenger.tabshell.material.button.ToggleButtonHistory;

/**
 *
 * @author Pavel Castornii
 */
public class FullFindPanelHistory extends FindPanelHistory {

    private ToggleButtonHistory wholeWordButton = new ToggleButtonHistory();

    private ToggleButtonHistory regExpButton = new ToggleButtonHistory();

    private ToggleButtonHistory highlightButton = new ToggleButtonHistory();

    public ToggleButtonHistory getWholeWordButton() {
        return wholeWordButton;
    }

    public ToggleButtonHistory getRegExpButton() {
        return regExpButton;
    }

    public ToggleButtonHistory getHighlightButton() {
        return highlightButton;
    }
}
