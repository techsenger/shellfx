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

import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.style.StyleUtils;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.dialogs.file.FileOpenerViewModel;
import com.techsenger.tabshell.dialogs.file.FileSaverViewModel;
import com.techsenger.tabshell.hex.data.DataInspectorViewModel;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.tabs.tabmanager.TabManagerViewModel;
import com.techsenger.tabshell.tabs.workertab.AbstractWorkerTabViewModel;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractHexEditorTabViewModel extends AbstractWorkerTabViewModel
        implements FileOpenerViewModel, FileSaverViewModel {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHexEditorTabViewModel.class);

    private static final String INVALID_CHAR = "\u2022";

    private final HexFormat hexFormat = HexFormat.of().withUpperCase();

    private final ObservableSource<Integer> layoutUpdateRequest = new SimpleObservableSource<>();

    private final HexDocument document;

    private final ObservableList<Integer> rowByteCounts =
            FXCollections.observableArrayList(8, 16, 24, 32, 48, 64, 96, 128, 256, 512, 1024);

    private final ObjectProperty<Integer> rowByteCount = new SimpleObjectProperty<>(24);

    private final BooleanProperty columnsEnabled = new SimpleBooleanProperty(true);

    private final ObservableList<Integer> columnByteCounts = FXCollections.observableArrayList(2, 4, 8);

    private final ObjectProperty<Integer> columnByteCount = new SimpleObjectProperty<>(8);

    private final ObjectProperty<ColumnSeparator> columnSeparator = new SimpleObjectProperty<>(ColumnSeparator.SPACE);

    private final ReadOnlyIntegerWrapper columnCount = new ReadOnlyIntegerWrapper();

    /**
     * Observable list is created only when file content is loaded, because it works faster.
     */
    private final ObservableList<Integer> offsets = FXCollections.observableArrayList();

    private final ReadOnlyBooleanWrapper contentModified = new ReadOnlyBooleanWrapper(false);

    private final ReadOnlyIntegerWrapper lastRowByteCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyDoubleWrapper charWidth = new ReadOnlyDoubleWrapper();

    /**
     * Caret uses {@link #charWidth}, so, it is declared after it.
     */
    private final CaretViewModel caret = new CaretViewModel(this);

    private final ObservableSource<Integer> moveRequest = new SimpleObservableSource<>();

    private final TabManagerViewModel rightTabManager = new TabManagerViewModel(HexComponentKeys.RIGHT_TAB_MANAGER);

    private final DataInspectorViewModel dataInspector;

    public AbstractHexEditorTabViewModel(ShellViewModel tabShell, GenericFile file) {
        super(tabShell);
        this.document = new HexDocument(file);
        setIcon(HexIcons.EDITOR);
        setTitle("Hex Editor");
        this.caret.shapeProperty().addListener((ov, oldV, newV) -> adjustCaretOnShapeChange(oldV, newV));
        this.dataInspector = createDataInspector();
    }

    @Override
    public AbstractHexEditorTabHelper<?> getComponentHelper() {
        return (AbstractHexEditorTabHelper) super.getComponentHelper();
    }

    @Override
    public List<ExtensionFilter> createOpenExtensionFilters() {
        return null;
    }

    @Override
    public List<ExtensionFilter> createSaveExtensionFilters() {
        return null;
    }

    public HexDocument getDocument() {
        return document;
    }

    @Override
    public void readFile() {
        this.caret.setDisabled(true);
        if (this.document.readFile()) {
            resetCaret();
            updateOffsets();
            updateLayout();
        }
    }

    @Override
    public void writeFile() {
        this.document.writeFile();
    }

    @Override
    public GenericFile getFile() {
        return this.document.getFile();
    }

    @Override
    public void setFile(GenericFile file) {
        this.document.setFile(file);
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

    public ReadOnlyDoubleProperty charWidthProperty() {
        return charWidth.getReadOnlyProperty();
    }

    public double getCharWidth() {
        return charWidth.get();
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

    public BooleanProperty columnsEnabledProperty() {
        return columnsEnabled;
    }

    public boolean areColumnsEnabled() {
        return columnsEnabled.get();
    }

    public void setColumnsEnabled(boolean enabled) {
        this.columnsEnabled.set(enabled);
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

    public DataInspectorViewModel getDataInspector() {
        return dataInspector;
    }

    public TabManagerViewModel getRightTabManager() {
        return rightTabManager;
    }

    protected DataInspectorViewModel createDataInspector() {
        return new DataInspectorViewModel(this.document, this.caret.offsetProperty());
    }

    @Override
    protected void postHistoryRestore() {
        super.postHistoryRestore();
        this.rowByteCount.addListener((ov, oldV, newV) -> {
            updateOffsets();
            updateLayout();
        });
        this.columnsEnabled.addListener((ov, oldV, newV) -> updateLayout());
        this.columnByteCount.addListener((ov, oldV, newV) -> updateLayout());
        this.columnSeparator.addListener((ov, oldV, newV) -> updateLayout());
    }

    ObservableList<Integer> getOffsets() {
        return this.offsets;
    }

    ObservableSource<Integer> layoutUpdateRequestSource() {
        return layoutUpdateRequest;
    }

    int calculateRowIndex(int offset) {
        int rowIndex = offset / getRowByteCount();
        return rowIndex;
    }

    int calculateByteIndex(int offset) {
        var byteIndex = offset % getRowByteCount();
        return byteIndex;
    }

    int calculateRowIndex(RowViewModel row) {
        int rowIndex = row.getModel().getOffset() / getRowByteCount();
        return rowIndex;
    }

    RowViewModel createRow(Integer offset) {
        var model = createRowModel(offset);
        return new RowViewModel(this, model);
    }

    RowModel createRowModel(Integer offset) {
        if (offset == null) {
            return null;
        }
        var content = this.document.getContent();
        int realLength = Math.min(getRowByteCount(), content.length - offset);
        var data = new byte[realLength];
        List<String> hexes = new ArrayList<>(data.length);
        List<String> asciis = new ArrayList<>(data.length);
        System.arraycopy(content, offset, data, 0, realLength);
        for (var i = 0; i < data.length; i++) {
            byte b = data[i];

            hexes.add(hexFormat.toHexDigits(b));
            if (b <= 31 || b == 127) {
                asciis.add(INVALID_CHAR);
            } else {
                asciis.add(Character.toString((char) (b & 0xFF)));
            }
        }
        return new RowModel(offset, hexFormat.toHexDigits(offset), hexes, asciis);
    }

    ObservableSource<Integer> moveRequestSource() {
        return moveRequest;
    }

    void moveCaretUp() {
        var rowIndex = calculateRowIndex(caret.getRowOffset());
        rowIndex--;
        if (rowIndex >= 0) {
            this.moveRequest.next(rowIndex);
        }
    }

    void moveCaretDown() {
        var rowIndex = calculateRowIndex(caret.getRowOffset());
        rowIndex++;
        if (rowIndex < this.offsets.size()) {
            adjustCaretDownForLastRow(rowIndex);
            this.moveRequest.next(rowIndex);
        }
    }

    void moveCaretLeft() {
        var atFirstByte = caret.getByteIndex() == 0;
        var notAtFirstRow = !caret.getRow().isFirst();
        var canChangeRow = ((caret.getPanel() == EditorPanel.HEX && caret.getBytePosition() == CaretBytePosition.FIRST)
                    || caret.getPanel() == EditorPanel.ASCII && caret.getBytePosition() != CaretBytePosition.THIRD);

        if (atFirstByte && notAtFirstRow && canChangeRow) {
            //previous row
            caret.setByteIndex(getRowByteCount() - 1);
            caret.setBytePosition(CaretBytePosition.SECOND);
            this.moveRequest.next(caret.getRowIndex() - 1);
        } else {
            //same row
            if (caret.getPanel() == EditorPanel.HEX) {
                if (!(caret.getByteIndex() == 0 && caret.getBytePosition() == CaretBytePosition.FIRST)) {
                    if (caret.getBytePosition() == CaretBytePosition.FIRST) {
                        caret.setByteIndex(caret.getByteIndex() - 1);
                        caret.setBytePosition(CaretBytePosition.SECOND);
                    } else if (caret.getBytePosition() == CaretBytePosition.SECOND) {
                        caret.setBytePosition(CaretBytePosition.FIRST);
                    } else if (caret.getBytePosition() == CaretBytePosition.THIRD) {
                        caret.setBytePosition(CaretBytePosition.SECOND);
                    }
                    this.moveRequest.next(null);
                }
            } else {
                if (caret.getByteIndex() != 0) {
                    if (caret.getBytePosition() != CaretBytePosition.THIRD) {
                        caret.setByteIndex(caret.getByteIndex() - 1);
                        caret.setBytePosition(CaretBytePosition.FIRST);
                        this.moveRequest.next(null);
                    } else {
                        caret.setBytePosition(CaretBytePosition.FIRST);
                        this.moveRequest.next(null);
                    }
                }
            }
        }
    }

    void moveCaretRight() {
        var atLastByte = caret.getByteIndex() + 1 >= caret.getRow().getModel().getByteCount();
        var notAtLastRow = !caret.getRow().isLast();
        var canChangeRow = ((caret.getPanel() == EditorPanel.HEX && caret.getBytePosition() != CaretBytePosition.FIRST)
                    || caret.getPanel() == EditorPanel.ASCII);

        if (atLastByte && notAtLastRow && canChangeRow) {
            //next row
            caret.setByteIndex(0);
            caret.setBytePosition(CaretBytePosition.FIRST);
            this.moveRequest.next(caret.getRowIndex() + 1);
        } else {
            //same row
            if (caret.getBytePosition() == CaretBytePosition.THIRD
                    && caret.getByteIndex() == caret.getRow().getModel().getByteCount() - 1) {
                return;
            }
            if (caret.getPanel() == EditorPanel.HEX) {
                if (!(caret.getByteIndex() + 1 >= this.caret.getRow().getModel().getByteCount()
                        && caret.getBytePosition() == CaretBytePosition.SECOND)) {
                    if (caret.getBytePosition() == CaretBytePosition.FIRST) {
                        caret.setBytePosition(CaretBytePosition.SECOND);
                    } else {
                        caret.setByteIndex(caret.getByteIndex() + 1);
                        caret.setBytePosition(CaretBytePosition.FIRST);
    //                    var bytePair = this.caret.getRow().getByteTextPairs().get(caret.getByteIndex());
    //                    if (!bytePair.isEmpty()) {
    //                        caret.setCaretBytePosition(CaretBytePosition.FIRST);
    //                    }
                    }
                    this.moveRequest.next(null);
                } else {
                    if (this.caret.getShape() == CaretShape.BAR && this.caret.getRow().isLast()) {
                        this.caret.setBytePosition(CaretBytePosition.THIRD);
                        this.moveRequest.next(null);
                    }
                }
            } else {
                if (caret.getByteIndex() + 1 != this.caret.getRow().getModel().getByteCount()) {
                    caret.setByteIndex(caret.getByteIndex() + 1);
                    this.moveRequest.next(null);
                } else {
                    if (this.caret.getShape() == CaretShape.BAR && this.caret.getRow().isLast()) {
                        this.caret.setBytePosition(CaretBytePosition.THIRD);
                        this.moveRequest.next(null);
                    }
                }
            }
        }
    }

    void moveCaretHome() {
        if (caret.getByteIndex() != 0
                || (caret.getPanel() == EditorPanel.HEX && caret.getBytePosition() == CaretBytePosition.SECOND)) {
            caret.setByteIndex(0);
            caret.setBytePosition(CaretBytePosition.FIRST);
            this.moveRequest.next(null);
        }
    }

    void moveCaretEnd() {
        caret.setByteIndex(caret.getRow().getModel().getByteCount() - 1);
        if (this.caret.getShape() == CaretShape.BAR) {
            caret.setBytePosition(CaretBytePosition.THIRD);
        } else {
            caret.setBytePosition(CaretBytePosition.SECOND);
        }
        this.moveRequest.next(null);
    }

    void adjustCaretDownForLastRow(int newRowIndex) {
        if (newRowIndex == this.offsets.size() - 1) {
            if (this.caret.getByteIndex() >= getLastRowByteCount()) {
                this.caret.setByteIndex(getLastRowByteCount() - 1);
                if (this.caret.getShape() == CaretShape.BAR) {
                    this.caret.setBytePosition(CaretBytePosition.THIRD);
                } else {
                    this.caret.setBytePosition(CaretBytePosition.SECOND);
                }
            }
        }
    }

    private void adjustCaretOnShapeChange(CaretShape oldShape, CaretShape newShape) {
        if (oldShape == CaretShape.BAR && this.caret.getBytePosition() == CaretBytePosition.THIRD) {
            this.caret.setBytePosition(CaretBytePosition.SECOND);
            this.moveRequest.next(null);
        }
    }

    private void resetCaret() {
        this.caret.setPanel(EditorPanel.HEX);
        this.caret.setByteIndex(0);
        this.caret.setBytePosition(CaretBytePosition.FIRST);
    }

    private void updateLayout() {
        calculateFixedLayout();
        this.caret.setDisabled(true);
        var rowIndex = calculateRowIndex(this.caret.getOffset());
        var byteIndex = calculateByteIndex(this.caret.getOffset());
        this.caret.setByteIndex(byteIndex);
        this.layoutUpdateRequest.next(rowIndex);
        this.caret.setDisabled(false);
        this.dataInspector.updateTypeItems();
    }

    private void updateOffsets() {
        List<Integer> tempOffsets = new ArrayList<>();
        var content = this.document.getContent();
        for (var offset = 0; offset < content.length; offset += getRowByteCount()) {
            tempOffsets.add(offset);
        }
        this.offsets.clear();
        this.offsets.addAll(tempOffsets);
        logger.debug("Offsets list size: {}", offsets.size());
    }

    private void calculateFixedLayout() {
        this.charWidth.set(StyleUtils.getMonospaceCharWidth(getShell().getSettings().getAppearance()
                .getMonospaceFont()));
        if (areColumnsEnabled()) {
            this.columnCount.set(getRowByteCount() / getColumnByteCount());
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
        var lastRowByteCount = this.document.getContent().length % getRowByteCount();
        if (lastRowByteCount == 0 && this.document.getContent().length > 0) {
            lastRowByteCount = getRowByteCount();
        }
        return lastRowByteCount;
    }
}
