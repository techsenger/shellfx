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

import com.techsenger.tabshell.shared.find.AbstractFindPaneHistory;
import com.techsenger.tabshell.core.history.HistoryUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class FindPaneHistory extends AbstractFindPaneHistory<DefaultFindPaneViewModel> {

    private List<String> replaceTexts;

    public List<String> getReplaceTexts() {
        return replaceTexts;
    }

    public void setReplaceTexts(List<String> replaceTexts) {
        this.replaceTexts = replaceTexts;
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        this.replaceTexts = new ArrayList<>();
    }

    @Override
    public void preSerialize() {
        super.preSerialize();
        HistoryUtils.limit(replaceTexts);
    }

    @Override
    public void restoreData(DefaultFindPaneViewModel viewModel) {
        super.restoreData(viewModel);
        viewModel.getReplaceTexts().addAll(this.replaceTexts);
    }

    @Override
    public void saveData(DefaultFindPaneViewModel viewModel) {
        super.saveData(viewModel);
        this.replaceTexts = new ArrayList<>(viewModel.getReplaceTexts());
    }
}
