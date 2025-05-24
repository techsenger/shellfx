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

import com.techsenger.tabshell.core.node.AbstractNodeViewModel;
import com.techsenger.tabshell.core.node.NodeKey;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class CaretViewModel extends AbstractNodeViewModel {

    private static final NodeKey HEX_EDITOR_CARET = new NodeKey("Hex Editor Caret");

    private RowViewModel row;

    private final ReadOnlyIntegerWrapper rowIndex = new ReadOnlyIntegerWrapper(0);

    /**
     * We don't use offset from row because of virtualization.
     */
    private final ReadOnlyIntegerWrapper rowOffset = new ReadOnlyIntegerWrapper(0);

    /**
     * Index in byte row.
     */
    private final ReadOnlyIntegerWrapper byteIndex = new ReadOnlyIntegerWrapper(0);

    /**
     * It is used only for hex panel.
     */
    private final ReadOnlyObjectWrapper<BytePosition> bytePosition = new ReadOnlyObjectWrapper<>(BytePosition.FIRST);

    private final ReadOnlyObjectWrapper<EditorPanel> panel = new ReadOnlyObjectWrapper<>(EditorPanel.HEX);

    private final ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper(true);

    private final ObjectProperty<CaretShape> shape = new SimpleObjectProperty<>(CaretShape.BAR);

    private double x;

    private double indicatorX;

    CaretViewModel() {

    }

    @Override
    public NodeKey getKey() {
        return HEX_EDITOR_CARET;
    }

    public ReadOnlyIntegerProperty rowIndexProperty() {
        return rowIndex.getReadOnlyProperty();
    }

    public int getRowIndex() {
        return rowIndex.get();
    }

    public ReadOnlyIntegerProperty rowOffsetProperty() {
        return rowOffset.getReadOnlyProperty();
    }

    public int getRowOffset() {
        return rowOffset.get();
    }

    public ReadOnlyIntegerProperty byteIndexProperty() {
        return byteIndex.getReadOnlyProperty();
    }

    public int getByteIndex() {
        return byteIndex.get();
    }

    public ReadOnlyObjectProperty<BytePosition> bytePositionProperty() {
        return bytePosition.getReadOnlyProperty();
    }

    public BytePosition getBytePosition() {
        return bytePosition.get();
    }

    public ReadOnlyObjectProperty<EditorPanel> panelProperty() {
        return panel.getReadOnlyProperty();
    }

    public EditorPanel getPanel() {
        return panel.get();
    }

    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled.getReadOnlyProperty();
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public ObjectProperty<CaretShape> shapeProperty() {
        return shape;
    }

    public CaretShape getShape() {
        return shape.get();
    }

    public void setShape(CaretShape shape) {
        this.shape.set(shape);
    }

    void setRowIndex(int index) {
        this.rowIndex.set(index);
    }

    void setRowOffset(int offset) {
        this.rowOffset.set(offset);
    }

    void setByteIndex(int index) {
        this.byteIndex.set(index);
    }

    void setBytePosition(BytePosition position) {
        this.bytePosition.set(position);
    }

    void setPanel(EditorPanel panel) {
        this.panel.set(panel);
    }

    void setDisabled(boolean value) {
        this.disabled.set(value);
    }

    void setRow(RowViewModel row) {
        this.row = row;
    }

    RowViewModel getRow() {
        return row;
    }

    double getX() {
        return x;
    }

    void setX(double x) {
        this.x = x;
    }

    double getIndicatorX() {
        return indicatorX;
    }

    void setIndicatorX(double indicatorX) {
        this.indicatorX = indicatorX;
    }
}
