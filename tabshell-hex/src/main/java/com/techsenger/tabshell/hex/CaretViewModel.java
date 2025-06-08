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
import com.techsenger.tabshell.hex.row.BodyRowViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public final class CaretViewModel extends AbstractNodeViewModel {

    private static final NodeKey HEX_EDITOR_CARET = new NodeKey("Hex Editor Caret");

    private final ReadOnlyObjectWrapper<CaretPosition> position = new ReadOnlyObjectWrapper<>();

    /**
     * In the vast majority of cases, a property for the offset is specifically required, which is why this property
     * was added, even though it partially duplicates the position.
     */
    private final ReadOnlyIntegerWrapper offset = new ReadOnlyIntegerWrapper();

    /**
     * This property is visible only within this class.
     */
    private final ObjectProperty<EditorPanel> panel = new SimpleObjectProperty<>();

    private final ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper(false);

    private final ReadOnlyDoubleWrapper x = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper indicatorX = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper indicatorWidth = new ReadOnlyDoubleWrapper();

    private final ObjectProperty<CaretShape> shape = new SimpleObjectProperty<>(CaretShape.BAR);

    private final AbstractHexEditorTabViewModel editor;

    private BodyRowViewModel row;

    CaretViewModel(AbstractHexEditorTabViewModel editor) {
        this.editor = editor;
        shape.addListener((ov, oldV, newV) -> updateWidhts(newV, editor.getCharWidth()));
        panel.addListener((ov, oldV, newV) -> updateWidhts(getShape(), editor.getCharWidth()));
        editor.charWidthProperty().addListener((ov, oldV, newV) -> updateWidhts(getShape(), newV.doubleValue()));
    }

    @Override
    public NodeKey getKey() {
        return HEX_EDITOR_CARET;
    }

    public ReadOnlyObjectWrapper<CaretPosition> positionProperty() {
        return position;
    }

    public CaretPosition getPosition() {
        return position.get();
    }

    public ReadOnlyIntegerWrapper offsetProperty() {
        return offset;
    }

    public int getOffset() {
        return offset.get();
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

    void setPosition(CaretPosition position) {
        this.position.set(position);
        this.offset.set(position.getOffset());
        this.panel.set(position.getPanel());
    }

    void setDisabled(boolean value) {
        this.disabled.set(value);
    }

    //todo: not public
    public BodyRowViewModel getRow() {
        return row;
    }

    void setRow(BodyRowViewModel row) {
        this.row = row;
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

    private void updateWidhts(CaretShape shape, double charWidth) {
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
        if (this.panel.get() == EditorPanel.HEX) {
            this.indicatorWidth.set(charWidth);
        } else {
            this.indicatorWidth.set(charWidth * 2);
        }
    }
}
