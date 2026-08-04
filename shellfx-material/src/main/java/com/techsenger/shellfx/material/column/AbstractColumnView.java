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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class factoring out the parts of {@link ColumnListView} and {@link ColumnTileView} that are identical
 * between the two: items/selection-model plumbing, the {@code manualRefresh}/{@code editable}/
 * {@code contextMenu} properties, focus/keyboard/context-menu wiring, and preferred-size delegation to the
 * concrete virtual flow. Everything that differs between the two views - even subtly, like row/column
 * geometry or refresh triggers - is deliberately left in the subclasses rather than forced into a shared
 * abstraction here.
 *
 * @param <T> the item type
 * @author Pavel Castornii
 */
public abstract class AbstractColumnView<T> extends Region {

    private static class SingleSelectionModelImpl<T> extends SingleSelectionModel<T> {

        private final AbstractColumnView<T> view;

        SingleSelectionModelImpl(AbstractColumnView<T> view) {
            this.view = view;
        }

        @Override
        protected T getModelItem(int index) {
            if (index >= 0 && index < view.getItems().size()) {
                return view.getItems().get(index);
            } else {
                return null;
            }
        }

        @Override
        protected int getItemCount() {
            return view.getItems().size();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractColumnView.class);

    private final BooleanProperty manualRefresh = new SimpleBooleanProperty();

    private final BooleanProperty editable = new SimpleBooleanProperty();

    private final ObjectProperty<ContextMenu> contextMenu = new SimpleObjectProperty<>();

    private final ObservableList<Integer> offsets = FXCollections.observableArrayList();

    private final ReadOnlyIntegerWrapper rowCount = new ReadOnlyIntegerWrapper();

    private final SingleSelectionModelImpl<T> selectionModel = new SingleSelectionModelImpl<>(this);

    private final ListChangeListener<T> itemsChangeListener = (change) -> {
        if (!isManualRefresh()) {
            refreshItems();
        }
    };

    private ObservableList<T> items;

    private int firstVisibleCellIndex = 0;

    /**
     * Always only one cell can be in edit mode.
     */
    private int editingCellIndex = -1;

    /**
     * Whether {@link #firstVisibleCellIndex} should be reset back to {@code 0} the next time a subclass's
     * {@code layoutChildren()} consumes it &mdash; set by {@link #requestLayoutPreservingPosition()}, which
     * captures the currently-first-visible cell right before a geometry-affecting change (e.g. the virtual
     * flow's own width/height changing) so that change doesn't jump the scroll position back to the start.
     */
    private boolean resetFirstVisibleCellIndexAfterLayout;

    protected AbstractColumnView() {
        setFocusTraversable(true);
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            // Requests focus as early as press-time, before the click (and the cell's own MOUSE_CLICKED-driven
            // focus request) completes. e.getTarget() is often a node deep inside a cell (its text/graphic), not
            // the cell itself - walk up the ancestor chain instead of a single instanceof check on the target.
            var node = e.getTarget() instanceof Node ? (Node) e.getTarget() : null;
            while (node != null && node != this) {
                if (isContainerOrCellNode(node)) {
                    requestFocus();
                    break;
                }
                node = node.getParent();
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
            } else if (event.getCode() == KeyCode.HOME) {
                selectHome();
                event.consume();
            } else if (event.getCode() == KeyCode.END) {
                selectEnd();
                event.consume();
            } else if (event.getCode() == KeyCode.PAGE_UP) {
                selectPageUp();
                event.consume();
            } else if (event.getCode() == KeyCode.PAGE_DOWN) {
                selectPageDown();
                event.consume();
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
        // A single listener here, instead of one per cell/row/column (as before): a per-cell listener on this
        // long-lived view's own selectedIndexProperty would leak every cell ever created for the lifetime of
        // the view - the property's listener list holds a strong reference to each cell, so none of them could
        // ever be garbage collected even after being discarded/replaced by the virtual flow.
        getSelectionModel().selectedIndexProperty()
                .addListener((ov, oldV, newV) -> updateSelectedCellHighlight(newV.intValue()));
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
            refreshItems();
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

    public SingleSelectionModel<T> getSelectionModel() {
        return this.selectionModel;
    }

    public ReadOnlyIntegerProperty rowCountProperty() {
        return rowCount.getReadOnlyProperty();
    }

    public int getRowCount() {
        return rowCount.get();
    }

    public void onResizeStarted() {
        this.firstVisibleCellIndex = resolveFirstVisibleCellIndex();
        logger.debug("onResizeStarted: firstVisibleCellIndex={}, height={}, width={}", this.firstVisibleCellIndex,
                getHeight(), getWidth());
    }

    public void onResizeFinished() {
        this.firstVisibleCellIndex = 0;
        logger.debug("onResizeFinished: height={}, width={}", getHeight(), getWidth());
    }

    @Override
    protected double computePrefWidth(double height) {
        // Once a real size is established, reuse it instead of asking the virtual flow to recompute its own
        // preferred width: that recomputation realizes/recycles cells outside the visible range to measure
        // them, and updateItem() on a recycled cell can itself trigger a requestLayout() (e.g. a Labeled's
        // text metrics changing) - re-dirtying layout on every single pulse forever with nothing to show for
        // it, since the resulting preferred width doesn't actually change.
        return getWidth() > 0 ? getWidth() : getVirtualFlow().prefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width) {
        return getHeight() > 0 ? getHeight() : getVirtualFlow().prefHeight(width);
    }

    void resolveGeometry() {
        applyCss();
        layout();
    }

    ObservableList<Integer> getOffsets() {
        return offsets;
    }

    void setRowCount(int rowCount) {
        this.rowCount.set(rowCount);
    }

    int getFirstVisibleCellIndex() {
        return firstVisibleCellIndex;
    }

    void setFirstVisibleCellIndex(int firstVisibleCellIndex) {
        this.firstVisibleCellIndex = firstVisibleCellIndex;
    }

    int getEditingCellIndex() {
        return editingCellIndex;
    }

    void setEditingCellIndex(int editingCellIndex) {
        this.editingCellIndex = editingCellIndex;
    }

    int resolveFirstVisibleCellIndex() {
        var cell = getVirtualFlow().getFirstVisibleCell();
        if (cell == null) {
            return 0;
        } else {
            return cell.getIndex();
        }
    }

    /**
     * Captures the currently-first-visible cell into {@link #firstVisibleCellIndex} and marks it for a
     * one-shot reset back to {@code 0} (via {@link #consumeFirstVisibleCellIndexReset()}), then requests a
     * layout pass &mdash; called by a subclass whenever its virtual flow's own width/height changes for a
     * reason other than {@link #onResizeStarted()}/{@link #onResizeFinished()} bracketing a recognized
     * external resize gesture (e.g. a scrollbar appearing/disappearing), so that change doesn't silently jump
     * the scroll position back to the first column/row.
     */
    void requestLayoutPreservingPosition() {
        this.firstVisibleCellIndex = resolveFirstVisibleCellIndex();
        this.resetFirstVisibleCellIndexAfterLayout = true;
        logger.debug("requestLayoutPreservingPosition: firstVisibleCellIndex={}, height={}, width={}",
                this.firstVisibleCellIndex, getHeight(), getWidth());
        requestLayout();
    }

    /**
     * Called by a subclass's {@code layoutChildren()} once it has consumed {@link #firstVisibleCellIndex} for
     * the current pass, resetting it back to {@code 0} if {@link #requestLayoutPreservingPosition()} asked for
     * that reset - a no-op otherwise, so an unrelated layout pass (e.g. one driven only by
     * {@link #onResizeStarted()}/{@link #onResizeFinished()}) doesn't clobber a value it didn't set.
     */
    void consumeFirstVisibleCellIndexReset() {
        if (this.resetFirstVisibleCellIndexAfterLayout) {
            this.firstVisibleCellIndex = 0;
            this.resetFirstVisibleCellIndexAfterLayout = false;
        }
    }

    abstract VirtualFlow<? extends IndexedCell<Integer>> getVirtualFlow();

    abstract boolean isContainerOrCellNode(Node node);

    abstract void refreshItems();

    abstract void updateSelectedCellHighlight(int selectedIndex);

    abstract void selectUp();

    abstract void selectDown();

    abstract void selectLeft();

    abstract void selectRight();

    abstract void selectHome();

    abstract void selectEnd();

    abstract void selectPageUp();

    abstract void selectPageDown();
}
