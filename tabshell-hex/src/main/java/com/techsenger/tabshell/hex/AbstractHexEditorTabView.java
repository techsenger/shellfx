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

import atlantafx.base.theme.Styles;
import com.techsenger.mvvm4fx.core.PulseListenerTiming;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.hex.data.DataInspectorView;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.tabs.TabHostView;
import com.techsenger.tabshell.tabs.workertab.AbstractWorkerTabView;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
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

    private final ComboBox<Integer> rowByteCountsComboBox = new ComboBox<>();

    private final ToggleButton columnsEnabledButton =
            new ToggleButton(null, new FontIconView(HexIcons.COLUMNS_ENABLED));

    private final ComboBox<Integer> columnByteCountsComboBox = new ComboBox<>();

    private final ComboBox<NumberBase> offsetNumberBaseComboBox = new ComboBox<>();

    private final ToolBar toolBar = new ToolBar();

    private final HeaderRowView headerRow;

    /**
     * Reusable cells in a virtual flow remain in memory after creation and are only released when virtualFlow.dispose()
     * is explicitly invoked.
     */
    private final List<BodyRowView> bodyRows = new ArrayList<>();

    /**
     * Integer is a row index.
     */
    private final VirtualFlow<Integer, BodyRowView> virtualFlow;

    private final VirtualizedScrollPane<VirtualFlow<Integer, BodyRowView>> virtualScrollPane;

    private final VBox mainPane = new VBox();

    private final CaretView caret;

    private final TabHostView rightTabHost;

    private final DataInspectorView<?> dataInspector;

    private int pulseCounter;

    public AbstractHexEditorTabView(ShellView<?> tabShell, T viewModel) {
        super(tabShell, viewModel);
        this.virtualFlow = VirtualFlow.createVertical(viewModel.getOffsets(), offset -> {
                var rowViewModel = viewModel.createRow(offset);
                var rowView = new BodyRowView(rowViewModel, this);
                this.bodyRows.add(rowView);
                rowView.initialize();
                return rowView;
        });
        this.virtualScrollPane = new VirtualizedScrollPane<>(virtualFlow);
        this.caret = new CaretView(viewModel.getCaret());
        this.rightTabHost = new TabHostView(viewModel.getRightTabHost());
        this.dataInspector = createDataInspector();
        this.headerRow = new HeaderRowView(viewModel.getHeaderRow(), this);
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

        rowByteCountsComboBox.setItems(viewModel.getRowByteCounts());
        rowByteCountsComboBox.getStyleClass().add(Styles.DENSE);
        rowByteCountsComboBox.setTooltip(new Tooltip("Bytes per Row"));

        columnsEnabledButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        columnsEnabledButton.setTooltip(new Tooltip("Columns Enabled"));

        columnByteCountsComboBox.setItems(viewModel.getColumnByteCounts());
        columnByteCountsComboBox.getStyleClass().add(Styles.DENSE);
        columnByteCountsComboBox.setTooltip(new Tooltip("Bytes per Column"));

        offsetNumberBaseComboBox.setItems(viewModel.getOffsetNumberBases());
        offsetNumberBaseComboBox.getStyleClass().add(Styles.DENSE);
        offsetNumberBaseComboBox.setTooltip(new Tooltip("Offset Display Base"));

        var css = AbstractHexEditorTabView.class.getResource("hexeditor.css").toExternalForm();
        getTopPane().getStylesheets().add(css);
        this.virtualScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(virtualScrollPane, Priority.ALWAYS);

        this.mainPane.getChildren().addAll(headerRow.getNode(), virtualScrollPane);
        VBox.setVgrow(this.mainPane, Priority.ALWAYS);

        VBox.setVgrow(this.rightTabHost.getNode(), Priority.ALWAYS);
        getRightPane().getChildren().add(this.rightTabHost.getNode());
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        this.headerRow.getNode().prefWidthProperty().bind(this.mainPane.widthProperty());
        this.rowByteCountsComboBox.valueProperty().bindBidirectional(viewModel.rowByteCountProperty());
        this.columnByteCountsComboBox.valueProperty().bindBidirectional(viewModel.columnByteCountProperty());
        this.columnsEnabledButton.selectedProperty().bindBidirectional(viewModel.columnsEnabledProperty());
        this.offsetNumberBaseComboBox.valueProperty().bindBidirectional(viewModel.offsetNumberBaseProperty());
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
        viewModel.layoutUpdateSource().addListener((newPosition) -> updateLayout(newPosition));
        viewModel.caretPositionSource().addListener((position) -> updateCaretPosition(position));
        this.mainPane.widthProperty().addListener((ov, oldV, newV) ->
                this.headerRow.updateAsciiPaneWidth(newV.doubleValue()));
        this.virtualScrollPane.estimatedScrollXProperty().addListener((ov, oldV, newV) -> {
            this.headerRow.getScrollableBox().setTranslateX(newV * -1);
            this.headerRow.updateAsciiPaneWidth(this.mainPane.getWidth());
        });
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        this.headerRow.initialize();
        this.caret.initialize();
        this.rightTabHost.initialize();
        this.dataInspector.initialize();
        this.rightTabHost.openTab(this.dataInspector);
        viewModel.readFile();
    }

    @Override
    protected void preDeinitialize(T viewModel) {
        super.preDeinitialize(viewModel);
        this.dataInspector.deinitialize();
        this.rightTabHost.deinitialize();
        this.caret.deinitialize();
        this.headerRow.deinitialize();
        for (var r : bodyRows) {
            r.deinitialize();
        }
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

    protected VirtualFlow<Integer, BodyRowView> getVirtualFlow() {
        return virtualFlow;
    }

    protected VirtualizedScrollPane<VirtualFlow<Integer, BodyRowView>> getVirtualScrollPane() {
        return virtualScrollPane;
    }

    protected VBox getMainPane() {
        return mainPane;
    }

    protected ComboBox<Integer> getRowByteCountsComboBox() {
        return rowByteCountsComboBox;
    }

    protected ToggleButton getColumnsEnabledButton() {
        return columnsEnabledButton;
    }

    protected ComboBox<Integer> getColumnByteCountsComboBox() {
        return columnByteCountsComboBox;
    }

    protected ComboBox<NumberBase> getOffsetNumberBaseComboBox() {
        return offsetNumberBaseComboBox;
    }

    private void updateLayout(CaretPosition newPosition) {
        this.mainPane.setVisible(false);
        this.headerRow.rebuild();
        for (var r : bodyRows) {
            r.rebuild();
            r.updateItem(r.getViewModel().getModel().getOffset());
        }
        BodyRowView row = null;
        if (newPosition != null) {
            //after clearing and adding new items flow is scrolled to the end
            this.virtualFlow.showAsFirst(newPosition.getRowIndex());
            row = this.virtualFlow.getCell(newPosition.getRowIndex());
        }
        var finalRow = row;
        this.pulseCounter = 0;
        //When the layout changes, the text coordinates are updated as well; therefore, to correctly calculate
        //the caret position, it is necessary to use a pulse listener.

        //For precise padding calculation, exact dimensions are only available in the PulseListener. However,
        //when the required padding is set within this listener, the changes will only become visible in the
        //next pulse. To prevent flickering during layout adjustments, the mainPane is made invisible and only
        //becomes visible again after the second pulse.
        addLayoutPulseListener(PulseListenerTiming.AFTER, () -> {
            if (this.pulseCounter == 0) {
                this.headerRow.setPanelPanesBackground();
                this.headerRow.updateAsciiPaneWidth(this.mainPane.getWidth());
                this.pulseCounter++;
                return true;
            } else {
                this.mainPane.setVisible(true);
                this.caret.moveTo(newPosition, finalRow);
                NodeUtils.requestFocus(virtualFlow);
                return false;
            }
        });
    }

    private void updateCaretPosition(CaretPosition newPos) {
        var curPos = this.caret.getViewModel().getPosition();
        if (newPos.getRowIndex() == curPos.getRowIndex()) {
            this.caret.moveTo(newPos, null);
        } else {
            BodyRowView row;
            if (newPos.getRowIndex() > curPos.getRowIndex()) {
                row = scrollDownTo(newPos.getRowIndex());
            } else {
                row = scrollUpTo(newPos.getRowIndex());
            }
            this.caret.moveTo(newPos, row);
        }
    }

    private BodyRowView scrollUpTo(int rowIndex) {
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

    private BodyRowView scrollDownTo(int rowIndex) {
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
        var caretRowIndex = this.caret.getViewModel().getPosition().getRowIndex();
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
        BodyRowView newCaretRow;

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
        var curPos = this.caret.getViewModel().getPosition();
        var newPos = CaretPosition.create(curPos.getPanel(),
                newCaretRowIndex, curPos.getByteIndex(), curPos.getByteLocation(), viewModel);
        this.caret.moveTo(newPos, newCaretRow);
    }
}
