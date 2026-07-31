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

import com.techsenger.toolkit.fx.FxPlatform;
import com.techsenger.toolkit.fx.utils.ScrollPosition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link ColumnViewUtils} against a real, shown {@code ColumnListView} and {@code ColumnTileView}.
 * Both wrap their own outer {@code VirtualFlow} of whole columns/rows (not individual items) &mdash; a
 * horizontal one for {@code ColumnListView}, a vertical one for {@code ColumnTileView} &mdash; which is
 * exactly the shape {@code VirtualFlowUtils}'s axis-aware {@code visibleRange()} needs to be exercised
 * against, with real, laid-out geometry rather than assumed. See {@code TableUtilsTest} (toolkit-fx) for why
 * a real display is required and why this can't run on a display-less CI runner as-is.
 *
 * <p>Assertions are deliberately expressed in terms of the outer flow's own first/last visible cell index
 * (a column index for {@code ColumnListView}, a row index for {@code ColumnTileView}) rather than adjacent
 * item indices, since how many items share one column/row depends on measured font metrics this test does
 * not control. What is controlled directly &mdash; column width for {@code ColumnListView}, column count for
 * {@code ColumnTileView} &mdash; is enough on its own to guarantee more columns/rows exist than fit in the
 * viewport at once, regardless of the actual measured row/tile height.
 *
 * <p>Construction is deliberately split from the scroll/assert step, with {@link #awaitCellsRealized} in
 * between: {@code ColumnListView}/{@code ColumnTileView} measure their row height (respectively, column
 * width) once, lazily, via a nested {@code Platform.runLater} (see {@code addRowHeightCell()}) &mdash; a
 * single outer {@code runLaterAndWait} is not enough to let that nested callback run, so a freshly
 * constructed view can still be sitting in its 1x1 bootstrap placeholder state (zero realized cells) if
 * acted on within the very same FX pulse. This is purely a test-timing concern: real usage always has many
 * pulses between construction and the first user interaction.
 *
 * @author Pavel Castornii
 */
class ColumnViewUtilsTest {

    private static final class Pair<A, B> {

        private final A first;

        private final B second;

        private Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }

    /**
     * A plain, non-observable holder — mutating {@link #setText} does not fire any change event, the same way
     * a domain object's field can mutate without the cell showing it finding out on its own. Used by the
     * {@code updateCell}/{@code updateCells} tests, which are specifically about forcing a redraw of such
     * silently-mutated data.
     */
    private static final class MutableItem {

        private String text;

        private MutableItem(String text) {
            this.text = text;
        }

        private String getText() {
            return text;
        }

        private void setText(String text) {
            this.text = text;
        }
    }

    private static Stage stage;

    @BeforeAll
    static void initJavaFxToolkit() throws InterruptedException {
        FxTestSupport.start();
        FxPlatform.runLaterAndWait(() -> {
            stage = new Stage();
            stage.setX(-3000);
            stage.setY(-3000);
        });
    }

    @AfterAll
    static void closeStage() throws InterruptedException {
        FxPlatform.runLaterAndWait(() -> stage.hide());
    }

    /**
     * Builds a {@code ColumnListView} with {@code itemCount} string items and a fixed, explicit
     * {@code columnWidth}, makes it the content of {@link #stage}'s scene at {@code width}x{@code height},
     * shows the stage (a no-op if already showing) and forces a layout pass. Must be called on the FX
     * Application Thread; the returned view still needs {@link #awaitCellsRealized} before scrolling — see
     * the class Javadoc.
     */
    private static ColumnListView<String> newColumnListView(int itemCount, double columnWidth, double width,
            double height) {
        var listView = new ColumnListView<String>();
        listView.setColumnWidth(columnWidth);
        listView.setItems(items(itemCount, "item-"));
        stage.setScene(new Scene(listView, width, height));
        if (!stage.isShowing()) {
            stage.show();
        }
        listView.applyCss();
        listView.layout();
        return listView;
    }

    /**
     * Builds a {@code ColumnTileView} with {@code itemCount} string items and a fixed, explicit
     * {@code columnCount}, the same way {@link #newColumnListView} builds a {@code ColumnListView} &mdash;
     * see there for details. {@code ColumnTileView}'s outer flow is vertical (rows of tiles), the transpose
     * of {@code ColumnListView}'s horizontal flow of columns.
     *
     * <p>Must be called on the FX Application Thread; the returned view still needs
     * {@link #awaitCellsRealized} before scrolling — see the class Javadoc.
     */
    private static ColumnTileView<String> newColumnTileView(int itemCount, int columnCount, double width,
            double height) {
        var tileView = new ColumnTileView<String>();
        tileView.setColumnCount(columnCount);
        tileView.setItems(items(itemCount, "item-"));
        stage.setScene(new Scene(tileView, width, height));
        if (!stage.isShowing()) {
            stage.show();
        }
        tileView.applyCss();
        tileView.layout();
        return tileView;
    }

    private static ObservableList<String> items(int itemCount, String prefix) {
        var items = FXCollections.<String>observableArrayList();
        for (int i = 0; i < itemCount; i++) {
            items.add(prefix + i);
        }
        return items;
    }

    /**
     * Builds a {@code ColumnListView} of {@link MutableItem}s the same way {@link #newColumnListView} does for
     * plain strings — see there for details. The cell factory renders each item's current text into a
     * {@code Label} graphic, the same pattern {@code ColumnListViewTest} uses. Used by the
     * {@code updateCell}/{@code updateCells} tests, which need an item whose displayed value can mutate
     * without the items list itself changing.
     */
    private static ColumnListView<MutableItem> newMutableColumnListView(int itemCount, double columnWidth,
            double width, double height) {
        var listView = new ColumnListView<MutableItem>();
        listView.setColumnWidth(columnWidth);
        var items = FXCollections.<MutableItem>observableArrayList();
        for (int i = 0; i < itemCount; i++) {
            items.add(new MutableItem("item-" + i));
        }
        listView.setItems(items);
        listView.setCellFactory(lv -> new ColumnListCell<MutableItem>() {
            private final Label label = new Label();

            {
                setGraphic(label);
            }

            @Override
            protected void updateItem(MutableItem item, boolean empty) {
                super.updateItem(item, empty);
                label.setText(empty || item == null ? null : item.getText());
            }
        });
        stage.setScene(new Scene(listView, width, height));
        if (!stage.isShowing()) {
            stage.show();
        }
        listView.applyCss();
        listView.layout();
        return listView;
    }

    /**
     * The {@link ColumnTileView} counterpart of {@link #newMutableColumnListView}.
     */
    private static ColumnTileView<MutableItem> newMutableColumnTileView(int itemCount, int columnCount,
            double width, double height) {
        var tileView = new ColumnTileView<MutableItem>();
        tileView.setColumnCount(columnCount);
        var items = FXCollections.<MutableItem>observableArrayList();
        for (int i = 0; i < itemCount; i++) {
            items.add(new MutableItem("item-" + i));
        }
        tileView.setItems(items);
        tileView.setCellFactory(tv -> new TileCell<MutableItem>() {
            private final Label label = new Label();

            {
                setGraphic(label);
            }

            @Override
            protected void updateItem(MutableItem item, boolean empty) {
                super.updateItem(item, empty);
                label.setText(empty || item == null ? null : item.getText());
            }
        });
        stage.setScene(new Scene(tileView, width, height));
        if (!stage.isShowing()) {
            stage.show();
        }
        tileView.applyCss();
        tileView.layout();
        return tileView;
    }

    /**
     * Returns the currently rendered text of the cell showing {@code itemIndex}, or {@code null} if it isn't
     * currently realized. Reads via {@link ColumnListView#getCell(int)} (package-private, this test is in the
     * same package) rather than a CSS lookup: {@code .cell} matches both the outer flow cell (a whole column)
     * and the inner item cells inside it, since both are plain {@code IndexedCell}s with no distinguishing
     * style class of their own.
     */
    private static String cellText(ColumnListView<MutableItem> listView, int itemIndex) {
        var cell = listView.getCell(itemIndex);
        return cell == null ? null : ((Label) cell.getGraphic()).getText();
    }

    /**
     * The {@link ColumnTileView} counterpart of {@link #cellText(ColumnListView, int)}.
     */
    private static String cellText(ColumnTileView<MutableItem> tileView, int itemIndex) {
        var cell = tileView.getCell(itemIndex);
        return cell == null ? null : ((Label) cell.getGraphic()).getText();
    }

    private static VirtualFlow<?> flowOf(Region owner) {
        return (VirtualFlow<?>) owner.lookup(".virtual-flow");
    }

    private static int firstVisibleFlowIndex(Region owner) {
        return flowOf(owner).getFirstVisibleCell().getIndex();
    }

    /**
     * Repeatedly pumps the FX Application Thread (re-applying CSS/layout each time) until {@code view}'s
     * outer flow has realized at least one cell, or gives up after a generous number of attempts. See the
     * class Javadoc for why a freshly constructed view needs this at all.
     */
    private static void awaitCellsRealized(Region view) throws InterruptedException {
        for (var attempt = 0; attempt < 50; attempt++) {
            var ready = FxTestSupport.onFxThread(() -> {
                view.applyCss();
                view.layout();
                var flow = flowOf(view);
                return flow != null && flow.getCellCount() > 0 && flow.getFirstVisibleCell() != null;
            });
            if (ready) {
                return;
            }
        }
        throw new IllegalStateException("View never realized any cells: " + view);
    }

    // ColumnListView: isFullyVisible

    @Test
    void isFullyVisible_columnListViewIndexWithinInitialViewport_returnsTrue() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var visible = FxTestSupport.onFxThread(() -> ColumnViewUtils.isFullyVisible(listView, 0));

        assertThat(visible).isTrue();
    }

    @Test
    void isFullyVisible_columnListViewIndexBeyondInitialViewport_returnsFalse() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var visible = FxTestSupport.onFxThread(() -> ColumnViewUtils.isFullyVisible(listView, 199));

        assertThat(visible).isFalse();
    }

    @Test
    void isFullyVisible_columnListViewAfterItemsReplaced_reflectsNewViewport() throws InterruptedException {
        // This is the regression this whole utility exists for: reading the viewport right after a structural
        // change must be reliable, not just work on an already-stable view.
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(3, 80, 200, 300)); // all 3 fit in one column
        awaitCellsRealized(listView);

        var visible = FxTestSupport.onFxThread(() -> {
            listView.setItems(items(200, "big-item-"));
            return new Pair<>(ColumnViewUtils.isFullyVisible(listView, 0),
                    ColumnViewUtils.isFullyVisible(listView, 199));
        });

        assertThat(visible.first).isTrue();
        assertThat(visible.second).isFalse();
    }

    // ColumnListView: scrollTo

    @Test
    void scrollTo_columnListViewPositionStart_targetBecomesFirstVisibleColumn() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var result = FxTestSupport.onFxThread(() -> {
            var expectedColumn = listView.resolveColumnIndex(100);
            ColumnViewUtils.scrollTo(listView, 100, ScrollPosition.START);
            return new Pair<>(firstVisibleFlowIndex(listView), expectedColumn);
        });

        assertThat(result.first).isEqualTo(result.second);
    }

    @Test
    void scrollTo_columnListViewPositionEnd_targetBecomesLastVisibleColumn() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var result = FxTestSupport.onFxThread(() -> {
            ColumnViewUtils.scrollTo(listView, 100, ScrollPosition.END);
            // Checked via isFullyVisible (the API under test), not the raw VirtualFlow cell: the raw
            // getLastVisibleCell() can be only partially visible (sticking out past the viewport edge) —
            // exactly the case visibleRange() (inside VirtualFlowUtils) exists to exclude, so comparing
            // against it directly would fail even when scrollTo positioned things correctly.
            var nextColumnFirstItem = (listView.resolveColumnIndex(100) + 1) * listView.getRowCount();
            return new Pair<>(ColumnViewUtils.isFullyVisible(listView, 100),
                    ColumnViewUtils.isFullyVisible(listView, nextColumnFirstItem));
        });

        assertThat(result.first).isTrue();
        assertThat(result.second).isFalse();
    }

    @Test
    void scrollTo_columnListViewPositionCenter_targetBecomesVisible() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var visible = FxTestSupport.onFxThread(() -> {
            ColumnViewUtils.scrollTo(listView, 100, ScrollPosition.CENTER);
            return ColumnViewUtils.isFullyVisible(listView, 100);
        });

        assertThat(visible).isTrue();
    }

    // ColumnListView: scrollToIfNeeded

    @Test
    void scrollToIfNeeded_columnListViewIndexAlreadyFullyVisible_doesNotMovePosition() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var result = FxTestSupport.onFxThread(() -> {
            var flow = flowOf(listView);
            var positionBefore = flow.getPosition();
            ColumnViewUtils.scrollToIfNeeded(listView, 0, ScrollPosition.CENTER);
            return new Pair<>(positionBefore, flow.getPosition());
        });

        assertThat(result.second).isEqualTo(result.first);
    }

    @Test
    void scrollToIfNeeded_columnListViewRightAfterItemsReplaced_reachesNewlyAddedIndex()
            throws InterruptedException {
        // Mirrors the real bug this API was built to fix: select+reveal an index that only exists after a
        // structural change, in the same call that discovers it, with no separate warm-up layout pass.
        var listView = FxTestSupport.onFxThread(() -> newColumnListView(3, 80, 200, 300)); // all 3 fit in one column
        awaitCellsRealized(listView);

        var visible = FxTestSupport.onFxThread(() -> {
            listView.setItems(items(200, "big-item-"));
            ColumnViewUtils.scrollToIfNeeded(listView, 199, ScrollPosition.CENTER);
            return ColumnViewUtils.isFullyVisible(listView, 199);
        });

        assertThat(visible).isTrue();
    }

    // ColumnTileView: isFullyVisible

    @Test
    void isFullyVisible_columnTileViewIndexWithinInitialViewport_returnsTrue() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var visible = FxTestSupport.onFxThread(() -> ColumnViewUtils.isFullyVisible(tileView, 0));

        assertThat(visible).isTrue();
    }

    @Test
    void isFullyVisible_columnTileViewIndexBeyondInitialViewport_returnsFalse() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var visible = FxTestSupport.onFxThread(() -> ColumnViewUtils.isFullyVisible(tileView, 199));

        assertThat(visible).isFalse();
    }

    @Test
    void isFullyVisible_columnTileViewAfterItemsReplaced_reflectsNewViewport() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(9, 3, 200, 300)); // all 9 fit within 3 rows
        awaitCellsRealized(tileView);

        var visible = FxTestSupport.onFxThread(() -> {
            tileView.setItems(items(200, "big-item-"));
            return new Pair<>(ColumnViewUtils.isFullyVisible(tileView, 0),
                    ColumnViewUtils.isFullyVisible(tileView, 199));
        });

        assertThat(visible.first).isTrue();
        assertThat(visible.second).isFalse();
    }

    // ColumnTileView: scrollTo

    @Test
    void scrollTo_columnTileViewPositionStart_targetBecomesFirstVisibleRow() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var result = FxTestSupport.onFxThread(() -> {
            var expectedRow = tileView.resolveRowIndex(100);
            ColumnViewUtils.scrollTo(tileView, 100, ScrollPosition.START);
            return new Pair<>(firstVisibleFlowIndex(tileView), expectedRow);
        });

        assertThat(result.first).isEqualTo(result.second);
    }

    @Test
    void scrollTo_columnTileViewPositionEnd_targetBecomesLastVisibleRow() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var result = FxTestSupport.onFxThread(() -> {
            ColumnViewUtils.scrollTo(tileView, 100, ScrollPosition.END);
            // Checked via isFullyVisible (the API under test), not the raw VirtualFlow cell — see the
            // ColumnListView positionEnd test for why.
            var nextRowFirstItem = (tileView.resolveRowIndex(100) + 1) * tileView.getColumnCount();
            return new Pair<>(ColumnViewUtils.isFullyVisible(tileView, 100),
                    ColumnViewUtils.isFullyVisible(tileView, nextRowFirstItem));
        });

        assertThat(result.first).isTrue();
        assertThat(result.second).isFalse();
    }

    @Test
    void scrollTo_columnTileViewPositionCenter_targetBecomesVisible() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var visible = FxTestSupport.onFxThread(() -> {
            ColumnViewUtils.scrollTo(tileView, 100, ScrollPosition.CENTER);
            return ColumnViewUtils.isFullyVisible(tileView, 100);
        });

        assertThat(visible).isTrue();
    }

    // ColumnTileView: scrollToIfNeeded

    @Test
    void scrollToIfNeeded_columnTileViewIndexAlreadyFullyVisible_doesNotMovePosition() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var result = FxTestSupport.onFxThread(() -> {
            var flow = flowOf(tileView);
            var positionBefore = flow.getPosition();
            ColumnViewUtils.scrollToIfNeeded(tileView, 0, ScrollPosition.CENTER);
            return new Pair<>(positionBefore, flow.getPosition());
        });

        assertThat(result.second).isEqualTo(result.first);
    }

    @Test
    void scrollToIfNeeded_columnTileViewRightAfterItemsReplaced_reachesNewlyAddedIndex()
            throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newColumnTileView(9, 3, 200, 300)); // all 9 fit within 3 rows
        awaitCellsRealized(tileView);

        var visible = FxTestSupport.onFxThread(() -> {
            tileView.setItems(items(200, "big-item-"));
            ColumnViewUtils.scrollToIfNeeded(tileView, 199, ScrollPosition.CENTER);
            return ColumnViewUtils.isFullyVisible(tileView, 199);
        });

        assertThat(visible).isTrue();
    }

    // ColumnListView: updateCell

    @Test
    void updateCell_columnListView_itemMutatedInPlace_cellTextReflectsNewValue() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newMutableColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var result = FxTestSupport.onFxThread(() -> {
            listView.getItems().get(5).setText("changed");
            var stillStale = cellText(listView, 5);
            ColumnViewUtils.updateCell(listView, 5);
            return new Pair<>(stillStale, cellText(listView, 5));
        });

        assertThat(result.first).isEqualTo("item-5");
        assertThat(result.second).isEqualTo("changed");
    }

    @Test
    void updateCell_columnListView_indexOutOfRange_doesNotThrow() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newMutableColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        FxTestSupport.onFxThread(() -> {
            ColumnViewUtils.updateCell(listView, 999);
            return null;
        });
    }

    // ColumnListView: updateCells

    @Test
    void updateCells_columnListViewOnlyVisibleTrue_doesNotRealizeColumnsBeyondViewport() throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newMutableColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var result = FxTestSupport.onFxThread(() -> {
            listView.getItems().get(0).setText("changed-visible");
            listView.getItems().get(199).setText("changed-far");
            ColumnViewUtils.updateCells(listView, true);
            // Read the visible-item text and the raw realized range, deliberately not cellText(listView, 199):
            // ColumnListView#getCell (which cellText reads through) creates the target column on demand if
            // it isn't already realized, so merely checking it would realize (and thus freshly render) it
            // regardless of what updateCells(true) did — that would defeat the point of this assertion.
            var stayedOutsideRealizedRange = flowOf(listView).getLastVisibleCell().getIndex()
                    < listView.resolveColumnIndex(199);
            return new Pair<>(cellText(listView, 0), stayedOutsideRealizedRange);
        });

        assertThat(result.first).isEqualTo("changed-visible");
        // Column 12 (holding item 199) is nowhere near this: only columns up to roughly 2-3 fit in a 200px
        // viewport at columnWidth=80, so updateCells(true) touching only the realized range never reaches it.
        assertThat(result.second).isTrue();
    }

    @Test
    void updateCells_columnListViewOnlyVisibleFalse_realizesAndUpdatesItemNeverScrolledTo()
            throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> newMutableColumnListView(200, 80, 200, 300));
        awaitCellsRealized(listView);

        var text = FxTestSupport.onFxThread(() -> {
            listView.getItems().get(199).setText("changed-far");
            ColumnViewUtils.updateCells(listView, false);
            return cellText(listView, 199);
        });

        assertThat(text).isEqualTo("changed-far");
    }

    // ColumnTileView: updateCell

    @Test
    void updateCell_columnTileView_itemMutatedInPlace_cellTextReflectsNewValue() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newMutableColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var result = FxTestSupport.onFxThread(() -> {
            tileView.getItems().get(5).setText("changed");
            var stillStale = cellText(tileView, 5);
            ColumnViewUtils.updateCell(tileView, 5);
            return new Pair<>(stillStale, cellText(tileView, 5));
        });

        assertThat(result.first).isEqualTo("item-5");
        assertThat(result.second).isEqualTo("changed");
    }

    @Test
    void updateCell_columnTileView_indexOutOfRange_doesNotThrow() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newMutableColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        FxTestSupport.onFxThread(() -> {
            ColumnViewUtils.updateCell(tileView, 999);
            return null;
        });
    }

    // ColumnTileView: updateCells

    @Test
    void updateCells_columnTileViewOnlyVisibleTrue_doesNotRealizeRowsBeyondViewport() throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newMutableColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var result = FxTestSupport.onFxThread(() -> {
            tileView.getItems().get(0).setText("changed-visible");
            tileView.getItems().get(199).setText("changed-far");
            ColumnViewUtils.updateCells(tileView, true);
            // Deliberately not cellText(tileView, 199) — see the identical ColumnListView test for why reading
            // through ColumnTileView#getCell would realize (and thus freshly render) the target row itself.
            var stayedOutsideRealizedRange = flowOf(tileView).getLastVisibleCell().getIndex()
                    < tileView.resolveRowIndex(199);
            return new Pair<>(cellText(tileView, 0), stayedOutsideRealizedRange);
        });

        assertThat(result.first).isEqualTo("changed-visible");
        assertThat(result.second).isTrue();
    }

    @Test
    void updateCells_columnTileViewOnlyVisibleFalse_realizesAndUpdatesItemNeverScrolledTo()
            throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> newMutableColumnTileView(200, 3, 200, 300));
        awaitCellsRealized(tileView);

        var text = FxTestSupport.onFxThread(() -> {
            tileView.getItems().get(199).setText("changed-far");
            ColumnViewUtils.updateCells(tileView, false);
            return cellText(tileView, 199);
        });

        assertThat(text).isEqualTo("changed-far");
    }
}
