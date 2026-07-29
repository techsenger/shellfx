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

import com.techsenger.toolkit.fx.FxPlatform;
import com.techsenger.toolkit.fx.utils.VirtualFlowUtils.ScrollPosition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
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
}
