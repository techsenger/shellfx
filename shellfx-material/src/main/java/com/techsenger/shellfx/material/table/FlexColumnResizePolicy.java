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

package com.techsenger.shellfx.material.table;

import java.util.Objects;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * A {@link TableView} column resize policy in which exactly one designated column — the <b>flex column</b> —
 * absorbs all extra or missing width, regardless of its position in the table, while every other column keeps
 * whatever width it currently has.
 *
 * <p>Unlike the built-in {@link TableView#CONSTRAINED_RESIZE_POLICY}, this policy does not disable the table's
 * horizontal scrollbar: if the sum of the non-flex columns' widths plus the flex column's
 * {@link TableColumn#getMinWidth() minimum width} exceeds the table's content width, a horizontal scrollbar
 * simply appears instead of the columns being forced to shrink below their bounds.
 *
 * <p>This policy requires no adapter or dedicated column interface: {@link TableColumn} already exposes the
 * width, minimum width and maximum width it needs, so it works with any {@link TableView}/{@link TableColumn}
 * pair as is.
 *
 * <h2>Behavior</h2>
 * <p>{@link TableView} invokes {@link #call(TableView.ResizeFeatures)} in two situations, both handled here:
 * <ul>
 *   <li>after the user drags a column's border — {@link TableView.ResizeFeatures#getColumn()} returns the
 *       column immediately to the left of that border and {@link TableView.ResizeFeatures#getDelta()} the drag
 *       amount. This policy applies that delta to the dragged column, and the opposite delta to its immediate
 *       right-hand neighbor, both clamped to their own min/max width — a local, two-column trade that leaves
 *       every other column, including the flex column, untouched whenever neither side of the dragged border is
 *       the flex column;</li>
 *   <li>whenever the table's content width changes for any reason (including as a direct result of the trade
 *       above, or because one side of it hit its bounds and could not fully compensate) — this policy then
 *       recomputes the flex column's width as the content width minus the sum of every other visible column's
 *       width, clamped to the flex column's min/max width. This step runs unconditionally and is what makes the
 *       flex column react correctly even when it is itself one of the two columns on either side of the dragged
 *       border — no special case for that position is needed.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * TableColumn<Row, String> nameColumn = ...;
 * tableView.setColumnResizePolicy(new FlexColumnResizePolicy(nameColumn));
 * }</pre>
 *
 * <p>Raw types are used here to match {@link TableView#setColumnResizePolicy} own (unparameterized) signature.
 *
 * <h2>Thread safety</h2>
 * <p>Instances are immutable after construction, but {@link #call(TableView.ResizeFeatures)} must only be
 * invoked on the JavaFX Application Thread, as it reads and writes live {@link TableColumn} state.
 *
 * @author Pavel Castornii
 */
@SuppressWarnings("rawtypes")
public final class FlexColumnResizePolicy implements Callback<TableView.ResizeFeatures, Boolean> {

    /**
     * Returns the visible leaf column immediately to the right of {@code column}, or {@code null} if
     * {@code column} is the last visible leaf column (or, defensively, if it is not found at all).
     *
     * @param features the resize features to read the table's visible leaf columns from
     * @param column   the column to find the right-hand neighbor of
     * @return the immediate right-hand neighbor, or {@code null} if there is none
     */
    @SuppressWarnings("unchecked") // same raw-features chain as call(); see the note there
    private static TableColumn<?, ?> findRightNeighbor(TableView.ResizeFeatures features, TableColumn<?, ?> column) {
        var columns = features.getTable().getVisibleLeafColumns();
        int index = columns.indexOf(column);
        if (index < 0 || index + 1 >= columns.size()) {
            return null;
        }
        return (TableColumn<?, ?>) columns.get(index + 1);
    }

    /**
     * Clamps {@code width} to {@code column}'s {@code [minWidth, maxWidth]} range.
     *
     * @param column the column whose bounds to clamp against
     * @param width  the proposed width
     * @return {@code width} clamped between {@code column.getMinWidth()} and {@code column.getMaxWidth()}
     */
    private static double clamp(TableColumn<?, ?> column, double width) {
        return Math.max(column.getMinWidth(), Math.min(column.getMaxWidth(), width));
    }

    private final TableColumn<?, ?> flexColumn;

    /**
     * Creates a policy in which {@code flexColumn} absorbs all extra or missing table width.
     *
     * @param flexColumn the column that grows or shrinks to fill the table's content width; must not be
     *                    {@code null} and should be one of the columns of the {@link TableView} this policy is
     *                    installed on
     * @throws NullPointerException if {@code flexColumn} is {@code null}
     */
    public FlexColumnResizePolicy(TableColumn<?, ?> flexColumn) {
        this.flexColumn = Objects.requireNonNull(flexColumn, "flexColumn must not be null");
    }

    /**
     * Applies a manual column drag, if any, and then resolves the flex column's width to fill the table's
     * remaining content width.
     *
     * @param features the resize features supplied by the {@link TableView} for this resize pass
     * @return always {@code true}, indicating the resize was handled
     */
    @Override
    @SuppressWarnings("unchecked")
    public Boolean call(TableView.ResizeFeatures features) {
        TableColumn<?, ?> draggedColumn = features.getColumn();
        if (draggedColumn != null) {
            double delta = features.getDelta();
            features.setColumnWidth(draggedColumn, clamp(draggedColumn, draggedColumn.getWidth() + delta));

            TableColumn<?, ?> neighbor = findRightNeighbor(features, draggedColumn);
            if (neighbor != null) {
                features.setColumnWidth(neighbor, clamp(neighbor, neighbor.getWidth() - delta));
            }
        }

        double othersWidth = 0;
        for (Object columnObject : features.getTable().getVisibleLeafColumns()) {
            TableColumn<?, ?> column = (TableColumn<?, ?>) columnObject;
            if (column != flexColumn) {
                othersWidth += column.getWidth();
            }
        }

        double flexWidth = clamp(flexColumn, features.getContentWidth() - othersWidth);
        features.setColumnWidth(flexColumn, flexWidth);
        return true;
    }
}
