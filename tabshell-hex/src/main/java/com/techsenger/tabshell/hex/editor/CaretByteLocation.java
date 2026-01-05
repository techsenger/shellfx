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
 * The location of the caret (cursor) within a single byte for both HEX and ASCII panels.
 * <p>In the HEX panel, a byte is represented as two hexadecimal digits. In the ASCII panel, a byte is shown as
 * a single character.
 *
 * <p>The rendering of the caret depends on its style (bar, block, or underscore) and the panel.
 *
 * @author Pavel Castornii
 */
public enum CaretByteLocation {

    /**
     * The first location that is compatible with all shape types.
     * <p>
     * HEX panel - the caret location associated with the first nibble of the byte:
     * <ul>
     *   <li>Bar: placed before the first digit — <code>|AB</code></li>
     *   <li>Block: highlights the first digit — <code>[A]B</code></li>
     *   <li>Underscore: underlines the first digit — <code><u>A</u>B</code></li>
     * </ul>
     *
     * ASCII panel - the caret location associated with the whole byte:
     * <ul>
     *   <li>Bar: placed before the character — <code>|A</code></li>
     *   <li>Block: highlights the whole character — <code>[A]</code></li>
     *   <li>Underscore: underlines the character — <code><u>A</u></code></li>
     * </ul>
     */
    FIRST,

    /**
     * The second location that is compatible with all shape types.
     * <p>
     * HEX panel - the caret location associated with the second nibble of the byte:
     * <ul>
     *   <li>Bar: placed between the two digits — <code>A|B</code></li>
     *   <li>Block: highlights the second digit — <code>A[B]</code></li>
     *   <li>Underscore: underlines the second digit — <code>A<u>B</u></code></li>
     * </ul>
     *
     * ASCII panel - behaves the same as {@link #FIRST} because the caret applies to the whole character:
     * <ul>
     *   <li>Bar: placed before the character — <code>|A</code></li>
     *   <li>Block: highlights the whole character — <code>[A]</code></li>
     *   <li>Underscore: underlines the character — <code><u>A</u></code></li>
     * </ul>
     */
    SECOND,

    /**
     * This location is supported only by bar-shaped carets and used in limited scenarios — for example, when
     * placing the caret at the very end of the row or during range selections.
     *
     * HEX panel - the caret location after the second nibble of the byte:
     * <ul>
     *   <li>Bar: placed after the byte — <code>AB|</code></li>
     * </ul>
     *
     * ASCII panel - the caret location after the character:
     * <ul>
     *   <li>Bar: placed after the character — <code>A|</code></li>
     * </ul>
     */
    THIRD
}
