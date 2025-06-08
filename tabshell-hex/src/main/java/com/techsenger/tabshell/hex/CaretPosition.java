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

package com.techsenger.tabshell.hex;

/**
 *
 * @author Pavel Castornii
 */
public final class CaretPosition {

    private final EditorPanel panel;

    /**
     * We don't use offset from row because of virtualization.
     */
    private final int rowOffset;

    /**
     * The index of the row relative to all visible and invisible rows.
     */
    private final int rowIndex;

    /**
     * The index of the byte in the row.
     */
    private final int byteIndex;

    private final CaretByteLocation byteLocation;

    private final int offset;

    CaretPosition(EditorPanel panel, int rowOffset, int rowIndex, int byteIndex,
            CaretByteLocation byteLocation) {
        this.panel = panel;
        this.rowOffset = rowOffset;
        this.rowIndex = rowIndex;
        this.byteIndex = byteIndex;
        this.byteLocation = byteLocation;
        this.offset = rowOffset + byteIndex;
    }

    public EditorPanel getPanel() {
        return panel;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getByteIndex() {
        return byteIndex;
    }

    public CaretByteLocation getByteLocation() {
        return byteLocation;
    }

    public int getOffset() {
        return offset;
    }
}
