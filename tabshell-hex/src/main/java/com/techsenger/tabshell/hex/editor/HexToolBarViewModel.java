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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.hex.HexComponentNames;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class HexToolBarViewModel extends AbstractAreaViewModel {

    private final ObservableList<Integer> rowByteCounts =
            FXCollections.observableArrayList(8, 16, 24, 32, 40, 48, 56, 64);

    private final ObjectProperty<Integer> rowByteCount = new SimpleObjectProperty<>(24);

    private final ObservableList<Integer> columnByteCounts = FXCollections.observableArrayList(2, 4, 8);

    private final ObjectProperty<Integer> columnByteCount = new SimpleObjectProperty<>(8);

    private final BooleanProperty columnsEnabled = new SimpleBooleanProperty(true);

    private final ObservableList<NumberBase> offsetNumberBases = FXCollections.observableArrayList(Arrays
            .stream(NumberBase.values()).filter(e -> e != NumberBase.BIN).collect(Collectors.toList()));

    private final ObjectProperty<NumberBase> offsetNumberBase = new SimpleObjectProperty<>(NumberBase.HEX);

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(HexComponentNames.HEX_TOOL_BAR);
    }

    public ObservableList<Integer> getRowByteCounts() {
        return rowByteCounts;
    }

    public ObjectProperty<Integer> rowByteCountProperty() {
        return rowByteCount;
    }

    public Integer getRowByteCount() {
        return this.rowByteCount.get();
    }

    public void setRowByteCount(Integer value) {
        this.rowByteCount.set(value);
    }

    public ObservableList<Integer> getColumnByteCounts() {
        return columnByteCounts;
    }

    public ObjectProperty<Integer> columnByteCountProperty() {
        return  columnByteCount;
    }

    public Integer getColumnByteCount() {
        return this.columnByteCount.get();
    }

    public void setColumnByteCount(Integer value) {
        this.columnByteCount.set(value);
    }

    public BooleanProperty columnsEnabledProperty() {
        return columnsEnabled;
    }

    public boolean areColumnsEnabled() {
        return columnsEnabled.get();
    }

    public void setColumnsEnabled(boolean enabled) {
        this.columnsEnabled.set(enabled);
    }

    public ObservableList<NumberBase> getOffsetNumberBases() {
        return offsetNumberBases;
    }

    public ObjectProperty<NumberBase> offsetNumberBaseProperty() {
        return offsetNumberBase;
    }

    public NumberBase getOffsetNumberBase() {
        return offsetNumberBase.get();
    }

    public void setOffsetNumberBase(NumberBase value) {
        this.offsetNumberBase.set(value);
    }

}
