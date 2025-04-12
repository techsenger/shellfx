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

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.dialog.DialogKey;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogViewModel;
import com.techsenger.tabshell.text.TextComponentKeys;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
class GoToLineDialogViewModel extends AbstractSimpleDialogViewModel {

    private final ObjectProperty<Integer> line = new SimpleObjectProperty<>();

    private final ObservableList<Integer> lines = FXCollections.observableArrayList();

    private final ObjectProperty<Integer> column = new SimpleObjectProperty<>();

    private final ObservableList<Integer> columns = FXCollections.observableArrayList();

    GoToLineDialogViewModel(HistoryManager historyManager) {
        super(DialogScope.TAB, false);
        setHistoryPolicy(HistoryPolicy.DATA);
        setHistoryProvider(() -> historyManager.getHistory(GoToLineDialogHistory.class,
                () -> new GoToLineDialogHistory()));
        prefWidthProperty().set(400);
        titleProperty().set("Go To Line");
        okDisableProperty().set(true);
        setCancelVisible(true);
        setButtonWidthEqual(true);
    }

    @Override
    public DialogKey getKey() {
        return TextComponentKeys.GO_TO_LINE_DIALOG;
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
    protected void postHistoryRestore() {
        super.postHistoryRestore();
        //history is always loaded but not always saved
        setHistoryPolicy(HistoryPolicy.NONE);
    }

    /**
     * line property changed only of leaving focus, so we need to check state on text change.
     * @param text
     */
    void checkOkButtonState(String text) {
        if (text.isEmpty()) {
            this.okDisableProperty().set(true);
        } else {
            this.okDisableProperty().set(false);
        }
    }
}
