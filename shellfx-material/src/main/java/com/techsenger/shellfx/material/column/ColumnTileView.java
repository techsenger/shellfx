/*
 * Copyright 2024-2026 Pavel Castornii.
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

package com.techsenger.shellfx.material.column;

import com.techsenger.annotations.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A vertically-scrolling grid of items, filled row-major (left-to-right, then wraps down), with a fixed
 * number of columns &mdash; the {@link ColumnListView} counterpart for a "Tiles" layout instead of a
 * "List" layout. See {@link ColumnListView}'s own javadoc for the resize/row-height-measurement notes,
 * which apply here unchanged.
 *
 * <p>Unlike {@link ColumnListView}, where column count is a value <em>derived</em> from item count and a
 * fixed row height, here {@link #columnCount} is the fixed <em>input</em> (row count is derived from it), so
 * this view computes its own cell width internally &mdash; no consumer-owned width API is needed.
 *
 * @author Pavel Castornii
 */
public class ColumnTileView<T> extends AbstractColumnView<T> {

    private static final Logger logger = LoggerFactory.getLogger(ColumnTileView.class);

    private static class ColumnTileViewRow<T> extends IndexedCell<Integer> {

        private static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");

        private final HBox node = new HBox();

        private final ColumnTileView<T> tileView;

        private final List<TileCell<T>> cachedCells = new ArrayList<>();

        private TileCell<T> selectedCell;

        private boolean dirty = true;

        ColumnTileViewRow(final ColumnTileView<T> tileView) {
            this.tileView = tileView;
            node.getStyleClass().add("row");
            node.setAlignment(Pos.CENTER_LEFT);
            setGraphic(node);
        }

        /**
         * {@code item} here is this row's own starting offset into {@link #getItems()} (see the class
         * javadoc), not one of the actual items it displays &mdash; so the guard below is an <em>index</em>
         * equality check ("this row already shows this same slice of the list, and nothing has marked it
         * {@link #dirty}"), not a content check. It is exactly what makes normal recycling cheap: a row whose
         * offset genuinely hasn't changed skips rebuilding its {@link #cachedCells} entirely.
         *
         * <p>The flip side: if one of those already-cached items mutates a field in place (with no change to
         * the item list/order itself, so the offset stays the same), this guard has no way to notice and
         * will keep skipping, leaving the mutation unrendered indefinitely. There is no by-item-content path
         * around this that reaches an off-screen, already-cached row, other than forcing it &mdash; see
         * {@link ColumnViewUtils#updateCell(ColumnTileView, int)}/{@link ColumnViewUtils#updateCells(ColumnTileView,
         * boolean) updateCells(..., false)}.
         */
        @Override
        public void updateItem(Integer item, boolean empty) {
            applyRowHeight();
            applyCellWidths();
            if (item != null && item.equals(getItem()) && !empty && !dirty) {
                return;
            }
            super.updateItem(item, empty);
            node.pseudoClassStateChanged(EMPTY, empty);
            // A cell about to be discarded here (e.g. a row recycled for a different offset after items
            // shrank) can currently own scene focus. Removing a focused node from the scene graph does not
            // reassign focus - Scene.getFocusOwner() is left pointing at a now-detached node, so no further
            // key events (e.g. arrow-key navigation) are dispatched anywhere until something explicitly
            // requests focus again. Move focus to the still-live container first.
            if (node.isFocusWithin()) {
                tileView.requestFocus();
            }
            node.getChildren().clear();
            clearSelection();
            if (item != null) {
                if (tileView.rowHeight.get() > 0) {
                    updateCells();
                    dirty = false;
                } else {
                    addRowHeightCell();
                }
            } else if (tileView.rowHeight.get() > 0) {
                updateEmptyCells();
            }
        }

        protected ColumnTileView<T> getTileView() {
            return tileView;
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new CellSkinBase<>(this);
        }

        /**
         * Applies {@link ColumnTileView#rowHeight} to this row's node, if resolved yet.
         */
        private void applyRowHeight() {
            var height = tileView.rowHeight.get();
            if (height > 0) {
                node.setMinHeight(height);
                node.setPrefHeight(height);
                node.setMaxHeight(height);
            }
        }

        /**
         * Applies the current per-cell width (see {@link ColumnTileView#computeCellWidth()}) to every cached
         * cell in this row, if resolved yet.
         */
        private void applyCellWidths() {
            var width = tileView.computeCellWidth();
            if (width < 0) {
                return;
            }
            for (var cell : cachedCells) {
                applyCellWidth(cell, width);
            }
        }

        private void applyCellWidth(TileCell<T> cell, double width) {
            cell.setMinWidth(width);
            cell.setPrefWidth(width);
            cell.setMaxWidth(width);
        }

        private HBox getNode() {
            return this.node;
        }

        private void updateCells() {
            var item = getItem();
            node.getStyleClass().removeAll("first", "last");
            if (item == 0) {
                node.getStyleClass().add("first");
            } else if (item == (this.tileView.getRowCount() - 1) * this.tileView.getColumnCount()) {
                node.getStyleClass().add("last");
            }
            var items = this.tileView.getItems();
            // item (this row's own offset) can be momentarily stale relative to a just-shrunk items list:
            // refresh()'s reentrancy guard can postpone an ITEMS-triggered offsets rebuild behind another,
            // unrelated trigger (see RefreshTrigger), and if that other trigger sees no rowCount change, its
            // own updateOffsets() call is skipped too - leaving this row pointing past the end of the new
            // list until the next refresh corrects it. Clamp instead of crashing; an empty row here
            // self-heals on that next refresh.
            var startIndex = Math.min(item, items.size());
            int endIndex = Math.min(startIndex + this.tileView.getColumnCount(), items.size());
            var cellItems = items.subList(startIndex, endIndex);
            var absentCells = cellItems.size() - this.cachedCells.size();
            for (var i = 0; i < absentCells; i++) {
                createCell();
            }
            //updating cells and adding them to node
            for (var i = 0; i < cellItems.size(); i++) {
                var cellItem = cellItems.get(i);
                TileCell cell = cachedCells.get(i);
                cell.updateItem((Object) cellItem, false);
                var cellIndex = item + i;
                //index must be updated only after setting non empty, see Cell#updateSelected(boolean selected).
                cell.updateIndex(cellIndex);
                if (cellIndex == tileView.getSelectionModel().getSelectedIndex() && cellIndex != -1) {
                    setSelectedCell(cell);
                }
                if (!cell.isEditing() && cell.isEditable() && cell.getIndex() == this.tileView.getEditingCellIndex()) {
                    cell.startEdit();
                }
                this.node.getChildren().add(cell);
            }
            if (cellItems.size() < this.cachedCells.size()) {
                for (var i = cellItems.size(); i < this.cachedCells.size(); i++) {
                    TileCell cell = cachedCells.get(i);
                    cell.updateItem(null, true);
                    var cellIndex = item + i;
                    cell.updateIndex(cellIndex);
                }
            }
        }

        /**
         * Fills a virtualization filler row (past the last real row, created by {@code VirtualFlow} itself to
         * cover the viewport) with {@link ColumnTileView#getColumnCount()} empty, non-selectable placeholder
         * cells, so the column separators (see {@code column-tile-view.css}, {@code .row > .cell}) keep going
         * all the way to the bottom of the viewport even when there are too few items to fill it &mdash; the
         * same look {@code ColumnListView}'s filler columns get for free from a border declared on {@code
         * .column} itself rather than on a per-cell child.
         */
        private void updateEmptyCells() {
            var columnCount = this.tileView.getColumnCount();
            var absentCells = columnCount - this.cachedCells.size();
            for (var i = 0; i < absentCells; i++) {
                createCell();
            }
            for (var i = 0; i < columnCount; i++) {
                TileCell cell = cachedCells.get(i);
                cell.updateItem(null, true);
                this.node.getChildren().add(cell);
            }
        }

        private void createCell() {
            var cell = this.tileView.getCellFactory().call((ColumnTileView) this.tileView);
            cell.setTileView((ColumnTileView) this.tileView);
            var width = tileView.computeCellWidth();
            if (width >= 0) {
                applyCellWidth(cell, width);
            }
            this.cachedCells.add(cell);
        }

        private void setSelectedCell(TileCell<T> cell) {
            this.selectedCell = cell;
            selectedCell.updateSelected(true); //will call requestLayout();
        }

        private void clearSelection() {
            if (selectedCell != null && !selectedCell.isEditing()) {
                selectedCell.updateSelected(false);
                selectedCell = null;
            }
        }

        /**
         * Empty cell is used to calculate row height.
         */
        private void addRowHeightCell() {
            createCell();
            TileCell<T> cell = cachedCells.get(0);
            ChangeListener<Number> listener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number oldV, Number newV) {
                    if (newV.doubleValue() > 1) {
                        cell.heightProperty().removeListener(this);
                        Platform.runLater(() -> tileView.rowHeight.set(newV.doubleValue()));
                    }
                }
            };
            cell.heightProperty().addListener(listener);
            this.node.getChildren().add(cell);
        }

        private void markDirty() {
            this.dirty = true;
        }
    }

    private static class TileVirtualFlow<T> extends VirtualFlow<ColumnTileViewRow<T>> {

        /**
         * By default the width of the scrollbar is 100 pixel so we wait until real width is set.
         */
        private Double vBarWidth = null;

        TileVirtualFlow() {
            var hBar = getHbar();
            hBar.setMinHeight(0);
            hBar.setPrefHeight(0);
            hBar.setMaxHeight(0);
            hBar.setOpacity(0);
            getVBar().widthProperty().addListener((ov, oldV, newV) -> vBarWidth = newV.doubleValue());
        }

        ScrollBar getVBar() {
            return super.getVbar();
        }

        /**
         * Returns the width of the virtual flow without the width of the vertical scroll bar.
         * @return
         */
        double getViewportWidth() {
            if (vBarWidth != null && getVBar().isVisible()) {
                return getWidth() - vBarWidth;
            } else {
                return getWidth();
            }
        }

        @Override
        protected List<ColumnTileViewRow<T>> getCells() {
            return super.getCells();
        }
    }

    /**
    * Triggers that cause a refresh when changed.
    */
    private enum RefreshTrigger {

        ITEMS,

        ROW_HEIGHT,

        COLUMN_COUNT,

        VIRTUAL_FLOW_WIDTH
    }

    /**
     * Always fixed height of the row, auto-measured the same way as {@link ColumnListView#rowHeight}.
     */
    private final DoubleProperty rowHeight = new SimpleDoubleProperty();

    /**
     * Fixed number of cells per row &mdash; the input this view is built around (see class javadoc).
     */
    private final IntegerProperty columnCount = new SimpleIntegerProperty(1);

    private final ObjectProperty<Callback<ColumnTileView<T>, TileCell<T>>> cellFactory
            = new SimpleObjectProperty();

    private final TileVirtualFlow<T> virtualFlow = new TileVirtualFlow<>();

    /**
     * The last-saved trigger for a refresh postponed behind an in-progress one; see
     * {@link AbstractColumnView#getCurrentType()} for the reentrancy guard this is part of.
     */
    private RefreshTrigger secondRefreshTrigger = null;

    private final List<WeakReference<ColumnTileViewRow<?>>> rows = new LinkedList<>();

    public ColumnTileView() {
        getStylesheets().add(ColumnTileView.class.getResource("column-tile-view.css").toExternalForm());
        getStyleClass().add("column-tile-view");
        //default cell factory
        setCellFactory(v -> new TileCell<>());
        getChildren().add(this.virtualFlow);
        this.rowHeight.set(-1);
        this.rowHeight.addListener((ov, oldV, newV) -> refresh(RefreshTrigger.ROW_HEIGHT, RefreshType.PRIMARY));
        this.columnCount.addListener((ov, oldV, newV) -> refresh(RefreshTrigger.COLUMN_COUNT, RefreshType.PRIMARY));
        this.virtualFlow.getVBar().widthProperty().addListener((ov, oldV, newV) -> updateCellWidths());
        this.virtualFlow.getVBar().visibleProperty().addListener((ov, oldV, newV) -> updateCellWidths());
        //firstVisibleCellIndex is set via onResizeStarted.
        this.virtualFlow.widthProperty().addListener((ov, oldV, newV) -> {
            // Not just a cosmetic width reapplication: the very first refresh() attempt (e.g. from
            // setMode() right after this view is added to the scene) commonly runs before the virtual flow
            // has a real width yet and bails out via refresh()'s own width guard, leaving rowHeight/offsets
            // permanently unresolved unless something retries once a real width is known - this does that,
            // mirroring ColumnListView's own virtualFlow.heightProperty() recovery listener.
            savePositionAndRefreshView(RefreshTrigger.VIRTUAL_FLOW_WIDTH);
            updateCellWidths();
        });
        virtualFlow.setCellFactory(vf -> new ColumnTileViewRow<>(this) {

            {
                rows.add(new WeakReference<>(this));
            }

            @Override
            public void updateIndex(int index) {
                super.updateIndex(index);
                if (index >= 0 && index < getOffsets().size()) {
                    updateItem(getOffsets().get(index), false);
                } else {
                    updateItem(null, true);
                }
            }
        });
    }

    public Callback<ColumnTileView<T>, TileCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public void setCellFactory(Callback<ColumnTileView<T>, TileCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public IntegerProperty columnCountProperty() {
        return columnCount;
    }

    public int getColumnCount() {
        return columnCount.get();
    }

    public void setColumnCount(int columnCount) {
        if (columnCount < 1) {
            throw new IllegalArgumentException("columnCount must be at least 1, got " + columnCount);
        }
        this.columnCount.set(columnCount);
    }

    /**
     * This method is called when manual refresh is enabled.
     */
    public void refresh() {
        refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
    }

    public int resolveRowIndex(int cellIndex) {
        if (getColumnCount() == 0) {
            return -1;
        }
        return cellIndex / getColumnCount();
    }

    public int resolveColumnIndex(int cellIndex) {
        if (getColumnCount() == 0) {
            return -1;
        }
        return cellIndex % getColumnCount();
    }

    /**
     * Returns the count of items in a specific row - the last row can have fewer than {@link #getColumnCount()}
     * items.
     *
     * @param rowIndex
     * @return
     */
    public int resolveColumnCount(int rowIndex) {
        int totalItems = this.getItems().size();
        int columnCount = getColumnCount();
        int fullRows = totalItems / columnCount;
        int remaining = totalItems % columnCount;
        if (rowIndex < fullRows) {
            return columnCount;
        } else if (rowIndex == fullRows && remaining > 0) {
            return remaining;
        }
        return 0;
    }

    /**
     * Scrolls to the first row.
     */
    public void scrollToFirstRow() {
        this.virtualFlow.scrollTo(0);
    }

    /**
     * Scrolls to the last row.
     */
    public void scrollToLastRow() {
        this.virtualFlow.scrollPixels(Integer.MAX_VALUE);
    }

    /**
     * Scrolls to N row and shows it as the first one.
     *
     * @param rowIndex
     */
    public void scrollToFirstRow(int rowIndex) {
        this.virtualFlow.scrollToTop(rowIndex);
    }

    /**
     * Scrolls to N row and shows it as the last one.
     *
     * @param rowIndex
     */
    public void scrollToLastRow(int rowIndex) {
        this.virtualFlow.scrollTo(rowIndex);
    }

    public void edit(int cellIndex) {
        if (!isEditable() || getEditingCellIndex() != -1) {
            return;
        }
        setEditingCellIndex(cellIndex);
        var rowIndex = resolveRowIndex(cellIndex);
        ColumnTileViewRow<T> row = null;
        for (var r : this.virtualFlow.getCells()) {
            if (r.getIndex() == rowIndex) {
                row = r;
                break;
            }
        }
        if (row != null) {
            row.markDirty();
            row.requestLayout();
        }
    }

    /**
     * Returns the currently realized cell showing {@code itemIndex}, or {@code null} if it isn't currently
     * realized (e.g. scrolled far out of view) or {@code itemIndex} is out of range. Used by
     * {@link ColumnViewUtils#updateCell(ColumnTileView, int)} to force just that one cell to re-render from
     * its current item, without touching any other cell.
     */
    @Nullable TileCell<T> getCell(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= getItems().size()) {
            return null;
        }
        var rowIndex = resolveRowIndex(itemIndex);
        var columnIndex = resolveColumnIndex(itemIndex);
        var row = this.virtualFlow.getCell(rowIndex);
        if (row == null || row.isEmpty()) {
            return null;
        }
        var children = row.getNode().getChildren();
        return columnIndex >= 0 && columnIndex < children.size() ? (TileCell<T>) children.get(columnIndex) : null;
    }

    /**
     * Forces cells to re-derive their visual content from their current item, without touching this view's
     * items/offset bookkeeping or scroll position/selection &mdash; the {@link ColumnTileView} counterpart of
     * {@code VirtualFlowUtils#updateCells}, reaching directly into each realized row's own cached cells
     * instead of toggling the row's index (which would be the only option available to a generic,
     * outside-the-package utility, since {@link ColumnTileViewRow}'s cached cells are a private
     * implementation detail).
     *
     * @param onlyVisible whether to touch only the currently visible rows (cheap) or every row (thorough)
     *     &mdash; see {@code VirtualFlowUtils#updateCells} for what each means
     */
    void forceUpdateCells(boolean onlyVisible) {
        int first;
        int last;
        if (onlyVisible) {
            var firstCell = this.virtualFlow.getFirstVisibleCell();
            var lastCell = this.virtualFlow.getLastVisibleCell();
            if (firstCell == null || lastCell == null) {
                return;
            }
            first = firstCell.getIndex();
            last = lastCell.getIndex();
        } else {
            first = 0;
            last = this.virtualFlow.getCellCount() - 1;
        }
        for (var index = first; index <= last; index++) {
            var row = this.virtualFlow.getCell(index);
            if (row != null && !row.isEmpty()) {
                for (var cell : row.cachedCells) {
                    if (!cell.isEmpty()) {
                        cell.updateItem(cell.getItem(), false);
                    }
                }
            }
        }
        applyCss();
        layout();
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        virtualFlow.resizeRelocate(0, 0, width, height);
        // resizeRelocate() only triggers the flow's own layout when its size actually changes, so a scroll
        // (a position/state change with no size change, e.g. from VirtualFlowUtils#scrollTo) would otherwise
        // never get processed here at all - explicitly laying it out unconditionally covers that case too.
        virtualFlow.layout();
    }

    @Override
    boolean isContainerOrCellNode(Node node) {
        return node instanceof HBox || node instanceof TileCell;
    }

    @Override
    VirtualFlow<ColumnTileViewRow<T>> getVirtualFlow() {
        return virtualFlow;
    }

    @Override
    void refreshItems() {
        refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
    }

    /**
     * Clears the previously selected cell's highlight on every currently realized row and, if
     * {@code selectedIndex} resolves to one of them, sets its new selected cell. Off-screen rows are not
     * touched here - {@link ColumnTileViewRow#updateItem} already recomputes the correct selected cell from
     * scratch whenever such a row is next reused, so there is nothing stale left for them to show once
     * scrolled back into view.
     */
    @Override
    void updateSelectedCellHighlight(int selectedIndex) {
        var rowIndex = selectedIndex == -1 ? -1 : resolveRowIndex(selectedIndex);
        var columnIndex = selectedIndex == -1 ? -1 : resolveColumnIndex(selectedIndex);
        for (var row : this.virtualFlow.getCells()) {
            row.clearSelection();
            if (row.getIndex() == rowIndex && columnIndex < row.cachedCells.size()) {
                row.setSelectedCell(row.cachedCells.get(columnIndex));
            }
        }
    }

    /**
     * This method makes entire cell visible and is called when user clicks mouse, so, the cell exists and visible.
     */
    void scrollToSelected() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var rowIndex = resolveRowIndex(selectedIndex);

        if (rowIndex == this.virtualFlow.getFirstVisibleCell().getIndex()) {
            scrollToFirstRow(rowIndex);
        } else if (rowIndex == this.virtualFlow.getLastVisibleCell().getIndex()) {
            scrollToLastRow(rowIndex);
        }
    }

    /**
     * Returns the width each cell should have to make {@link #getColumnCount()} of them exactly fill the
     * available viewport width, or a negative value if not resolvable yet (no columns configured).
     */
    double computeCellWidth() {
        var count = getColumnCount();
        return count > 0 ? virtualFlow.getViewportWidth() / count : -1;
    }

    @Override
    void selectUp() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - getColumnCount();
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        } else {
            // Already in the first row - UP has nowhere else to go row-wise, so jump to the very first item
            // overall instead of doing nothing (mirrors HOME).
            selectHome();
        }
    }

    @Override
    void selectDown() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        if (resolveRowIndex(selectedIndex) >= getRowCount() - 1) {
            // Already in the last row - DOWN has nowhere else to go row-wise, so jump to the very last item
            // overall instead of doing nothing (mirrors END).
            selectEnd();
            return;
        }
        var newSelectedIndex = selectedIndex + getColumnCount();
        var lastIndex = getItems().size() - 1;
        // The last row may hold fewer than getColumnCount() items, so the same-column target in it can be
        // past the last real item - land on the last item instead of refusing to move down at all.
        selectNext(selectedIndex, Math.min(newSelectedIndex, lastIndex));
    }

    @Override
    void selectLeft() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - 1;
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        }
    }

    @Override
    void selectRight() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex + 1;
        if (newSelectedIndex < getItems().size()) {
            selectNext(selectedIndex, newSelectedIndex);
        }
    }

    private void selectPrevious(int selectedIndex, int newSelectedIndex) {
        //firstly we provide the row (it can be absent)
        var newRowIndex = resolveRowIndex(newSelectedIndex);
        var firstVisibleRowIndex = this.virtualFlow.getFirstVisibleCell().getIndex();
        if (newRowIndex <= firstVisibleRowIndex) {
            scrollToFirstRow(newRowIndex);
        }
        getSelectionModel().select(newSelectedIndex);
    }

    private void selectNext(int selectedIndex, int newSelectedIndex) {
        //firstly we provide the row (it can be absent)
        var newRowIndex = resolveRowIndex(newSelectedIndex);
        var lastVisibleRowIndex = this.virtualFlow.getLastVisibleCell().getIndex();
        if (newRowIndex >= lastVisibleRowIndex) {
            scrollToLastRow(newRowIndex);
        }
        getSelectionModel().select(newSelectedIndex);
    }

    @Override
    void selectHome() {
        if (this.getItems().isEmpty()) {
            return;
        }
        scrollToFirstRow();
        getSelectionModel().select(0);
    }

    @Override
    void selectEnd() {
        if (this.getItems().isEmpty()) {
            return;
        }
        scrollToLastRow();
        getSelectionModel().select(this.getItems().size() - 1);
    }

    /**
     * Mirrors how {@code TableView}/{@code ListView} page up/down: the whole item sequence is read in row
     * order (left to right within a row, then the start of the next row), so "the current page" is every
     * item whose row is currently, fully visible, and its end is the last item of the last fully visible row.
     * If the selection isn't already sitting there, this lands on it without scrolling (it's already
     * visible); only once the selection is already at that exact spot does this scroll to a genuinely new,
     * non-overlapping page (starting right after the old last fully visible row, not repeating it) and select
     * that new page's last item.
     */
    @Override
    void selectPageDown() {
        var firstFullyVisibleRow = firstFullyVisibleRowIndex();
        var lastFullyVisibleRow = lastFullyVisibleRowIndex();
        if (lastFullyVisibleRow < 0) {
            return;
        }
        var target = lastItemIndexInRow(lastFullyVisibleRow);
        if (getSelectionModel().getSelectedIndex() == target) {
            var nextRow = lastFullyVisibleRow + 1;
            if (nextRow < getRowCount()) {
                var pageHeight = lastFullyVisibleRow - firstFullyVisibleRow + 1;
                // The new page's last row is computed from the still-current (stable) page's own height, not
                // re-derived from geometry after scrolling: near the end of the items, a row with nothing
                // after it can be reported as fully visible by VirtualFlow even when it doesn't actually fit
                // the viewport height (nothing left to virtualize away), which would otherwise silently skip
                // or misjudge the next page's boundary.
                var newLastRow = Math.min(getRowCount() - 1, nextRow + pageHeight - 1);
                forceScrollToFirstRow(nextRow);
                target = lastItemIndexInRow(newLastRow);
            }
        }
        getSelectionModel().select(target);
    }

    /**
     * Mirrors {@link #selectPageDown} in the opposite direction: "the current page" starts at the first item
     * of the first fully visible row. If the selection isn't already there, this lands on it without
     * scrolling; only once already there does this scroll to a genuinely new, non-overlapping page (ending
     * right before the old first fully visible row, not repeating it) and select that new page's first item.
     */
    @Override
    void selectPageUp() {
        var firstFullyVisibleRow = firstFullyVisibleRowIndex();
        var lastFullyVisibleRow = lastFullyVisibleRowIndex();
        if (firstFullyVisibleRow < 0) {
            return;
        }
        var target = firstFullyVisibleRow * getColumnCount();
        if (getSelectionModel().getSelectedIndex() == target && firstFullyVisibleRow > 0) {
            var pageHeight = lastFullyVisibleRow - firstFullyVisibleRow + 1;
            var newFirstRow = Math.max(0, firstFullyVisibleRow - pageHeight);
            // newFirstRow (not a fresh firstFullyVisibleRowIndex() read after scrolling) is used directly as
            // the target - see selectPageDown for why re-deriving from post-scroll geometry is unreliable
            // near a data boundary (here, the very first row).
            forceScrollToFirstRow(newFirstRow);
            target = newFirstRow * getColumnCount();
        }
        getSelectionModel().select(target);
    }

    /**
     * Like {@link #scrollToFirstRow(int)}, but corrects for a real {@code VirtualFlow} behavior: scrolling a
     * row near the end of the data to the viewport's leading edge can get pulled back to an earlier row so
     * the viewport doesn't end up showing blank space past the last real row. If a pull-back is detected,
     * this nudges the flow forward by the exact combined height of the skipped rows (all the same,
     * fixed {@link #rowHeight}) to force the intended row to genuinely be first, blank space and all.
     */
    private void forceScrollToFirstRow(int rowIndex) {
        scrollToFirstRow(rowIndex);
        applyCss();
        layout();
        var first = this.virtualFlow.getFirstVisibleCell();
        var actualFirst = first == null ? rowIndex : first.getIndex();
        if (actualFirst < rowIndex) {
            this.virtualFlow.scrollPixels((rowIndex - actualFirst) * this.rowHeight.get());
            applyCss();
            layout();
        }
    }

    private int firstFullyVisibleRowIndex() {
        var first = this.virtualFlow.getFirstVisibleCell();
        if (first == null) {
            return -1;
        }
        var index = first.getIndex();
        // getFirstVisibleCell() can itself be only partially visible at the leading edge (e.g. right after
        // the user drags the vertical scrollbar to an arbitrary, row-boundary-unaligned position) - the next
        // row is the first genuinely fully visible one. Checked against the cell's own rendered bounds
        // (translated into this view's coordinate space) rather than ColumnViewUtils.isFullyVisible: that
        // helper derives visibility from VirtualFlow's own position/index bookkeeping, which can be thrown
        // off after a manual, unaligned scroll - reading the already-laid-out geometry directly cannot be
        // wrong the way an estimate can.
        return isRowFullyVisible(first) ? index : index + 1;
    }

    private int lastFullyVisibleRowIndex() {
        var last = this.virtualFlow.getLastVisibleCell();
        if (last == null) {
            return -1;
        }
        var index = last.getIndex();
        return isRowFullyVisible(last) ? index : index - 1;
    }

    private boolean isRowFullyVisible(IndexedCell<?> row) {
        var bounds = sceneToLocal(row.localToScene(row.getBoundsInLocal()));
        return bounds != null && bounds.getMinY() >= -0.5 && bounds.getMaxY() <= getHeight() + 0.5;
    }

    private int lastItemIndexInRow(int rowIndex) {
        return rowIndex * getColumnCount() + resolveColumnCount(rowIndex) - 1;
    }

    private void savePositionAndRefreshView(RefreshTrigger refreshTrigger) {
        setFirstVisibleCellIndex(resolveFirstVisibleCellIndex());
        refresh(refreshTrigger, RefreshType.PRIMARY);
        setFirstVisibleCellIndex(0);
    }

    /**
     * Re-applies the current cell width to every currently live row's cells. Deliberately independent of
     * {@link #refresh(RefreshTrigger, RefreshType)} &mdash; a width change is a pure view-level resize, not a
     * data change (see {@link ColumnListView#updateColumnWidths()} for the equivalent reasoning there).
     */
    private void updateCellWidths() {
        var iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            var ref = iterator.next();
            var row = ref.get();
            if (row == null) {
                iterator.remove();
            } else {
                row.applyCellWidths();
            }
        }
    }


    /**
     * This method is called when view or data has been changed.
     *
     * @param refreshTrigger
     */
    private void refresh(RefreshTrigger refreshTrigger, RefreshType type) {
        logger.debug("Refresh request, trigger: {}, type: {}", refreshTrigger, type);
        if (this.getItems() == null) {
            return;
        }
        if (getWidth() < 0.1) {
            return;
        }
        if (rowHeight.get() < 0) {
            prepareRowHeightResolving();
            return;
        }
        var iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            var ref = iterator.next();
            var row = ref.get();
            if (row == null) {
                iterator.remove();
            } else {
                row.markDirty();
            }
        }

        if (type == RefreshType.PRIMARY && getCurrentType() != null) {
            secondRefreshTrigger = refreshTrigger; // there can be multiple attempt, so the last one is saved
            logger.debug("Refresh request saved and postponed, trigger: {}, type: {}", refreshTrigger, type);
            return;
        }
        try {
            setCurrentType(type);
            var newRowCount = (int) Math.ceil((double) this.getItems().size() / getColumnCount());
            if (refreshTrigger == RefreshTrigger.ITEMS) {
                if (getSelectionModel().getSelectedIndex() != -1) {
                    getSelectionModel().clearSelection();
                }
                setEditingCellIndex(-1);
                updateOffsets(newRowCount);
                scrollToFirstRow(getFirstVisibleCellIndex());
            } else {
                if (getRowCount() != newRowCount) {
                    updateOffsets(newRowCount);
                    scrollToFirstRow(getFirstVisibleCellIndex());
                }
            }
            logger.debug("Refreshed, trigger: {}, type: {}, itemsCount: {}", refreshTrigger, type, getItems().size());
            if (type == RefreshType.PRIMARY && secondRefreshTrigger != null) {
                refresh(secondRefreshTrigger, RefreshType.SECONDARY);
                secondRefreshTrigger = null;
            }
        } finally {
            if (type == RefreshType.SECONDARY) {
                setCurrentType(RefreshType.PRIMARY);
            } else {
                setCurrentType(null);
            }
        }
    }

    private void prepareRowHeightResolving() {
        setRowCount(1);
        this.getOffsets().setAll(List.of(0));
    }

    private void updateOffsets(int rowCount) {
        setRowCount(rowCount);
        List<Integer> offs = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            offs.add(i * getColumnCount());
        }
        this.getOffsets().clear();
        this.getOffsets().addAll(offs);
        for (var r : virtualFlow.getCells()) {
            r.requestLayout();
        }
        virtualFlow.setCellCount(this.getOffsets().size());
    }
}
