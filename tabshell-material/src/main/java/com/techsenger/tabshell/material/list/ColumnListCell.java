/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.material.list;

import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Pavel Castornii
 */
public class ColumnListCell<T> extends IndexedCell<T> {

    private ColumnListView<T> listView;

    /**
     * Set this flag to true when you want to start editing only via {@link ColumnListView#edit(int)} method.
     */
    private boolean manualEdit = false;

    public ColumnListCell() {
        getStyleClass().add("cell");
        setFocusTraversable(true);
        addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (isSelected() && e.getClickCount() == 1) {
                if (!isEditing() && isEditable() && !manualEdit) {
                    listView.edit(getIndex());
                }
            } else {
                listView.setSelectedByAction(true);
                listView.getSelectionModel().select(getIndex());
                NodeUtils.requestFocus(this);
            }
            listView.scrollToSelected();
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
        listView.setEditingCellIndex(-1);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        listView.setEditingCellIndex(-1);
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

    void setListView(ColumnListView<T> listView) {
        this.listView = listView;
    }

    ColumnListView<T> getListView() {
        return listView;
    }
}
