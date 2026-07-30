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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@code ColumnTileView} against a real, shown {@code Stage} &mdash; see {@code ColumnViewUtilsTest}
 * for why a real display is required.
 *
 * @author Pavel Castornii
 */
class ColumnTileViewTest {

    /**
     * Fixed pixel height forced on every cell (see {@link #newRealizedTileView}) so that row height - and
     * everything paging/scrolling math derives from it - never depends on the font actually resolved on the
     * machine running the test, which is why this test suite used to be flaky in CI (a different font
     * substituted there produced a different row height than on a developer machine). Deliberately matches
     * what the font-driven measurement already resolved to locally - not just any fixed value works, see
     * {@code selectEnd_fromStart_selectsLastItemAndScrollsToEnd}'s history.
     */
    private static final double CELL_HEIGHT = 18;

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

    private static double computePrefHeight(ColumnTileView<?> tileView) throws ReflectiveOperationException {
        Method method = AbstractColumnView.class.getDeclaredMethod("computePrefHeight", double.class);
        method.setAccessible(true);
        return (double) method.invoke(tileView, -1.0);
    }

    /**
     * Builds a {@code ColumnTileView} with {@code itemCount} string items and a fixed, explicit
     * {@code columnCount}, makes it the content of {@link #stage}'s scene at {@code width}x{@code height},
     * shows the stage (a no-op if already showing), forces a layout pass, and polls until row height has
     * actually resolved (see {@code ColumnViewUtilsTest}'s class Javadoc for why that needs its own pulses).
     */
    private static ColumnTileView<String> newRealizedTileView(int itemCount, int columnCount, double width,
            double height) throws InterruptedException {
        var tileView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnTileView<String>();
            view.setColumnCount(columnCount);
            view.setCellFactory(v -> {
                var cell = new TileCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            var region = new Region();
                            region.setMinSize(1, CELL_HEIGHT);
                            region.setPrefSize(1, CELL_HEIGHT);
                            region.setMaxSize(Double.MAX_VALUE, CELL_HEIGHT);
                            setGraphic(region);
                        }
                    }
                };
                cell.setStyle("-fx-pref-height: " + CELL_HEIGHT + "px; -fx-min-height: " + CELL_HEIGHT
                        + "px; -fx-max-height: " + CELL_HEIGHT + "px;");
                return cell;
            });
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
        for (var attempt = 0; attempt < 50 && tileView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                tileView.applyCss();
                tileView.layout();
                return null;
            });
        }
        return tileView;
    }

    private static void pressKey(ColumnTileView<?> view, KeyCode code) throws InterruptedException {
        FxTestSupport.onFxThread(() -> {
            Event.fireEvent(view, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
            view.applyCss();
            view.layout();
            return null;
        });
    }

    /**
     * Mirrors {@code ColumnTileView#lastFullyVisibleRowIndex} against the public API, to state expectations
     * independently of that private implementation. "Fully visible" is checked against the cell's own
     * rendered bounds (translated into the view's coordinate space), matching production - see
     * {@code ColumnTileView#isRowFullyVisible}. Must only be called on the FX Application Thread (e.g. from
     * within {@link FxTestSupport#onFxThread}) since it reads live scene-graph/layout state.
     */
    private static int lastFullyVisibleRowIndex(ColumnTileView<?> view) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var last = flow.getLastVisibleCell();
        var index = last.getIndex();
        return isRowFullyVisible(view, last) ? index : index - 1;
    }

    /**
     * Must only be called on the FX Application Thread - see {@link #lastFullyVisibleRowIndex}.
     */
    private static int firstFullyVisibleRowIndex(ColumnTileView<?> view) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var first = flow.getFirstVisibleCell();
        var index = first.getIndex();
        return isRowFullyVisible(view, first) ? index : index + 1;
    }

    /**
     * Must only be called on the FX Application Thread - see {@link #lastFullyVisibleRowIndex}.
     */
    private static boolean isRowFullyVisible(ColumnTileView<?> view, IndexedCell<?> row) {
        var bounds = view.sceneToLocal(row.localToScene(row.getBoundsInLocal()));
        return bounds != null && bounds.getMinY() >= -0.5 && bounds.getMaxY() <= view.getHeight() + 0.5;
    }

    /**
     * Counts how many cells are currently showing the selected highlight across every row between the raw
     * first and last visible ones (including any only partially visible), catching a stale highlight left
     * behind on a scrolled-past-but-still-live row. Must only be called on the FX Application Thread.
     */
    private static int countHighlightedCellsInVisibleRange(ColumnTileView<?> view) {
        var flow = (VirtualFlow<?>) view.lookup(".virtual-flow");
        var first = flow.getFirstVisibleCell();
        var last = flow.getLastVisibleCell();
        if (first == null || last == null) {
            return 0;
        }
        var count = 0;
        for (var rowIndex = first.getIndex(); rowIndex <= last.getIndex(); rowIndex++) {
            var row = flow.getCell(rowIndex);
            var rowNode = (HBox) row.getGraphic();
            for (var node : rowNode.getChildren()) {
                if (((TileCell<?>) node).isSelected()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void computePrefHeight_itemsShrunkInPlaceBeforeManualRefresh_doesNotThrow() throws InterruptedException {
        // Mirrors ColumnListViewTest's regression: ColumnTileView's updateCells() has the identical
        // copy-pasted shape, so the identical fix applies - a row's own cached offset can momentarily point
        // past the end of an in-place-shrunk items list, whenever something reads the view's geometry between
        // the items mutation and the explicit refresh() call that would otherwise rebuild offsets/rowCount.
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < 500; i++) {
            items.add("item-" + i);
        }
        var tileView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnTileView<String>();
            view.setColumnCount(3);
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
        for (var attempt = 0; attempt < 50 && tileView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                tileView.applyCss();
                tileView.layout();
                return null;
            });
        }

        assertThatCode(() -> FxTestSupport.onFxThread(() -> {
            items.clear();
            items.addAll(List.of("new-0", "new-1", "new-2"));
            try {
                return computePrefHeight(tileView);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        })).doesNotThrowAnyException();
    }

    @Test
    void updateItem_focusedCellDiscardedByItemsShrink_focusMovesToTileView() throws InterruptedException {
        // Mirrors ColumnListViewTest's focus regression: the row holding a focused TileCell gets recycled for
        // a different offset once items shrink drastically; removing a focused node from the scene graph does
        // not reassign focus, so Scene.getFocusOwner() was left pointing at a now-detached cell.
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < 500; i++) {
            items.add("item-" + i);
        }
        var tileView = FxTestSupport.onFxThread(() -> {
            var view = new ColumnTileView<String>();
            view.setColumnCount(3);
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
        for (var attempt = 0; attempt < 50 && tileView.getRowCount() <= 1; attempt++) {
            FxTestSupport.onFxThread(() -> {
                tileView.applyCss();
                tileView.layout();
                return null;
            });
        }

        var focusedCell = FxTestSupport.onFxThread(() -> {
            var rowIndex = tileView.resolveRowIndex(50);
            tileView.scrollToFirstRow(rowIndex);
            tileView.applyCss();
            tileView.layout();
            tileView.getSelectionModel().select(50);
            var flow = (VirtualFlow<?>) tileView.lookup(".virtual-flow");
            var row = flow.getFirstVisibleCell();
            var rowNode = (HBox) row.getGraphic();
            var cellIndexWithinRow = 50 - rowIndex * tileView.getColumnCount();
            var cell = rowNode.getChildren().get(cellIndexWithinRow);
            cell.requestFocus();
            return cell;
        });

        FxTestSupport.onFxThread(() -> {
            items.clear();
            items.addAll(List.of("new-0", "new-1", "new-2"));
            tileView.refresh();
            tileView.applyCss();
            tileView.layout();
            return null;
        });

        assertThat(FxTestSupport.onFxThread(tileView::isFocused)).isTrue();
        assertThat(FxTestSupport.onFxThread(focusedCell::isFocused)).isFalse();
    }

    // HOME / END / PAGE_UP / PAGE_DOWN

    @Test
    void selectHome_afterScrollingAway_selectsFirstItemAndScrollsToStart() throws InterruptedException {
        var tileView = newRealizedTileView(200, 3, 300, 150);
        pressKey(tileView, KeyCode.END);

        pressKey(tileView, KeyCode.HOME);

        FxTestSupport.onFxThread(() -> {
            assertThat(tileView.getSelectionModel().getSelectedIndex()).isZero();
            assertThat(firstFullyVisibleRowIndex(tileView)).isZero();
            return null;
        });
    }

    @Test
    void selectEnd_fromStart_selectsLastItemAndScrollsToEnd() throws InterruptedException {
        var tileView = newRealizedTileView(200, 3, 300, 150);

        pressKey(tileView, KeyCode.END);

        FxTestSupport.onFxThread(() -> {
            assertThat(tileView.getSelectionModel().getSelectedIndex()).isEqualTo(199);
            assertThat(ColumnViewUtils.isFullyVisible(tileView, 199)).isTrue();
            return null;
        });
    }

    @Test
    void selectPageDown_selectionNotAtPageEnd_selectsLastFullyVisibleRowWithoutScrolling()
            throws InterruptedException {
        var tileView = newRealizedTileView(200, 3, 300, 150);
        var before = FxTestSupport.onFxThread(() -> {
            var firstRowBefore = firstFullyVisibleRowIndex(tileView);
            var lastFullyVisibleRow = lastFullyVisibleRowIndex(tileView);
            var expectedTarget = (lastFullyVisibleRow + 1) * tileView.getColumnCount() - 1;
            return Map.entry(firstRowBefore, expectedTarget);
        });

        pressKey(tileView, KeyCode.PAGE_DOWN);

        FxTestSupport.onFxThread(() -> {
            assertThat(tileView.getSelectionModel().getSelectedIndex()).isEqualTo(before.getValue());
            assertThat(firstFullyVisibleRowIndex(tileView)).isEqualTo(before.getKey());
            return null;
        });
    }

    @Test
    void selectPageDown_selectionAlreadyAtPageEnd_scrollsForwardAndSelectsNewPageEnd() throws InterruptedException {
        var tileView = newRealizedTileView(200, 3, 300, 150);
        var firstRowBefore = FxTestSupport.onFxThread(() -> firstFullyVisibleRowIndex(tileView));
        pressKey(tileView, KeyCode.PAGE_DOWN); // lands on the current page's last item, no scroll yet

        pressKey(tileView, KeyCode.PAGE_DOWN); // already there - now it must scroll forward

        FxTestSupport.onFxThread(() -> {
            assertThat(firstFullyVisibleRowIndex(tileView)).isGreaterThan(firstRowBefore);
            var lastFullyVisibleRow = lastFullyVisibleRowIndex(tileView);
            var expectedTarget = (lastFullyVisibleRow + 1) * tileView.getColumnCount() - 1;
            assertThat(tileView.getSelectionModel().getSelectedIndex()).isEqualTo(expectedTarget);
            return null;
        });
    }

    @Test
    void selectPageUp_selectionNotAtPageStart_selectsFirstFullyVisibleRowWithoutScrolling()
            throws InterruptedException {
        var tileView = newRealizedTileView(200, 3, 300, 150);
        pressKey(tileView, KeyCode.END);
        var lastRowBefore = FxTestSupport.onFxThread(() -> lastFullyVisibleRowIndex(tileView));

        pressKey(tileView, KeyCode.PAGE_UP);

        FxTestSupport.onFxThread(() -> {
            var firstFullyVisibleRow = firstFullyVisibleRowIndex(tileView);
            assertThat(tileView.getSelectionModel().getSelectedIndex())
                    .isEqualTo(firstFullyVisibleRow * tileView.getColumnCount());
            assertThat(lastFullyVisibleRowIndex(tileView)).isEqualTo(lastRowBefore);
            return null;
        });
    }

    @Test
    void selectPageUp_selectionAlreadyAtPageStart_scrollsBackAndSelectsNewPageStart() throws InterruptedException {
        var tileView = newRealizedTileView(200, 3, 300, 150);
        pressKey(tileView, KeyCode.END);
        var lastRowBefore = FxTestSupport.onFxThread(() -> lastFullyVisibleRowIndex(tileView));
        pressKey(tileView, KeyCode.PAGE_UP); // lands on the current page's first item, no scroll yet
        var firstRowAfterFirstPress = FxTestSupport.onFxThread(() -> firstFullyVisibleRowIndex(tileView));

        pressKey(tileView, KeyCode.PAGE_UP); // already there - now it must scroll back

        FxTestSupport.onFxThread(() -> {
            assertThat(lastFullyVisibleRowIndex(tileView)).isLessThan(lastRowBefore);
            var firstFullyVisibleRow = firstFullyVisibleRowIndex(tileView);
            assertThat(firstFullyVisibleRow).isLessThan(firstRowAfterFirstPress);
            assertThat(tileView.getSelectionModel().getSelectedIndex())
                    .isEqualTo(firstFullyVisibleRow * tileView.getColumnCount());
            return null;
        });
    }

    @Test
    void selectPageDown_repeatedlyToEnd_neverLeavesMoreThanOneCellHighlighted() throws InterruptedException {
        // Mirrors ColumnListViewTest's regression: a stale highlight was observed to remain on an old cell
        // after PageDown, alongside the newly selected one - most easily reproduced near the tail of the
        // data, where consecutive pages can end up overlapping by a repeated row whose cells aren't recycled,
        // so they rely entirely on the centralized selection listener to have their old highlight cleared.
        var itemCount = 149;
        var tileView = newRealizedTileView(itemCount, 3, 300, 150);
        for (var attempt = 0; attempt < 20; attempt++) {
            pressKey(tileView, KeyCode.PAGE_DOWN);
            var attemptNumber = attempt;
            var reachedEnd = FxTestSupport.onFxThread(() -> {
                assertThat(countHighlightedCellsInVisibleRange(tileView))
                        .as("after PAGE_DOWN #" + attemptNumber)
                        .isLessThanOrEqualTo(1);
                return tileView.getSelectionModel().getSelectedIndex() == itemCount - 1;
            });
            if (reachedEnd) {
                break;
            }
        }
    }

    @Test
    void selectPageDown_fromPartiallyVisibleRow_neverLeavesMoreThanOneCellHighlighted() throws InterruptedException {
        // Mirrors ColumnListViewTest's regression: the double-highlight was specifically observed only when
        // some row was partially (not fully) visible at the moment PageDown was pressed - e.g. right after a
        // manual scrollbar drag left the viewport mid-row rather than at a clean page-aligned position - so
        // this repeatedly forces that exact starting condition before each press.
        var tileView = newRealizedTileView(200, 3, 300, 150);
        for (var attempt = 0; attempt < 10; attempt++) {
            var attemptNumber = attempt;
            FxTestSupport.onFxThread(() -> {
                var flow = (VirtualFlow<?>) tileView.lookup(".virtual-flow");
                flow.scrollPixels(5); // small, sub-row nudge - leaves a row only partially visible
                tileView.applyCss();
                tileView.layout();
                return null;
            });
            pressKey(tileView, KeyCode.PAGE_DOWN);
            FxTestSupport.onFxThread(() -> {
                assertThat(countHighlightedCellsInVisibleRange(tileView))
                        .as("after PAGE_DOWN #" + attemptNumber + ", from a partially visible row")
                        .isLessThanOrEqualTo(1);
                return null;
            });
        }
    }
}
