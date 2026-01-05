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

package com.techsenger.tabshell.hex.editor;

/**
 *
 * @author Pavel Castornii
 */
final class SelectionInfo {

    enum SelectionType {

        SIMPLE, JOINED
    }

    static class RowInfo {

        private final int index;

        private final int byteIndex;

        private final int byteCount;

        RowInfo(int index, int byteIndex, int byteCount) {
            this.index = index;
            this.byteIndex = byteIndex;
            this.byteCount = byteCount;
        }

        public int getIndex() {
            return index;
        }

        public int getByteIndex() {
            return byteIndex;
        }

        public int getByteCount() {
            return byteCount;
        }

        @Override
        public String toString() {
            return "[" + "index:" + index + ", byteIndex:" + byteIndex + ", byteCount:" + byteCount + ']';
        }
    }

    private final SelectionType type;

    private final int rowCount;

    private final RowInfo firstRow;

    private final RowInfo lastRow;

    SelectionInfo(SelectionType type, int rowCount, RowInfo firstRow, RowInfo lastRow) {
        this.type = type;
        this.rowCount = rowCount;
        this.firstRow = firstRow;
        this.lastRow = lastRow;
    }

    public SelectionType getType() {
        return type;
    }

    public int getRowCount() {
        return rowCount;
    }

    public RowInfo getFirstRow() {
        return firstRow;
    }

    public RowInfo getLastRow() {
        return lastRow;
    }

    @Override
    public String toString() {
        return "[" + "type:" + type + ", rowCount:" + rowCount + ", firstRow:" + firstRow
                + ", lastRow:" + lastRow + ']';
    }
}
