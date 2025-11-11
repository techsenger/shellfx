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
import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.style.StyleUtils;
import com.techsenger.tabshell.hex.HexComponentNames;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class HexAreaViewModel extends AbstractPaneViewModel {

    private static final Logger logger = LoggerFactory.getLogger(HexAreaViewModel.class);

    private static final int OFFSET_MIN_LENGTH = 8;

    private static final String INVALID_CHAR = "\u2022";

    private final HexToolBarViewModel toolBar;

    private final ObservableSource<CaretPosition> layoutUpdate = new SimpleObservableSource<>();

    private final ReadOnlyIntegerWrapper lastRowByteCount = new ReadOnlyIntegerWrapper();

    private final ObjectProperty<ColumnSeparator> columnSeparator = new SimpleObjectProperty<>(ColumnSeparator.SPACE);

    private final ReadOnlyIntegerWrapper columnCount = new ReadOnlyIntegerWrapper();

    /**
     * Observable list is created only when file content is loaded, because it works faster. The size of this
     * list is equal to total (visible and invisible) row count.
     */
    private final ObservableList<Integer> offsets = FXCollections.observableArrayList();

    private final ReadOnlyIntegerWrapper offsetLength = new ReadOnlyIntegerWrapper();

    private final ReadOnlyBooleanWrapper contentModified = new ReadOnlyBooleanWrapper(false);

    private final ReadOnlyObjectWrapper<Dimension2D> charSize = new ReadOnlyObjectWrapper<>();

    /**
     * Caret uses {@link #charWidth}, so, it is declared after it.
     */
    private final CaretViewModel caret;

    /**
     * Caret position is updated either via this source or via {@link #layoutUpdate}.
     */
    private final ObservableSource<CaretPosition> caretPosition = new SimpleObservableSource<>();

    private final Font monospaceFont;

    private final HexDocument document;

    public HexAreaViewModel(HexToolBarViewModel toolBar, Font monospaceFont, HexDocument document) {
        this.caret = new CaretViewModel(this::getRowByteCount, charSize);
        this.toolBar = toolBar;
        this.monospaceFont = monospaceFont;
        this.document = document;
        this.caret.shapeProperty().addListener((ov, oldV, newV) -> adjustCaretOnShapeChange(oldV, newV));
    }

    public CaretViewModel getCaret() {
        return caret;
    }

    public ReadOnlyBooleanProperty contentModifiedProperty() {
        return contentModified.getReadOnlyProperty();
    }

    public boolean isContentModified() {
        return this.contentModified.get();
    }

    public ReadOnlyIntegerProperty columnCountProperty() {
        return columnCount.getReadOnlyProperty();
    }

    public int getColumnCount() {
        return columnCount.get();
    }

    public ReadOnlyIntegerProperty offsetLengthProperty() {
        return offsetLength.getReadOnlyProperty();
    }

    public int getOffsetLength() {
        return offsetLength.get();
    }

    public ReadOnlyObjectProperty<Dimension2D> charSizeProperty() {
        return charSize.getReadOnlyProperty();
    }

    public Dimension2D getCharSize() {
        return charSize.get();
    }

    public ObjectProperty<ColumnSeparator> columnSeparatorProperty() {
        return columnSeparator;
    }

    public ColumnSeparator getColumnSeparator() {
        return columnSeparator.get();
    }

    public void setColumnSeparator(ColumnSeparator separator) {
        this.columnSeparator.set(separator);
    }

    public ReadOnlyIntegerProperty lastRowByteCountProperty() {
        return lastRowByteCount.getReadOnlyProperty();
    }

    public int getLastRowByteCount() {
        return this.lastRowByteCount.get();
    }

    public int calculateRowIndex(int offset) {
        int rowIndex = offset / this.toolBar.getRowByteCount();
        return rowIndex;
    }

    public int calculateByteIndex(int offset) {
        var byteIndex = offset % this.toolBar.getRowByteCount();
        return byteIndex;
    }

    public void moveCaretTo(EditorPanel panel, int rowIndex, int byteIndex, CaretByteLocation byteLocation) {
        var position = CaretPosition.create(panel, rowIndex, byteIndex, byteLocation, this);
        this.caretPosition.next(position);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(HexComponentNames.HEX_AREA);
    }

    protected int getRowByteCount(int rowIndex) {
        if (rowIndex + 1 == this.offsets.size()) {
            return this.getLastRowByteCount();
        } else {
            return this.toolBar.getRowByteCount();
        }
    }

    void addListeners() {
        this.toolBar.rowByteCountProperty().addListener((ov, oldV, newV) -> updateLayout(true, null));
        this.toolBar.columnsEnabledProperty().addListener((ov, oldV, newV) -> updateLayout(false, null));
        this.toolBar.columnByteCountProperty().addListener((ov, oldV, newV) -> updateLayout(false, null));
        this.columnSeparator.addListener((ov, oldV, newV) -> updateLayout(false, null));
        this.toolBar.offsetNumberBaseProperty().addListener((ov, oldV, newV) -> {
            updateOffsetLength();
            updateLayout(false, null);
        });
    }

    HexToolBarViewModel getToolBar() {
        return toolBar;
    }

    ObservableList<Integer> getOffsets() {
        return this.offsets;
    }

    ObservableSource<CaretPosition> layoutUpdateSource() {
        return layoutUpdate;
    }

    int calculateRowIndex(RowData data) {
        int rowIndex = data.getOffset() / this.toolBar.getRowByteCount();
        return rowIndex;
    }

    ObservableSource<CaretPosition> caretPositionSource() {
        return caretPosition;
    }

    HexDocument getDocument() {
        return document;
    }

    RowData createRowData(Integer offset) {
        if (offset == null) {
            return null;
        }
        var content = getDocument().getContent();
        int realLength = Math.min(getToolBar().getRowByteCount(), content.length - offset);
        var data = new byte[realLength];
        List<String> hexes = new ArrayList<>(data.length);
        List<String> asciis = new ArrayList<>(data.length);
        System.arraycopy(content, offset, data, 0, realLength);
        for (var i = 0; i < data.length; i++) {
            byte b = data[i];
            hexes.add(NumberBaseUtils.convertToHex(b));
            if (b <= 31 || b == 127) {
                asciis.add(INVALID_CHAR);
            } else {
                asciis.add(Character.toString((char) (b & 0xFF)));
            }
        }
        var index = calculateRowIndex(offset);
        return new RowData(offset, index, hexes, asciis);
    }

    void moveCaretUp() {
        var currentPos = this.caret.getPosition();
        var rowIndex = currentPos.getRowIndex();
        rowIndex--;
        if (rowIndex >= 0) {
            moveCaretTo(currentPos.getPanel(), rowIndex, currentPos.getByteIndex(), currentPos.getByteLocation());
        }
    }

    void moveCaretDown() {
        var currentPos = this.caret.getPosition();
        var rowIndex = currentPos.getRowIndex();
        rowIndex++;
        if (rowIndex < this.offsets.size()) {
            moveCaretTo(currentPos.getPanel(), rowIndex, currentPos.getByteIndex(), currentPos.getByteLocation());
        }
    }

    void moveCaretLeft() {
        var currentPos = this.caret.getPosition();
        var atFirstByte = currentPos.getByteIndex() == 0;
        var notAtFirstRow = !(currentPos.getRowIndex() == 0);
        var canChangeRow = ((currentPos.getPanel() == EditorPanel.HEX
                && currentPos.getByteLocation() == CaretByteLocation.FIRST)
                    || currentPos.getPanel() == EditorPanel.ASCII
                && currentPos.getByteLocation() != CaretByteLocation.THIRD);

        if (atFirstByte && notAtFirstRow && canChangeRow) {
            //previous row
            moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex() - 1, this.toolBar.getRowByteCount() - 1,
                    CaretByteLocation.SECOND);
        } else {
            //same row
            if (currentPos.getPanel() == EditorPanel.HEX) {
                if (!(currentPos.getByteIndex() == 0 && currentPos.getByteLocation() == CaretByteLocation.FIRST)) {
                    int byteIndex = currentPos.getByteIndex();
                    var byteLocation = currentPos.getByteLocation();
                    if (currentPos.getByteLocation() == CaretByteLocation.FIRST) {
                        byteIndex = currentPos.getByteIndex() - 1;
                        byteLocation = CaretByteLocation.SECOND;
                    } else if (currentPos.getByteLocation() == CaretByteLocation.SECOND) {
                        byteLocation = CaretByteLocation.FIRST;
                    } else if (currentPos.getByteLocation() == CaretByteLocation.THIRD) {
                        byteLocation = CaretByteLocation.SECOND;
                    }
                    moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), byteIndex, byteLocation);
                }
            } else {
                if (currentPos.getByteIndex() != 0) {
                    int byteIndex = currentPos.getByteIndex();
                    var byteLocation = currentPos.getByteLocation();
                    if (currentPos.getByteLocation() != CaretByteLocation.THIRD) {
                        byteIndex = currentPos.getByteIndex() - 1;
                        byteLocation = CaretByteLocation.FIRST;
                    } else {
                        byteLocation = CaretByteLocation.FIRST;
                    }
                    moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), byteIndex, byteLocation);
                }
            }
        }
    }

    void moveCaretRight() {
        var currentPos = this.caret.getPosition();
        var rowByteCount = getRowByteCount(currentPos.getRowIndex());
        var atLastByte = currentPos.getByteIndex() + 1 >= rowByteCount;
        var atLastRow = currentPos.getRowIndex() == this.offsets.size() - 1;
        var canChangeRow = ((currentPos.getPanel() == EditorPanel.HEX
                && currentPos.getByteLocation() != CaretByteLocation.FIRST)
                || currentPos.getPanel() == EditorPanel.ASCII);

        if (atLastByte && !atLastRow && canChangeRow) {
            //next row
            moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex() + 1, 0, CaretByteLocation.FIRST);
        } else {
            //same row
            int byteIndex = currentPos.getByteIndex();
            var byteLocation = currentPos.getByteLocation();
            if (currentPos.getByteLocation() == CaretByteLocation.THIRD
                    && currentPos.getByteIndex() == rowByteCount - 1) {
                return;
            }
            if (currentPos.getPanel() == EditorPanel.HEX) {
                if (!(currentPos.getByteIndex() + 1 >= rowByteCount
                        && currentPos.getByteLocation() == CaretByteLocation.SECOND)) {
                    if (currentPos.getByteLocation() == CaretByteLocation.FIRST) {
                        byteLocation = CaretByteLocation.SECOND;
                    } else {
                        byteIndex = currentPos.getByteIndex() + 1;
                        byteLocation = CaretByteLocation.FIRST;
                    }
                    moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), byteIndex, byteLocation);
                } else {
                    if (this.caret.getShape() == CaretShape.BAR && atLastRow) {
                        byteLocation = CaretByteLocation.THIRD;
                        moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), byteIndex, byteLocation);
                    }
                }
            } else {
                if (currentPos.getByteIndex() + 1 != rowByteCount) {
                    byteIndex = currentPos.getByteIndex() + 1;
                } else {
                    if (this.caret.getShape() == CaretShape.BAR && atLastRow) {
                        byteLocation = CaretByteLocation.THIRD;
                    }
                }
                moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), byteIndex, byteLocation);
            }
        }
    }

    void moveCaretHome() {
        var currentPos = this.caret.getPosition();
        if (currentPos.getByteIndex() != 0 || (currentPos.getPanel() == EditorPanel.HEX
                && currentPos.getByteLocation() == CaretByteLocation.SECOND)) {
            moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), 0, CaretByteLocation.FIRST);
        }
    }

    void moveCaretEnd() {
        var currentPos = this.caret.getPosition();
        var rowByteCount = getRowByteCount(currentPos.getRowIndex());
        var byteLocation = currentPos.getByteLocation();
        if (this.caret.getShape() == CaretShape.BAR) {
            byteLocation = CaretByteLocation.THIRD;
        } else {
            byteLocation = CaretByteLocation.SECOND;
        }
        var byteIndex = rowByteCount - 1;
        moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), byteIndex, byteLocation);
    }

    void updateOnNewFile() {
        var newPos = CaretPosition.create(EditorPanel.HEX, 0, 0, CaretByteLocation.FIRST, this);
        updateOffsetLength();
        updateLayout(true, newPos);
    }

    private void adjustCaretOnShapeChange(CaretShape oldShape, CaretShape newShape) {
        var currentPos = this.caret.getPosition();
        if (oldShape == CaretShape.BAR && currentPos.getByteLocation() == CaretByteLocation.THIRD) {
            moveCaretTo(currentPos.getPanel(), currentPos.getRowIndex(), currentPos.getByteIndex(),
                    CaretByteLocation.SECOND);
        }
    }

    private void updateLayout(boolean updateOffsets, CaretPosition pos) {
        this.caret.setDisabled(true);
        if (updateOffsets) {
            updateOffsets();
            calculateFixedLayout();
            if (pos == null) {
                var rowIndex = calculateRowIndex(this.caret.getOffset());
                var byteIndex = calculateByteIndex(this.caret.getOffset());
                var curPos = this.caret.getPosition();
                pos = CaretPosition.create(curPos.getPanel(), rowIndex, byteIndex, curPos.getByteLocation(), this);
            }
            this.layoutUpdate.next(pos);
        } else {
            this.layoutUpdate.next(null);
        }
        this.caret.setDisabled(false);
    }

    private void updateOffsets() {
        List<Integer> tempOffsets = new ArrayList<>();
        var content = this.document.getContent();
        for (var offset = 0; offset < content.length; offset += this.toolBar.getRowByteCount()) {
            tempOffsets.add(offset);
        }
        this.offsets.clear();
        this.offsets.addAll(tempOffsets);
        logger.debug("{} Offsets list size: {}", getDescriptor().getLogPrefix(), offsets.size());
    }

    private void updateOffsetLength() {
        if (this.toolBar.getOffsetNumberBase() == NumberBase.HEX) {
            this.offsetLength.set(8);
        } else {
            var length = NumberBaseUtils.calculateOffsetLength(this.document.getContent(),
                    this.toolBar.getOffsetNumberBase());
            length = Math.max(length, OFFSET_MIN_LENGTH);
            this.offsetLength.set(length);
        }
    }

    private void calculateFixedLayout() {
        var chSize = StyleUtils.getMonospaceCharSize(this.monospaceFont);
        this.charSize.set(chSize);
        if (this.toolBar.areColumnsEnabled()) {
            this.columnCount.set(this.toolBar.getRowByteCount() / this.toolBar.getColumnByteCount());
        } else {
            this.columnCount.set(0);
        }
        this.lastRowByteCount.set(calculateLastRowByteCount());
    }

//    /**
//     * This method needs to be adjusted according to the changes in the layout properties.
//     * <p>Calculates the layout so that rows are fully visible at the current window size.
//     */
//    private void calculateFittingLayout() {
//        this.charWidth.set(StyleUtils.getMonospaceCharWidth(getShell().getSettings().getAppearance()
//                .getMonospaceFont()));
//        //4 * 2 bytes, 5 spaces, 4 ascii chars
//        var columnCharCount = (getColumnByteCount() * 2) + (getColumnByteCount() + 1) + getColumnByteCount();
//        //insets for both sides of offset, and half inset between text and vertical scrollbar
//        var insets = 3 * SizeConstants.HALF_INSET;
//        //10 - scrollbar width, 10 - rounding correction
//        var editorWidth = getShell().getWidth() - 10 - 10;
//        int maxCharCount = (int) ((editorWidth - insets) / getCharWidth());
//        double rowWidth = 0;
//        do {
//            //8 - offset, 1 - extra char between hex and ascii
//            this.columnCount.set((maxCharCount - 8 - 1) / columnCharCount);
//            var rowCharCount  = (getColumnCount() * columnCharCount) + 8 + 1;
//            rowWidth = rowCharCount * getCharWidth() + insets + getColumnCount();
//            if (rowWidth > editorWidth) {
//                maxCharCount--;
//            } else {
//                logger.debug("EditorWidth: {}, charWidth: {}, maxCharCount : {}, columnCharCount: {}, "
//                        + "columnCount: {}, rowCharCount: {}, rowWidth: {}",  editorWidth, getCharWidth(),
//                        maxCharCount, columnCharCount, getColumnCount(), rowCharCount, rowWidth);
//
//                break;
//            }
//        } while (true);
//        this.rowByteCount.set(getColumnByteCount() * getColumnCount());
//        this.lastRowByteCount.set(calculateLastRowByteCount());
//    }

    private int calculateLastRowByteCount() {
        var lastRowByteCount = this.document.getContent().length % this.toolBar.getRowByteCount();
        if (lastRowByteCount == 0 && this.document.getContent().length > 0) {
            lastRowByteCount = this.toolBar.getRowByteCount();
        }
        return lastRowByteCount;
    }

    /**
     * Returns true if this row is the last one among all visible and non-visible rows.
     *
     * @return true if this is the last row; false otherwise.
     */
    private boolean isRowLast(int offset) {
        var last = offsets.get(offsets.size() - 1) == offset;
        return last;
    }
}
