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

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.ComponentName;

/**
 *
 * @author Pavel Castornii
 */
class BodyRowViewModel extends AbstractRowViewModel {

    private static final ComponentName HEX_EDITOR_BODY_ROW = new ComponentName("HexEditorBodyRow");

    private RowModel model;

    private boolean focused;

    BodyRowViewModel(HexAreaViewModel area, RowModel model) {
        super(area);
        this.model = model;
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

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(HEX_EDITOR_BODY_ROW);
    }

    void setModel(RowModel model) {
        this.model = model;
    }

    void setFocused(boolean focused) {
        this.focused = focused;
    }
}
