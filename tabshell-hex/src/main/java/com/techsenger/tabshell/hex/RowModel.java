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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
final class RowModel {

    private static final String INVALID_CHAR = "\u2022";

    static RowModel create(Integer offset, AbstractHexEditorTabViewModel editor) {
        if (offset == null) {
            return null;
        }
        var content = editor.getDocument().getContent();
        int realLength = Math.min(editor.getRowByteCount(), content.length - offset);
        var data = new byte[realLength];
        List<String> hexes = new ArrayList<>(data.length);
        List<String> asciis = new ArrayList<>(data.length);
        System.arraycopy(content, offset, data, 0, realLength);
        for (var i = 0; i < data.length; i++) {
            byte b = data[i];
            hexes.add(NumberBaseUtils.convertToHex(b));
            if (b <= 31 || b == 127) {
                asciis.add(INVALID_CHAR);
            } else {
                asciis.add(Character.toString((char) (b & 0xFF)));
            }
        }
        var index = editor.calculateRowIndex(offset);
        return new RowModel(offset, index, hexes, asciis);
    }

    private final int offset;

    private final int index;

    private final List<String> hexes;

    private final List<String> asciis;

    private RowModel(int offset, int index, List<String> hexes, List<String> asciis) {
        this.offset = offset;
        this.index = index;
        this.hexes = hexes;
        this.asciis = asciis;
    }

    public int getOffset() {
        return offset;
    }

    public int getIndex() {
        return index;
    }

    public List<String> getHexes() {
        return hexes;
    }

    public List<String> getAsciis() {
        return asciis;
    }

    /**
     * Returns the count of bytes in this row.
     * @return
     */
    public int getByteCount() {
        return this.hexes.size();
    }
}
