/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.shared.find.AbstractFindPanelHistory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelHistory extends AbstractFindPanelHistory {

    private List<String> replaceTexts = new ArrayList<>();

    public List<String> getReplaceTexts() {
        return replaceTexts;
    }

    public void setReplaceTexts(List<String> replaceTexts) {
        this.replaceTexts = replaceTexts;
    }

    @Override
    public void preSerialize() {
        super.preSerialize();
        HistoryUtils.limit(replaceTexts);
    }
}
