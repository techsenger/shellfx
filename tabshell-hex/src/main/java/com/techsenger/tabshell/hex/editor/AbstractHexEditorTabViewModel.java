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

import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleUtils;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.dialogs.file.FileOpenerViewModel;
import com.techsenger.tabshell.dialogs.file.FileSaverViewModel;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.tabs.workertab.AbstractWorkerTabViewModel;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
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

    static final int COLUMN_BYTE_COUNT = 4;

    private static final String INVALID_CHAR = "\u2022";

    private final HexFormat hexFormat = HexFormat.of().withUpperCase();

    private final ObservableSource<Boolean> contentLoaded = new SimpleObservableSource<>();

    private final HexDocument document;

    /**
     * Observable list is created only when file content is loaded, because it works faster.
     */
    private ObservableList<Integer> offsets = FXCollections.observableArrayList();

    private ReadOnlyBooleanWrapper contentModified = new ReadOnlyBooleanWrapper(false);

    private ReadOnlyIntegerWrapper columnCount = new ReadOnlyIntegerWrapper();

    private ReadOnlyIntegerWrapper rowByteCount = new ReadOnlyIntegerWrapper();

    private ReadOnlyIntegerWrapper lastRowByteCount = new ReadOnlyIntegerWrapper();

    private ReadOnlyDoubleWrapper charWidth = new ReadOnlyDoubleWrapper();

    /**
     * Caret uses {@link #charWidth}, so, it is declared after it.
     */
    private final CaretViewModel caret = new CaretViewModel(this);

    public AbstractHexEditorTabViewModel(ShellViewModel tabShell, GenericFile file) {
        super(tabShell);
        this.document = new HexDocument(file);
        setIcon(HexIcons.EDITOR);
        setTitle("Hex Editor");
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
            calculateLayout();
            this.offsets = createOffsets();
            this.caret.setPanel(EditorPanel.HEX);
            this.caret.setRowOffset(0);
            this.caret.setRowIndex(0);
            this.caret.setByteIndex(0);
            this.caret.setBytePosition(BytePosition.FIRST);
            //when file is opened the position of the caret is calculated by char width as there can be no bytes
            this.caret.setX(getCharWidth());
            this.caret.setIndicatorX(getCharWidth());
            this.contentLoaded.next(true);
            this.caret.setDisabled(false);
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

    public ReadOnlyIntegerProperty rowByteCountProperty() {
        return rowByteCount.getReadOnlyProperty();
    }

    public int getRowByteCount() {
        return this.rowByteCount.get();
    }

    public ReadOnlyIntegerProperty lastRowByteCountProperty() {
        return lastRowByteCount.getReadOnlyProperty();
    }

    public int getLastRowByteCount() {
        return this.lastRowByteCount.get();
    }

    ObservableList<Integer> getOffsets() {
        return this.offsets;
    }

    ObservableSource<Boolean> contentLoadedSource() {
        return contentLoaded;
    }

    int calculateRowIndex(int offset) {
        int rowIndex = offset / (getRowByteCount());
        return rowIndex;
    }

    int calculateRowIndex(RowViewModel row) {
        int rowIndex = row.getModel().getOffset() / (getRowByteCount());
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

    private ObservableList<Integer> createOffsets() {
        List<Integer> tempOffsets = new ArrayList<>();
        var content = this.document.getContent();
        for (var offset = 0; offset < content.length; offset += getRowByteCount()) {
            tempOffsets.add(offset);
        }
        this.offsets.addAll(tempOffsets);
        logger.debug("Offsets list size: {}", offsets.size());
        return offsets;
    }

    private void calculateLayout() {
        this.charWidth.set(StyleUtils.getMonospaceCharWidth(getShell().getSettings().getAppearance()
                .getMonospaceFont()));
        //4 * 2 bytes, 5 spaces, 4 ascii chars
        var columnCharCount = (COLUMN_BYTE_COUNT * 2) + (COLUMN_BYTE_COUNT + 1) + COLUMN_BYTE_COUNT;
        //insets for both sides of offset, and half inset between text and vertical scrollbar
        var insets = 3 * SizeConstants.HALF_INSET;
        //10 - scrollbar width, 10 - rounding correction
        var editorWidth = getShell().getWidth() - 10 - 10;
        int maxCharCount = (int) ((editorWidth - insets) / getCharWidth());
        double rowWidth = 0;
        do {
            //8 - offset, 1 - extra char between hex and ascii
            this.columnCount.set((maxCharCount - 8 - 1) / columnCharCount);
            var rowCharCount  = (getColumnCount() * columnCharCount) + 8 + 1;
            rowWidth = rowCharCount * getCharWidth() + insets + getColumnCount();
            if (rowWidth > editorWidth) {
                maxCharCount--;
            } else {
                logger.debug("EditorWidth: {}, charWidth: {}, maxCharCount : {}, columnCharCount: {}, columnCount: {},"
                        + " rowCharCount: {}, rowWidth: {}",  editorWidth, getCharWidth(), maxCharCount,
                        columnCharCount, getColumnCount(), rowCharCount, rowWidth);

                break;
            }
        } while (true);
        this.rowByteCount.set(COLUMN_BYTE_COUNT * getColumnCount());
        var lastRowBCount = this.document.getContent().length % getRowByteCount();
        if (lastRowBCount == 0 && this.document.getContent().length > 0) {
            lastRowBCount = getRowByteCount();
        }
        this.lastRowByteCount.set(lastRowBCount);
    }
}
