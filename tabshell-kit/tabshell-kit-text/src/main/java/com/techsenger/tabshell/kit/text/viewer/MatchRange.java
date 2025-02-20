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

package com.techsenger.tabshell.kit.text.viewer;

/**
 *
 * @author Pavel Castornii
 */
class MatchRange {

    /**
     * Index in all matches list.
     */
    private int index;

    /**
     * Inclusive.
     */
    private int start;

    /**
     * Exclusive.
     */
    private int end;

    /**
     * Indicates if match was replaced.
     */
    private boolean replaced;

    MatchRange(int index, int start, int end) {
        this.index = index;
        this.start = start;
        this.end = end;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void addToStart(int diff) {
        this.start += diff;
    }

    public void addToEnd(int diff) {
        this.end += diff;
    }

    public boolean isReplaced() {
        return replaced;
    }

    public void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }

    /**
     * Checks if this range intersects with region (from start to end) fully or partially.
     *
     * @param start
     * @param end
     */
    public boolean intersects(int start, int end) {
        var result = this.start < end && this.end > start;
        return result;
    }

    /**
     * Checks if specific position is inside range.
     *
     * @param position
     * @return
     */
    public boolean hasInside(int position) {
        return position > this.start && position < this.end;
    }

    @Override
    public String toString() {
        return "{" + "index=" + index + ", start=" + start + ", end=" + end + ", replaced=" + replaced + '}';
    }

    public int lengthDifference(String text) {
        var diff = text.codePointCount(0, text.length()) - (this.end - this.start);
        return diff;
    }
}
