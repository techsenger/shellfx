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

package com.techsenger.tabshell.hex.row;

import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.hex.AbstractHexEditorTabViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class BodyRowViewModel extends AbstractRowViewModel {

    private static final PaneKey HEX_EDITOR_BODY_ROW = new PaneKey("Hex Editor Body Row");

    private RowModel model;

    private boolean focused;

    public BodyRowViewModel(AbstractHexEditorTabViewModel editor, RowModel model) {
        super(editor);
        this.model = model;
    }

    @Override
    public PaneKey getKey() {
        return HEX_EDITOR_BODY_ROW;
    }

    public RowModel getModel() {
        return model;
    }

    public boolean isFocused() {
        return focused;
    }

    /**
     * Returns true if this row is the first one among all visible and non-visible rows.
     *
     * @return true if this is the first row; false otherwise.
     */
    public boolean isFirst() {
        return this.model.getOffset() == 0;
    }

    void setModel(RowModel model) {
        this.model = model;
    }

    //todo: not public
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
