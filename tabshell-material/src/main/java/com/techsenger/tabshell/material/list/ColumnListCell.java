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

package com.techsenger.tabshell.material.list;

import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.css.PseudoClass;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Pavel Castornii
 */
public class ColumnListCell<T> extends IndexedCell<T> {

    private static final PseudoClass EDITING = PseudoClass.getPseudoClass("editing");

    private ColumnListView<T> listView;

    /**
     * Set this flag to true when you want to start editing only via {@link ColumnListView#edit(int)} method.
     */
    private boolean manualEdit = false;

    public ColumnListCell() {
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
    public void startEdit() {
        // See the constructor of Cell (the link is split into two parts):
        // https://github.com/openjdk/jfx/blob/277aec13d0879718a9ac2231402e19eed6f70d20/modules
        // /javafx.controls/src/main/java/javafx/scene/control/Cell.java#L361
        //
        // When a cell has focus and loses it while editing, the edit is cancelled. To prevent this, we transfer
        // focus to the list view before calling super.startEdit(). This way the cell is already unfocused when editing
        // begins, so the listener in Cell cannot trigger cancelEdit(). Focus will then move naturally to the
        // TextField once it is created. It is important to note, if a TextField is created on this pulse, it will get
        // focus on the next one, so we can't use it now, before super.startEdit().
        listView.requestFocus();
        super.startEdit();
        pseudoClassStateChanged(EDITING, true);
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
        listView.setEditingCellIndex(-1);
        pseudoClassStateChanged(EDITING, false);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        listView.setEditingCellIndex(-1);
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

    void setListView(ColumnListView<T> listView) {
        this.listView = listView;
    }

    ColumnListView<T> getListView() {
        return listView;
    }
}
