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

import com.techsenger.toolkit.fx.FxPlatform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link TableColumnManager}, which is also the only real (non-abstract-stub) coverage of
 * {@link AbstractTableColumnManager}'s shared {@code addColumns()}/wiring logic &mdash; see
 * {@link TreeTableColumnManagerTest} for the second, independent instantiation of that same shared logic.
 *
 * <p>Deliberately never creates a {@code Stage}/{@code Scene}: see {@code NoOverflowToolBarSkinTest} for why
 * that's avoided in this environment. This means column order and sort order (plain {@code ObservableList}
 * mutations) and applied preferred widths are all fully testable here, but a real user-drag width change
 * (which only happens through a resize policy during an actual layout pass) is not attempted.
 *
 * @author Pavel Castornii
 */
class TableColumnManagerTest {

    private enum TestColumn implements TableColumnName {
        A, B, C;

        @Override
        public String getText() {
            return name();
        }
    }

    private record Call<V>(TestColumn name, V value) { }

    @BeforeAll
    static void initJavaFxToolkit() {
        try {
            System.setProperty("glass.platform", "Headless");
            FxPlatform.start();
        } catch (IllegalStateException alreadyStarted) {
            // toolkit already running in this JVM (e.g. started by another test class); nothing to do
        }
    }

    private final TableView<Object> tableView = new TableView<>();

    private final TableColumnManager<Object> manager = new TableColumnManager<>(tableView);

    private final List<Call<Double>> widthCalls = new ArrayList<>();

    private final List<Call<Integer>> indexCalls = new ArrayList<>();

    private final List<Call<Integer>> sortIndexCalls = new ArrayList<>();

    private final List<Call<TableColumn.SortType>> sortTypeCalls = new ArrayList<>();

    private void registerFactory(TestColumn name) {
        manager.registerColumnFactory(name, () -> new NamedTableColumn<Object, Object>(name, name.name()));
    }

    private void registerAllFactories() {
        registerFactory(TestColumn.A);
        registerFactory(TestColumn.B);
        registerFactory(TestColumn.C);
    }

    private void registerRecordingListeners() {
        manager.setWidthListener((name, width) -> widthCalls.add(new Call<>((TestColumn) name, width)));
        manager.setIndexListener((name, index) -> indexCalls.add(new Call<>((TestColumn) name, index)));
        manager.setSortIndexListener((name, index) -> sortIndexCalls.add(new Call<>((TestColumn) name, index)));
        manager.setSortTypeListener((name, type) -> sortTypeCalls.add(new Call<>((TestColumn) name, type)));
    }

    private TableColumnInfo infoOf(TestColumn name, int index) {
        var info = new TableColumnInfo(name);
        info.setIndex(index);
        return info;
    }

    // A plain mapOf(info) would have its key type inferred as TestColumn, not TableColumnName,
    // which addColumns()'s Map<TableColumnName, ...> parameter does not accept (generics are invariant).
    private Map<TableColumnName, TableColumnInfo> mapOf(TableColumnInfo... infos) {
        var map = new HashMap<TableColumnName, TableColumnInfo>();
        for (var info : infos) {
            map.put(info.getName(), info);
        }
        return map;
    }

    @Test
    void addColumns_indicesOutOfMapOrder_columnsOrderedByIndex() {
        registerAllFactories();
        var infos = new HashMap<TableColumnName, TableColumnInfo>();
        infos.put(TestColumn.A, infoOf(TestColumn.A, 2));
        infos.put(TestColumn.B, infoOf(TestColumn.B, 0));
        infos.put(TestColumn.C, infoOf(TestColumn.C, 1));

        manager.addColumns(infos);

        assertThat(tableView.getColumns().stream().map(c -> ((NamedTableColumn<?, ?>) c).getName()).toList())
                .containsExactly(TestColumn.B, TestColumn.C, TestColumn.A);
    }

    @Test
    void addColumns_widthAndSortTypeSet_appliedToColumn() {
        registerFactory(TestColumn.A);
        var info = infoOf(TestColumn.A, 0);
        info.setWidth(123.0);
        info.setSortIndex(0);
        info.setSortType(TableColumn.SortType.DESCENDING);

        manager.addColumns(mapOf(info));

        var column = manager.getColumnsByName().get(TestColumn.A);
        assertThat(column.getPrefWidth()).isEqualTo(123.0);
        assertThat(column.getSortType()).isEqualTo(TableColumn.SortType.DESCENDING);
    }

    @Test
    void addColumns_mixedSortIndexNulls_onlyNonNullAddedToSortOrderInOrder() {
        registerAllFactories();
        var a = infoOf(TestColumn.A, 0);
        var b = infoOf(TestColumn.B, 1);
        b.setSortIndex(1);
        b.setSortType(TableColumn.SortType.ASCENDING);
        var c = infoOf(TestColumn.C, 2);
        c.setSortIndex(0);
        c.setSortType(TableColumn.SortType.ASCENDING);

        manager.addColumns(mapOf(a, b, c));

        assertThat(tableView.getSortOrder().stream().map(sc -> ((NamedTableColumn<?, ?>) sc).getName()).toList())
                .containsExactly(TestColumn.C, TestColumn.B);
    }

    @Test
    void addColumns_always_listenersNotInvoked() {
        registerAllFactories();
        registerRecordingListeners();
        var a = infoOf(TestColumn.A, 0);
        a.setWidth(50.0);
        a.setSortIndex(0);
        a.setSortType(TableColumn.SortType.ASCENDING);

        manager.addColumns(mapOf(a));

        assertThat(widthCalls).isEmpty();
        assertThat(indexCalls).isEmpty();
        assertThat(sortIndexCalls).isEmpty();
        assertThat(sortTypeCalls).isEmpty();
    }

    @Test
    void addColumns_duplicateIndex_bothColumnsKept() {
        registerFactory(TestColumn.A);
        registerFactory(TestColumn.B);
        var a = infoOf(TestColumn.A, 0);
        var b = infoOf(TestColumn.B, 0);

        manager.addColumns(mapOf(a, b));

        assertThat(tableView.getColumns()).hasSize(2);
    }

    @Test
    void addColumns_duplicateSortIndex_bothColumnsKeptInSortOrder() {
        registerFactory(TestColumn.A);
        registerFactory(TestColumn.B);
        var a = infoOf(TestColumn.A, 0);
        a.setSortIndex(0);
        a.setSortType(TableColumn.SortType.ASCENDING);
        var b = infoOf(TestColumn.B, 1);
        b.setSortIndex(0);
        b.setSortType(TableColumn.SortType.ASCENDING);

        manager.addColumns(mapOf(a, b));

        assertThat(tableView.getSortOrder()).hasSize(2);
    }

    @Test
    void sortOrder_activeSortColumnReplaced_nullThenNewIndexReported() {
        registerAllFactories();
        var a = infoOf(TestColumn.A, 0);
        a.setSortIndex(0);
        a.setSortType(TableColumn.SortType.ASCENDING);
        var b = infoOf(TestColumn.B, 1);
        manager.addColumns(mapOf(a, b));
        registerRecordingListeners();

        // Simulates what a single-column-sort header click does: the previously-sorted column drops out of
        // the sort order entirely as the newly-clicked one takes over as the sole active sort column.
        tableView.getSortOrder().setAll(manager.getColumnsByName().get(TestColumn.B));

        assertThat(sortIndexCalls).containsExactly(new Call<Integer>(TestColumn.A, null), new Call<>(TestColumn.B, 0));
    }

    @Test
    void columns_reorderedDirectly_newIndicesReportedForEveryColumn() {
        registerAllFactories();
        var a = infoOf(TestColumn.A, 0);
        var b = infoOf(TestColumn.B, 1);
        manager.addColumns(mapOf(a, b));
        registerRecordingListeners();

        // Simulates a drag-to-reorder: A and B swap places.
        var columnA = manager.getColumnsByName().get(TestColumn.A);
        var columnB = manager.getColumnsByName().get(TestColumn.B);
        tableView.getColumns().setAll(columnB, columnA);

        assertThat(indexCalls).containsExactly(new Call<>(TestColumn.B, 0), new Call<>(TestColumn.A, 1));
    }

    @Test
    void widthListener_prefWidthChangedAfterBuild_listenerFires() {
        registerFactory(TestColumn.A);
        manager.addColumns(mapOf(infoOf(TestColumn.A, 0)));
        registerRecordingListeners();

        manager.getColumnsByName().get(TestColumn.A).setPrefWidth(77.0);

        assertThat(widthCalls).containsExactly(new Call<>(TestColumn.A, 77.0));
    }

    @Test
    void addColumns_noFactoryRegistered_throws() {
        var info = infoOf(TestColumn.A, 0);

        assertThatThrownBy(() -> manager.addColumns(mapOf(info)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("A");
    }

    @Test
    void getColumnsByName_attemptToModify_throwsUnsupportedOperationException() {
        registerFactory(TestColumn.A);
        manager.addColumns(mapOf(infoOf(TestColumn.A, 0)));

        assertThatThrownBy(() -> manager.getColumnsByName().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
