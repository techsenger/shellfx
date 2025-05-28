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
 * Represents a paired set of textual representations (HEX and ASCII) for a single byte within a hex editor row.
 *
 * <p>This class links the visual displays of a byte's HEX value (e.g., "1A") and its ASCII character (e.g., ".")
 * in synchronized fashion.
 *
 * @author Pavel Castornii
 */
class ByteTextPair {

    private final RowView rowView;

    private final ByteText hexText;

    private final ByteText asciiText;

    private boolean empty;

    private int index;

    ByteTextPair(RowView rowView, ByteText hexText, ByteText asciiText) {
        this.rowView = rowView;
        this.hexText = hexText;
        this.hexText.setPair(this);
        this.asciiText = asciiText;
        this.asciiText.setPair(this);
    }

    public ByteText getHexText() {
        return hexText;
    }

    public ByteText getAsciiText() {
        return asciiText;
    }

    public boolean isEmpty() {
        return empty;
    }

    public int getIndex() {
        return index;
    }

    public RowView getRow() {
        return rowView;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
