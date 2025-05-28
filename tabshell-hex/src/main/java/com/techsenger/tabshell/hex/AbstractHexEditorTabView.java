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

import com.techsenger.mvvm4fx.core.PulseListenerTiming;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.hex.data.DataInspectorView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.tabs.tabmanager.TabManagerView;
import com.techsenger.tabshell.tabs.workertab.AbstractWorkerTabView;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
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
public abstract class AbstractHexEditorTabView<T extends AbstractHexEditorTabViewModel>
        extends AbstractWorkerTabView<T> {

    private static final double ROW_VISIBILITY_TOLERANCE = 2.0;

    /**
     * Contains information for pageUp and pageDown scroll.
     */
    private record PageScroll(int firstRowIndex, double rowHeiht, int caretVisibleRowIndex, int scrollRowCount) { }

    private final Button newButton = new Button(null, new FontIconView(CoreIcons.ADD));

    private final Button clearButton = new Button(null, new FontIconView(CoreIcons.CLEAR));

    private final Button cutButton = new Button(null, new FontIconView(CoreIcons.CUT));

    private final Button copyButton = new Button(null, new FontIconView(CoreIcons.COPY));

    private final Button pasteButton = new Button(null, new FontIconView(CoreIcons.PASTE));

    private final Button undoButton = new Button(null, new FontIconView(CoreIcons.UNDO));

    private final Button redoButton = new Button(null, new FontIconView(CoreIcons.REDO));

    private final Button findButton = new Button(null, new FontIconView(CoreIcons.FIND));

    private final Button replaceButton = new Button(null, new FontIconView(CoreIcons.REPLACE));

    private final ToolBar toolBar = new ToolBar();

    /**
     * Integer is a row index.
     */
    private VirtualFlow<Integer, RowView> virtualFlow;

    private VirtualizedScrollPane<VirtualFlow<Integer, RowView>> virtualScrollPane;

    private final CaretView caret;

    private final TabManagerView rightTabManager;

    private final DataInspectorView<?> dataInspector;

    public AbstractHexEditorTabView(ShellView<?> tabShell, T viewModel) {
        super(tabShell, viewModel);
        this.caret = new CaretView(this, viewModel.getCaret());
        this.rightTabManager = new TabManagerView(viewModel.getRightTabManager());
        this.dataInspector = createDataInspector();
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

    protected DataInspectorView<?> createDataInspector() {
        return new DataInspectorView<>(getViewModel().getDataInspector());
    }

    @Override
    protected void build(T viewModel) {
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

        var css = AbstractHexEditorTabView.class.getResource("hexeditor.css").toExternalForm();
        getTopPane().getStylesheets().add(css);
        virtualFlow = VirtualFlow.createVertical(viewModel.getOffsets(), offset -> {
                var rowViewModel = viewModel.createRow(offset);
                var rowView = new RowView(rowViewModel, this);
                rowView.initialize();
                return rowView;
        });
        virtualScrollPane = new VirtualizedScrollPane<>(virtualFlow);
        this.virtualScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(virtualScrollPane, Priority.ALWAYS);

        VBox.setVgrow(this.rightTabManager.getNode(), Priority.ALWAYS);
        getRightPane().getChildren().add(this.rightTabManager.getNode());
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        virtualFlow.setOnMousePressed(e -> virtualFlow.requestFocus());
        virtualFlow.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            e.consume();
            if (viewModel.getOffsets().isEmpty()) {
                return;
            }
            switch (e.getCode()) {
                case UP: viewModel.moveCaretUp(); break;
                case DOWN: viewModel.moveCaretDown(); break;
                case LEFT: viewModel.moveCaretLeft(); break;
                case RIGHT: viewModel.moveCaretRight(); break;
                case HOME: viewModel.moveCaretHome(); break;
                case END: viewModel.moveCaretEnd(); break;
                case PAGE_UP: moveCaretPageUp(); break;
                case PAGE_DOWN: moveCaretPageDown(); break;
            }
        });
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.contentLoadedSource().addListener((newV) -> {
            this.virtualFlow.showAsFirst(0); //after clearing and adding new items flow is scrolled to the end
            var row = this.virtualFlow.getCell(0);
            this.caret.moveTo(row);
            addLayoutPulseListener(PulseListenerTiming.AFTER, () -> {
                NodeUtils.requestFocus(virtualFlow);
                return false;
            });
        });
        viewModel.moveRequestSource().addListener((row) -> onMoveRequest(row));
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        this.caret.initialize();
        this.rightTabManager.initialize();
        this.dataInspector.initialize();
        this.rightTabManager.openTab(this.dataInspector);
        viewModel.readFile();
    }

    @Override
    protected void preDeinitialize(T viewModel) {
        super.preDeinitialize(viewModel);
        this.dataInspector.deinitialize();
        this.rightTabManager.deinitialize();
        this.caret.deinitialize();
    }

    protected Button getNewButton() {
        return newButton;
    }

    protected Button getClearButton() {
        return clearButton;
    }

    protected Button getCutButton() {
        return cutButton;
    }

    protected Button getCopyButton() {
        return copyButton;
    }

    protected Button getPasteButton() {
        return pasteButton;
    }

    protected Button getUndoButton() {
        return undoButton;
    }

    protected Button getRedoButton() {
        return redoButton;
    }

    protected Button getFindButton() {
        return findButton;
    }

    protected Button getReplaceButton() {
        return replaceButton;
    }

    protected ToolBar getToolBar() {
        return toolBar;
    }

    protected VirtualFlow<Integer, RowView> getVirtualFlow() {
        return virtualFlow;
    }

    protected VirtualizedScrollPane<VirtualFlow<Integer, RowView>> getVirtualScrollPane() {
        return virtualScrollPane;
    }

    private void onMoveRequest(Integer newRow) {
        if (newRow == null) {
            this.caret.move();
        } else {
            var currentRow = this.caret.getViewModel().getRowIndex();
            RowView row;
            if (newRow > currentRow) {
                row = scrollDownTo(newRow);
            } else {
                row = scrollUpTo(newRow);
            }
            this.caret.moveTo(row);
        }
    }

    private RowView scrollUpTo(int rowIndex) {
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

    private RowView scrollDownTo(int rowIndex) {
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
        var pageScroll = createPageScroll();
        int calculatedNewFirstRowIndex = pageScroll.firstRowIndex() - pageScroll.scrollRowCount();
        this.virtualFlow.scrollYBy(pageScroll.scrollRowCount * pageScroll.rowHeiht() * -1);
        Platform.runLater(() -> {
            moveCaretOnPageScroll(calculatedNewFirstRowIndex, pageScroll.caretVisibleRowIndex, 0);
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
        var pageScroll = createPageScroll();
        int calculatedNewFirstRowIndex = pageScroll.firstRowIndex() + pageScroll.scrollRowCount();
        this.virtualFlow.scrollYBy(pageScroll.scrollRowCount * pageScroll.rowHeiht());
        Platform.runLater(() -> {
            moveCaretOnPageScroll(calculatedNewFirstRowIndex, pageScroll.caretVisibleRowIndex,
                    this.virtualFlow.visibleCells().size() - 1);
        });
    }

    private PageScroll createPageScroll() {
        var viewModel = getViewModel();

        //resolving which row is fully visible and which is not
        var firstRow = this.virtualFlow.visibleCells().get(0);
        var firstRowIndex = viewModel.calculateRowIndex(firstRow.getViewModel());
        double firstRowFlowOffset = Math.max(0, -firstRow.getNode().getBoundsInParent().getMinY());
        boolean firstRowFullyVisible = ROW_VISIBILITY_TOLERANCE >= firstRowFlowOffset;

        var visibleRowTotalHeight = this.virtualFlow.visibleCells().size() * firstRow.getNode().getHeight();
        visibleRowTotalHeight -= firstRowFlowOffset;
        boolean lastRowFullyVisible = ROW_VISIBILITY_TOLERANCE >= visibleRowTotalHeight - this.virtualFlow.getHeight();

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

        return new PageScroll(firstRowIndex, firstRow.getNode().getHeight(), caretVisibleRowIndex, scrollRowCount);
    }

    private void moveCaretOnPageScroll(int calculatedNewFirstRowIndex, int caretVisibleRowIndex, int endCaretRowIndex) {
        var viewModel = getViewModel();
        RowView newCaretRow;

        //we don't know how many rows were actually scrolled
        var realNewFirstRow = this.virtualFlow.visibleCells().get(0);
        var realNewFirstRowIndex = viewModel.calculateRowIndex(realNewFirstRow.getViewModel());
        //if fewer rows are scrolled than requested, the caret is placed on the first or last row.
        if (realNewFirstRowIndex == calculatedNewFirstRowIndex) {
            newCaretRow = this.virtualFlow.visibleCells().get(caretVisibleRowIndex);
        } else {
            newCaretRow = this.virtualFlow.visibleCells().get(endCaretRowIndex);
        }
        int newCaretRowIndex = viewModel.calculateRowIndex(newCaretRow.getViewModel());
        viewModel.adjustCaretDownForLastRow(newCaretRowIndex);
        this.caret.moveTo(newCaretRow);
    }
}
