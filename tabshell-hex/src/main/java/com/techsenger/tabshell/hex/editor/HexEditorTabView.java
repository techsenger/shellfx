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

import com.techsenger.mvvm4fx.core.ComponentHelper;
import com.techsenger.mvvm4fx.core.PulseListenerTiming;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.tabs.workertab.AbstractWorkerTabView;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.PAGE_DOWN;
import static javafx.scene.input.KeyCode.PAGE_UP;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabView extends AbstractWorkerTabView<HexEditorTabViewModel> {

    /**
     * Contains information for pageUp and pageDown navigation.
     */
    private record PageNavigation(int firstRowIndex, double rowHeiht, int caretVisibleRowIndex, int scrollRowCount) { }

    private final Button newButton = new Button(null, new FontIconView(CoreIcons.ADD));

    private final Button clearButton = new Button(null, new FontIconView(CoreIcons.CLEAR));

    private final Button cutButton = new Button(null, new FontIconView(CoreIcons.CUT));

    private final Button copyButton = new Button(null, new FontIconView(CoreIcons.COPY));

    private final Button pasteButton = new Button(null, new FontIconView(CoreIcons.PASTE));

    private final Button undoButton = new Button(null, new FontIconView(CoreIcons.UNDO));

    private final Button redoButton = new Button(null, new FontIconView(CoreIcons.REDO));

    private final Button findButton = new Button(null, new FontIconView(CoreIcons.FIND));

    private final Button replaceButton = new Button(null, new FontIconView(CoreIcons.REPLACE));

    private final ToolBar toolBar = new ToolBar(
            newButton, clearButton, new Separator(Orientation.VERTICAL),  cutButton, copyButton, pasteButton,
            new Separator(Orientation.VERTICAL),  undoButton, redoButton, new Separator(Orientation.VERTICAL),
            findButton, replaceButton);

    /**
     * Integer is row index.
     */
    private VirtualFlow<Integer, RowView> virtualFlow;

    private VirtualizedScrollPane<VirtualFlow<Integer, RowView>> virtualScrollPane;

    private final CaretView caret;

    public HexEditorTabView(ShellView<?> tabShell, HexEditorTabViewModel viewModel) {
        super(tabShell, viewModel);
        this.caret = new CaretView(viewModel.getCaret());
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void doOnSelected() {
        super.doOnSelected();
        NodeUtils.requestFocus(virtualFlow);
    }

    public CaretView getCaret() {
        return caret;
    }

    @Override
    protected void build(HexEditorTabViewModel viewModel) {
        super.build(viewModel);
        newButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        newButton.setTooltip(new Tooltip("New"));
        clearButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        clearButton.setTooltip(new Tooltip("Clear"));
        cutButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        cutButton.setTooltip(new Tooltip("Cut"));
        copyButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        copyButton.setTooltip(new Tooltip("Copy"));
        pasteButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pasteButton.setTooltip(new Tooltip("Paste"));
        undoButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        undoButton.setTooltip(new Tooltip("Undo"));
        redoButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        redoButton.setTooltip(new Tooltip("Redo"));
        findButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        findButton.setTooltip(new Tooltip("Find"));
        replaceButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        replaceButton.setTooltip(new Tooltip("Replace"));

        var css = HexEditorTabView.class.getResource("hexeditor.css").toExternalForm();
        getTopPane().getStylesheets().add(css);
        getTopPane().getChildren().add(toolBar);
    }

    @Override
    protected void addListeners(HexEditorTabViewModel viewModel) {
        super.addListeners(viewModel);
        viewModel.contentLoadedSource().addListener((newV) -> {
            this.caret.getViewModel().setDisabled(true);
            virtualFlow = VirtualFlow.createVertical(viewModel.getOffsets(),
                    offset -> {
                        var rowViewModel = viewModel.createRow(offset);
                        var rowView = new RowView(rowViewModel, this);
                        rowView.initialize();
                        return rowView;
                    });
            virtualScrollPane = new VirtualizedScrollPane<>(virtualFlow);
            this.virtualScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            VBox.setVgrow(virtualScrollPane, Priority.ALWAYS);
            getTopPane().getChildren().add(virtualScrollPane);
            virtualFlow.setOnMousePressed(e -> virtualFlow.requestFocus());
            virtualFlow.addEventFilter(KeyEvent.KEY_PRESSED, e -> handleKeyPressed(e));
            addLayoutPulseListener(PulseListenerTiming.AFTER, () -> {
                var row = this.virtualFlow.getCell(0);
                moveCaretTo(EditorPanel.HEX, row, 0, 0, BytePart.HIGH);
                NodeUtils.requestFocus(virtualFlow);
                this.caret.getViewModel().setDisabled(false);
                return false;
            });
        });
        viewModel.getCaret().byteIndexProperty().addListener((ov, oldV, newV) -> {
            updateIndicatorX();
        });
    }

    @Override
    protected void postInitialize(HexEditorTabViewModel viewModel) {
        super.postInitialize(viewModel);
        this.caret.initialize();
        viewModel.readFile();
    }

    @Override
    protected void preDeinitialize(HexEditorTabViewModel viewModel) {
        super.preDeinitialize(viewModel);
        this.caret.deinitialize();
    }

    @Override
    protected ComponentHelper<?> createComponentHelper() {
        return new HexEditorTabHelper(this);
    }

    RowView scrollUpTo(int rowIndex) {
        var row = virtualFlow.getCellIfVisible(rowIndex).orElse(null);
        if (row == null) {
            virtualFlow.showAsFirst(rowIndex);
            row = virtualFlow.getCell(rowIndex);
        } else {
            //it can be visible but not entirely
            if (virtualFlow.getFirstVisibleIndex() == rowIndex) {
                virtualFlow.showAsFirst(rowIndex);
            }
        }
        return row;
    }

    RowView scrollDownTo(int rowIndex) {
        var row = virtualFlow.getCellIfVisible(rowIndex).orElse(null);
        if (row == null) {
            virtualFlow.showAsLast(rowIndex);
            row = virtualFlow.getCell(rowIndex);
        } else {
            //it can be visible but not entirely
            if (virtualFlow.getLastVisibleIndex() == rowIndex) {
                virtualFlow.showAsLast(rowIndex);
            }
        }
        return row;
    }

    void moveCaretTo(EditorPanel panel, RowView row, int rowIndex, int byteIndex, BytePart part) {
        var caretVewModel = getViewModel().getCaret();
        updateCaretRow(row);
        caretVewModel.setPanel(panel);
        caretVewModel.setRowOffset(row.getViewModel().getModel().getOffset());
        caretVewModel.setRowIndex(rowIndex);
        caretVewModel.setByteIndex(byteIndex);
        caretVewModel.setBytePart(part);
        updateCaretX();
        completeCaretMove();
    }

    private void handleKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case UP: moveCaretUp(); break;
            case DOWN: moveCaretDown(); break;
            case LEFT: moveCaretLeft(); break;
            case RIGHT: moveCaretRight(); break;
            case PAGE_UP: moveCaretPageUp(); break;
            case PAGE_DOWN: moveCaretPageDown(); break;
            case HOME: moveCaretHome(); break;
            case END: moveCaretEnd(); break;
        }
        e.consume();
    }

    private void moveCaretTo(RowView row, int rowIndex) {
        var caretVewModel = getViewModel().getCaret();
        updateCaretRow(row);
        caretVewModel.setRowOffset(row.getViewModel().getModel().getOffset());
        caretVewModel.setRowIndex(rowIndex);
        completeCaretMove();
    }

    private PageNavigation createPageNavigation() {
        var viewModel = getViewModel();
        var charSize = viewModel.getShell().getSettings().getAppearance().getMonospaceFont().getSize();

        //resolving which row is fully visible and which is not
        var firstRow = this.virtualFlow.visibleCells().get(0);
        var firstRowIndex = viewModel.calculateRowIndex(firstRow.getViewModel());
        var charTopPadding = (firstRow.getNode().getHeight() - charSize) / 2;
        double firstRowFlowOffset = Math.max(0, -firstRow.getNode().getBoundsInParent().getMinY());
        boolean firstRowFullyVisible = charTopPadding >= firstRowFlowOffset;

        var visibleRowTotalHeight = this.virtualFlow.visibleCells().size() * firstRow.getNode().getHeight();
        visibleRowTotalHeight -= firstRowFlowOffset;
        boolean lastRowFullyVisible = charTopPadding >= (visibleRowTotalHeight - this.virtualFlow.getHeight());

        //calculating caret row index diff
        var caretRowIndex = this.caret.getViewModel().getRowIndex();
        //the index of the visible row owning the caret
        var caretVisibleRowIndex = caretRowIndex - firstRowIndex;

        int scrollRowCount;
        if (firstRowFullyVisible) {
            if (lastRowFullyVisible) {
                scrollRowCount = this.virtualFlow.visibleCells().size();
            } else {
                scrollRowCount = this.virtualFlow.visibleCells().size() - 1;
            }
        } else {
            if (lastRowFullyVisible) {
                scrollRowCount = this.virtualFlow.visibleCells().size() - 1;
            } else {
                if (this.virtualFlow.visibleCells().size() >= 2) {
                    scrollRowCount = this.virtualFlow.visibleCells().size() - 2;
                } else {
                    scrollRowCount = this.virtualFlow.visibleCells().size() - 1;
                }
            }
        }

        return new PageNavigation(firstRowIndex, firstRow.getNode().getHeight(), caretVisibleRowIndex, scrollRowCount);
    }

    private void moveCaretOnPagination(int calculatedNewFirstRowIndex, int caretVisibleRowIndex, int endCaretRowIndex) {
        var viewModel = getViewModel();
        RowView newCaretRow;
        int newCaretRowIndex;

        //we don't know how many rows were actually scrolled
        var realNewFirstRow = this.virtualFlow.visibleCells().get(0);
        var realNewFirstRowIndex = viewModel.calculateRowIndex(realNewFirstRow.getViewModel());
        //if fewer rows were scrolled than requested, the caret is placed on the last row.
        if (realNewFirstRowIndex == calculatedNewFirstRowIndex) {
            newCaretRow = this.virtualFlow.visibleCells().get(caretVisibleRowIndex);
            newCaretRowIndex = viewModel.calculateRowIndex(newCaretRow.getViewModel());
        } else {
            newCaretRow = this.virtualFlow.visibleCells().get(endCaretRowIndex);
            newCaretRowIndex = viewModel.calculateRowIndex(newCaretRow.getViewModel());
        }
        moveCaretTo(newCaretRow, newCaretRowIndex);
    }

    /**
     * Determines the first visible row after performing a PageUp scroll in a virtualized scroll flow. The
     * algorithm is based on two key principles:
     *
     * 1. After scrolling, virtual rows should appear in the same position within the viewport as they did before
     * scrolling. This ensures visual continuity.
     *
     * 2. No content should be skipped — all rows must be shown fully to the user.
     *
     * Based on the visibility of the first and last visible rows before scrolling, the first visible row after
     * scrolling is determined as follows:
     *
    * 1. If the first row is fully visible and the last row is fully visible, then the row immediately preceding the
    * first row becomes the last visible row after scrolling.
    *
    * 2. If the first row is fully visible and the last row is partially visible, then the first visible row
    * becomes the last visible row after scrolling.
    *
    * 3. If the first row is partially visible and the last row is fully visible, then the first visible
    * row becomes the last visible row after scrolling.
    *
    * 4. If the first row is partially visible and the last row is partially visible, then the second visible
    * row becomes the last visible row after scrolling.
     */
    private void moveCaretPageUp() {
        var viewModel = getViewModel();
        if (viewModel.getOffsets().isEmpty()) {
            return;
        }
        var pageNavigation = createPageNavigation();
        int calculatedNewFirstRowIndex = pageNavigation.firstRowIndex() - pageNavigation.scrollRowCount();
        this.virtualFlow.scrollYBy(pageNavigation.scrollRowCount * pageNavigation.rowHeiht() * -1);
        Platform.runLater(() -> {
            moveCaretOnPagination(calculatedNewFirstRowIndex, pageNavigation.caretVisibleRowIndex, 0);
        });
    }

    /**
     * Determines the first visible row after performing a PageDown scroll in a virtualized scroll flow. The
     * algorithm is based on two key principles:
     *
     * 1. After scrolling, virtual rows should appear in the same position within the viewport as it did before
     * scrolling. This ensures visual continuity.
     *
     * 2. No content should be skipped — all rows must be shown fully to the user.
     *
     * Based on the visibility of the first and last visible rows before scrolling, the first visible row after
     * scrolling is determined as follows:
     *
     * 1. If the first row is fully visible and the last row is fully visible, then the row immediately following the
     * last row becomes the new first visible row.
     *
     * 2. If the first row is fully visible and the last row is partially visible, then the last visible row
     * becomes the new first visible row.
     *
     * 3. If the first row is partially visible and the last row is fully visible, then the last visible
     * row becomes the new first visible row after scrolling.
     *
     * 4. If the first row is partially visible and the last row is partially visible, then the second-to-last
     * visible row becomes the new first visible row after scrolling.
     */
    private void moveCaretPageDown() {
        var viewModel = getViewModel();
        if (viewModel.getOffsets().isEmpty()) {
            return;
        }
        var pageNavigation = createPageNavigation();
        int calculatedNewFirstRowIndex = pageNavigation.firstRowIndex() + pageNavigation.scrollRowCount();
        this.virtualFlow.scrollYBy(pageNavigation.scrollRowCount * pageNavigation.rowHeiht());
        Platform.runLater(() -> {
            moveCaretOnPagination(calculatedNewFirstRowIndex, pageNavigation.caretVisibleRowIndex,
                    this.virtualFlow.visibleCells().size() - 1);
        });
    }

    private void moveCaretUp() {
        var caretViewModel = getViewModel().getCaret();
        var rowIndex = getViewModel().calculateRowIndex(caretViewModel.getRowOffset());
        rowIndex--;
        if (rowIndex >= 0) {
            var row = scrollUpTo(rowIndex);
            updateCaretRow(row);
            caretViewModel.setRowIndex(rowIndex);
            caretViewModel.setRowOffset(row.getViewModel().getModel().getOffset());
            completeCaretMove();
        }
    }

    private void moveCaretDown() {
        var caretViewModel = getViewModel().getCaret();
        var rowIndex = getViewModel().calculateRowIndex(caretViewModel.getRowOffset());
        rowIndex++;
        if (rowIndex < getViewModel().getOffsets().size()) {
            var row = scrollDownTo(rowIndex);
            updateCaretRow(row);
            caretViewModel.setRowIndex(rowIndex);
            caretViewModel.setRowOffset(row.getViewModel().getModel().getOffset());
            completeCaretMove();
        }
    }

    private void moveCaretLeft() {
        var caretViewModel = getViewModel().getCaret();
        if (caretViewModel.getByteIndex() - 1 < 0) {
            if ((caretViewModel.getPanel() == EditorPanel.HEX && caretViewModel.getBytePart() == BytePart.HIGH)
                    || caretViewModel.getPanel() == EditorPanel.ASCII) {
                var rowIndex = getViewModel().calculateRowIndex(caretViewModel.getRowOffset());
                rowIndex--;
                if (rowIndex >= 0) {
                    var row = scrollUpTo(rowIndex);
                    moveCaretTo(caretViewModel.getPanel(), row, rowIndex, row.getByteTextPairs().size() - 1,
                            BytePart.LOW);
                }
            } else {
                doMoveCaretLeft();
            }
        } else {
            doMoveCaretLeft();
        }
    }

    private void moveCaretRight() {
        var caretViewModel = getViewModel().getCaret();
        if (caretViewModel.getByteIndex() + 1 == getViewModel().getRowByteCount()) {
            if ((caretViewModel.getPanel() == EditorPanel.HEX && caretViewModel.getBytePart() == BytePart.LOW)
                    || caretViewModel.getPanel() == EditorPanel.ASCII) {
                var rowIndex = getViewModel().calculateRowIndex(caretViewModel.getRowOffset());
                rowIndex++;
                if (rowIndex < getViewModel().getOffsets().size()) {
                    var row = scrollDownTo(rowIndex);
                    moveCaretTo(caretViewModel.getPanel(), row, rowIndex, 0, BytePart.HIGH);
                }
            } else {
                doMoveCaretRight();
            }
        } else {
            doMoveCaretRight();
        }
    }

    private void moveCaretHome() {
        var caretViewModel = getViewModel().getCaret();
        if (caretViewModel.getByteIndex() != 0
                || (caretViewModel.getPanel() == EditorPanel.HEX && caretViewModel.getBytePart() == BytePart.LOW)) {
            caretViewModel.setByteIndex(0);
            caretViewModel.setBytePart(BytePart.HIGH);
            updateCaretX();
            completeCaretMove();
        }
    }

    private void moveCaretEnd() {
        var caretViewModel = getViewModel().getCaret();
        if (caretViewModel.getByteIndex()  + 1 != this.caret.getRow().getByteTextPairs().size()
                || (caretViewModel.getPanel() == EditorPanel.HEX && caretViewModel.getBytePart() == BytePart.HIGH)) {
            caretViewModel.setByteIndex(this.caret.getRow().getByteTextPairs().size() - 1);
            caretViewModel.setBytePart(BytePart.HIGH);
            updateCaretX();
            completeCaretMove();
        }
    }

    /**
     * Is not called when only row is changed.
     */
    private void updateCaretX() {
        var caretViewModel = getViewModel().getCaret();
        var bytePair = this.caret.getRow().getByteTextPairs().get(caretViewModel.getByteIndex());
        if (caretViewModel.getPanel() == EditorPanel.HEX) {
            var text = bytePair.getHexText();
            if (caretViewModel.getBytePart() == BytePart.HIGH) {
                caretViewModel.setX(text.getBoundsInParent().getMinX());
            } else {
                double textWidth = text.getLayoutBounds().getWidth();
                double widthHalf = textWidth / 2;
                caretViewModel.setX(text.getBoundsInParent().getMinX() + widthHalf);
            }
        } else {
            var text = bytePair.getAsciiText();
            caretViewModel.setX(text.getBoundsInParent().getMinX());
        }
        updateIndicatorX();
    }

    private void updateIndicatorX() {
        var row = this.caret.getRow();
        if (row != null) {
            var caretVM = this.caret.getViewModel();
            var text = row.getText(caretVM.getPanel().opposite(), caretVM.getByteIndex());
            var textBounds = text.getBoundsInParent();
            this.caret.getIndicator().setTranslateX(textBounds.getMinX());
            this.caret.getIndicator().setWidth(textBounds.getMaxX() - textBounds.getMinX());
        }
    }

    private void doMoveCaretLeft() {
        var caretViewModel = getViewModel().getCaret();
        if (caretViewModel.getPanel() == EditorPanel.HEX) {
            if (!(caretViewModel.getByteIndex() == 0 && caretViewModel.getBytePart() == BytePart.HIGH)) {
                if (caretViewModel.getBytePart() == BytePart.HIGH) {
                    caretViewModel.setByteIndex(caretViewModel.getByteIndex() - 1);
                    caretViewModel.setBytePart(BytePart.LOW);
                    updateCaretX();
                } else {
                    caretViewModel.setBytePart(BytePart.HIGH);
                    updateCaretX();
                }
            }
        } else {
            if (caretViewModel.getByteIndex() != 0) {
                caretViewModel.setByteIndex(caretViewModel.getByteIndex() - 1);
                updateCaretX();
            }
        }
        completeCaretMove();
    }

    private void doMoveCaretRight() {
        var caretViewModel = getViewModel().getCaret();
        if (caretViewModel.getPanel() == EditorPanel.HEX) {
            if (!(caretViewModel.getByteIndex() + 1 == this.caret.getRow().getByteTextPairs().size()
                    && caretViewModel.getBytePart() == BytePart.LOW)) {
                if (caretViewModel.getBytePart() == BytePart.HIGH) {
                        caretViewModel.setBytePart(BytePart.LOW);
                        updateCaretX();
                } else {
                    caretViewModel.setByteIndex(caretViewModel.getByteIndex() + 1);
                    var bytePair = this.caret.getRow().getByteTextPairs().get(caretViewModel.getByteIndex());
                    if (!bytePair.isEmpty()) {
                        caretViewModel.setBytePart(BytePart.HIGH);
                        updateCaretX();
                    }
                }
            }
        } else {
            if (caretViewModel.getByteIndex() + 1 != this.caret.getRow().getByteTextPairs().size()) {
                caretViewModel.setByteIndex(caretViewModel.getByteIndex() + 1);
                updateCaretX();
            }
        }
        completeCaretMove();
    }

    private void updateCaretRow(RowView newRow) {
        var caretRow = this.caret.getRow();
        if (caretRow != newRow && caretRow != null) {
            caretRow.getViewModel().setFocused(false);
            caretRow.removeCaret();
        }
        this.caret.setRow(newRow);
        this.caret.getNode().setHeight(newRow.getNode().getHeight());
        this.caret.getViewModel().setRow(newRow.getViewModel());
    }

    private void completeCaretMove() {
        var caretRow = this.caret.getRow();
        caretRow.getViewModel().setFocused(true);
        caretRow.removeCaret();
        caretRow.addCaret();
        //when cursor is moved it must always be visible
        if (!this.caret.getViewModel().isDisabled()) {
            this.caret.getViewModel().setVisible(true);
        }
    }
}
