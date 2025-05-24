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

/**
 * The position of the caret (cursor) within a single byte (2 hex digits).
 *
 * @author Pavel Castornii
 */
public enum BytePosition {

    /**
     * The caret position associated with the first nibble of the byte.
     * <p>
     * For bar caret: placed before the first digit (e.g., |AB)<br>
     * For block caret: highlighting the first digit (e.g., [A]B)<br>
     * For underscore caret: under the first digit (e.g., <u>A</u>B)
     */
    FIRST,

    /**
     * The caret position associated with the second nibble of the byte.
     * <p>
     * For bar caret: placed between the two digits (e.g., A|B)<br>
     * For block caret: highlighting the second digit (e.g., A[B])<br>
     * For underscore caret: under the second digit (e.g., A<u>B</u>)
     */
    SECOND,

    /**
     * The caret position after the second nibble of the byte.
     * <p>
     * This position is supported only by bar-style carets and used in limited scenarios —
     * for example, when placing the caret at the very end of the file or during range selections.
     *
     * <p>
     * Example (bar caret): AB|
     */
    THIRD
}
