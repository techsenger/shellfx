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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.core.Name;
import com.techsenger.toolkit.core.function.Factory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumnBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and tracks the columns of a {@code TableColumnBase}-based control (e.g. {@code TableView} or
 * {@code TreeTableView}) from named factories, translating between the control's live column list/sort order
 * and column-info snapshots (order, width, sort) used to persist and restore column state, and reporting live,
 * user-driven changes back through listener callbacks so a caller can persist them.
 *
 * <p>{@link TableColumnManager} is the {@code TableView} instantiation of this class; {@link TreeTableColumnManager}
 * is the {@code TreeTableView} one. Both are thin: everything that doesn't depend on which concrete column type
 * ({@code NamedTableColumn} vs. {@code NamedTreeTableColumn}) is in play &mdash; wiring to the control's
 * {@code getColumns()}/{@code getSortOrder()} lists, and the {@link #addColumns} algorithm itself &mdash; lives
 * here, once. A subclass only needs to implement {@link #createColumn} (building a concrete column and wiring its
 * width/sort-type listeners, which do differ by family, since {@code TableColumn.SortType} and
 * {@code TreeTableColumn.SortType} are unrelated types) and {@link #getName} (a one-line accessor).
 *
 * <p><b>Never replace the control's items list to refresh its data</b> (e.g. {@code tableView.setItems(new
 * ObservableList)}) &mdash; {@code TableView} clears its own sort order as an internal side effect of swapping
 * the items list, silently discarding whatever this manager just applied via {@link #addColumns} or whatever the
 * user just chose by clicking a column header. Always mutate the existing list in place instead, e.g.
 * {@code tableView.getItems().setAll(newData)}. (Not independently confirmed for {@code TreeTableView}, which has
 * no {@code setItems()} of its own to fall into this trap the same way — but the same caution likely applies to
 * replacing its root {@code TreeItem} outright rather than mutating its children in place.)
 *
 * <p>Deliberately not parameterized by the control's row type: for {@code TableColumn<S, T>} that row type is
 * {@code TableColumnBase}'s own first type parameter, but {@code TreeTableColumn<S, T>} actually extends
 * {@code TableColumnBase<TreeItem<S>, T>} &mdash; i.e. its {@code TableColumnBase} row type is {@code
 * TreeItem<S>}, not {@code S}. A shared row-type parameter here would be correct for one family and wrong for
 * the other. {@link TableColumnManager}/{@link TreeTableColumnManager} each carry their own row-type parameter
 * instead, at the point where it's actually correct for that family.
 *
 * @param <N> the column-name type, identifying a column independent of its current position
 * @param <C> the concrete column type this manager builds, e.g. {@code NamedTableColumn<S, ?>}
 * @param <ST> the column family's sort-type enum, e.g. {@code TableColumn.SortType}
 * @author Pavel Castornii
 */
public abstract class AbstractTableColumnManager<N extends Name, C extends TableColumnBase<?, ?>, ST> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTableColumnManager.class);

    private final ObservableList<C> columns;

    private final ObservableList<C> sortOrder;

    private final Map<N, Factory<C>> columnFactoriesByName = new HashMap<>();

    private final Map<N, C> modifiableColumnsByName = new HashMap<>();

    private final @Unmodifiable Map<N, C> columnsByName = Collections.unmodifiableMap(modifiableColumnsByName);

    private BiConsumer<N, Double> widthListener;

    private BiConsumer<N, Integer> indexListener;

    private boolean indexListenerDisabled;

    private BiConsumer<N, Integer> sortIndexListener;

    private boolean sortIndexListenerDisabled;

    private BiConsumer<N, ST> sortTypeListener;

    /**
     * Wires this manager to the given column list and sort-order list &mdash; typically {@code
     * control.getColumns()} and {@code control.getSortOrder()} of the {@code TableView}/{@code TreeTableView} a
     * subclass is built for. Takes the lists directly (rather than the control itself, or an abstract accessor
     * method) specifically so a subclass constructor can pass its own constructor parameter straight through
     * before assigning any of its own fields.
     *
     * @param columns the control's live column list
     * @param sortOrder the control's live sort-order list
     */
    protected AbstractTableColumnManager(ObservableList<C> columns, ObservableList<C> sortOrder) {
        this.columns = columns;
        this.sortOrder = sortOrder;
        columns.addListener((ListChangeListener<? super C>) e -> {
            if (!indexListenerDisabled && indexListener != null) {
                for (var i = 0; i < columns.size(); i++) {
                    indexListener.accept(getName(columns.get(i)), i);
                }
            }
        });
        sortOrder.addListener((ListChangeListener<? super C>) e -> {
            if (sortIndexListenerDisabled || sortIndexListener == null) {
                return;
            }
            // A column dropped from the sort order (e.g. replaced as the active sort column by another one)
            // must be reported with a null sort index, otherwise its last known sort index is never cleared
            // and can collide with whichever column is sorted next.
            while (e.next()) {
                if (e.wasRemoved()) {
                    for (var removed : e.getRemoved()) {
                        if (!sortOrder.contains(removed)) {
                            sortIndexListener.accept(getName(removed), null);
                        }
                    }
                }
            }
            for (var i = 0; i < sortOrder.size(); i++) {
                sortIndexListener.accept(getName(sortOrder.get(i)), i);
            }
        });
    }

    /**
     * Registers the factory that builds the column identified by {@code name}, replacing any factory previously
     * registered for it. {@link #addColumns} looks up this factory when it needs to construct that column.
     *
     * @param name the column's identity, independent of its position
     * @param factory builds a fresh column instance on demand; may be called more than once over the manager's
     *     lifetime (e.g. once per table tab that redisplays the same logical column set)
     */
    public void registerColumnFactory(N name, Factory<C> factory) {
        this.columnFactoriesByName.put(name, factory);
    }

    /**
     * Removes the factory registered for {@code name}, if any. After this call, {@link #addColumns} can no
     * longer build that column; typically used before offering the column as removable (a "hide column"
     * feature) rather than during normal operation.
     *
     * @param name the column's identity to unregister
     */
    public void unregisterColumnFactory(N name) {
        this.columnFactoriesByName.remove(name);
    }

    /**
     * Returns the callback most recently set via {@link #setWidthListener}, or {@code null} if none was set.
     *
     * @return the current width-change listener
     */
    public BiConsumer<N, Double> getWidthListener() {
        return widthListener;
    }

    /**
     * Sets the callback invoked whenever a column's width changes because of live user interaction (e.g.
     * dragging a column border) &mdash; not because {@link #addColumns} applied a persisted width while
     * building the column, which never invokes any listener.
     *
     * @param widthListener called with the column's name and its new width in pixels
     */
    public void setWidthListener(BiConsumer<N, Double> widthListener) {
        this.widthListener = widthListener;
    }

    /**
     * Returns the callback most recently set via {@link #setIndexListener}, or {@code null} if none was set.
     *
     * @return the current column-position listener
     */
    public BiConsumer<N, Integer> getIndexListener() {
        return indexListener;
    }

    /**
     * Sets the callback invoked whenever a column's position among the control's visible columns changes because
     * of live user interaction (e.g. dragging a column to reorder it). Never called for the reordering
     * {@link #addColumns} itself performs while building columns.
     *
     * @param indexListener called with the column's name and its new zero-based position
     */
    public void setIndexListener(BiConsumer<N, Integer> indexListener) {
        this.indexListener = indexListener;
    }

    /**
     * Returns the callback most recently set via {@link #setSortIndexListener}, or {@code null} if none was set.
     *
     * @return the current sort-position listener
     */
    public BiConsumer<N, Integer> getSortIndexListener() {
        return sortIndexListener;
    }

    /**
     * Sets the callback invoked whenever a column's position in the control's sort order changes because of live
     * user interaction (e.g. clicking a column header to sort by it, or clicking a different column so this one
     * is no longer part of the sort). The callback must accept a {@code null} index: it is called with {@code
     * null} for a column that just dropped out of the sort order entirely, since that column's last known sort
     * index would otherwise never be cleared and could collide with whichever column is sorted next. Never
     * called for the sort order {@link #addColumns} itself establishes while building columns.
     *
     * @param sortIndexListener called with the column's name and its new zero-based sort position, or {@code
     *     null} if the column is no longer part of the sort
     */
    public void setSortIndexListener(BiConsumer<N, Integer> sortIndexListener) {
        this.sortIndexListener = sortIndexListener;
    }

    /**
     * Returns the callback most recently set via {@link #setSortTypeListener}, or {@code null} if none was set.
     *
     * @return the current sort-type listener
     */
    public BiConsumer<N, ST> getSortTypeListener() {
        return sortTypeListener;
    }

    /**
     * Sets the callback invoked whenever a column's sort direction changes because of live user interaction
     * (e.g. clicking an already-sorted column's header to flip ascending/descending). A subclass's
     * {@link #createColumn} implementation is responsible for wiring this listener to the concrete column's own
     * sort-type property, since that property differs by column family.
     *
     * @param sortTypeListener called with the column's name and its new sort type
     */
    public void setSortTypeListener(BiConsumer<N, ST> sortTypeListener) {
        this.sortTypeListener = sortTypeListener;
    }

    /**
     * Returns an unmodifiable, live view of the columns this manager has built so far, keyed by name.
     *
     * @return the columns-by-name map; not a copy
     */
    public @Unmodifiable Map<N, C> getColumnsByName() {
        return columnsByName;
    }

    /**
     * Builds a column per entry of {@code infosByName} (via {@link #createColumn}) and adds them to the control
     * in {@link ColumnInfo#getIndex()} order (and, for entries with a non-null {@link ColumnInfo#getSortIndex()},
     * to the sort order too), applying each entry's persisted width and sort type. Listeners are not called for
     * the changes this method makes.
     *
     * <p>Columns are ordered by sorting the entries, not by indexing into an array/map with their {@code index}/
     * {@code sortIndex} value, so gaps in those values (e.g. a persisted history that predates a since-removed
     * column) are harmless. Two entries sharing the same {@code index} or {@code sortIndex} are both kept (in
     * whatever order the sort happens to break the tie) and logged as a warning, rather than one silently
     * replacing the other.
     *
     * <p>{@code infosByName} is expected to hold one entry per column the caller ever knows about, not just the
     * currently visible ones (see {@link ColumnInfo#isVisible()}) &mdash; entries whose column is currently
     * hidden are skipped and no column is built for them.
     *
     * @param infos the persisted column state to apply, keyed by column name
     */
    public void addColumns(Collection<? extends ColumnInfo<N, ST>> infos) {
        indexListenerDisabled = true;
        sortIndexListenerDisabled = true;

        // A local record is implicitly static, so it cannot reference the enclosing class's own N/C/ST type
        // variables directly - it needs (and here reuses, for readability) its own.
        record Entry<N extends Name, C, ST>(ColumnInfo<N, ST> info, C column) { }

        var entries = new ArrayList<Entry<N, C, ST>>();
        for (var info : infos) {
            if (!info.isVisible()) {
                continue;
            }
            var column = createColumn(info.getName(), info.getWidth(), info.getSortType());
            modifiableColumnsByName.put(info.getName(), column);
            entries.add(new Entry<>(info, column));
        }

        var byIndex = entries.stream().sorted(Comparator.comparingInt(e -> e.info().getIndex())).toList();
        for (var i = 1; i < byIndex.size(); i++) {
            if (byIndex.get(i - 1).info().getIndex() == byIndex.get(i).info().getIndex()) {
                logger.warn("Duplicate column index {} for {} and {}", byIndex.get(i).info().getIndex(),
                        getName(byIndex.get(i - 1).column()), getName(byIndex.get(i).column()));
            }
        }
        for (var entry : byIndex) {
            columns.add(entry.column());
        }

        var bySortIndex = entries.stream()
                .filter(e -> e.info().getSortIndex() != null)
                .sorted(Comparator.comparingInt(e -> e.info().getSortIndex()))
                .toList();
        for (var i = 1; i < bySortIndex.size(); i++) {
            if (bySortIndex.get(i - 1).info().getSortIndex().equals(bySortIndex.get(i).info().getSortIndex())) {
                logger.warn("Duplicate column sort index {} for {} and {}",
                        bySortIndex.get(i).info().getSortIndex(), getName(bySortIndex.get(i - 1).column()),
                        getName(bySortIndex.get(i).column()));
            }
        }
        for (var entry : bySortIndex) {
            Objects.requireNonNull(entry.info().getSortType(), "No sort type for column " + getName(entry.column()));
            sortOrder.add(entry.column());
        }

        indexListenerDisabled = false;
        sortIndexListenerDisabled = false;
    }

    /**
     * Builds one column for {@code name} (via {@link #createColumn}) and adds it as the last column of the
     * control, applying {@code info}'s persisted width and sort type. Unlike {@link #addColumns}, this is for
     * adding a single column after the control is already showing others &mdash; e.g. a "show column" menu
     * action &mdash; so, unlike {@code addColumns}, listeners are not suppressed: the control's own column-list
     * listener reports this column (and, harmlessly, every other already-positioned one) through
     * {@link #getIndexListener()} exactly as it would for any other live change to the column list.
     *
     * @param name the column to add; must not already be present
     * @param info the persisted width/sort-type to apply; its {@link ColumnInfo#getIndex() index} is ignored,
     *     since the column is always appended at the end
     * @return the newly built column
     */
    public C addColumn(ColumnInfo<N, ST> info) {
        var column = createColumn(info.getName(), info.getWidth(), info.getSortType());
        modifiableColumnsByName.put(info.getName(), column);
        columns.add(column);
        if (info.getSortIndex() != null) {
            Objects.requireNonNull(info.getSortType(), "No sort type for column " + info.getName());
            sortOrder.add(column);
        }
        return column;
    }

    /**
     * Removes the column identified by {@code name} from the control, if it is currently shown. The remaining
     * columns' indices (and, if applicable, sort indices) are updated through the usual listeners, since removal
     * is a live change to the control's own column/sort-order lists.
     *
     * @param name the column to remove
     */
    public void removeColumn(N name) {
        var column = modifiableColumnsByName.remove(name);
        if (column == null) {
            return;
        }
        columns.remove(column);
        sortOrder.remove(column);
    }

    /**
     * Removes every column identified by {@code names} that is currently shown, via repeated {@link #removeColumn}
     * calls &mdash; the counterpart to {@link #addColumns} for tearing down more than one column at once.
     *
     * @param names the columns to remove
     */
    public void removeColumns(Collection<N> names) {
        for (var name : names) {
            removeColumn(name);
        }
    }

    /**
     * Returns the live map of column factories keyed by column name, for a subclass's {@link #createColumn} to
     * look up.
     *
     * @return the column-factories-by-name map; not a copy
     */
    protected Map<N, Factory<C>> getColumnFactoriesByName() {
        return columnFactoriesByName;
    }

    /**
     * Builds one column for {@code name} via the registered factory, applying {@code width}/{@code sortType} if
     * non-null, and wires the concrete column's width and sort-type properties to
     * {@link #getWidthListener()}/{@link #getSortTypeListener()}. This is the one piece of real work a subclass
     * must implement itself, since {@code TableColumnBase} does not declare a common sort-type property (each
     * family has its own, unrelated {@code SortType} enum) &mdash; everything else {@link #addColumns} needs is
     * shared here in the abstract base.
     *
     * @param name the column to build
     * @param width the persisted preferred width to apply, or {@code null} to leave the factory's default
     * @param sortType the persisted sort type to apply, or {@code null} to leave the factory's default
     * @return the newly built, fully wired column
     */
    protected abstract C createColumn(N name, Double width, ST sortType);

    /**
     * Returns {@code column}'s name. Implemented by a subclass as a one-line delegation to the concrete column
     * type's own name accessor (e.g. {@code NamedTableColumn.getName()}).
     *
     * @param column the column to identify
     * @return the column's name
     */
    protected abstract N getName(C column);
}
