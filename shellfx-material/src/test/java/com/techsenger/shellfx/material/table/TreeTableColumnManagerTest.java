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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link TreeTableColumnManager} &mdash; the second, independent instantiation of
 * {@link AbstractTableColumnManager}'s shared {@code addColumns()}/wiring logic; see
 * {@link TableColumnManagerTest} for the first one and for why width changes from a real user drag are not
 * attempted here (no {@code Stage}/{@code Scene} is ever created in this environment).
 *
 * @author Pavel Castornii
 */
class TreeTableColumnManagerTest {

    private enum TestColumn implements TreeTableColumnName {
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

    // TreeTableView appears to silently discard additions to getSortOrder() when it has no root - the sort
    // pass it triggers internally presumably fails without one, and the change gets reverted like a rejected
    // sort policy would. TableView has no such requirement (it works fine with no items at all), so this is
    // TreeTableView-specific.
    private final TreeTableView<Object> treeTableView = new TreeTableView<>(new TreeItem<>(new Object()));

    private final TreeTableColumnManager<Object> manager = new TreeTableColumnManager<>(treeTableView);

    private final List<Call<Double>> widthCalls = new ArrayList<>();

    private final List<Call<Integer>> indexCalls = new ArrayList<>();

    private final List<Call<Integer>> sortIndexCalls = new ArrayList<>();

    private final List<Call<TreeTableColumn.SortType>> sortTypeCalls = new ArrayList<>();

    private void registerFactory(TestColumn name) {
        manager.registerColumnFactory(name, () -> new NamedTreeTableColumn<Object, Object>(name, name.name()));
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

    private TreeTableColumnInfo infoOf(TestColumn name, int index) {
        var info = new TreeTableColumnInfo(name);
        info.setIndex(index);
        return info;
    }

    @Test
    void addColumns_indicesOutOfMapOrder_columnsOrderedByIndex() {
        registerAllFactories();
        var infos = new HashSet<TreeTableColumnInfo>();
        infos.add(infoOf(TestColumn.A, 2));
        infos.add(infoOf(TestColumn.B, 0));
        infos.add(infoOf(TestColumn.C, 1));

        manager.addColumns(infos);

        assertThat(treeTableView.getColumns().stream()
                .map(c -> ((NamedTreeTableColumn<?, ?>) c).getName()).toList())
                .containsExactly(TestColumn.B, TestColumn.C, TestColumn.A);
    }

    @Test
    void addColumns_widthAndSortTypeSet_appliedToColumn() {
        registerFactory(TestColumn.A);
        var info = infoOf(TestColumn.A, 0);
        info.setWidth(123.0);
        info.setSortIndex(0);
        info.setSortType(TreeTableColumn.SortType.DESCENDING);

        manager.addColumns(Set.of(info));

        var column = manager.getColumnsByName().get(TestColumn.A);
        assertThat(column.getPrefWidth()).isEqualTo(123.0);
        assertThat(column.getSortType()).isEqualTo(TreeTableColumn.SortType.DESCENDING);
    }

    @Test
    void addColumns_mixedSortIndexNulls_onlyNonNullAddedToSortOrderInOrder() {
        registerAllFactories();
        var a = infoOf(TestColumn.A, 0);
        var b = infoOf(TestColumn.B, 1);
        b.setSortIndex(1);
        b.setSortType(TreeTableColumn.SortType.ASCENDING);
        var c = infoOf(TestColumn.C, 2);
        c.setSortIndex(0);
        c.setSortType(TreeTableColumn.SortType.ASCENDING);

        manager.addColumns(Set.of(a, b, c));

        assertThat(treeTableView.getSortOrder().stream()
                .map(sc -> ((NamedTreeTableColumn<?, ?>) sc).getName()).toList())
                .containsExactly(TestColumn.C, TestColumn.B);
    }

    @Test
    void addColumns_always_listenersNotInvoked() {
        registerAllFactories();
        registerRecordingListeners();
        var a = infoOf(TestColumn.A, 0);
        a.setWidth(50.0);
        a.setSortIndex(0);
        a.setSortType(TreeTableColumn.SortType.ASCENDING);

        manager.addColumns(Set.of(a));

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

        manager.addColumns(Set.of(a, b));

        assertThat(treeTableView.getColumns()).hasSize(2);
    }

    @Test
    void addColumns_duplicateSortIndex_bothColumnsKeptInSortOrder() {
        registerFactory(TestColumn.A);
        registerFactory(TestColumn.B);
        var a = infoOf(TestColumn.A, 0);
        a.setSortIndex(0);
        a.setSortType(TreeTableColumn.SortType.ASCENDING);
        var b = infoOf(TestColumn.B, 1);
        b.setSortIndex(0);
        b.setSortType(TreeTableColumn.SortType.ASCENDING);

        manager.addColumns(Set.of(a, b));

        assertThat(treeTableView.getSortOrder()).hasSize(2);
    }

    @Test
    void sortOrder_activeSortColumnReplaced_nullThenNewIndexReported() {
        registerAllFactories();
        var a = infoOf(TestColumn.A, 0);
        a.setSortIndex(0);
        a.setSortType(TreeTableColumn.SortType.ASCENDING);
        var b = infoOf(TestColumn.B, 1);
        manager.addColumns(Set.of(a, b));
        registerRecordingListeners();

        // Simulates what a single-column-sort header click does: the previously-sorted column drops out of
        // the sort order entirely as the newly-clicked one takes over as the sole active sort column.
        treeTableView.getSortOrder().setAll(manager.getColumnsByName().get(TestColumn.B));

        assertThat(sortIndexCalls).containsExactly(new Call<Integer>(TestColumn.A, null), new Call<>(TestColumn.B, 0));
    }

    @Test
    void columns_reorderedDirectly_newIndicesReportedForEveryColumn() {
        registerAllFactories();
        var a = infoOf(TestColumn.A, 0);
        var b = infoOf(TestColumn.B, 1);
        manager.addColumns(Set.of(a, b));
        registerRecordingListeners();

        // Simulates a drag-to-reorder: A and B swap places.
        var columnA = manager.getColumnsByName().get(TestColumn.A);
        var columnB = manager.getColumnsByName().get(TestColumn.B);
        treeTableView.getColumns().setAll(columnB, columnA);

        assertThat(indexCalls).containsExactly(new Call<>(TestColumn.B, 0), new Call<>(TestColumn.A, 1));
    }

    @Test
    void widthListener_prefWidthChangedAfterBuild_listenerFires() {
        registerFactory(TestColumn.A);
        manager.addColumns(Set.of(infoOf(TestColumn.A, 0)));
        registerRecordingListeners();

        manager.getColumnsByName().get(TestColumn.A).setPrefWidth(77.0);

        assertThat(widthCalls).containsExactly(new Call<>(TestColumn.A, 77.0));
    }

    @Test
    void addColumns_noFactoryRegistered_throws() {
        var info = infoOf(TestColumn.A, 0);

        assertThatThrownBy(() -> manager.addColumns(Set.of(info)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("A");
    }

    @Test
    void getColumnsByName_attemptToModify_throwsUnsupportedOperationException() {
        registerFactory(TestColumn.A);
        manager.addColumns(Set.of(infoOf(TestColumn.A, 0)));

        assertThatThrownBy(() -> manager.getColumnsByName().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
