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

import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.css.PseudoClass;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.input.MouseEvent;

/**
 * The {@link ColumnTileView} counterpart of {@link ColumnListCell} &mdash; same click-to-select/
 * second-click-to-edit/scroll-into-view behavior, just tied to {@link ColumnTileView} directly instead of
 * sharing a type with {@link ColumnListView}'s cells.
 *
 * @author Pavel Castornii
 */
public class TileCell<T> extends IndexedCell<T> {

    private static final PseudoClass EDITING = PseudoClass.getPseudoClass("editing");

    private ColumnTileView<T> tileView;

    /**
     * Set this flag to true when you want to start editing only via {@link ColumnTileView#edit(int)} method.
     */
    private boolean manualEdit = false;

    public TileCell() {
        setFocusTraversable(true);
        addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (isSelected() && e.getClickCount() == 1) {
                if (!isEditing() && isEditable() && !manualEdit) {
                    tileView.edit(getIndex());
                }
            } else {
                tileView.getSelectionModel().select(getIndex());
                NodeUtils.requestFocus(this);
            }
            tileView.scrollToSelected();
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
    }

    @Override
    public void startEdit() {
        // See ColumnListCell#startEdit for why focus is transferred before super.startEdit().
        tileView.requestFocus();
        super.startEdit();
        pseudoClassStateChanged(EDITING, true);
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
        tileView.setEditingCellIndex(-1);
        pseudoClassStateChanged(EDITING, false);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        tileView.setEditingCellIndex(-1);
        pseudoClassStateChanged(EDITING, false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CellSkinBase<>(this);
    }

    protected boolean isManualEdit() {
        return manualEdit;
    }

    protected void setManualEdit(boolean manualEdit) {
        this.manualEdit = manualEdit;
    }

    void setTileView(ColumnTileView<T> tileView) {
        this.tileView = tileView;
    }

    ColumnTileView<T> getTileView() {
        return tileView;
    }
}
