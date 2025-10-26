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

package com.techsenger.tabshell.demos.full;

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.ShellTabKey;
import com.techsenger.tabshell.hex.AbstractHexEditorTabViewModel;
import com.techsenger.tabshell.hex.CaretShape;
import com.techsenger.tabshell.hex.ColumnSeparator;
import com.techsenger.tabshell.storage.GenericFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabViewModel extends AbstractHexEditorTabViewModel {

    private final ObservableList<CaretShape> caretShapes = FXCollections.observableArrayList(CaretShape.values());

    private final ObservableList<ColumnSeparator> columnSeparators =
            FXCollections.observableArrayList(ColumnSeparator.values());

    public HexEditorTabViewModel(ShellViewModel tabShell, GenericFile file) {
        super(tabShell, file);
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> tabShell.getHistoryManager().getOrCreateHistory(HexEditorTabHistory.class,
                HexEditorTabHistory::new));
    }

    @Override
    public ShellTabKey getKey() {
        return DemoComponentKeys.HEX_EDITOR;
    }

    public ObservableList<CaretShape> getCaretShapes() {
        return caretShapes;
    }

    public ObservableList<ColumnSeparator> getColumnSeparators() {
        return columnSeparators;
    }
}
