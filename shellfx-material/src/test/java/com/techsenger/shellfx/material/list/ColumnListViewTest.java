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

    private static double computePrefWidth(ColumnListView<?> listView) throws ReflectiveOperationException {
        Method method = ColumnListView.class.getDeclaredMethod("computePrefWidth", double.class);
        method.setAccessible(true);
        return (double) method.invoke(listView, -1.0);
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
}
