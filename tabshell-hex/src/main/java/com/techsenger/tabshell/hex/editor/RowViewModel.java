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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.tabshell.core.node.AbstractNodeViewModel;
import com.techsenger.tabshell.core.node.NodeKey;

/**
 *
 * @author Pavel Castornii
 */
public class RowViewModel extends AbstractNodeViewModel {

    private static final NodeKey HEX_EDITOR_ROW_KEY = new NodeKey("Hex Editor Row");

    private final AbstractHexEditorTabViewModel editor;

    /**
     * The count of columns when layout was built.
     */
    private int columnCount;

    private RowModel model;

    private boolean focused;

    RowViewModel(AbstractHexEditorTabViewModel editor, RowModel model) {
        this.model = model;
        this.editor = editor;
    }

    @Override
    public NodeKey getKey() {
        return HEX_EDITOR_ROW_KEY;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public RowModel getModel() {
        return model;
    }

    public void setModel(RowModel model) {
        this.model = model;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    boolean shoulRebuilRow() {
        return this.columnCount != editor.getColumnCount();
    }

    AbstractHexEditorTabViewModel getEditor() {
        return editor;
    }
}
