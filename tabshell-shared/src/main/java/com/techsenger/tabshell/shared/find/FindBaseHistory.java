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
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class FindBaseHistory extends AreaHistory {

    private List<String> findTexts;

    private boolean matchCaseSelected;

    private boolean wholeWordSelected;

    private boolean regExpSelected;

    private boolean highlightSelected;

    public List<String> getFindTexts() {
        return findTexts;
    }

    public void setFindTexts(List<String> findTexts) {
        this.findTexts = findTexts;
    }

    @Override
    public void preSerialize() {
        super.preSerialize();
        HistoryUtils.limit(findTexts);
    }

    public boolean isMatchCaseSelected() {
        return matchCaseSelected;
    }

    public void setMatchCaseSelected(boolean matchCaseSelected) {
        this.matchCaseSelected = matchCaseSelected;
    }

    public boolean isWholeWordSelected() {
        return wholeWordSelected;
    }

    public void setWholeWordSelected(boolean wholeWordSelected) {
        this.wholeWordSelected = wholeWordSelected;
    }

    public boolean isRegExpSelected() {
        return regExpSelected;
    }

    public void setRegExpSelected(boolean regExpSelected) {
        this.regExpSelected = regExpSelected;
    }

    public boolean isHighlightSelected() {
        return highlightSelected;
    }

    public void setHighlightSelected(boolean highlightSelected) {
        this.highlightSelected = highlightSelected;
    }
}
