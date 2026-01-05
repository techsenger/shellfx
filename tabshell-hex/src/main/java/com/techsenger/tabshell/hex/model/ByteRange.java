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

package com.techsenger.tabshell.hex.model;

/**
 *
 * @author Pavel Castornii
 */
public class ByteRange {

    private final int start;

    private final int end;

    /**
     * Creates a new byte range with the specified start (inclusive) and end (exclusive) indexes.
     *
     * @param start the inclusive start index of the range
     * @param end   the exclusive end index of the range
     */
    public ByteRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the inclusive start index of this byte range.
     *
     * @return the start index (inclusive)
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the exclusive end index of this byte range.
     *
     * @return the end index (exclusive)
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + "start:" + start + ", end:" + end + ']';
    }
}
