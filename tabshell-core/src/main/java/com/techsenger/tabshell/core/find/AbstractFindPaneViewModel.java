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

package com.techsenger.tabshell.core.find;

import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
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
public abstract class AbstractFindPaneViewModel extends AbstractPaneViewModel {

    private final StringProperty text = new SimpleStringProperty();

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

    /**
     * This variable indicates if the history is being update. It exists because of bug in ComboBox when item is
     * added to items editor text is changed. see JDK-8333275
     */
    private final BooleanProperty historyUpdated = new SimpleBooleanProperty();

    public AbstractFindPaneViewModel() {

    }

    public StringProperty textProperty() {
        return this.text;
    }

    public String getText() {
        return this.text.get();
    }

    public void setText(String text) {
        this.text.set(text);
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

    public BooleanProperty historyUpdatedProperty() {
        return historyUpdated;
    }

    public boolean isHistoryUpdated() {
        return historyUpdated.get();
    }

    public void setHistoryUpdated(boolean value) {
        this.historyUpdated.set(value);
    }

    protected void addFindText() {
        historyUpdatedProperty().set(true);
        var findText = findTextProperty().get();
        HistoryUtils.addFirst(getFindTexts(), findText);
        findTextProperty().set(findText);
        historyUpdatedProperty().set(false);
    }

    protected abstract void resetMatches();
}
