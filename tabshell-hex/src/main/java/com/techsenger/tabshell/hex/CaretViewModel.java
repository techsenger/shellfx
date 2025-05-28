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

package com.techsenger.tabshell.hex;

import com.techsenger.tabshell.core.node.AbstractNodeViewModel;
import com.techsenger.tabshell.core.node.NodeKey;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public final class CaretViewModel extends AbstractNodeViewModel {

    private static final NodeKey HEX_EDITOR_CARET = new NodeKey("Hex Editor Caret");

    private final ReadOnlyIntegerWrapper rowIndex = new ReadOnlyIntegerWrapper(0);

    /**
     * We don't use offset from row because of virtualization.
     */
    private final ReadOnlyIntegerWrapper rowOffset = new ReadOnlyIntegerWrapper(0);

    /**
     * Index in byte row.
     */
    private final ReadOnlyIntegerWrapper byteIndex = new ReadOnlyIntegerWrapper(0);

    private final ReadOnlyObjectWrapper<CaretBytePosition> bytePosition =
            new ReadOnlyObjectWrapper<>(CaretBytePosition.FIRST);

    private final ReadOnlyObjectWrapper<EditorPanel> panel = new ReadOnlyObjectWrapper<>(EditorPanel.HEX);

    private final ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper(true);

    private final ObjectProperty<CaretShape> shape = new SimpleObjectProperty<>(CaretShape.BAR);

    private final ReadOnlyDoubleWrapper x = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper indicatorX = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper indicatorWidth = new ReadOnlyDoubleWrapper();

    private final AbstractHexEditorTabViewModel editor;

    private final ReadOnlyIntegerWrapper offset = new ReadOnlyIntegerWrapper(0);

    private RowViewModel row;

    CaretViewModel(AbstractHexEditorTabViewModel editor) {
        this.editor = editor;
        shape.addListener((ov, oldV, newV) -> updateWidhts(newV, getPanel(), editor.getCharWidth()));
        panel.addListener((ov, oldV, newV) -> updateWidhts(getShape(), newV, editor.getCharWidth()));
        editor.charWidthProperty().addListener((ov, oldV, newV) -> updateWidhts(getShape(), getPanel(),
                newV.doubleValue()));

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

    public ReadOnlyObjectProperty<CaretBytePosition> bytePositionProperty() {
        return bytePosition.getReadOnlyProperty();
    }

    public CaretBytePosition getBytePosition() {
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

    public double getX() {
        return x.doubleValue();
    }

    public ReadOnlyDoubleProperty xProperty() {
        return x.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty indicatorXProperty() {
        return indicatorX.getReadOnlyProperty();
    }

    public double getIndicatorX() {
        return indicatorX.get();
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return width.getReadOnlyProperty();
    }

    public double getWidth() {
        return width.get();
    }

    public ReadOnlyDoubleProperty indicatorWidthProperty() {
        return indicatorWidth.getReadOnlyProperty();
    }

    public double getIndicatorWidth() {
        return indicatorWidth.get();
    }

    public ReadOnlyIntegerProperty offsetProperty() {
        return offset;
    }

    public int getOffset() {
        return offset.get();
    }

    void setByteIndex(int index) {
        this.byteIndex.set(index);
    }

    void setBytePosition(CaretBytePosition position) {
        this.bytePosition.set(position);
    }

    void setPanel(EditorPanel panel) {
        this.panel.set(panel);
    }

    void setDisabled(boolean value) {
        this.disabled.set(value);
    }

    RowViewModel getRow() {
        return row;
    }

    void setRow(RowViewModel row) {
        this.row = row;
        setRowOffset(row.getModel().getOffset());
        setRowIndex(this.editor.calculateRowIndex(row));
        this.offset.set(getRowOffset() + getByteIndex());
    }

    ReadOnlyDoubleWrapper xWrapper() {
        return this.x;
    }

    ReadOnlyDoubleWrapper indicatorXWrapper() {
        return this.x;
    }

    void setX(double x) {
        this.x.set(x);
    }

    void setIndicatorX(double indicatorX) {
        this.indicatorX.set(indicatorX);
    }

    private void setRowIndex(int index) {
        this.rowIndex.set(index);
    }

    private void setRowOffset(int offset) {
        this.rowOffset.set(offset);
    }

    private void updateWidhts(CaretShape shape, EditorPanel panel, double charWidth) {
        switch (shape) {
            case BAR:
                this.width.set(1);
                break;
            case BLOCK:
                this.width.set(charWidth);
                break;
            case UNDERSCORE:
                this.width.set(charWidth);
                break;
            default:
                throw new AssertionError();
        }
        if (panel == EditorPanel.HEX) {
            this.indicatorWidth.set(charWidth);
        } else {
            this.indicatorWidth.set(charWidth * 2);
        }
    }
}
