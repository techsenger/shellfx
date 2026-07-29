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

import com.techsenger.toolkit.fx.utils.VirtualFlowUtils;
import com.techsenger.toolkit.fx.utils.VirtualFlowUtils.ScrollPosition;

/**
 * Scroll-position utilities for {@link ColumnListView} and {@link ColumnTileView}, mirroring what
 * {@code TableUtils}/{@code ListViewUtils} (toolkit-fx) offer for {@code TableView}/{@code ListView}.
 *
 * <p>Both controls lay their items out across a discrete number of columns/rows, and each is itself a
 * {@code VirtualFlow} whose own cells are, respectively, whole columns ({@link ColumnListView}, scrolling
 * horizontally) or whole rows ({@link ColumnTileView}, scrolling vertically) &mdash; not individual items. So
 * every method here first translates the given item index into that outer flow's own column/row index (via
 * {@link ColumnListView#resolveColumnIndex}/{@link ColumnTileView#resolveRowIndex}), then delegates the actual
 * scrolling to {@link VirtualFlowUtils}, which already knows how to scroll either orientation of flow. Neither
 * control needs any change to support this &mdash; both already expose a standard {@code .virtual-flow} node
 * that {@link VirtualFlowUtils} can look up on its own.
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

    private ColumnViewUtils() {
        // empty
    }
}
