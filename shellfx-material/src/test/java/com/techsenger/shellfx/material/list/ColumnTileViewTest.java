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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.HBox;
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

    private static double computePrefHeight(ColumnTileView<?> tileView) throws ReflectiveOperationException {
        Method method = ColumnTileView.class.getDeclaredMethod("computePrefHeight", double.class);
        method.setAccessible(true);
        return (double) method.invoke(tileView, -1.0);
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
}
