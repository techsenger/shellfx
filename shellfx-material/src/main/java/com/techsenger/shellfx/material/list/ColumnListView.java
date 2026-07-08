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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This listView requires calling {@link ColumnListView#onResizeStarted()} and
 * {@link ColumnListView#onResizeFinished()} on resizing. See DialogResizeEvent and StageResizeEvent.
 *
 * <p>Row height is calculated creating one empty cell in one column before creating cells for items.
 *
 * <p>The cell selection occurs in two stages: 1_ Marking the cell as selected and saving a reference to
 * it in the listView. 2) Updating the selectionModel. The only exception is changeListener for selectionModel.
 *
 * @author Pavel Castornii
 */
public class ColumnListView<T> extends Region {

    private static final Logger logger = LoggerFactory.getLogger(ColumnListView.class);

    private static class SingleSelectionModelImpl<T> extends SingleSelectionModel<T> {

        private final ColumnListView<T> listView;

        SingleSelectionModelImpl(ColumnListView<T> listView) {
            this.listView = listView;
        }

        @Override
        protected T getModelItem(int index) {
            if (index >= 0 && index < this.listView.items.size()) {
                return this.listView.items.get(index);
            } else {
                return null;
            }
        }

        @Override
        protected int getItemCount() {
            return this.listView.items.size();
        }
    }

    private static class ColumnListViewColumn<T> extends IndexedCell<Integer>  {

        private final VBox node = new VBox();

        private final ColumnListView<T> listView;

        private final List<ColumnListCell<T>> cachedCells = new ArrayList<>();

        private ColumnListCell<T> selectedCell;

        private boolean dirty = true;

        ColumnListViewColumn(final ColumnListView<T> listView) {
            this.listView = listView;
            node.getStyleClass().add("column");
            node.setAlignment(Pos.TOP_LEFT);
            setGraphic(node);
            listView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
                clearSelection();
                if (newV.intValue() != -1) {
                    //scroll to selected to create it if it hasn't beed created yet
                    var columnIndex = listView.resolveColumnIndex(newV.intValue());
                    if (getIndex() == columnIndex) {
                        var rowIndex = listView.resolveRowIndex(newV.intValue());
                        if (rowIndex < cachedCells.size()) {
                            setSelectedCell(cachedCells.get(rowIndex));

                        }
                    }
                }
            });
        }

        @Override
        public void updateItem(Integer item, boolean empty) {
            if (item != null && item.equals(getItem()) && !empty && !dirty) {
                return;
            }
            super.updateItem(item, empty);
            node.getChildren().clear();
            clearSelection();
            if (item != null) {
                if (listView.rowHeight.get() > 0) {
                    updateCells();
                    dirty = false;
                } else {
                    addRowHeightCell();
                }
            }
        }

        protected ColumnListView<T> getListView() {
            return listView;
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new CellSkinBase<>(this);
        }

        private VBox getNode() {
            return this.node;
        }

        private void updateCells() {
            var item = getItem();
            node.getStyleClass().removeAll("first", "last");
            if (item == 0) {
                node.getStyleClass().add("first");
            } else if (item == (this.listView.getColumnCount() - 1) * this.listView.getRowCount()) {
                node.getStyleClass().add("last");
            }
            int endIndex = Math.min(item + this.listView.getRowCount(), this.listView.getItems().size());
            var cellItems = this.listView.getItems().subList(item, endIndex);
            var absentCells = cellItems.size() - this.cachedCells.size();
            for (var i = 0; i < absentCells; i++) {
                createCell();
            }
            //updating cells and adding them to node
            for (var i = 0; i < cellItems.size(); i++) {
                var cellItem = cellItems.get(i);
                ColumnListCell cell = cachedCells.get(i);
                cell.updateItem((Object) cellItem, false);
                var cellIndex = item + i;
                //index must be updated only after setting non empty, see Cell#updateSelected(boolean selected).
                cell.updateIndex(cellIndex);
                if (cellIndex == listView.getSelectionModel().getSelectedIndex() && cellIndex != -1) {
                    setSelectedCell(cell);
                }
                if (!cell.isEditing() && cell.isEditable() && cell.getIndex() == this.listView.editingCellIndex) {
                    cell.startEdit();
                }
                this.node.getChildren().add(cell);
            }
            if (cellItems.size() < this.cachedCells.size()) {
                for (var i = cellItems.size(); i < this.cachedCells.size(); i++) {
                    ColumnListCell cell = cachedCells.get(i);
                    cell.updateItem(null, true);
                    var cellIndex = item + i;
                    cell.updateIndex(cellIndex);
                }
            }
        }

        private void createCell() {
            var cell = this.listView.getCellFactory().call((ColumnListView) this.listView);
            cell.setListView((ColumnListView) this.listView);
            this.cachedCells.add(cell);
        }

        private void setSelectedCell(ColumnListCell<T> cell) {
            this.selectedCell = cell;
            selectedCell.updateSelected(true); //will call requestLayout();
        }

        private void clearSelection() {
            if (selectedCell != null && !selectedCell.isEditing()) {
                selectedCell.updateSelected(false);
                selectedCell = null;
            }
        }

        /**
         * Empty cell is used to calculate row height.
         */
        private void addRowHeightCell() {
            createCell();
            ColumnListCell<T> cell = cachedCells.get(0);
            ChangeListener<Number> listener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number oldV, Number newV) {
                    if (newV.doubleValue() > 1) {
                        cell.heightProperty().removeListener(this);
                        Platform.runLater(() -> listView.rowHeight.set(newV.doubleValue()));
                    }
                }
            };
            cell.heightProperty().addListener(listener);
            this.node.getChildren().add(cell);
        }

        private void markDirty() {
            this.dirty = true;
        }
    }

    private static class ColumnVirtualFlow<T> extends VirtualFlow<ColumnListViewColumn<T>> {

        /**
         * By default the height of the scrollbar is 100 pixel so we wait until real height is set.
         */
        private Double hBarHeight = null;

        ColumnVirtualFlow() {
            var vBar = getVbar();
            vBar.setMinWidth(0);
            vBar.setPrefWidth(0);
            vBar.setMaxWidth(0);
            vBar.setOpacity(0);
            setVertical(false);
            getHBar().heightProperty().addListener((ov, oldV, newV) -> hBarHeight = newV.doubleValue());
        }

        ScrollBar getHBar() {
            return super.getHbar();
        }

        /**
         * Returns the height of the virtual flow without the height of the horizontal scroll bar.
         * @return
         */
        double getViewportHeight() {
            if (hBarHeight != null && getHBar().isVisible()) {
                return getHeight() - hBarHeight;
            } else {
                return getHeight();
            }
        }

        @Override
        protected List<ColumnListViewColumn<T>> getCells() {
            return super.getCells();
        }

    }

    /**
    * Triggers that cause a refresh when changed.
    */
    private enum RefreshTrigger {

        ITEMS,

        ROW_HEIGHT,

        SCROLL_BAR_HEIGHT,

        SCROLL_BAR_VISIBILITY,

        VIRTUAL_FLOW_HEIGHT
    }

    private enum RefreshType {
        PRIMARY, SECONDARY
    }

    /**
     * Always fixed height of the row.
     */
    private final DoubleProperty rowHeight = new SimpleDoubleProperty();

    /**
     * If true then data observable list changes are ignored and it is necessary to call refresh() method. The reason
     * is that same list can be used by tables and they can sort items.
     */
    private final BooleanProperty manualRefresh = new SimpleBooleanProperty();

    private final ObservableList<Integer> offsets = FXCollections.observableArrayList();

    private final ObjectProperty<Callback<ColumnListView<T>, ColumnListCell<T>>> cellFactory
            = new SimpleObjectProperty();

    private final ReadOnlyIntegerWrapper rowCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyIntegerWrapper columnCount = new ReadOnlyIntegerWrapper();

    private final SingleSelectionModelImpl<T> selectionModel = new SingleSelectionModelImpl<>(this);

    private final ObjectProperty<ContextMenu> contextMenu = new SimpleObjectProperty<>();

    /**
     * This class uses JavaFX VirtualFlow instead of Flowless VirtualFlow because, in Flowless VirtualFlow, slow
     * scrolling of cells with different widths (for a horizontal VirtualFlow) stutters slightly. This happens because
     * VirtualFlow does not know the total width of its contents until everything has been rendered, so it uses an
     * estimated width that it continuously updates as more items are displayed. When this estimated width is updated,
     * the scrollbar also adjusts accordingly by shifting the thumb and resizing it if necessary.
     *
     * Working with JavaFX VirtualFlow it is important to remember, that calling cell.updateSelected(boolean) can
     * invoke cell.requestLayout() if selected property changes.
     */
    private final ColumnVirtualFlow<T> virtualFlow = new ColumnVirtualFlow<>();

    private final BooleanProperty editable = new SimpleBooleanProperty();

    private ObservableList<T> items;

    private int firstVisibleCellIndex = 0;

    private boolean selectedByAction;

    /**
     * Always only one sell can be in edit mode.
     */
    private int editingCellIndex = -1;

    /**
     * Tracks whether a refresh is currently in progress and prevents reentrant refreshes.
     *
     * <p>Without this guard, changing {@code rowCount} or {@code columnCount} inside {@link #updateOffsets(int,
     * RefreshTrigger)} fires their change listeners, which invoke {@link #refresh(RefreshTrigger, RefreshType)} again,
     * causing infinite recursion and eventually an {@link OutOfMemoryError}.
     *
     * <p>If another refresh is requested while a primary refresh is executing, the request is postponed and executed
     * once as a secondary refresh after the current refresh completes.
     */
    private RefreshType currentType;

    private RefreshTrigger secondRefreshTrigger = null;

    private final ListChangeListener<T> itemsChangeListener = (change) -> {
        if (!isManualRefresh()) {
            refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
        }
    };

    private final List<WeakReference<ColumnListViewColumn<?>>> columns = new LinkedList<>();

    public ColumnListView() {
        getStylesheets().add(ColumnListView.class.getResource("column-list-view.css").toExternalForm());
        getStyleClass().add("column-list-view");
        //default cell factory
        setCellFactory(v -> new ColumnListCell<>());
        setFocusTraversable(true);
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if ((e.getTarget() instanceof VBox) || (e.getTarget() instanceof ColumnListCell)) {
                requestFocus();
            }
        });
        getChildren().add(this.virtualFlow);
        this.rowHeight.set(-1);
        this.rowHeight.addListener((ov, oldV, newV) -> refresh(RefreshTrigger.ROW_HEIGHT, RefreshType.PRIMARY));
        this.virtualFlow.getHBar().heightProperty()
                .addListener((ov, oldV, newV) -> refresh(RefreshTrigger.SCROLL_BAR_HEIGHT, RefreshType.PRIMARY));
        this.virtualFlow.getHBar().visibleProperty()
                .addListener((ov, oldV, newV) -> refresh(RefreshTrigger.SCROLL_BAR_VISIBILITY, RefreshType.PRIMARY));
        //firstVisibleCellIndex is set via onResizeStarted.
        this.virtualFlow.heightProperty()
                .addListener((ov, oldV, newV) -> savePositionAndRefreshView(RefreshTrigger.VIRTUAL_FLOW_HEIGHT));
        virtualFlow.setCellFactory(vf -> new ColumnListViewColumn<>(this) {

            {
                columns.add(new WeakReference<>(this));
            }

            @Override
            public void updateIndex(int index) {
                super.updateIndex(index);
                if (index >= 0 && index < offsets.size()) {
                    updateItem(offsets.get(index), false);
                } else {
                    updateItem(null, true);
                }
            }
        });

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                selectUp();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                selectDown();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                selectLeft();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                selectRight();
                event.consume();
            }
        });

        this.selectionModel.selectedIndexProperty().addListener((ov, oldV, newV) -> {
            //there can be two types of events - selection from code or selection from user; user selections are ignored
            if (newV.intValue() != -1 && !selectedByAction) {
                //scroll to selected to create it if it hasn't beed created yet
                var columnIndex = resolveColumnIndex(newV.intValue());
                scrollToFirstColumn(columnIndex);
                this.selectedByAction = false;
            }
        });
        this.contextMenu.addListener((ov, oldV, newV) -> {
            if (newV == null) {
                setOnContextMenuRequested(null);
            } else {
                setOnContextMenuRequested(event -> {
                    newV.show(this, event.getScreenX(), event.getScreenY());
                    event.consume();
                });
            }
        });
    }

    public Callback<ColumnListView<T>, ColumnListCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public void setCellFactory(Callback<ColumnListView<T>, ColumnListCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public void setItems(ObservableList<T> items) {
        if (this.items != null) {
            this.items.removeListener(itemsChangeListener);
        }
        this.items = items;
        if (this.items != null) {
            this.items.addListener(itemsChangeListener);
        }
        if (!isManualRefresh()) {
            refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
        }
    }

    public BooleanProperty manualRefreshProperty() {
        return manualRefresh;
    }

    public boolean isManualRefresh() {
        return manualRefresh.get();
    }

    public void setManualRefresh(boolean manualRefresh) {
        this.manualRefresh.set(manualRefresh);
    }

    public ReadOnlyIntegerProperty rowCountProperty() {
        return rowCount.getReadOnlyProperty();
    }

    public int getRowCount() {
        return rowCount.get();
    }

    public ReadOnlyIntegerProperty columnCountProperty() {
        return columnCount.getReadOnlyProperty();
    }

    public int getColumnCount() {
        return columnCount.get();
    }

    public SingleSelectionModel<T> getSelectionModel() {
        return this.selectionModel;
    }

    /**
     * This method is called when manual refresh is enabled.
     */
    public void refresh() {
        refresh(RefreshTrigger.ITEMS, RefreshType.PRIMARY);
    }

    /**
     * Returns the column index or -1.
     *
     * @param cellIndex
     * @return
     */
    public int resolveColumnIndex(int cellIndex) {
        if (getRowCount() == 0) {
            return -1;
        }
        var columnIndex = (int) cellIndex / getRowCount();
        return columnIndex;
    }

    public int resolveRowIndex(int cellIndex) {
        var rowIndex = cellIndex % getRowCount();
        return rowIndex;
    }

    /**
     * Returns the count of rows in concrete column - the first and the last column can have less then rowCount rows.
     *
     * @param columnIndex
     * @return
     */
    public int resolveRowCount(int columnIndex) {
        int totalItems = this.items.size();
        int rowCount = getRowCount();
        int columnCount = this.offsets.size();

        int fullColumns = totalItems / rowCount;
        int remaining = totalItems % rowCount;

        if (columnIndex < fullColumns) {
            return rowCount;
        } else if (columnIndex == fullColumns && remaining > 0) {
            return remaining;
        }
        return 0;
    }

    /**
     * Scrolls the the first column.
     */
    public void scrollToFirstColumn() {
        this.virtualFlow.scrollTo(0);
    }

    /**
     * Scrolls to the last column.
     */
    public void scrollToLastColumn() {
        this.virtualFlow.scrollPixels(Integer.MAX_VALUE);
    }

    /**
     * Scrolls to N column and shows it as the first one.
     *
     * @param columnIndex
     */
    public void scrollToFirstColumn(int columnIndex) {
        this.virtualFlow.scrollToTop(columnIndex);
    }

    /**
     * Scrolls to N column and shows it as the last one.
     *
     * @param columnIndex
     */
    public void scrollToLastColumn(int columnIndex) {
        this.virtualFlow.scrollTo(columnIndex);
    }

    public void onResizeStarted() {
        this.firstVisibleCellIndex = resolveFirstVisibleCellIndex();
    }

    public void onResizeFinished() {
        this.firstVisibleCellIndex = 0;
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public boolean isEditable() {
        return editable.get();
    }

    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    public void setContextMenu(ContextMenu menu) {
        this.contextMenu.set(menu);
    }

    public ContextMenu getContextMenu() {
        return this.contextMenu.get();
    }

    public ObjectProperty<ContextMenu> contextMenuProperty() {
        return this.contextMenu;
    }

    public void edit(int cellIndex) {
        if (!isEditable() || this.editingCellIndex != -1) {
            return;
        }
        this.editingCellIndex = cellIndex;
        var columnIndex = resolveColumnIndex(cellIndex);
        ColumnListViewColumn<T> column = null;
        for (var c : this.virtualFlow.getCells()) {
            if (c.getIndex() == columnIndex) {
                column = c;
                break;
            }
        }
        if (column != null) {
            column.markDirty();
            column.requestLayout();
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return virtualFlow.prefWidth(-1);
    }

    @Override
    protected double computePrefHeight(double width) {
        return virtualFlow.prefHeight(-1);
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        virtualFlow.resizeRelocate(0, 0, width, height);
    }

    void setEditingCellIndex(int editingCellIndex) {
        this.editingCellIndex = editingCellIndex;
    }

    /**
     * This method makes entire cell visible and is called when user clicks mouse, so, the cell exists and visible.
     */
    void scrollToSelected() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var columnIndex = resolveColumnIndex(selectedIndex);

        if (columnIndex == this.virtualFlow.getFirstVisibleCell().getIndex()) {
            scrollToFirstColumn(columnIndex);
        } else if (columnIndex == this.virtualFlow.getLastVisibleCell().getIndex()) {
            scrollToLastColumn(columnIndex);
        }

    }

    void setSelectedByAction(boolean selectedByAction) {
        this.selectedByAction = selectedByAction;
    }

    private ColumnListCell<T> getCell(int columnIndex, int rowIndex) {
        ColumnListViewColumn column = this.virtualFlow.getCell(columnIndex);
        var cell = (ColumnListCell<T>) column.getNode().getChildren().get(rowIndex);
        return cell;
    }

    private ColumnListCell<T> getCell(int cellIndex) {
        var columnIndex = resolveColumnIndex(cellIndex);
        var rowIndex = resolveRowIndex(cellIndex);
        return getCell(columnIndex, rowIndex);
    }

    private void selectUp() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - 1;
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        }
    }

    private void selectDown() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex + 1;
        if (newSelectedIndex < getItems().size()) {
            selectNext(selectedIndex, newSelectedIndex);
        }
    }

    private void selectLeft() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex - getRowCount();
        if (newSelectedIndex >= 0) {
            selectPrevious(selectedIndex, newSelectedIndex);
        }
    }

    private void selectRight() {
        var selectedIndex = getSelectionModel().getSelectedIndex();
        var newSelectedIndex = selectedIndex + getRowCount();
        if (newSelectedIndex < getItems().size()) {
            selectNext(selectedIndex, newSelectedIndex);
        }
    }

    private void selectPrevious(int selectedIndex, int newSelectedIndex) {
        //firstly we provide the column (it can be absent)
        var columnIndex = resolveColumnIndex(selectedIndex);
        var newColumnIndex = resolveColumnIndex(newSelectedIndex);
        var firstVisibleColumnIndex = this.virtualFlow.getFirstVisibleCell().getIndex();
        if (newColumnIndex <= firstVisibleColumnIndex) {
            scrollToFirstColumn(newColumnIndex);
        }
        getSelectionModel().select(newSelectedIndex);
    }

    private void selectNext(int selectedIndex, int newSelectedIndex) {
        //firstly we provide the column (it can be absent)
        var columnIndex = resolveColumnIndex(selectedIndex);
        var newColumnIndex = resolveColumnIndex(newSelectedIndex);
        var lastVisibleColumnIndex = this.virtualFlow.getLastVisibleCell().getIndex();
        if (newColumnIndex >= lastVisibleColumnIndex) {
            scrollToLastColumn(newColumnIndex);
        }
        getSelectionModel().select(newSelectedIndex);
    }

    private void savePositionAndRefreshView(RefreshTrigger refreshTrigger) {
        this.firstVisibleCellIndex = resolveFirstVisibleCellIndex();
        refresh(refreshTrigger, RefreshType.PRIMARY);
        this.firstVisibleCellIndex = 0;
    }

    private int resolveFirstVisibleCellIndex() {
        var cell = this.virtualFlow.getFirstVisibleCell();
        if (cell == null) {
            return 0;
        } else {
            return cell.getIndex();
        }
    }

    private void scrollToCell(int cellIndex) {
        int columnIndex = cellIndex / getRowCount();
        scrollToFirstColumn(columnIndex);
    }

    /**
     * This method is called when view or data has been changed.
     *
     * @param refreshTrigger
     */
    private void refresh(RefreshTrigger refreshTrigger, RefreshType type) {
        logger.debug("Refresh request, trigger: {}, type: {}", refreshTrigger, type);
        if (this.items == null) {
            return;
        }
        if (getHeight() < 0.1) {
            return;
        }
        if (rowHeight.get() < 0) {
            prepareRowHeightResolving();
            return;
        }
        var iterator = this.columns.iterator();
        while (iterator.hasNext()) {
            var ref = iterator.next();
            var column = ref.get();
            if (column == null) {
                iterator.remove();
            } else {
                column.markDirty();
            }
        }

        var rowCount = (int) (this.virtualFlow.getViewportHeight() / rowHeight.get());
        if (rowCount <= 0) {
            return;
        }
        if (type == RefreshType.PRIMARY && currentType != null) {
            secondRefreshTrigger = refreshTrigger; // there can be multiple attempt, so the last one is saved
            logger.debug("Refresh request saved and postponed, trigger: {}, type: {}", refreshTrigger, type);
            return;
        }
        try {
            currentType = type;
            if (refreshTrigger == RefreshTrigger.ITEMS) {
                if (getSelectionModel().getSelectedIndex() != -1) {
                    getSelectionModel().clearSelection();
                }
                this.editingCellIndex = -1;
                updateOffsets(rowCount, refreshTrigger);
                scrollToFirstColumn(firstVisibleCellIndex);
            } else {
                if (getRowCount() != rowCount) {
                    updateOffsets(rowCount, refreshTrigger);
                    scrollToFirstColumn(firstVisibleCellIndex);
                }
            }
            logger.debug("Refreshed, trigger: {}, type: {}, itemsCount: {}", refreshTrigger, type, items.size());
            if (type == RefreshType.PRIMARY && secondRefreshTrigger != null) {
                refresh(secondRefreshTrigger, RefreshType.SECONDARY);
                secondRefreshTrigger = null;
            }
        } finally {
            if (type == RefreshType.SECONDARY) {
                currentType = RefreshType.PRIMARY;
            } else {
                currentType = null;
            }
        }
    }

    private void prepareRowHeightResolving() {
        this.rowCount.set(1);
        this.columnCount.set(1);
        this.offsets.addAll(List.of(0));
    }

    private void updateOffsets(int rowCount, RefreshTrigger refreshTrigger) {
        this.rowCount.set(rowCount);
        int columnCount = (int) Math.ceil((double) this.items.size() / rowCount);
        this.columnCount.set(columnCount);
        List<Integer> offs = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            offs.add(i * rowCount);
        }
        this.offsets.clear();
        this.offsets.addAll(offs);
        for (var c : virtualFlow.getCells()) {
            c.requestLayout();
        }
        virtualFlow.setCellCount(this.offsets.size());
    }
}
