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

import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.core.area.AreaMediator;
import com.techsenger.tabshell.core.history.HistoryUtils;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    private final BooleanProperty notFound = new SimpleBooleanProperty();

    private final BooleanProperty caseSelected = new SimpleBooleanProperty(false);

    private final BooleanProperty wholeWordSelected = new SimpleBooleanProperty(false);

    private final BooleanProperty wholeWordDisable = new SimpleBooleanProperty(false);

    private final BooleanProperty regExpSelected = new SimpleBooleanProperty(false);

    private final BooleanProperty regExpDisable = new SimpleBooleanProperty(false);

    private final BooleanProperty highlightSelected = new SimpleBooleanProperty(false);

    private final ObjectProperty<Runnable> closeAction = new SimpleObjectProperty<>();

    private final StringProperty resultText = new SimpleStringProperty();

    private final BooleanProperty resultTextVisible = new SimpleBooleanProperty(false);

    public AbstractFindPanelViewModel() {

    }

    public StringProperty findTextProperty() {
        return this.findText;
    }

    public String getFindText() {
        return this.findText.get();
    }

    public void setFindText(String text) {
        this.findText.set(text);
    }

    public BooleanProperty notFoundProperty() {
        return this.notFound;
    }

    public boolean isNotFound() {
        return this.notFound.get();
    }

    public void setNotFound(boolean value) {
        this.notFound.set(value);
    }

    public BooleanProperty caseSelectedProperty() {
        return caseSelected;
    }

    public boolean isCaseSelected() {
        return caseSelected.get();
    }

    public void setCaseSelected(boolean value) {
        this.caseSelected.set(value);
    }

    public BooleanProperty wholeWordSelectedProperty() {
        return wholeWordSelected;
    }

    public boolean isWholeWordSelected() {
        return wholeWordSelected.get();
    }

    public void setWholeWordSelected(boolean value) {
        this.wholeWordSelected.set(value);
    }

    public BooleanProperty wholeWordDisableProperty() {
        return wholeWordDisable;
    }

    public boolean isWholeWordDisable() {
        return wholeWordDisable.get();
    }

    public void setWholeWordDisable(boolean value) {
        this.wholeWordDisable.set(value);
    }

    public BooleanProperty regExpSelectedProperty() {
        return regExpSelected;
    }

    public boolean isRegExpSelected() {
        return regExpSelected.get();
    }

    public void setRegExpSelected(boolean value) {
        this.regExpSelected.set(value);
    }

    public BooleanProperty regExpDisableProperty() {
        return regExpDisable;
    }

    public boolean isRegExpDisable() {
        return regExpDisable.get();
    }

    public void setRegExpDisable(boolean value) {
        this.regExpDisable.set(value);
    }

    public BooleanProperty highlightSelectedProperty() {
        return highlightSelected;
    }

    public boolean isHighlightSelected() {
        return highlightSelected.get();
    }

    public void setHighlightSelected(boolean value) {
        this.highlightSelected.set(value);
    }

    public StringProperty resultTextProperty() {
        return resultText;
    }

    public String getResultText() {
        return resultText.get();
    }

    public void setResultText(String text) {
        this.resultText.set(text);
    }

    public BooleanProperty resultTextVisibleProperty() {
        return resultTextVisible;
    }

    public boolean isResultTextVisible() {
        return resultTextVisible.get();
    }

    public void setResultTextVisible(boolean value) {
        this.resultTextVisible.set(value);
    }

    public ObjectProperty<Runnable> closeActionProperty() {
        return closeAction;
    }

    public Runnable getCloseAction() {
        return closeAction.get();
    }

    public void setCloseAction(Runnable value) {
        this.closeAction.set(value);
    }

    public ObservableList<String> getFindTexts() {
        return findTexts;
    }

    @Override
    protected AbstractFindPanelHistory getHistory() {
        return (AbstractFindPanelHistory) super.getHistory();
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        caseSelectedProperty().set(h.getCaseButton().isSelected());
        wholeWordSelectedProperty().set(h.getWholeWordButton().isSelected());
        regExpSelectedProperty().set(h.getRegExpButton().isSelected());
        highlightSelectedProperty().set(h.getHighlightButton().isSelected());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.getCaseButton().setSelected(caseSelectedProperty().get());
        h.getWholeWordButton().setSelected(wholeWordSelectedProperty().get());
        h.getRegExpButton().setSelected(regExpSelectedProperty().get());
        h.getHighlightButton().setSelected(highlightSelectedProperty().get());
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

    protected void addFindText() {
        var findText = findTextProperty().get();
        HistoryUtils.addFirst(getFindTexts(), findText);
        findTextProperty().set(findText);
    }

    protected abstract void resetMatches();
}
