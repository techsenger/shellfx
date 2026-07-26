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
 *       dragged column and {@link TableView.ResizeFeatures#getDelta()} the drag amount; this policy applies
 *       that delta to the dragged column first, clamped to its own min/max width, unless the dragged column is
 *       the flex column itself, which is left for the second step below;</li>
 *   <li>whenever the table's content width changes for any reason (including as a direct result of the first
 *       step) — this policy then recomputes the flex column's width as the content width minus the sum of every
 *       other visible column's width, clamped to the flex column's min/max width.</li>
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
    // unchecked: features is raw (see the class-level rawtypes note), so every generic method called on it below
    // - getColumn(), getTable().getVisibleLeafColumns() - returns erased types, requiring unchecked assignments
    // and an unchecked cast; confined to this method since the constructor and clamp() never touch features.
    @Override
    @SuppressWarnings("unchecked")
    public Boolean call(TableView.ResizeFeatures features) {
        TableColumn<?, ?> draggedColumn = features.getColumn();
        if (draggedColumn != null && draggedColumn != flexColumn) {
            double newWidth = clamp(draggedColumn, draggedColumn.getWidth() + features.getDelta());
            features.setColumnWidth(draggedColumn, newWidth);
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

    /**
     * Clamps {@code width} to {@code column}'s {@code [minWidth, maxWidth]} range.
     *
     * @param column the column whose bounds to clamp against
     * @param width  the proposed width
     * @return {@code width} clamped between {@code column.getMinWidth()} and {@code column.getMaxWidth()}
     */
    private double clamp(TableColumn<?, ?> column, double width) {
        return Math.max(column.getMinWidth(), Math.min(column.getMaxWidth(), width));
    }
}
