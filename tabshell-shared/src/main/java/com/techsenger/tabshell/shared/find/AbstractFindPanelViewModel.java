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

import com.techsenger.tabshell.material.button.ToggleButtonViewModel;
import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.core.area.AreaMediator;
import com.techsenger.tabshell.core.history.HistoryUtils;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPanelViewModel<T extends AreaMediator> extends AbstractAreaViewModel<T> {

    private final StringProperty findText = new SimpleStringProperty();

    private final ObservableList<String> findTexts = FXCollections.observableArrayList();

    private final ReadOnlyBooleanWrapper notFound = new ReadOnlyBooleanWrapper();

    private final ToggleButtonViewModel matchCase = new ToggleButtonViewModel();

    private final ReadOnlyStringWrapper matchesText = new ReadOnlyStringWrapper();

    private final ReadOnlyBooleanWrapper matchesVisible = new ReadOnlyBooleanWrapper(false);

    public StringProperty findTextProperty() {
        return this.findText;
    }

    public String getFindText() {
        return this.findText.get();
    }

    public void setFindText(String text) {
        this.findText.set(text);
    }

    public ObservableList<String> getFindTexts() {
        return findTexts;
    }

    public ReadOnlyBooleanProperty notFoundProperty() {
        return notFound.getReadOnlyProperty();
    }

    public boolean isNotFound() {
        return notFound.get();
    }

    public ToggleButtonViewModel getMatchCase() {
        return matchCase;
    }

    public ReadOnlyStringProperty matchesTextProperty() {
        return matchesText.getReadOnlyProperty();
    }

    public String getMatchesText() {
        return matchesText.get();
    }

    public ReadOnlyBooleanProperty matchesVisibleProperty() {
        return matchesVisible.getReadOnlyProperty();
    }

    public boolean isMatchesVisible() {
        return matchesVisible.get();
    }

    protected void setNotFound(boolean value) {
        notFound.set(value);
    }

    protected void setMatchesText(String value) {
        matchesText.set(value);
    }

    protected void setMatchesVisible(boolean value) {
        matchesVisible.set(value);
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        matchCase.setSelected(h.getMatchCaseButton().isSelected());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.getMatchCaseButton().setSelected(matchCase.isSelected());
    }

    @Override
    protected void restoreData() {
        super.restoreData();
        var h = getHistory();
        getFindTexts().addAll(h.getFindTexts());
    }

    @Override
    protected void saveData() {
        super.saveData();
        var h = getHistory();
        h.setFindTexts(new ArrayList<>(getFindTexts()));
    }

    protected void saveFindTextToHistory() {
        HistoryUtils.addFirst(getFindTexts(), getFindText());
    }

    @Override
    protected AbstractFindPanelHistory getHistory() {
        return (AbstractFindPanelHistory) super.getHistory();
    }

    protected abstract void resetMatches();
}
