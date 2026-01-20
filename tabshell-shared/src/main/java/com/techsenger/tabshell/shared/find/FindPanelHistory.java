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

import com.techsenger.tabshell.core.area.AreaHistory;
import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.material.button.ToggleButtonHistory;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelHistory extends AreaHistory {

    private List<String> findTexts;

    private ToggleButtonHistory matchCaseButton = new ToggleButtonHistory();

    public List<String> getFindTexts() {
        return findTexts;
    }

    public void setFindTexts(List<String> findTexts) {
        this.findTexts = findTexts;
    }

    public ToggleButtonHistory getMatchCaseButton() {
        return matchCaseButton;
    }

    @Override
    public void preSerialize() {
        super.preSerialize();
        HistoryUtils.limit(findTexts);
    }
}
