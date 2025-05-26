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

import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
class RowModel {

    private final int offset;

    private final String hexOffset;

    private final List<String> hexes;

    private final List<String> asciis;

    RowModel(int offset, String hexOffset,  List<String> hexes, List<String> asciis) {
        this.offset = offset;
        this.hexOffset = hexOffset;
        this.hexes = hexes;
        this.asciis = asciis;
    }

    public int getOffset() {
        return offset;
    }

    public String getHexOffset() {
        return hexOffset;
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
