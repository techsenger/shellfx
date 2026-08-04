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
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listView requires calling {@link ColumnListView#onResizeStarted()} and
 * {@link ColumnListView#onResizeFinished()} on resizing. See DialogResizeEvent and StageResizeEvent.
 *
 * <p>Row height is calculated creating one empty cell in one column before creating cells for items.
 *
 * <p>The cell selection occurs in two stages: 1_ Marking the cell as selected and saving a reference to
 * it in the listView. 2) Updating the selectionModel. The only exception is changeListener for selectionModel.
 *
 * <p>{@link #columnWidth} and {@link #visibleColumnCount} are two mutually exclusive ways to size columns -
 * setting one is meant to be used instead of the other, not alongside it (if both are set, the widths
 * derived from {@link #visibleColumnCount} win; see that property's Javadoc for why). {@code columnWidth}
 * picks a fixed pixel width and lets however many columns fit in the current width show (the count varies
 * with the view's width). {@code visibleColumnCount} does the opposite: it picks a fixed count of columns to
 * always show fully, and derives each column's width from the view's current width so that any
 * {@code visibleColumnCount} consecutive columns - wherever the view happens to be scrolled to - sum to
 * exactly that width, with no column ever showing partially.
 *
 * @author Pavel Castornii
 */
public class ColumnListView<T> extends AbstractColumnView<T> {

    /**
     * Named reasons {@code layoutChildren()} can have work to do, recorded into {@link #pendingTriggers} by
     * the specific listener/call site that noticed each one and logged (at debug level) so a layout pass that
     * turns out to do nothing observable can still be told apart from one genuinely driven by one of these -
     * see {@link #layoutChildren()}.
     */
    enum RequestLayoutTrigger {

        ITEMS,

        ROW_HEIGHT,

        SCROLL_BAR_HEIGHT,

        SCROLL_BAR_VISIBILITY,

        VIRTUAL_FLOW_HEIGHT
    }

    private static class ColumnListViewColumn<T> extends IndexedCell<Integer>  {

        private static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");

        private final VBox node = new VBox();

        private final ColumnListView<T> listView;

        private final List<ColumnListCell<T>> cachedCells = new ArrayList<>();

        private ColumnListCell<T> selectedCell;

        private boolean dirty = true;

        ColumnListViewColumn(final ColumnListView<T> listView) {
            this.listView = listView;
            node.getStyleClass().add("column");
            node.setAlignment(Pos.TOP_LEFT);
            setGraphic(node);
        }

        /**
         * {@code item} here is this column's own starting offset into {@link #getItems()} (see the class
         * javadoc), not one of the actual items it displays &mdash; so the guard below is an <em>index</em>
         * equality check ("this column already shows this same slice of the list, and nothing has marked it
         * {@link #dirty}"), not a content check. It is exactly what makes normal recycling cheap: a column
         * whose offset genuinely hasn't changed skips rebuilding its {@link #cachedCells} entirely.
         *
         * <p>The flip side: if one of those already-cached items mutates a field in place (with no change to
         * the item list/order itself, so the offset stays the same), this guard has no way to notice and
         * will keep skipping, leaving the mutation unrendered indefinitely. There is no by-item-content path
         * around this that reaches an off-screen, already-cached column, other than forcing it &mdash; see
         * {@link ColumnViewUtils#updateCell(ColumnListView, int)}/{@link ColumnViewUtils#updateCells(ColumnListView,
         * boolean) updateCells(..., false)}.
         */
        @Override
        public void updateItem(Integer item, boolean empty) {
            applyColumnWidth();
            if (item != null && item.equals(getItem()) && !empty && !dirty) {
                return;
            }
            super.updateItem(item, empty);
            node.pseudoClassStateChanged(EMPTY, empty);
            // A cell about to be discarded here (e.g. a column recycled for a different offset after items
            // shrank) can currently own scene focus. Removing a focused node from the scene graph does not
            // reassign focus - Scene.getFocusOwner() is left pointing at a now-detached node, so no further
            // key events (e.g. arrow-key navigation) are dispatched anywhere until something explicitly
            // requests focus again. Move focus to the still-live container first.
            if (node.isFocusWithin()) {
                listView.requestFocus();
            }
            node.getChildren().clear();
            clearSelection();
            if (item != null) {
                if (listView.rowHeight > 0) {
                    updateCells();
                    dirty = false;
                } else {
                    addRowHeightCell();
                }
            }
        }

        protected ColumnListView<T> getListView() {
            return listView;
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new CellSkinBase<>(this);
        }

        /**
         * Applies {@link ColumnListView#columnWidth}/{@link ColumnListView#visibleColumnCount} to this
         * column's node, if either is set (a negative resolved width leaves width entirely to CSS, as before
         * this feature existed). Resolves the width from {@link #getIndex()} - this column's position in the
         * outer flow - not {@link #getItem()}: {@code getIndex()} is what a filler column (past the last real
         * one, created by the flow itself to keep covering the viewport - see
         * {@link ColumnListView#resolveColumnWidth}) has, even though it has no item/offset at all, and it is
         * already up to date by the time this runs (unlike {@code getItem()}, which is only refreshed by
         * {@code super.updateItem(...)}, called after this, so it would still read the previous, stale value
         * while this column is being recycled for a different one).
         */
        private void applyColumnWidth() {
            var width = listView.resolveColumnWidth(getIndex());
            if (width < 0) {
                return;
            }
            node.setMinWidth(width);
            node.setPrefWidth(width);
            node.setMaxWidth(width);
            // column-list-view.css gives every individual .cell its own, independent width (min 15em, max
            // 100000, pref = content size) - column width alone doesn't constrain a cell with long content,
            // it must be set on the cell itself too.
            for (var cell : cachedCells) {
                applyCellWidth(cell, width);
            }
        }

        private void applyCellWidth(ColumnListCell<T> cell, double width) {
            cell.setMinWidth(width);
            cell.setPrefWidth(width);
            cell.setMaxWidth(width);
        }

        private VBox getNode() {
            return this.node;
        }

        private void updateCells() {
            var item = getItem();
            node.getStyleClass().removeAll("first", "last");
            if (item == 0) {
                node.getStyleClass().add("first");
            } else if (item == (this.listView.getColumnCount() - 1) * this.listView.getRowCount()) {
                node.getStyleClass().add("last");
            }
            var items = this.listView.getItems();
            // item (this column's own offset) can be momentarily stale relative to a just-shrunk items list -
            // e.g. items shrinking requests an ITEMS-triggered layout pass, but this column's own updateItem
            // can still run once more (with its old offset) before that pass rebuilds offsets. Clamp instead
            // of crashing; an empty column here self-heals on the next layout pass.
            var startIndex = Math.min(item, items.size());
            int endIndex = Math.min(startIndex + this.listView.getRowCount(), items.size());
            var cellItems = items.subList(startIndex, endIndex);
            var absentCells = cellItems.size() - this.cachedCells.size();
            for (var i = 0; i < absentCells; i++) {
                createCell();
            }
            //updating cells and adding them to node
            for (var i = 0; i < cellItems.size(); i++) {
                var cellItem = cellItems.get(i);
                ColumnListCell cell = cachedCells.get(i);
                cell.updateItem((Object) cellItem, false);
                var cellIndex = item + i;
                //index must be updated only after setting non empty, see Cell#updateSelected(boolean selected).
                cell.updateIndex(cellIndex);
                if (cellIndex == listView.getSelectionModel().getSelectedIndex() && cellIndex != -1) {
                    setSelectedCell(cell);
                }
                if (!cell.isEditing() && cell.isEditable() && cell.getIndex() == this.listView.getEditingCellIndex()) {
                    cell.startEdit();
                }
                this.node.getChildren().add(cell);
            }
            if (cellItems.size() < this.cachedCells.size()) {
                for (var i = cellItems.size(); i < this.cachedCells.size(); i++) {
                    ColumnListCell cell = cachedCells.get(i);
                    cell.updateItem(null, true);
                    var cellIndex = item + i;
                    cell.updateIndex(cellIndex);
                }
            }
        }

        private void createCell() {
            var cell = this.listView.getCellFactory().call((ColumnListView) this.listView);
            cell.setListView((ColumnListView) this.listView);
            var width = listView.getColumnWidth();
            if (width >= 0) {
                applyCellWidth(cell, width);
            }
            this.cachedCells.add(cell);
        }

        private void setSelectedCell(ColumnListCell<T> cell) {
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
         * Empty cell is used to calculate row height. Measured synchronously - the cell is CSS-applied and
         * autosized right here (it already has a live {@code Scene} through {@link #node}, so theme lookups
         * resolve correctly) instead of waiting for an actual layout pass and a {@code heightProperty()}
         * notification, so no pulse boundary needs to be crossed to know the result.
         */
        private void addRowHeightCell() {
            createCell();
            ColumnListCell<T> cell = cachedCells.get(0);
            this.node.getChildren().add(cell);
            cell.applyCss();
            cell.autosize();
            // Rounded up to match how this VBox column itself snaps each real child's height once actually
            // laid out (pixel-snapping rounds a fractional pref height like 25.2 up to 26.0) - using the raw,
            // unsnapped value here would under-measure rowHeight, so rowCount ends up one row too many and the
            // column's real rendered height (rowCount * actual per-row height) overflows past the computed
            // viewport height straight into the horizontal scrollbar.
            var height = Math.ceil(cell.getHeight());
            if (height > 1) {
                listView.rowHeight = height;
                listView.requestLayout(RequestLayoutTrigger.ROW_HEIGHT);
            }
        }

        private void markDirty() {
            this.dirty = true;
        }
    }

    private static class ColumnVirtualFlow<T> extends VirtualFlow<ColumnListViewColumn<T>> {

        ColumnVirtualFlow() {
            var vBar = getVbar();
            vBar.setMinWidth(0);
            vBar.setPrefWidth(0);
            vBar.setMaxWidth(0);
            vBar.setOpacity(0);
            setVertical(false);
        }

        ScrollBar getHBar() {
            return super.getHbar();
        }

        /**
         * Returns the height of the virtual flow without the height of the horizontal scroll bar. Reads the
         * bar's own current, live height/visibility directly - not a value cached from a previous
         * {@code heightProperty()} notification - so this is accurate immediately after this flow's own
         * {@code layoutChildren()} has run (which is what actually decides/sizes the bar), with no lag waiting
         * for that change to separately propagate through a listener.
         */
        double getViewportHeight() {
            return getHBar().isVisible() ? getHeight() - getHBar().getHeight() : getHeight();
        }

        @Override
        protected List<ColumnListViewColumn<T>> getCells() {
            return super.getCells();
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(ColumnListView.class);

    /**
     * Always fixed height of the row, resolved once (synchronously, from a throwaway probe cell - see
     * {@link ColumnListViewColumn#addRowHeightCell()}) and never reset afterward; {@code -1} means "not yet
     * resolved". Not an observable property: nothing outside {@link ColumnListViewColumn#addRowHeightCell()}
     * needs to react to it changing, since that method itself calls {@link #requestLayout()} right after
     * setting it.
     */
    private double rowHeight = -1;

    /**
     * Explicit, API-set width (in pixels) applied to every column, overriding whatever {@code -fx-min-width}/
     * {@code -fx-pref-width}/{@code -fx-max-width} CSS would otherwise apply to the {@code .column} node. A
     * negative value (the default) means "not set" &mdash; column width is left entirely to CSS, as before this
     * property existed. Ignored while {@link #visibleColumnCount} is set (see there) &mdash; the two are
     * mutually exclusive ways to size columns.
     *
     * <p>This is a pure view-level concern: changing it never touches {@link #items}, {@link #offsets}, or
     * {@link #rowCount}/{@link #columnCount} &mdash; it only re-applies the new width to already-materialized
     * column cells, via {@link #updateColumnWidths()}, independent of {@code layoutChildren()}'s own geometry
     * recompute.
     */
    private final DoubleProperty columnWidth = new SimpleDoubleProperty();

    /**
     * The fixed number of columns that should always be fully, simultaneously visible, mutually exclusive
     * with {@link #columnWidth} (see the class Javadoc). A value &lt;= 0 (the default) means "not set" &mdash;
     * {@link #columnWidth}/CSS decide column width instead, and however many columns happen to fit at that
     * width are shown, including a partial one at the trailing edge.
     *
     * <p>When set to N &gt; 0, every column's width is derived from the view's own current width W as
     * {@code base + (columnIndex % N < remainder ? 1 : 0)}, where {@code base = floor(W / N)} and
     * {@code remainder = W - base * N}. This - not a single shared width like {@code columnWidth} - is what
     * guarantees that any N <em>consecutive</em> columns, wherever the view is scrolled to, sum to exactly W:
     * N consecutive integers always cover every residue mod N exactly once, so exactly {@code remainder} of
     * them fall in the "+1" group regardless of which N-column window is currently showing. A single shared
     * width cannot do this whenever W does not divide evenly by N, since {@code floor(W / N) * N < W} then,
     * always leaving a leftover sliver of an extra column peeking in at one edge.
     *
     * <p>Recomputed (via {@link #updateColumnWidths()}) whenever this property or the view's own width
     * changes, so it stays correct across resizes without the caller needing to recompute anything.
     */
    private final IntegerProperty visibleColumnCount = new SimpleIntegerProperty();

    private final ObjectProperty<Callback<ColumnListView<T>, ColumnListCell<T>>> cellFactory
            = new SimpleObjectProperty();

    private final ReadOnlyIntegerWrapper columnCount = new ReadOnlyIntegerWrapper();

    /**
     * This class uses JavaFX VirtualFlow instead of Flowless VirtualFlow because, in Flowless VirtualFlow, slow
     * scrolling of cells with different widths (for a horizontal VirtualFlow) stutters slightly. This happens because
     * VirtualFlow does not know the total width of its contents until everything has been rendered, so it uses an
     * estimated width that it continuously updates as more items are displayed. When this estimated width is updated,
     * the scrollbar also adjusts accordingly by shifting the thumb and resizing it if necessary.
     *
     * Working with JavaFX VirtualFlow it is important to remember, that calling cell.updateSelected(boolean) can
     * invoke cell.requestLayout() if selected property changes.
     */
    private final ColumnVirtualFlow<T> virtualFlow = new ColumnVirtualFlow<>();

    /**
     * Reasons pending for the next {@code layoutChildren()} pass, set by the specific listener/call site that
     * noticed each one (see {@link #requestLayout(RequestLayoutTrigger)}) and consumed/cleared there - see
     * {@link #layoutChildren()} for how {@link RequestLayoutTrigger#ITEMS} in particular changes its behavior
     * (clearing selection/editing state, unconditionally recomputing offsets) versus every other trigger
     * (which only recomputes when the resolved row count actually differs).
     */
    private final EnumSet<RequestLayoutTrigger> pendingTriggers = EnumSet.noneOf(RequestLayoutTrigger.class);

    private final List<WeakReference<ColumnListViewColumn<?>>> columns = new LinkedList<>();

    public ColumnListView() {
        getStylesheets().add(ColumnListView.class.getResource("column-list-view.css").toExternalForm());
        getStyleClass().add("column-list-view");
        //default cell factory
        setCellFactory(v -> new ColumnListCell<>());
        getChildren().add(this.virtualFlow);
        this.columnWidth.set(-1);
        this.columnWidth.addListener((ov, oldV, newV) -> updateColumnWidths());
        this.visibleColumnCount.set(-1);
        this.visibleColumnCount.addListener((ov, oldV, newV) -> updateColumnWidths());
        this.widthProperty().addListener((ov, oldV, newV) -> {
            if (this.visibleColumnCount.get() > 0) {
                updateColumnWidths();
            }
        });
        this.virtualFlow.getHBar().heightProperty().addListener((ov, oldV, newV) ->
                requestLayout(RequestLayoutTrigger.SCROLL_BAR_HEIGHT));
        this.virtualFlow.getHBar().visibleProperty().addListener((ov, oldV, newV) ->
                requestLayout(RequestLayoutTrigger.SCROLL_BAR_VISIBILITY));
        //firstVisibleCellIndex is set via onResizeStarted.
        this.virtualFlow.heightProperty().addListener((ov, oldV, newV) -> {
            pendingTriggers.add(RequestLayoutTrigger.VIRTUAL_FLOW_HEIGHT);
            logger.debug("Requesting layout, trigger: {}", RequestLayoutTrigger.VIRTUAL_FLOW_HEIGHT);
            requestLayoutPreservingPosition();
        });
        virtualFlow.setCellFactory(vf -> new ColumnListViewColumn<>(this) {

            {
                columns.add(new WeakReference<>(this));
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

    public Callback<ColumnListView<T>, ColumnListCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public void setCellFactory(Callback<ColumnListView<T>, ColumnListCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public ReadOnlyIntegerProperty columnCountProperty() {
        return columnCount.getReadOnlyProperty();
    }

    public int getColumnCount() {
        return columnCount.get();
    }

    /**
     * The explicit, API-set column width, in pixels &mdash; see {@link #columnWidth}.
     */
    public DoubleProperty columnWidthProperty() {
        return columnWidth;
    }

    public double getColumnWidth() {
        return columnWidth.get();
    }

    /**
     * Sets every column's width to {@code columnWidth} pixels, overriding CSS. Pass a negative value to go
     * back to CSS-driven width.
     */
    public void setColumnWidth(double columnWidth) {
        this.columnWidth.set(columnWidth);
    }

    /**
     * The fixed number of always-fully-visible columns, in pixels &mdash; see {@link #visibleColumnCount}.
     */
    public IntegerProperty visibleColumnCountProperty() {
        return visibleColumnCount;
    }

    public int getVisibleColumnCount() {
        return visibleColumnCount.get();
    }

    /**
     * Sizes columns so that exactly {@code visibleColumnCount} of them are always fully visible at once,
     * overriding {@link #columnWidth}/CSS. Pass a value &lt;= 0 to go back to {@code columnWidth}/CSS-driven
     * width instead - see {@link #visibleColumnCount} for the sizing formula and why it matters.
     */
    public void setVisibleColumnCount(int visibleColumnCount) {
        this.visibleColumnCount.set(visibleColumnCount);
    }

    /**
     * This method is called when manual refresh is enabled.
     */
    public void refresh() {
        refreshItems();
        resolveGeometry();
    }

    /**
     * Returns the column index or -1.
     *
     * @param cellIndex
     * @return
     */
    public int resolveColumnIndex(int cellIndex) {
        if (getRowCount() == 0) {
            return -1;
        }
        var columnIndex = (int) cellIndex / getRowCount();
        return columnIndex;
    }

    public int resolveRowIndex(int cellIndex) {
        if (getRowCount() == 0) {
            return -1;
        }
        var rowIndex = cellIndex % getRowCount();
        return rowIndex;
    }

    /**
     * Returns the count of rows in concrete column - the first and the last column can have less then rowCount rows.
     *
     * @param columnIndex
     * @return
     */
    public int resolveRowCount(int columnIndex) {
        int totalItems = this.getItems().size();
        int rowCount = getRowCount();
        int columnCount = this.getOffsets().size();

        int fullColumns = totalItems / rowCount;
        int remaining = totalItems % rowCount;

        if (columnIndex < fullColumns) {
            return rowCount;
        } else if (columnIndex == fullColumns && remaining > 0) {
            return remaining;
        }
        return 0;
    }

    /**
     * Scrolls the the first column.
     */
    public void scrollToFirstColumn() {
        this.virtualFlow.scrollTo(0);
    }

    /**
     * Scrolls to the last column.
     */
    public void scrollToLastColumn() {
        this.virtualFlow.scrollPixels(Integer.MAX_VALUE);
    }

    /**
     * Scrolls to N column and shows it as the first one.
     *
     * @param columnIndex
     */
    public void scrollToFirstColumn(int columnIndex) {
        this.virtualFlow.scrollToTop(columnIndex);
    }

    /**
     * Scrolls to N column and shows it as the last one.
     *
     * @param columnIndex
     */
    public void scrollToLastColumn(int columnIndex) {
        this.virtualFlow.scrollTo(columnIndex);
    }

    public void edit(int cellIndex) {
        if (!isEditable() || getEditingCellIndex() != -1) {
            return;
        }
        setEditingCellIndex(cellIndex);
        var columnIndex = resolveColumnIndex(cellIndex);
        ColumnListViewColumn<T> column = null;
        for (var c : this.virtualFlow.getCells()) {
            if (c.getIndex() == columnIndex) {
                column = c;
                break;
            }
        }
        if (column != null) {
            column.markDirty();
            column.requestLayout();
        }
    }

    /**
     * The single place that recomputes offsets/row count/column count from current items, row height, and
     * viewport size. Every cause that needs a recompute (items changing, the row-height probe cell resolving,
     * the horizontal scrollbar's own height/visibility changing, the virtual flow's own height changing) goes
     * through {@link #requestLayout(RequestLayoutTrigger)} (see the constructor) and lets this - JavaFX's own
     * non-reentrant layout-pass mechanism - pick it up; nothing here can recurse into itself the way a
     * hand-rolled "postpone and retry" guard would have to defend against. {@link #pendingTriggers} is read
     * and cleared unconditionally up front (not just when non-empty) so a pass with no pending trigger at all -
     * i.e. one caused by something outside this class entirely, such as the virtual flow's own internal
     * layout churn - is clearly visible as such in the log, rather than silently looking identical to one of
     * ours.
     */
    @Override
    protected void layoutChildren() {
        if (getItems() != null && getHeight() >= 0.1) {
            if (rowHeight < 0) {
                prepareRowHeightResolving();
                virtualFlow.resizeRelocate(0, 0, getWidth(), getHeight());
                virtualFlow.applyCss();
                virtualFlow.layout();
            }
            if (rowHeight >= 0) {
                var triggers = EnumSet.copyOf(pendingTriggers);
                pendingTriggers.clear();
                var newRowCount = (int) (this.virtualFlow.getViewportHeight() / rowHeight);
                logger.trace("layoutChildren: triggers={}, currentRowCount={}, newRowCount={}, "
                        + "viewportHeight={}, rowHeight={}, flowPosition={}, flowWidth={}, flowHeight={}",
                        triggers, getRowCount(), newRowCount, this.virtualFlow.getViewportHeight(), rowHeight,
                        this.virtualFlow.getPosition(), this.virtualFlow.getWidth(), this.virtualFlow.getHeight());
                var itemsTriggered = triggers.contains(RequestLayoutTrigger.ITEMS);
                if (newRowCount > 0 && (itemsTriggered || getRowCount() != newRowCount)) {
                    // Only marked dirty when a genuine geometry change is about to be applied - not on every
                    // layoutChildren() pass (this now runs far more often than the old, separately-triggered
                    // refresh() ever did, since any descendant's requestLayout() - e.g. edit()'s own, on just
                    // one column - bubbles up here too). Sweeping unconditionally would force every column to
                    // rebuild on passes that change nothing, discarding e.g. a cell mid-edit for no reason.
                    var iterator = this.columns.iterator();
                    while (iterator.hasNext()) {
                        var ref = iterator.next();
                        var column = ref.get();
                        if (column == null) {
                            iterator.remove();
                        } else {
                            column.markDirty();
                        }
                    }
                    updateOffsets(newRowCount);
                    scrollToFirstColumn(getFirstVisibleCellIndex());
                }
                consumeFirstVisibleCellIndexReset();
            }
        }
        virtualFlow.resizeRelocate(0, 0, getWidth(), getHeight());
    }

    @Override
    boolean isContainerOrCellNode(Node node) {
        return node instanceof VBox || node instanceof ColumnListCell;
    }

    @Override
    VirtualFlow<ColumnListViewColumn<T>> getVirtualFlow() {
        return virtualFlow;
    }

    @Override
    void refreshItems() {
        // Cleared here, synchronously, the moment items actually change - not deferred into layoutChildren()'s
        // own handling of the ITEMS trigger, since that can run much later (only on the next real layout
        // pass), by which point a caller may have already set a brand new selection for the new items (e.g.
        // FileTabFxView's setSelectedFile() right after refresh()) - clearing it there would wipe out that
        // fresh selection, not just genuinely stale state left over from before the items changed.
        if (getSelectionModel().getSelectedIndex() != -1) {
            getSelectionModel().clearSelection();
        }
        setEditingCellIndex(-1);
        requestLayout(RequestLayoutTrigger.ITEMS);
    }

    /**
     * Clears the previously selected cell's highlight on every currently realized column and, if
     * {@code selectedIndex} resolves to one of them, sets its new selected cell. Off-screen columns are not
     * touched here - {@link ColumnListViewColumn#updateItem} already recomputes the correct selected cell from
     * scratch whenever such a column is next reused, so there is nothing stale left for them to show once
     * scrolled back into view.
     */
    @Override
    void updateSelectedCellHighlight(int selectedIndex) {
        var columnIndex = selectedIndex == -1 ? -1 : resolveColumnIndex(selectedIndex);
        var rowIndex = selectedIndex == -1 ? -1 : resolveRowIndex(selectedIndex);
        for (var column : this.virtualFlow.getCells()) {
            column.clearSelection();
            if (column.getIndex() == columnIndex && rowIndex < column.cachedCells.size()) {
                column.setSelectedCell(column.cachedCells.get(rowIndex));
            }
        }
    }

    /**
     * This method makes entire cell visible and is called when user clicks mouse, so, the cell exists and visible.
     */
    void scrollToSelected() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var columnIndex = resolveColumnIndex(selectedIndex);

        if (columnIndex == this.virtualFlow.getFirstVisibleCell().getIndex()) {
            scrollToFirstColumn(columnIndex);
        } else if (columnIndex == this.virtualFlow.getLastVisibleCell().getIndex()) {
            scrollToLastColumn(columnIndex);
        }

    }

    /**
     * Returns the currently realized cell showing {@code itemIndex}, or {@code null} if it isn't currently
     * realized (e.g. scrolled far out of view) or {@code itemIndex} is out of range. Used by
     * {@link ColumnViewUtils#updateCell(ColumnListView, int)} to force just that one cell to re-render from
     * its current item, without touching any other cell.
     */
    @Nullable ColumnListCell<T> getCell(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= getItems().size()) {
            return null;
        }
        var columnIndex = resolveColumnIndex(itemIndex);
        var rowIndex = resolveRowIndex(itemIndex);
        return getCell(columnIndex, rowIndex);
    }

    /**
     * Forces cells to re-derive their visual content from their current item, without touching this view's
     * items/offset bookkeeping or scroll position/selection &mdash; the {@link ColumnListView} counterpart of
     * {@code VirtualFlowUtils#updateCells}, reaching directly into each realized column's own cached cells
     * instead of toggling the column's index (which would be the only option available to a generic,
     * outside-the-package utility, since {@link ColumnListViewColumn}'s cached cells are a private
     * implementation detail).
     *
     * @param onlyVisible whether to touch only the currently visible columns (cheap) or every column
     *     (thorough) &mdash; see {@code VirtualFlowUtils#updateCells} for what each means
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
            var column = this.virtualFlow.getCell(index);
            if (column != null && !column.isEmpty()) {
                for (var cell : column.cachedCells) {
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
    void selectUp() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - 1;
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        }
    }

    @Override
    void selectDown() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex + 1;
        if (newSelectedIndex < getItems().size()) {
            selectNext(selectedIndex, newSelectedIndex);
        }
    }

    @Override
    void selectLeft() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - getRowCount();
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        } else {
            // Already in the first column - LEFT has nowhere else to go column-wise, so jump to the very
            // first item overall instead of doing nothing (mirrors HOME).
            selectHome();
        }
    }

    @Override
    void selectRight() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        if (resolveColumnIndex(selectedIndex) >= getColumnCount() - 1) {
            // Already in the last column - RIGHT has nowhere else to go column-wise, so jump to the very
            // last item overall instead of doing nothing (mirrors END).
            selectEnd();
            return;
        }
        var newSelectedIndex = selectedIndex + getRowCount();
        var lastIndex = getItems().size() - 1;
        // The last column may hold fewer than getRowCount() rows, so the same-row target in it can be past
        // the last real item - land on the last item instead of refusing to move right at all.
        selectNext(selectedIndex, Math.min(newSelectedIndex, lastIndex));
    }

    @Override
    void selectHome() {
        if (this.getItems().isEmpty()) {
            return;
        }
        scrollToFirstColumn();
        getSelectionModel().select(0);
    }

    @Override
    void selectEnd() {
        if (this.getItems().isEmpty()) {
            return;
        }
        scrollToLastColumn();
        getSelectionModel().select(this.getItems().size() - 1);
    }

    /**
     * Mirrors how {@code TableView}/{@code ListView} page up/down: the whole item sequence is read in column
     * order (top to bottom within a column, then the top of the next column), so "the current page" is every
     * item whose column is currently, fully visible, and its end is the last item of the last fully visible
     * column. If the selection isn't already sitting there, this lands on it without scrolling (it's already
     * visible); only once the selection is already at that exact spot does this scroll to a genuinely new,
     * non-overlapping page (starting right after the old last fully visible column, not repeating it) and
     * select that new page's last item.
     */
    @Override
    void selectPageDown() {
        var firstFullyVisibleColumn = firstFullyVisibleColumnIndex();
        var lastFullyVisibleColumn = lastFullyVisibleColumnIndex();
        if (lastFullyVisibleColumn < 0) {
            return;
        }
        var target = lastItemIndexInColumn(lastFullyVisibleColumn);
        if (getSelectionModel().getSelectedIndex() == target) {
            var nextColumn = lastFullyVisibleColumn + 1;
            if (nextColumn < getColumnCount()) {
                // With visibleColumnCount set, it - not the current viewport's own fully-visible span - is
                // the page width: right after a manual scrollbar drag, or on the very first page turn from an
                // odd starting position, the current viewport may not yet be page-aligned, so
                // lastFullyVisibleColumn - firstFullyVisibleColumn + 1 can under-count (e.g. report 2 columns
                // even though visibleColumnCount guarantees 3 once aligned), landing the selection in the
                // middle of the new page instead of at its genuine end.
                var pageWidth = getVisibleColumnCount() > 0
                        ? getVisibleColumnCount()
                        : lastFullyVisibleColumn - firstFullyVisibleColumn + 1;
                var newLastColumn = Math.min(getColumnCount() - 1, nextColumn + pageWidth - 1);
                target = lastItemIndexInColumn(newLastColumn);
                forceScrollToFirstColumn(nextColumn);
            }
        }
        getSelectionModel().select(target);
    }

    /**
     * Mirrors {@link #selectPageDown} in the opposite direction: "the current page" starts at the first item
     * of the first fully visible column. If the selection isn't already there, this lands on it without
     * scrolling; only once already there does this scroll to a genuinely new, non-overlapping page ending
     * right before the old first fully visible column, not repeating it) and select that new page's first
     * item.
     */
    @Override
    void selectPageUp() {
        var firstFullyVisibleColumn = firstFullyVisibleColumnIndex();
        var lastFullyVisibleColumn = lastFullyVisibleColumnIndex();
        if (firstFullyVisibleColumn < 0) {
            return;
        }
        var target = firstFullyVisibleColumn * getRowCount();
        if (getSelectionModel().getSelectedIndex() == target && firstFullyVisibleColumn > 0) {
            // See the matching comment in selectPageDown for why visibleColumnCount, when set, is used
            // directly instead of the current viewport's own fully-visible span.
            var pageWidth = getVisibleColumnCount() > 0
                    ? getVisibleColumnCount()
                    : lastFullyVisibleColumn - firstFullyVisibleColumn + 1;
            var newFirstColumn = Math.max(0, firstFullyVisibleColumn - pageWidth);
            // newFirstColumn (not a fresh firstFullyVisibleColumnIndex() read after scrolling) is used
            // directly as the target - see selectPageDown for why re-deriving from post-scroll geometry is
            // unreliable near a data boundary (here, the very first column).
            target = newFirstColumn * getRowCount();
            forceScrollToFirstColumn(newFirstColumn);
        }
        getSelectionModel().select(target);
    }

    private @Nullable ColumnListCell<T> getCell(int columnIndex, int rowIndex) {
        ColumnListViewColumn column = this.virtualFlow.getCell(columnIndex);
        if (column == null || column.isEmpty()) {
            return null;
        }
        var children = column.getNode().getChildren();
        return rowIndex >= 0 && rowIndex < children.size() ? (ColumnListCell<T>) children.get(rowIndex) : null;
    }

    private void selectPrevious(int selectedIndex, int newSelectedIndex) {
        //firstly we provide the column (it can be absent)
        var columnIndex = resolveColumnIndex(selectedIndex);
        var newColumnIndex = resolveColumnIndex(newSelectedIndex);
        var firstVisibleColumnIndex = this.virtualFlow.getFirstVisibleCell().getIndex();
        if (newColumnIndex <= firstVisibleColumnIndex) {
            scrollToFirstColumn(newColumnIndex);
            // Without forcing the scroll to actually be laid out here, select() below fires
            // updateSelectedCellHighlight() while the target column is still the pre-scroll one as far as
            // virtualFlow.getCells() is concerned, so nothing gets marked selected - the selection itself
            // still moves, but no cell highlight shows until something else happens to force a layout.
            applyCss();
            layout();
        }
        getSelectionModel().select(newSelectedIndex);
    }

    private void selectNext(int selectedIndex, int newSelectedIndex) {
        //firstly we provide the column (it can be absent)
        var columnIndex = resolveColumnIndex(selectedIndex);
        var newColumnIndex = resolveColumnIndex(newSelectedIndex);
        var lastVisibleColumnIndex = this.virtualFlow.getLastVisibleCell().getIndex();
        if (newColumnIndex >= lastVisibleColumnIndex) {
            scrollToLastColumn(newColumnIndex);
            // See the identical comment in selectPrevious.
            applyCss();
            layout();
        }
        getSelectionModel().select(newSelectedIndex);
    }

    /**
     * Like {@link #scrollToFirstColumn(int)}, but corrects for a real {@code VirtualFlow} behavior: scrolling
     * a column near the end of the data to the viewport's leading edge can get pulled back to an earlier
     * column so the viewport doesn't end up showing blank space past the last real column - which is exactly
     * what should happen with {@link #visibleColumnCount} unset, but not when it's set, since that feature's
     * whole point is to always show that many columns (backed by filler cells) even past the end of the real
     * data. If a pull-back is detected, this nudges the flow forward by the exact combined width of the
     * skipped columns (computed from {@link #resolveColumnWidth}, which handles filler columns too) to force
     * the intended column to genuinely be first, blank space and all.
     */
    private void forceScrollToFirstColumn(int columnIndex) {
        scrollToFirstColumn(columnIndex);
        applyCss();
        layout();
        var first = this.virtualFlow.getFirstVisibleCell();
        var actualFirst = first == null ? columnIndex : first.getIndex();
        if (actualFirst < columnIndex) {
            var pixelsToNudge = 0.0;
            for (var i = actualFirst; i < columnIndex; i++) {
                pixelsToNudge += resolveColumnWidth(i);
            }
            this.virtualFlow.scrollPixels(pixelsToNudge);
            applyCss();
            layout();
        }
    }

    private int firstFullyVisibleColumnIndex() {
        var first = this.virtualFlow.getFirstVisibleCell();
        if (first == null) {
            return -1;
        }
        var index = first.getIndex();
        // getFirstVisibleCell() can itself be only partially visible at the leading edge (e.g. right after
        // the user drags the horizontal scrollbar to an arbitrary, column-boundary-unaligned position) - the
        // next column is the first genuinely fully visible one. Checked against the cell's own rendered
        // bounds (translated into this view's coordinate space) rather than ColumnViewUtils.isFullyVisible:
        // that helper derives visibility from VirtualFlow's own position/index bookkeeping, which this
        // control's remainder-distributed, not-quite-uniform column widths (see visibleColumnCount) can throw
        // off after a manual, unaligned scroll - reading the already-laid-out geometry directly cannot be
        // wrong the way an estimate can.
        return isColumnFullyVisible(first) ? index : index + 1;
    }

    private int lastFullyVisibleColumnIndex() {
        var last = this.virtualFlow.getLastVisibleCell();
        if (last == null) {
            return -1;
        }
        var index = last.getIndex();
        return isColumnFullyVisible(last) ? index : index - 1;
    }

    private boolean isColumnFullyVisible(IndexedCell<?> column) {
        var bounds = sceneToLocal(column.localToScene(column.getBoundsInLocal()));
        return bounds != null && bounds.getMinX() >= -0.5 && bounds.getMaxX() <= getWidth() + 0.5;
    }

    private int lastItemIndexInColumn(int columnIndex) {
        return columnIndex * getRowCount() + resolveRowCount(columnIndex) - 1;
    }

    private void scrollToCell(int cellIndex) {
        int columnIndex = cellIndex / getRowCount();
        scrollToFirstColumn(columnIndex);
    }

    /**
     * Records {@code trigger} as a reason {@link #layoutChildren()} has work to do and requests a layout pass
     * - the named-trigger counterpart of a plain {@link #requestLayout()} call, used by every listener/call
     * site that wants {@code layoutChildren()} to actually recompute geometry (as opposed to, e.g.
     * {@link #edit(int)}, which only needs one column's own nested layout to run).
     */
    private void requestLayout(RequestLayoutTrigger trigger) {
        pendingTriggers.add(trigger);
        logger.trace("Requesting layout, trigger: {}", trigger);
        requestLayout();
    }

    /**
     * Re-applies {@link #columnWidth}/{@link #visibleColumnCount} to every currently live column cell.
     * Deliberately independent of {@code layoutChildren()}'s own geometry recompute &mdash; a column width
     * change is a pure view-level resize, not a data change, so it must not touch {@link #offsets}/
     * {@link #rowCount}/{@link #columnCount}.
     */
    private void updateColumnWidths() {
        var iterator = this.columns.iterator();
        while (iterator.hasNext()) {
            var ref = iterator.next();
            var column = ref.get();
            if (column == null) {
                iterator.remove();
            } else {
                column.applyColumnWidth();
            }
        }
    }

    /**
     * Resolves the width to apply to the column at {@code columnIndex} - its position in the outer flow, i.e.
     * {@code ColumnListViewColumn#getIndex()}, which a filler column past the last real one (created by the
     * flow itself to keep covering the viewport, with no item/offset of its own at all) still has, so it gets
     * sized exactly like a real column would &mdash; honoring {@link #visibleColumnCount}/{@link #columnWidth}'s
     * mutual exclusivity (see the class Javadoc). Returns a negative value to mean "leave width to CSS",
     * matching {@link #columnWidth}'s own sentinel.
     */
    private double resolveColumnWidth(int columnIndex) {
        var desiredCount = visibleColumnCount.get();
        if (desiredCount <= 0) {
            return columnWidth.get();
        }
        if (columnIndex < 0) {
            return -1;
        }
        var base = Math.floor(getWidth() / desiredCount);
        var remainder = (int) (getWidth() - base * desiredCount);
        return base + (columnIndex % desiredCount < remainder ? 1 : 0);
    }

    /**
     * Sets up the transient, single-cell placeholder geometry ({@link #getRowCount()}{@code  == 1},
     * {@link #getColumnCount()}{@code  == 1}, one offset) used only to get a single real column realized so
     * its probe cell can measure {@link #rowHeight} (see {@link ColumnListViewColumn#addRowHeightCell()});
     * idempotent, so calling it again on a later {@code layoutChildren()} pass (before row height actually
     * resolves) is harmless.
     */
    private void prepareRowHeightResolving() {
        setRowCount(1);
        this.columnCount.set(1);
        this.getOffsets().setAll(List.of(0));
        virtualFlow.setCellCount(1);
    }

    private void updateOffsets(int rowCount) {
        setRowCount(rowCount);
        int columnCount = (int) Math.ceil((double) this.getItems().size() / rowCount);
        logger.debug("updateOffsets: itemsPerColumn={}, columnCount={}, itemsCount={}", rowCount, columnCount,
                this.getItems().size());
        this.columnCount.set(columnCount);
        List<Integer> offs = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            offs.add(i * rowCount);
        }
        this.getOffsets().clear();
        this.getOffsets().addAll(offs);
        for (var c : virtualFlow.getCells()) {
            c.requestLayout();
        }
        virtualFlow.setCellCount(this.getOffsets().size());
    }
}
