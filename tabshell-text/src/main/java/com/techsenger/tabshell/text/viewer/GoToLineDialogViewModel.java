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

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.DialogMediator;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.history.HistoryUtils;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogViewModel;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class GoToLineDialogViewModel extends AbstractSimpleDialogViewModel<DialogMediator> {

    private final ObjectProperty<Integer> line = new SimpleObjectProperty<>();

    private final ObservableList<Integer> lines = FXCollections.observableArrayList();

    private final ObjectProperty<Integer> column = new SimpleObjectProperty<>();

    private final ObservableList<Integer> columns = FXCollections.observableArrayList();

    GoToLineDialogViewModel(HistoryManager historyManager) {
        super(DialogScope.TAB, false);
        setHistoryPolicy(HistoryPolicy.DATA);
        setHistoryProvider(() -> historyManager.getOrCreateHistory(GoToLineDialogHistory.class,
                GoToLineDialogHistory::new));
        prefWidthProperty().set(400);
        titleProperty().set("Go To Line");
        getOk().setDisable(true);
        setButtonWidthEqual(true);
    }

    public ObjectProperty<Integer> lineProperty() {
        return line;
    }

    public Integer getLine() {
        return line.get();
    }

    public void setLine(Integer value) {
        this.line.set(value);
    }

    public ObjectProperty<Integer> columnProperty() {
        return column;
    }

    public Integer getColumn() {
        return column.get();
    }

    public void setColumn(Integer value) {
        this.column.set(value);
    }

    public ObservableList<Integer> getLines() {
        return lines;
    }

    public ObservableList<Integer> getColumns() {
        return columns;
    }

    @Override
    public CloseCheckResult canClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void initialize() {
        super.initialize();
        //history is always loaded but not always saved
        setHistoryPolicy(HistoryPolicy.NONE);
    }

    @Override
    protected GoToLineDialogHistory getHistory() {
        return (GoToLineDialogHistory) super.getHistory();
    }

    @Override
    protected void restoreData() {
        super.restoreData();
        var h = getHistory();
        getLines().addAll(h.getLines());
        getColumns().addAll(h.getColumns());
    }

    @Override
    protected void saveData() {
        super.saveData();
        var h = getHistory();
        var l = lineProperty().get();
        if (l != null) {
            HistoryUtils.addFirst(h.getLines(), l);
        }
        var c = columnProperty().get();
        if (c != null) {
            HistoryUtils.addFirst(h.getColumns(), l);
        }
    }

    /**
     * line property changed only of leaving focus, so we need to check state on text change.
     * @param text
     */
    void checkOkButtonState(String text) {
        getOk().setDisable(text.isEmpty());
    }
}
