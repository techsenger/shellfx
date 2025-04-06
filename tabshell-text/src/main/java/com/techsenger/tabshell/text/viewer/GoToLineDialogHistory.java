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
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogHistory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class GoToLineDialogHistory extends AbstractSimpleDialogHistory<GoToLineDialogViewModel> {

    private List<Integer> lines;

    private List<Integer> columns;

    public List<Integer> getLines() {
        return lines;
    }

    public void setLines(List<Integer> lines) {
        this.lines = lines;
    }

    public List<Integer> getColumns() {
        return columns;
    }

    public void setColumns(List<Integer> columns) {
        this.columns = columns;
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        lines = new ArrayList<>();
        columns = new ArrayList<>();
    }

    @Override
    public void preSerialize() {
        super.preSerialize();
        HistoryUtils.limit(lines);
        HistoryUtils.limit(columns);
    }

    @Override
    public void restoreData(GoToLineDialogViewModel viewModel) {
        super.restoreData(viewModel);
        viewModel.getLines().addAll(this.lines);
        viewModel.getColumns().addAll(this.columns);
    }

    @Override
    public void saveData(GoToLineDialogViewModel viewModel) {
        super.saveData(viewModel);
        var l = viewModel.lineProperty().get();
        if (l != null) {
            HistoryUtils.addFirst(this.lines, l);
        }
        var c = viewModel.columnProperty().get();
        if (c != null) {
            HistoryUtils.addFirst(this.columns, l);
        }
    }
}
