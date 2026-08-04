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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@code ColumnListView} against a real, shown {@code Stage} &mdash; see {@code ColumnViewUtilsTest}
 * for why a real display is required.
 *
 * @author Pavel Castornii
 */
class ColumnListViewTest {

    private static Stage stage;

    @BeforeAll
    static void initJavaFxToolkit() throws InterruptedException {
        FxTestSupport.start();
        FxPlatform.runLaterAndWait(() -> stage = new Stage());
    }

    @AfterAll
    static void closeStage() throws InterruptedException {
        FxPlatform.runLaterAndWait(() -> stage.hide());
    }

    private static double computePrefWidth(ColumnListView<?> listView) throws ReflectiveOperationException {
        Method method = AbstractColumnView.class.getDeclaredMethod("computePrefWidth", double.class);
        method.setAccessible(true);
        return (double) method.invoke(listView, -1.0);
    }

    private static double computePrefHeight(ColumnListView<?> listView) throws ReflectiveOperationException {
        Method method = AbstractColumnView.class.getDeclaredMethod("computePrefHeight", double.class);
        method.setAccessible(true);
        return (double) method.invoke(listView, -1.0);
    }

    /**
     * Builds a {@code ColumnListView} with {@code itemCount} string items and a fixed, explicit
     * {@code columnWidth}, makes it the content of {@link #stage}'s scene at {@code width}x{@code height},
     * shows the stage (a no-op if already showing), forces a layout pass, and polls until row height has
     * actually resolved (see {@code ColumnViewUtilsTest}'s class Javadoc for why that needs its own pulses).
     */
    private static ColumnListView<String> newRealizedListView(int itemCount, double columnWidth, double width,
            double height) throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setColumnWidth(columnWidth);
            var items = FXCollections.<String>observableArrayList();
            for (int i = 0; i < itemCount; i++) {
                items.add("item-" + i);
            }
            view.setItems(items);
            stage.setScene(new Scene(view, width, height));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }
        return listView;
    }

    private static void pressKey(ColumnListView<?> view, KeyCode code) throws InterruptedException {
        FxTestSupport.onFxThread(() -> {
            Event.fireEvent(view, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
            view.applyCss();
            view.layout();
            return null;
        });
    }

    /**
     * Mirrors {@code ColumnListView#lastFullyVisibleColumnIndex} against the public API, to state expectations
     * independently of that private implementation. "Fully visible" is checked against the cell's own
     * rendered bounds (translated into the view's coordinate space), matching production - see
     * {@code ColumnListView#isColumnFullyVisible}. Must only be called on the FX Application Thread (e.g.
     * from within {@link FxTestSupport#onFxThread}) since it reads live scene-graph/layout state.
     */
    private static int lastFullyVisibleColumnIndex(ColumnListView<?> view) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var last = flow.getLastVisibleCell();
        var index = last.getIndex();
        return isColumnFullyVisible(view, last) ? index : index - 1;
    }

    /**
     * Must only be called on the FX Application Thread - see {@link #lastFullyVisibleColumnIndex}.
     */
    private static int firstFullyVisibleColumnIndex(ColumnListView<?> view) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var first = flow.getFirstVisibleCell();
        var index = first.getIndex();
        return isColumnFullyVisible(view, first) ? index : index + 1;
    }

    /**
     * Must only be called on the FX Application Thread - see {@link #lastFullyVisibleColumnIndex}.
     */
    private static boolean isColumnFullyVisible(ColumnListView<?> view, IndexedCell<?> column) {
        var bounds = view.sceneToLocal(column.localToScene(column.getBoundsInLocal()));
        return bounds != null && bounds.getMinX() >= -0.5 && bounds.getMaxX() <= view.getWidth() + 0.5;
    }

    @Test
    void computePrefWidth_itemsShrunkInPlaceBeforeManualRefresh_doesNotThrow() throws InterruptedException {
        // Reproduces a real crash (IllegalArgumentException: fromIndex > toIndex from
        // ColumnListViewColumn#updateCells) seen with FileChooser's manual-refresh list view: a column's own
        // cached offset can momentarily point past the end of an in-place-shrunk items list, whenever
        // something reads the view's geometry (here: a fresh prefWidth computation, exactly like a parent
        // container auto-sizing to content would trigger) between the items mutation and the explicit
        // refresh() call that would otherwise rebuild offsets/rowCount to match.
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < 500; i++) {
            items.add("item-" + i);
        }
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setColumnWidth(80);
            view.setManualRefresh(true);
            view.setItems(items);
            view.refresh();
            stage.setScene(new Scene(view, 300, 150));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }

        assertThatCode(() -> FxTestSupport.onFxThread(() -> {
            items.clear();
            items.addAll(List.of("new-0", "new-1", "new-2"));
            try {
                return computePrefWidth(listView);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        })).doesNotThrowAnyException();
    }

    @Test
    void updateItem_focusedCellDiscardedByItemsShrink_focusMovesToListView() throws InterruptedException {
        // Reproduces a real FileChooser bug: the user clicks a folder deep in the list (focus lands on its
        // ColumnListCell, not the outer ColumnListView - see ColumnListCell's MOUSE_CLICKED filter), then
        // navigates into it, which shrinks items drastically. The column holding that cell gets recycled for
        // a different offset; removing a focused node from the scene graph does not reassign focus, so
        // Scene.getFocusOwner() was left pointing at a now-detached cell and no further key event (e.g.
        // arrow-key navigation) had anywhere live to go.
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < 500; i++) {
            items.add("item-" + i);
        }
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setColumnWidth(80);
            view.setManualRefresh(true);
            view.setItems(items);
            view.refresh();
            stage.setScene(new Scene(view, 300, 150));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }

        var focusedCell = FxTestSupport.onFxThread(() -> {
            var columnIndex = listView.resolveColumnIndex(50);
            listView.scrollToFirstColumn(columnIndex);
            listView.applyCss();
            listView.layout();
            listView.getSelectionModel().select(50);
            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            var column = flow.getFirstVisibleCell();
            var columnNode = (VBox) column.getGraphic();
            var cellIndexWithinColumn = 50 - columnIndex * listView.getRowCount();
            var cell = columnNode.getChildren().get(cellIndexWithinColumn);
            cell.requestFocus();
            return cell;
        });

        FxTestSupport.onFxThread(() -> {
            items.clear();
            items.addAll(List.of("new-0", "new-1", "new-2"));
            listView.refresh();
            listView.applyCss();
            listView.layout();
            return null;
        });

        assertThat(FxTestSupport.onFxThread(listView::isFocused)).isTrue();
        assertThat(FxTestSupport.onFxThread(focusedCell::isFocused)).isFalse();
    }

    // HOME / END / PAGE_UP / PAGE_DOWN

    @Test
    void selectHome_afterScrollingAway_selectsFirstItemAndScrollsToStart() throws InterruptedException {
        var listView = newRealizedListView(200, 80, 250, 300);
        pressKey(listView, KeyCode.END);

        pressKey(listView, KeyCode.HOME);

        FxTestSupport.onFxThread(() -> {
            assertThat(listView.getSelectionModel().getSelectedIndex()).isZero();
            assertThat(firstFullyVisibleColumnIndex(listView)).isZero();
            return null;
        });
    }

    @Test
    void selectEnd_fromStart_selectsLastItemAndScrollsToEnd() throws InterruptedException {
        var listView = newRealizedListView(200, 80, 250, 300);

        pressKey(listView, KeyCode.END);

        FxTestSupport.onFxThread(() -> {
            assertThat(listView.getSelectionModel().getSelectedIndex()).isEqualTo(199);
            assertThat(ColumnViewUtils.isFullyVisible(listView, 199)).isTrue();
            return null;
        });
    }

    @Test
    void selectPageDown_selectionNotAtPageEnd_selectsLastFullyVisibleColumnWithoutScrolling()
            throws InterruptedException {
        var listView = newRealizedListView(200, 80, 250, 300);
        var before = FxTestSupport.onFxThread(() -> {
            var firstColumnBefore = firstFullyVisibleColumnIndex(listView);
            var lastFullyVisibleColumn = lastFullyVisibleColumnIndex(listView);
            var expectedTarget = (lastFullyVisibleColumn + 1) * listView.getRowCount() - 1;
            return Map.entry(firstColumnBefore, expectedTarget);
        });

        pressKey(listView, KeyCode.PAGE_DOWN);

        FxTestSupport.onFxThread(() -> {
            assertThat(listView.getSelectionModel().getSelectedIndex()).isEqualTo(before.getValue());
            assertThat(firstFullyVisibleColumnIndex(listView)).isEqualTo(before.getKey());
            return null;
        });
    }

    @Test
    void selectPageDown_selectionAlreadyAtPageEnd_scrollsForwardAndSelectsNewPageEnd() throws InterruptedException {
        var listView = newRealizedListView(200, 80, 250, 300);
        var firstColumnBefore = FxTestSupport.onFxThread(() -> firstFullyVisibleColumnIndex(listView));
        pressKey(listView, KeyCode.PAGE_DOWN); // lands on the current page's last item, no scroll yet

        pressKey(listView, KeyCode.PAGE_DOWN); // already there - now it must scroll forward

        FxTestSupport.onFxThread(() -> {
            assertThat(firstFullyVisibleColumnIndex(listView)).isGreaterThan(firstColumnBefore);
            var lastFullyVisibleColumn = lastFullyVisibleColumnIndex(listView);
            var expectedTarget = (lastFullyVisibleColumn + 1) * listView.getRowCount() - 1;
            assertThat(listView.getSelectionModel().getSelectedIndex()).isEqualTo(expectedTarget);
            return null;
        });
    }

    @Test
    void selectPageUp_selectionNotAtPageStart_selectsFirstFullyVisibleColumnWithoutScrolling()
            throws InterruptedException {
        var listView = newRealizedListView(200, 80, 250, 300);
        pressKey(listView, KeyCode.END);
        var lastColumnBefore = FxTestSupport.onFxThread(() -> lastFullyVisibleColumnIndex(listView));

        pressKey(listView, KeyCode.PAGE_UP);

        FxTestSupport.onFxThread(() -> {
            var firstFullyVisibleColumn = firstFullyVisibleColumnIndex(listView);
            assertThat(listView.getSelectionModel().getSelectedIndex())
                    .isEqualTo(firstFullyVisibleColumn * listView.getRowCount());
            assertThat(lastFullyVisibleColumnIndex(listView)).isEqualTo(lastColumnBefore);
            return null;
        });
    }

    @Test
    void selectPageUp_selectionAlreadyAtPageStart_scrollsBackAndSelectsNewPageStart() throws InterruptedException {
        var listView = newRealizedListView(200, 80, 250, 300);
        pressKey(listView, KeyCode.END);
        var lastColumnBefore = FxTestSupport.onFxThread(() -> lastFullyVisibleColumnIndex(listView));
        pressKey(listView, KeyCode.PAGE_UP); // lands on the current page's first item, no scroll yet
        var firstColumnAfterFirstPress = FxTestSupport.onFxThread(() -> firstFullyVisibleColumnIndex(listView));

        pressKey(listView, KeyCode.PAGE_UP); // already there - now it must scroll back

        FxTestSupport.onFxThread(() -> {
            assertThat(lastFullyVisibleColumnIndex(listView)).isLessThan(lastColumnBefore);
            var firstFullyVisibleColumn = firstFullyVisibleColumnIndex(listView);
            assertThat(firstFullyVisibleColumn).isLessThan(firstColumnAfterFirstPress);
            assertThat(listView.getSelectionModel().getSelectedIndex())
                    .isEqualTo(firstFullyVisibleColumn * listView.getRowCount());
            return null;
        });
    }

    // visibleColumnCount

    /**
     * Builds a {@code ColumnListView} sized via {@link ColumnListView#setVisibleColumnCount} instead of
     * {@link ColumnListView#setColumnWidth} - otherwise identical to {@link #newRealizedListView}.
     */
    private static ColumnListView<String> newRealizedListViewWithVisibleColumnCount(int itemCount,
            int visibleColumnCount, double width, double height) throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setVisibleColumnCount(visibleColumnCount);
            var items = FXCollections.<String>observableArrayList();
            for (int i = 0; i < itemCount; i++) {
                items.add("item-" + i);
            }
            view.setItems(items);
            stage.setScene(new Scene(view, width, height));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }
        return listView;
    }

    /**
     * Asserts exactly {@code expectedCount} columns are fully visible and that they fill {@code view}'s width
     * edge to edge, with no gap or partial column at either side - the core invariant
     * {@link ColumnListView#visibleColumnCount} exists to guarantee, regardless of scroll position. Must only
     * be called on the FX Application Thread.
     */
    private static void assertExactlyFillsViewport(ColumnListView<?> view, int expectedCount) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var first = flow.getFirstVisibleCell();
        var last = flow.getLastVisibleCell();
        assertThat(last.getIndex() - first.getIndex() + 1).isEqualTo(expectedCount);
        assertThat(first.getBoundsInParent().getMinX()).isZero();
        assertThat(last.getBoundsInParent().getMaxX()).isEqualTo(view.getWidth());
    }

    @Test
    void setVisibleColumnCount_initialPage_exactlyFillsViewportWithNoPartialColumn() throws InterruptedException {
        // 251 is deliberately not evenly divisible by 3, to exercise the remainder-distribution formula.
        var listView = newRealizedListViewWithVisibleColumnCount(200, 3, 251, 300);

        FxTestSupport.onFxThread(() -> {
            assertExactlyFillsViewport(listView, 3);
            return null;
        });
    }

    @Test
    void setVisibleColumnCount_scrolledToMiddle_stillExactlyFillsViewportWithNoPartialColumn()
            throws InterruptedException {
        var listView = newRealizedListViewWithVisibleColumnCount(200, 3, 251, 300);

        FxTestSupport.onFxThread(() -> {
            listView.scrollToFirstColumn(5);
            listView.applyCss();
            listView.layout();
            assertExactlyFillsViewport(listView, 3);
            return null;
        });
    }

    @Test
    void setVisibleColumnCount_columnWidthAlsoSet_visibleColumnCountWinsOverColumnWidth()
            throws InterruptedException {
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setColumnWidth(10); // deliberately a very different width, to prove it gets ignored
            view.setVisibleColumnCount(3);
            var items = FXCollections.<String>observableArrayList();
            for (int i = 0; i < 200; i++) {
                items.add("item-" + i);
            }
            view.setItems(items);
            stage.setScene(new Scene(view, 251, 300));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }

        FxTestSupport.onFxThread(() -> {
            assertExactlyFillsViewport(listView, 3);
            return null;
        });
    }

    @Test
    void setVisibleColumnCount_viewResized_recomputesToStillFillNewWidthExactly() throws InterruptedException {
        var listView = newRealizedListViewWithVisibleColumnCount(200, 3, 251, 300);

        FxTestSupport.onFxThread(() -> {
            // 331 is, like 251, deliberately not evenly divisible by 3. Resizing the stage (not swapping in a
            // new Scene) is what a real window resize looks like, and it is what actually drives
            // ColumnListView's own widthProperty listener.
            stage.setWidth(331);
            listView.applyCss();
            listView.layout();
            return null;
        });

        FxTestSupport.onFxThread(() -> {
            assertExactlyFillsViewport(listView, 3);
            return null;
        });
    }

    @Test
    void setVisibleColumnCount_notEnoughItemsForOneColumn_stillShowsAllFillerColumnsFillingViewport()
            throws InterruptedException {
        // Only 3 items - way fewer than a single column's row count, so 2 of the 3 desired columns are
        // filler ones (created by VirtualFlow itself past the last real column, with no item/offset at all)
        // rather than real data columns. getFirstVisibleCell()/getLastVisibleCell() do not count those (they
        // are scoped to [0, cellCount)), so this checks each column directly via getCell(index) instead.
        var listView = newRealizedListViewWithVisibleColumnCount(3, 3, 251, 300);

        FxTestSupport.onFxThread(() -> {
            assertThat(listView.getColumnCount()).isEqualTo(1);
            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            var totalWidth = 0.0;
            for (var i = 0; i < 3; i++) {
                var cell = flow.getCell(i);
                assertThat(cell.getIndex()).isEqualTo(i);
                totalWidth += ((VBox) cell.getGraphic()).getWidth();
            }
            assertThat(totalWidth).isEqualTo(listView.getWidth());
            return null;
        });
    }

    @Test
    void selectPageDown_viewportNotYetPageAligned_usesVisibleColumnCountAsPageWidth() throws InterruptedException {
        // Regression test: right after a manual scrollbar drag (or any other reason the viewport isn't yet
        // page-aligned), lastFullyVisibleColumnIndex - firstFullyVisibleColumnIndex + 1 can under-count
        // relative to visibleColumnCount (e.g. report 2 fully visible columns even though visibleColumnCount
        // guarantees 3 once aligned), landing selectPageDown's scroll in the middle of the new page instead
        // of at its genuine end.
        var listView = newRealizedListViewWithVisibleColumnCount(200, 3, 300, 300);
        FxTestSupport.onFxThread(() -> {
            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            flow.scrollPixels(5); // small, sub-column nudge - breaks page alignment without changing columns
            listView.applyCss();
            listView.layout();
            return null;
        });
        var spanBeforeScroll = FxTestSupport.onFxThread(
                () -> lastFullyVisibleColumnIndex(listView) - firstFullyVisibleColumnIndex(listView) + 1);
        assertThat(spanBeforeScroll).as("test setup must under-align relative to visibleColumnCount").isLessThan(3);

        pressKey(listView, KeyCode.PAGE_DOWN); // lands on the current (narrow) page's end, no scroll yet
        var nextColumn = FxTestSupport.onFxThread(() -> lastFullyVisibleColumnIndex(listView) + 1);

        pressKey(listView, KeyCode.PAGE_DOWN); // already there - now it must scroll, pageWidth = visibleColumnCount

        FxTestSupport.onFxThread(() -> {
            var expectedNewLastColumn = Math.min(listView.getColumnCount() - 1,
                    nextColumn + listView.getVisibleColumnCount() - 1);
            var expectedTarget = (expectedNewLastColumn + 1) * listView.getRowCount() - 1;
            assertThat(listView.getSelectionModel().getSelectedIndex()).isEqualTo(expectedTarget);
            return null;
        });
    }

    @Test
    void selectPageDown_repeatedlyToEnd_neverLeavesMoreThanOneCellHighlighted() throws InterruptedException {
        // Regression test: a stale highlight was observed to remain on an old cell after PageDown, alongside
        // the newly selected one - most easily reproduced near the tail of the data with visibleColumnCount
        // set, where consecutive pages can end up overlapping by a repeated column (see
        // selectPageDown_viewportNotYetPageAligned_usesVisibleColumnCountAsPageWidth) and the overlapping
        // column's cells aren't recycled, so they rely entirely on the centralized selection listener to have
        // their old highlight cleared.
        var itemCount = 149;
        var listView = newRealizedListViewWithVisibleColumnCount(itemCount, 3, 300, 300);
        for (var attempt = 0; attempt < 20; attempt++) {
            pressKey(listView, KeyCode.PAGE_DOWN);
            var attemptNumber = attempt;
            var reachedEnd = FxTestSupport.onFxThread(() -> {
                assertThat(countHighlightedCellsInVisibleRange(listView))
                        .as("after PAGE_DOWN #" + attemptNumber)
                        .isLessThanOrEqualTo(1);
                return listView.getSelectionModel().getSelectedIndex() == itemCount - 1;
            });
            if (reachedEnd) {
                break;
            }
        }
    }

    @Test
    void selectPageDown_fromPartiallyVisibleColumn_neverLeavesMoreThanOneCellHighlighted()
            throws InterruptedException {
        // The double-highlight was specifically observed only when some column was partially (not fully)
        // visible at the moment PageDown was pressed - e.g. right after a manual scrollbar drag left the
        // viewport mid-column rather than at a clean page-aligned position - so this repeatedly forces that
        // exact starting condition before each press, rather than only reaching it incidentally near the end.
        var listView = newRealizedListViewWithVisibleColumnCount(200, 3, 300, 300);
        for (var attempt = 0; attempt < 10; attempt++) {
            var attemptNumber = attempt;
            FxTestSupport.onFxThread(() -> {
                var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
                flow.scrollPixels(5); // small, sub-column nudge - leaves a column only partially visible
                listView.applyCss();
                listView.layout();
                return null;
            });
            pressKey(listView, KeyCode.PAGE_DOWN);
            FxTestSupport.onFxThread(() -> {
                assertThat(countHighlightedCellsInVisibleRange(listView))
                        .as("after PAGE_DOWN #" + attemptNumber + ", from a partially visible column")
                        .isLessThanOrEqualTo(1);
                return null;
            });
        }
    }

    /**
     * Whether the cell holding {@code itemIndex} is currently showing the selected highlight. Must only be
     * called on the FX Application Thread.
     */
    private static boolean isCellHighlighted(ColumnListView<?> view, int itemIndex) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var columnIndex = view.resolveColumnIndex(itemIndex);
        var rowIndex = view.resolveRowIndex(itemIndex);
        var column = flow.getCell(columnIndex);
        if (column == null || column.getIndex() != columnIndex) {
            return false;
        }
        var columnNode = (VBox) column.getGraphic();
        if (rowIndex >= columnNode.getChildren().size()) {
            return false;
        }
        return ((ColumnListCell<?>) columnNode.getChildren().get(rowIndex)).isSelected();
    }

    /**
     * Counts how many cells are currently showing the selected highlight across every column between the
     * raw first and last visible ones (including any only partially visible), catching a stale highlight
     * left behind on a scrolled-past-but-still-live column. Must only be called on the FX Application Thread.
     */
    private static int countHighlightedCellsInVisibleRange(ColumnListView<?> view) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var first = flow.getFirstVisibleCell();
        var last = flow.getLastVisibleCell();
        if (first == null || last == null) {
            return 0;
        }
        var count = 0;
        for (var columnIndex = first.getIndex(); columnIndex <= last.getIndex(); columnIndex++) {
            var column = flow.getCell(columnIndex);
            var columnNode = (VBox) column.getGraphic();
            for (var node : columnNode.getChildren()) {
                if (((ColumnListCell<?>) node).isSelected()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void selectLeft_scrollingRequired_selectedCellShowsHighlight() throws InterruptedException {
        var listView = newRealizedListViewWithVisibleColumnCount(300, 3, 251, 300);
        var rowCount = FxTestSupport.onFxThread(listView::getRowCount);
        FxTestSupport.onFxThread(() -> {
            listView.scrollToFirstColumn(5);
            listView.applyCss();
            listView.layout();
            listView.getSelectionModel().select(5 * rowCount);
            listView.applyCss();
            listView.layout();
            return null;
        });

        pressKey(listView, KeyCode.LEFT);

        FxTestSupport.onFxThread(() -> {
            var expectedSelected = 4 * rowCount;
            assertThat(listView.getSelectionModel().getSelectedIndex()).isEqualTo(expectedSelected);
            assertThat(isCellHighlighted(listView, expectedSelected)).isTrue();
            return null;
        });
    }

    @Test
    void selectLeft_pressedRepeatedly_eachStepShowsHighlightAndScrollsSanely() throws InterruptedException {
        // Mirrors selectLeft_scrollingRequired_selectedCellShowsHighlight but repeated several times in a
        // row, since a real regression here (variable per-column widths confusing VirtualFlow's own internal
        // position estimate) only showed up after a few repeated scrolls, not on the very first one.
        var listView = newRealizedListViewWithVisibleColumnCount(300, 3, 251, 300);
        var rowCount = FxTestSupport.onFxThread(listView::getRowCount);
        FxTestSupport.onFxThread(() -> {
            listView.scrollToFirstColumn(9);
            listView.applyCss();
            listView.layout();
            listView.getSelectionModel().select(9 * rowCount);
            listView.applyCss();
            listView.layout();
            return null;
        });

        for (var column = 8; column >= 3; column--) {
            pressKey(listView, KeyCode.LEFT);
            var expectedColumn = column;
            FxTestSupport.onFxThread(() -> {
                var expectedSelected = expectedColumn * rowCount;
                assertThat(listView.getSelectionModel().getSelectedIndex()).isEqualTo(expectedSelected);
                assertThat(isCellHighlighted(listView, expectedSelected))
                        .as("column %d should show its selection highlight", expectedColumn).isTrue();
                assertThat(listView.getColumnCount())
                        .as("total column count must not have been corrupted by repeated scrolling")
                        .isEqualTo((int) Math.ceil(300.0 / rowCount));
                return null;
            });
        }
    }

    @Test
    void mousePressed_targetsInnerNodeOfAlreadySelectedCell_stillRequestsFocus() throws InterruptedException {
        // Reproduces a real bug: switching to LIST mode pre-selects an entry (e.g. "..") without focusing the
        // view (like switching mode via a menu does), and clicking that already-selected entry never focused
        // it either. ColumnListCell's own MOUSE_CLICKED handler only requests focus when selecting a cell for
        // the first time - an already-selected cell relies entirely on ColumnListView's own MOUSE_PRESSED
        // filter, which used to check e.getTarget() with a single instanceof - but a real click's pick target
        // is often a node deep inside the cell (its text/graphic), not the Cell itself, so that check silently
        // failed for every click on already-selected content.
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setColumnWidth(80);
            view.setCellFactory(lv -> new ColumnListCell<String>() {
                private final Label label = new Label();

                {
                    setGraphic(label);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    label.setText(empty ? null : item);
                }
            });
            var items = FXCollections.<String>observableArrayList();
            for (int i = 0; i < 20; i++) {
                items.add("item-" + i);
            }
            view.setItems(items);
            stage.setScene(new Scene(view, 250, 300));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }

        FxTestSupport.onFxThread(() -> {
            listView.getSelectionModel().select(0);
            listView.applyCss();
            listView.layout();

            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            var column = flow.getCell(0);
            var columnNode = (VBox) column.getGraphic();
            var cell0 = columnNode.getChildren().get(0);
            var innerLabel = ((ColumnListCell<?>) cell0).getGraphic();

            // Fires MOUSE_PRESSED with innerLabel as the literal event target (mimicking a real click that
            // lands on the cell's inner graphic/text, as picking would normally resolve) at cell0's screen
            // position.
            var bounds = cell0.localToScene(cell0.getBoundsInLocal());
            var sceneX = bounds.getMinX() + 2;
            var sceneY = bounds.getMinY() + 2;
            var pressed = new MouseEvent(MouseEvent.MOUSE_PRESSED, sceneX, sceneY, sceneX, sceneY,
                    MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, true, false, false,
                    null);
            Event.fireEvent(innerLabel, pressed);
            listView.applyCss();
            listView.layout();

            assertThat(listView.isFocused()).isTrue();
            return null;
        });
    }

    @Test
    void horizontalScrollbar_whenVisible_lastRowDoesNotOverlapIt() throws InterruptedException {
        // Regression test: getRowCount() must be computed against the viewport height minus the horizontal
        // scrollbar's own height (see ColumnListView#getViewportHeight()) once that scrollbar is actually
        // showing - otherwise the last row of cells gets rendered underneath it.
        var listView = newRealizedListView(200, 80, 250, 300);

        FxTestSupport.onFxThread(() -> {
            var hbar = (ScrollBar) listView.lookup(".scroll-bar:horizontal");
            assertThat(hbar).as("a horizontal scrollbar should be showing").isNotNull();
            assertThat(hbar.isVisible()).isTrue();
            var hbarBounds = listView.sceneToLocal(hbar.localToScene(hbar.getBoundsInLocal()));
            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            var column = flow.getFirstVisibleCell();
            var columnNode = (VBox) column.getGraphic();
            var lastCell = columnNode.getChildren().get(columnNode.getChildren().size() - 1);
            var lastCellBounds = listView.sceneToLocal(lastCell.localToScene(lastCell.getBoundsInLocal()));
            assertThat(lastCellBounds.getMaxY()).isLessThanOrEqualTo(hbarBounds.getMinY() + 0.5);
            return null;
        });
    }

    @Test
    void horizontalScrollbar_afterManualRefreshOnAlreadyShownView_lastRowDoesNotOverlapIt()
            throws InterruptedException {
        // Mirrors shellfx-dialogs' FileListView: manualRefresh, no columnWidth/visibleColumnCount set (CSS/
        // content-driven column width), starting small and growing via refresh() on an already-shown view -
        // like navigating from a small folder into a much larger one.
        var listView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnListView<String>();
            view.setManualRefresh(true);
            // The default ColumnListCell renders nothing (no setText/setGraphic), so columns would collapse
            // to zero content-driven width and never need a horizontal scrollbar regardless of item count -
            // a real cell factory (like FileListCell in shellfx-dialogs) is needed to reproduce this.
            view.setCellFactory(lv -> new ColumnListCell<String>() {
                private final Label label = new Label();

                {
                    setGraphic(label);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    label.setText(empty ? null : item);
                }
            });
            var items = FXCollections.<String>observableArrayList();
            for (int i = 0; i < 5; i++) {
                items.add("item-" + i);
            }
            view.setItems(items);
            view.refresh();
            // 306 = 17 * 18 (this view's row height, established empirically) - chosen so a naive
            // height/rowHeight computation lands exactly on 17, while correctly subtracting the horizontal
            // scrollbar's own ~8px height lands on 16 - the two disagree, so this test can actually tell
            // whether the subtraction happens instead of both landing on the same floor()-truncated value.
            stage.setScene(new Scene(view, 250, 306));
            if (!stage.isShowing()) {
                stage.show();
            }
            view.applyCss();
            view.layout();
            return view;
        });
        for (var attempt = 0; attempt < 50 && listView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }

        FxTestSupport.onFxThread(() -> {
            var items = FXCollections.<String>observableArrayList();
            for (int i = 0; i < 300; i++) {
                items.add("some-fairly-long-file-name-" + i + ".txt");
            }
            listView.setItems(items);
            listView.refresh();
            listView.applyCss();
            listView.layout();
            return null;
        });
        for (var attempt = 0; attempt < 50; attempt++) {
            FxTestSupport.onFxThread(() -> {
                listView.applyCss();
                listView.layout();
                return null;
            });
        }

        FxTestSupport.onFxThread(() -> {
            var hbar = (ScrollBar) listView.lookup(".scroll-bar:horizontal");
            assertThat(hbar).as("a horizontal scrollbar should be showing").isNotNull();
            assertThat(hbar.isVisible()).isTrue();
            var hbarBounds = listView.sceneToLocal(hbar.localToScene(hbar.getBoundsInLocal()));
            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            var column = flow.getFirstVisibleCell();
            var columnNode = (VBox) column.getGraphic();
            var lastCell = columnNode.getChildren().get(columnNode.getChildren().size() - 1);
            var lastCellBounds = listView.sceneToLocal(lastCell.localToScene(lastCell.getBoundsInLocal()));
            assertThat(lastCellBounds.getMaxY()).isLessThanOrEqualTo(hbarBounds.getMinY() + 0.5);
            return null;
        });
    }

    @Test
    void edit_unrelatedLayoutPassAfterwards_staysInEditingState() throws InterruptedException {
        // Regression test: a real bug seen in FileChooser (which uses ColumnListView) - starting an edit
        // (ColumnListCell#startEdit moves focus to the listView before creating the edit control) used to get
        // immediately undone by the very next, otherwise unrelated layout pass (e.g. the one that focus change
        // itself can trigger), because that pass used to unconditionally mark every column dirty, forcing the
        // editing column to rebuild from scratch and discard its mid-edit cell.
        var listView = newRealizedListView(20, 80, 250, 300);
        FxTestSupport.onFxThread(() -> {
            listView.setEditable(true);
            listView.edit(0);
            listView.applyCss();
            listView.layout();
            return null;
        });

        FxTestSupport.onFxThread(() -> {
            listView.requestLayout();
            listView.applyCss();
            listView.layout();

            var flow = (VirtualFlow<?>) listView.lookup(".virtual-flow");
            var column = flow.getCell(0);
            var columnNode = (VBox) column.getGraphic();
            var cell0 = (ColumnListCell<?>) columnNode.getChildren().get(0);
            assertThat(cell0.isEditing()).isTrue();
            return null;
        });
    }

    @Test
    void computePrefHeight_afterSettling_reusesActualHeightInsteadOfRescanningCellContent()
            throws InterruptedException {
        // AbstractColumnView#computePrefHeight() used to delegate straight to the virtual flow's own
        // prefHeight(), which realizes/recycles columns outside the visible range purely to measure them.
        // Recycling a column reassigns its cells to different items via updateItem(), and in the real
        // FileChooserDialog - shown inside a Priority.ALWAYS VBox that re-queries this method on every idle
        // layout pass - a recycled cell's Labeled text genuinely changing fired textMetricsChanged() ->
        // requestLayout(), re-arming the whole ancestor chain forever. That multi-pulse churn isn't reliably
        // reproducible in headless glass (nor was it in a real, live one, despite extensive attempts), but the
        // underlying defect doesn't need the full loop to observe: once the view has a real, already
        // established height, computePrefHeight() should just report it, not recompute something else from
        // realized cell content - which is exactly what it did before the fix (see the assertion below).
        var view = FxTestSupport.onFxThread(() -> {
            var v = new ColumnListView<Integer>();
            v.setCellFactory(lv -> new ColumnListCell<Integer>() {
                private final Label label = new Label();

                {
                    setGraphic(label);
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    label.setText(empty || item == null ? null : "Cell " + item);
                }
            });
            var items = FXCollections.<Integer>observableArrayList();
            for (int i = 0; i < 149; i++) {
                items.add(i);
            }
            v.setItems(items);
            stage.setScene(new Scene(v, 774, 289));
            if (!stage.isShowing()) {
                stage.show();
            }
            v.applyCss();
            v.layout();
            return v;
        });
        for (var attempt = 0; attempt < 50 && view.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                view.applyCss();
                view.layout();
                return null;
            });
        }

        var prefHeight = FxTestSupport.onFxThread(() -> {
            try {
                return computePrefHeight(view);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(prefHeight)
                .as("computePrefHeight() should report the view's actual, already-established height")
                .isEqualTo(view.getHeight());
    }
}
