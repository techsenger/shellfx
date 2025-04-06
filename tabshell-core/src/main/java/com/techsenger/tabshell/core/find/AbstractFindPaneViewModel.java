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

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.history.HistoryUtils;
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

    public StringProperty findTextProperty() {
        return this.findText;
    }

    public BooleanProperty notFoundProperty() {
        return this.notFound;
    }

    public BooleanProperty caseSelectedProperty() {
        return caseSelected;
    }

    public BooleanProperty wholeWordSelectedProperty() {
        return wholeWordSelected;
    }

    public BooleanProperty wholeWordDisableProperty() {
        return wholeWordDisable;
    }

    public BooleanProperty regExpSelectedProperty() {
        return regExpSelected;
    }

    public BooleanProperty regExpDisableProperty() {
        return regExpDisable;
    }

    public BooleanProperty highlightSelectedProperty() {
        return highlightSelected;
    }

    public StringProperty resultTextProperty() {
        return resultText;
    }

    public BooleanProperty resultTextVisibleProperty() {
        return resultTextVisible;
    }

    public ObjectProperty<Runnable> closeActionProperty() {
        return closeAction;
    }

    public ObservableList<String> getFindTexts() {
        return findTexts;
    }

    public BooleanProperty historyUpdatedProperty() {
        return historyUpdated;
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
