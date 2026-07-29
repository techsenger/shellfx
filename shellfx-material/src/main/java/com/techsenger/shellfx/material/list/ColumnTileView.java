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

package com.techsenger.shellfx.material.list;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
public class ColumnTileView<T> extends Region {

    private static final Logger logger = LoggerFactory.getLogger(ColumnTileView.class);

    private static class SingleSelectionModelImpl<T> extends SingleSelectionModel<T> {

        private final ColumnTileView<T> tileView;

        SingleSelectionModelImpl(ColumnTileView<T> tileView) {
            this.tileView = tileView;
        }

        @Override
        protected T getModelItem(int index) {
            if (index >= 0 && index < this.tileView.items.size()) {
                return this.tileView.items.get(index);
            } else {
                return null;
            }
        }

        @Override
        protected int getItemCount() {
            return this.tileView.items.size();
        }
    }

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
                if (!cell.isEditing() && cell.isEditable() && cell.getIndex() == this.tileView.editingCellIndex) {
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

    private enum RefreshType {
        PRIMARY, SECONDARY
    }

    /**
     * Always fixed height of the row, auto-measured the same way as {@link ColumnListView#rowHeight}.
     */
    private final DoubleProperty rowHeight = new SimpleDoubleProperty();

    /**
     * Fixed number of cells per row &mdash; the input this view is built around (see class javadoc).
     */
    private final IntegerProperty columnCount = new SimpleIntegerProperty(1);

    /**
     * If true then data observable list changes are ignored and it is necessary to call refresh() method. The reason
     * is that same list can be used by tables and they can sort items.
     */
    private final BooleanProperty manualRefresh = new SimpleBooleanProperty();

    private final ObservableList<Integer> offsets = FXCollections.observableArrayList();

    private final ObjectProperty<Callback<ColumnTileView<T>, TileCell<T>>> cellFactory
            = new SimpleObjectProperty();

    private final ReadOnlyIntegerWrapper rowCount = new ReadOnlyIntegerWrapper();

    private final SingleSelectionModelImpl<T> selectionModel = new SingleSelectionModelImpl<>(this);

    private final ObjectProperty<ContextMenu> contextMenu = new SimpleObjectProperty<>();

    private final TileVirtualFlow<T> virtualFlow = new TileVirtualFlow<>();

    private final BooleanProperty editable = new SimpleBooleanProperty();

    private ObservableList<T> items;

    private int firstVisibleCellIndex = 0;

    /**
     * Always only one cell can be in edit mode.
     */
    private int editingCellIndex = -1;

    /**
     * See {@link ColumnListView#currentType} for why this guard exists.
     */
    private RefreshType currentType;

    private RefreshTrigger secondRefreshTrigger = null;

    private final ListChangeListener<T> itemsChangeListener = (change) -> {
        if (!isManualRefresh()) {
            refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
        }
    };

    private final List<WeakReference<ColumnTileViewRow<?>>> rows = new LinkedList<>();

    public ColumnTileView() {
        getStylesheets().add(ColumnTileView.class.getResource("column-tile-view.css").toExternalForm());
        getStyleClass().add("column-tile-view");
        //default cell factory
        setCellFactory(v -> new TileCell<>());
        setFocusTraversable(true);
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if ((e.getTarget() instanceof HBox) || (e.getTarget() instanceof TileCell)) {
                requestFocus();
            }
        });
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
        // A single listener here, instead of one per row (as before): a per-row listener on this long-lived
        // view's own selectedIndexProperty would leak every row ever created for the lifetime of the view -
        // the property's listener list holds a strong reference to each row, so none of them could ever be
        // garbage collected even after being discarded/replaced by the virtual flow.
        getSelectionModel().selectedIndexProperty()
                .addListener((ov, oldV, newV) -> updateSelectedCellHighlight(newV.intValue()));
        virtualFlow.setCellFactory(vf -> new ColumnTileViewRow<>(this) {

            {
                rows.add(new WeakReference<>(this));
            }

            @Override
            public void updateIndex(int index) {
                super.updateIndex(index);
                if (index >= 0 && index < offsets.size()) {
                    updateItem(offsets.get(index), false);
                } else {
                    updateItem(null, true);
                }
            }
        });

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                selectUp();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                selectDown();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                selectLeft();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                selectRight();
                event.consume();
            }
        });

        this.contextMenu.addListener((ov, oldV, newV) -> {
            if (newV == null) {
                setOnContextMenuRequested(null);
            } else {
                setOnContextMenuRequested(event -> {
                    newV.show(this, event.getScreenX(), event.getScreenY());
                    event.consume();
                });
            }
        });
    }

    public Callback<ColumnTileView<T>, TileCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public void setCellFactory(Callback<ColumnTileView<T>, TileCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public void setItems(ObservableList<T> items) {
        if (this.items != null) {
            this.items.removeListener(itemsChangeListener);
        }
        this.items = items;
        if (this.items != null) {
            this.items.addListener(itemsChangeListener);
        }
        if (!isManualRefresh()) {
            refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
        }
    }

    public BooleanProperty manualRefreshProperty() {
        return manualRefresh;
    }

    public boolean isManualRefresh() {
        return manualRefresh.get();
    }

    public void setManualRefresh(boolean manualRefresh) {
        this.manualRefresh.set(manualRefresh);
    }

    public ReadOnlyIntegerProperty rowCountProperty() {
        return rowCount.getReadOnlyProperty();
    }

    public int getRowCount() {
        return rowCount.get();
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

    public SingleSelectionModel<T> getSelectionModel() {
        return this.selectionModel;
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
        int totalItems = this.items.size();
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

    public void onResizeStarted() {
        this.firstVisibleCellIndex = resolveFirstVisibleCellIndex();
    }

    public void onResizeFinished() {
        this.firstVisibleCellIndex = 0;
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public boolean isEditable() {
        return editable.get();
    }

    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    public void setContextMenu(ContextMenu menu) {
        this.contextMenu.set(menu);
    }

    public ContextMenu getContextMenu() {
        return this.contextMenu.get();
    }

    public ObjectProperty<ContextMenu> contextMenuProperty() {
        return this.contextMenu;
    }

    public void edit(int cellIndex) {
        if (!isEditable() || this.editingCellIndex != -1) {
            return;
        }
        this.editingCellIndex = cellIndex;
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

    @Override
    protected double computePrefWidth(double height) {
        return virtualFlow.prefWidth(-1);
    }

    @Override
    protected double computePrefHeight(double width) {
        return virtualFlow.prefHeight(-1);
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

    void setEditingCellIndex(int editingCellIndex) {
        this.editingCellIndex = editingCellIndex;
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

    private void selectUp() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - getColumnCount();
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        }
    }

    private void selectDown() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        if (resolveRowIndex(selectedIndex) >= getRowCount() - 1) {
            return;
        }
        var newSelectedIndex = selectedIndex + getColumnCount();
        var lastIndex = getItems().size() - 1;
        // The last row may hold fewer than getColumnCount() items, so the same-column target in it can be
        // past the last real item - land on the last item instead of refusing to move down at all.
        selectNext(selectedIndex, Math.min(newSelectedIndex, lastIndex));
    }

    private void selectLeft() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - 1;
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        }
    }

    private void selectRight() {
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

    private void savePositionAndRefreshView(RefreshTrigger refreshTrigger) {
        this.firstVisibleCellIndex = resolveFirstVisibleCellIndex();
        refresh(refreshTrigger, RefreshType.PRIMARY);
        this.firstVisibleCellIndex = 0;
    }

    private int resolveFirstVisibleCellIndex() {
        var cell = this.virtualFlow.getFirstVisibleCell();
        if (cell == null) {
            return 0;
        } else {
            return cell.getIndex();
        }
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
     * Clears the previously selected cell's highlight on every currently realized row and, if
     * {@code selectedIndex} resolves to one of them, sets its new selected cell. Off-screen rows are not
     * touched here - {@link ColumnTileViewRow#updateItem} already recomputes the correct selected cell from
     * scratch whenever such a row is next reused, so there is nothing stale left for them to show once
     * scrolled back into view.
     */
    private void updateSelectedCellHighlight(int selectedIndex) {
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
     * This method is called when view or data has been changed.
     *
     * @param refreshTrigger
     */
    private void refresh(RefreshTrigger refreshTrigger, RefreshType type) {
        logger.debug("Refresh request, trigger: {}, type: {}", refreshTrigger, type);
        if (this.items == null) {
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

        if (type == RefreshType.PRIMARY && currentType != null) {
            secondRefreshTrigger = refreshTrigger; // there can be multiple attempt, so the last one is saved
            logger.debug("Refresh request saved and postponed, trigger: {}, type: {}", refreshTrigger, type);
            return;
        }
        try {
            currentType = type;
            var newRowCount = (int) Math.ceil((double) this.items.size() / getColumnCount());
            if (refreshTrigger == RefreshTrigger.ITEMS) {
                if (getSelectionModel().getSelectedIndex() != -1) {
                    getSelectionModel().clearSelection();
                }
                this.editingCellIndex = -1;
                updateOffsets(newRowCount);
                scrollToFirstRow(firstVisibleCellIndex);
            } else {
                if (getRowCount() != newRowCount) {
                    updateOffsets(newRowCount);
                    scrollToFirstRow(firstVisibleCellIndex);
                }
            }
            logger.debug("Refreshed, trigger: {}, type: {}, itemsCount: {}", refreshTrigger, type, items.size());
            if (type == RefreshType.PRIMARY && secondRefreshTrigger != null) {
                refresh(secondRefreshTrigger, RefreshType.SECONDARY);
                secondRefreshTrigger = null;
            }
        } finally {
            if (type == RefreshType.SECONDARY) {
                currentType = RefreshType.PRIMARY;
            } else {
                currentType = null;
            }
        }
    }

    private void prepareRowHeightResolving() {
        this.rowCount.set(1);
        this.offsets.setAll(List.of(0));
    }

    private void updateOffsets(int rowCount) {
        this.rowCount.set(rowCount);
        List<Integer> offs = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            offs.add(i * getColumnCount());
        }
        this.offsets.clear();
        this.offsets.addAll(offs);
        for (var r : virtualFlow.getCells()) {
            r.requestLayout();
        }
        virtualFlow.setCellCount(this.offsets.size());
    }
}
