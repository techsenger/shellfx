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
import com.techsenger.toolkit.fx.utils.ScrollPosition;
import com.techsenger.toolkit.fx.utils.VirtualFlowUtils;

/**
 * Item-index-based utilities for {@link ColumnListView} and {@link ColumnTileView}, mirroring what
 * {@code TableUtils}/{@code ListViewUtils} (toolkit-fx) offer for {@code TableView}/{@code ListView}.
 *
 * <p>Both controls lay their items out across a discrete number of columns/rows, and each is itself a
 * {@code VirtualFlow} whose own cells are, respectively, whole columns ({@link ColumnListView}, scrolling
 * horizontally) or whole rows ({@link ColumnTileView}, scrolling vertically) &mdash; not individual items. So
 * every method here first translates the given item index into that outer flow's own column/row index (via
 * {@link ColumnListView#resolveColumnIndex}/{@link ColumnTileView#resolveRowIndex}).
 *
 * <p>The scroll/visibility methods then delegate to {@link VirtualFlowUtils}, which already knows how to
 * scroll either orientation of flow — neither control needs any change to support this, since both already
 * expose a standard {@code .virtual-flow} node that {@link VirtualFlowUtils} can look up on its own. The
 * {@code updateCell}/{@code updateCells} methods deliberately do <em>not</em> delegate to
 * {@link VirtualFlowUtils}, though: that utility can only ever reach a whole column/row (the flow's own
 * cell), not the individual item cell inside it, since those are a private implementation detail of
 * {@link ColumnListView}/{@link ColumnTileView} themselves — see each method's own javadoc.
 *
 * @author Pavel Castornii
 */
public final class ColumnViewUtils {

    /**
     * Returns whether the column holding {@code itemIndex} is currently, fully visible in {@code listView}'s
     * viewport.
     *
     * @param listView  the list view to check
     * @param itemIndex the item index to check
     * @return {@code true} if the item's column is fully visible, {@code false} otherwise
     */
    public static boolean isFullyVisible(ColumnListView<?> listView, int itemIndex) {
        var columnIndex = listView.resolveColumnIndex(itemIndex);
        return columnIndex >= 0 && VirtualFlowUtils.isFullyVisible(listView, columnIndex, true);
    }

    /**
     * Scrolls {@code listView} so the column holding {@code itemIndex} lands at {@code position} within the
     * viewport, regardless of whether it is already visible. Use {@link #scrollToIfNeeded} instead if the
     * point is only to guarantee visibility without disturbing an already-fine scroll position.
     *
     * @param listView  the list view to scroll
     * @param itemIndex the item index to scroll to
     * @param position  where the item's column should end up in the viewport
     */
    public static void scrollTo(ColumnListView<?> listView, int itemIndex, ScrollPosition position) {
        var columnIndex = listView.resolveColumnIndex(itemIndex);
        if (columnIndex >= 0) {
            VirtualFlowUtils.scrollTo(listView, columnIndex, position, true);
        }
    }

    /**
     * Scrolls {@code listView} only when the column holding {@code itemIndex} is not already fully visible;
     * an already-visible item is left untouched. When a scroll is needed, the column ends up at
     * {@code position} within the viewport — see {@link #scrollTo}.
     *
     * @param listView  the list view to scroll
     * @param itemIndex the item index that should be visible
     * @param position  where the item's column should end up in the viewport if it needs to be scrolled to
     */
    public static void scrollToIfNeeded(ColumnListView<?> listView, int itemIndex, ScrollPosition position) {
        var columnIndex = listView.resolveColumnIndex(itemIndex);
        if (columnIndex >= 0) {
            VirtualFlowUtils.scrollToIfNeeded(listView, columnIndex, position, true);
        }
    }

    /**
     * Returns whether the row holding {@code itemIndex} is currently, fully visible in {@code tileView}'s
     * viewport.
     *
     * @param tileView  the tile view to check
     * @param itemIndex the item index to check
     * @return {@code true} if the item's row is fully visible, {@code false} otherwise
     */
    public static boolean isFullyVisible(ColumnTileView<?> tileView, int itemIndex) {
        var rowIndex = tileView.resolveRowIndex(itemIndex);
        return rowIndex >= 0 && VirtualFlowUtils.isFullyVisible(tileView, rowIndex, true);
    }

    /**
     * Scrolls {@code tileView} so the row holding {@code itemIndex} lands at {@code position} within the
     * viewport, regardless of whether it is already visible. Use {@link #scrollToIfNeeded} instead if the
     * point is only to guarantee visibility without disturbing an already-fine scroll position.
     *
     * @param tileView  the tile view to scroll
     * @param itemIndex the item index to scroll to
     * @param position  where the item's row should end up in the viewport
     */
    public static void scrollTo(ColumnTileView<?> tileView, int itemIndex, ScrollPosition position) {
        var rowIndex = tileView.resolveRowIndex(itemIndex);
        if (rowIndex >= 0) {
            VirtualFlowUtils.scrollTo(tileView, rowIndex, position, true);
        }
    }

    /**
     * Scrolls {@code tileView} only when the row holding {@code itemIndex} is not already fully visible; an
     * already-visible item is left untouched. When a scroll is needed, the row ends up at {@code position}
     * within the viewport — see {@link #scrollTo}.
     *
     * @param tileView  the tile view to scroll
     * @param itemIndex the item index that should be visible
     * @param position  where the item's row should end up in the viewport if it needs to be scrolled to
     */
    public static void scrollToIfNeeded(ColumnTileView<?> tileView, int itemIndex, ScrollPosition position) {
        var rowIndex = tileView.resolveRowIndex(itemIndex);
        if (rowIndex >= 0) {
            VirtualFlowUtils.scrollToIfNeeded(tileView, rowIndex, position, true);
        }
    }

    /**
     * Forces the cell currently showing {@code itemIndex} to re-derive its visual content from its current
     * item, without touching {@code listView}'s items/scroll position/selection. Unlike every other method in
     * this class, this does not delegate to {@link VirtualFlowUtils}: that utility only ever sees
     * {@code listView}'s own flow cells, which are whole columns, not individual items, so it could only
     * force-update an entire column (32+ cells) to fix the one this method targets. Instead this reaches
     * directly into {@link ColumnListView#getCell(int)}, which knows how to resolve an item index down to the
     * one cached cell that represents it.
     *
     * @param listView  the list view to update
     * @param itemIndex the item index whose cell should be updated
     */
    public static void updateCell(ColumnListView<?> listView, int itemIndex) {
        forceUpdate(listView.getCell(itemIndex));
        listView.applyCss();
        listView.layout();
    }

    /**
     * The {@link ColumnTileView} counterpart of {@link #updateCell(ColumnListView, int)} — see there for why
     * this does not delegate to {@link VirtualFlowUtils}.
     *
     * @param tileView  the tile view to update
     * @param itemIndex the item index whose cell should be updated
     */
    public static void updateCell(ColumnTileView<?> tileView, int itemIndex) {
        forceUpdate(tileView.getCell(itemIndex));
        tileView.applyCss();
        tileView.layout();
    }

    /**
     * Forces cells of {@code listView} to re-derive their visual content from their current item, without
     * touching its items/scroll position/selection — see {@link ColumnListView#forceUpdateCells} (not
     * {@link VirtualFlowUtils}, for the same reason {@link #updateCell(ColumnListView, int)} doesn't: this
     * reaches directly into each realized column's own cached cells, one item at a time, rather than only
     * being able to touch a whole column at once via the generic flow-index-based API). Unlike
     * {@code VirtualFlowUtils#updateCells}, {@code onlyVisible} here genuinely means "every item currently
     * visible" vs. "every item, full stop" &mdash; not "every flow cell", since a flow cell (a column) is
     * always expanded down to its individual items either way.
     *
     * @param listView    the list view to update
     * @param onlyVisible whether to touch only items in the currently visible columns (cheap) or every item
     *                    regardless of visibility (thorough)
     */
    public static void updateCells(ColumnListView<?> listView, boolean onlyVisible) {
        listView.forceUpdateCells(onlyVisible);
    }

    /**
     * The {@link ColumnTileView} counterpart of {@link #updateCells(ColumnListView, boolean)}.
     *
     * @param tileView    the tile view to update
     * @param onlyVisible whether to touch only items in the currently visible rows (cheap) or every item
     *                    regardless of visibility (thorough)
     */
    public static void updateCells(ColumnTileView<?> tileView, boolean onlyVisible) {
        tileView.forceUpdateCells(onlyVisible);
    }

    /**
     * Re-invokes {@code updateItem} on {@code cell} with its own current item, forcing it to fully re-render
     * &mdash; a real, method-scoped type parameter (unlike the wildcard capture {@code updateCell} itself
     * receives from {@code listView}/{@code tileView}) so the compiler can see {@code cell.getItem()}'s result
     * is exactly the type {@code cell.updateItem} expects.
     */
    private static <T> void forceUpdate(@Nullable ColumnListCell<T> cell) {
        if (cell != null && !cell.isEmpty()) {
            cell.updateItem(cell.getItem(), false);
        }
    }

    /**
     * The {@link TileCell} counterpart of {@link #forceUpdate(ColumnListCell)}.
     */
    private static <T> void forceUpdate(@Nullable TileCell<T> cell) {
        if (cell != null && !cell.isEmpty()) {
            cell.updateItem(cell.getItem(), false);
        }
    }

    private ColumnViewUtils() {
        // empty
    }
}
