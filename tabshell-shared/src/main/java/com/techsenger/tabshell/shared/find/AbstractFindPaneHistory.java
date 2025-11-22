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

package com.techsenger.tabshell.shared.find;

import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.core.area.AbstractAreaHistory;
import com.techsenger.tabshell.material.button.ToggleButtonHistory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPaneHistory<T extends AbstractFindPaneViewModel> extends AbstractAreaHistory<T> {

    private List<String> findTexts;

    private ToggleButtonHistory caseButton = new ToggleButtonHistory();

    private ToggleButtonHistory wholeWordButton = new ToggleButtonHistory();

    private ToggleButtonHistory regExpButton = new ToggleButtonHistory();

    private ToggleButtonHistory highlightButton = new ToggleButtonHistory();

    public List<String> getFindTexts() {
        return findTexts;
    }

    public ToggleButtonHistory getCaseButton() {
        return caseButton;
    }

    public ToggleButtonHistory getWholeWordButton() {
        return wholeWordButton;
    }

    public ToggleButtonHistory getRegExpButton() {
        return regExpButton;
    }

    public ToggleButtonHistory getHighlightButton() {
        return highlightButton;
    }

    @Override
    public void restoreAppearance(T viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.caseSelectedProperty().set(caseButton.isSelected());
        viewModel.wholeWordSelectedProperty().set(wholeWordButton.isSelected());
        viewModel.regExpSelectedProperty().set(regExpButton.isSelected());
        viewModel.highlightSelectedProperty().set(highlightButton.isSelected());
    }

    @Override
    public void saveAppearance(T viewModel) {
        super.saveAppearance(viewModel);
        caseButton.setSelected(viewModel.caseSelectedProperty().get());
        wholeWordButton.setSelected(viewModel.wholeWordSelectedProperty().get());
        regExpButton.setSelected(viewModel.regExpSelectedProperty().get());
        highlightButton.setSelected(viewModel.highlightSelectedProperty().get());
    }

    @Override
    public void restoreData(T viewModel) {
        super.restoreData(viewModel);
        viewModel.getFindTexts().addAll(this.findTexts);
    }

    @Override
    public void saveData(T viewModel) {
        super.saveData(viewModel);
        this.findTexts = new ArrayList<>(viewModel.getFindTexts());
    }

    @Override
    public void preSerialize() {
        super.preSerialize();
        HistoryUtils.limit(findTexts);
    }
}
