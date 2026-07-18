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

package com.techsenger.shellfx.layout.dockhost;

/**
 * Identifies which existing sibling(s) donate space to a newly inserted TabDock, when more than one candidate
 * is available.
 * <p>
 * When several siblings are chosen as donors ({@link #NEAREST_SIBLINGS}, {@link #ALL_SIBLINGS}), each
 * contributes proportionally to its own current size relative to the combined size of the chosen donors — a
 * donor twice the size of another gives up twice as much space. For example, if the two nearest siblings are
 * 1000px and 500px and the new TabDock needs 300px, the first gives up 200px and the second 100px.
 *
 * @author Pavel Castornii
 */
public enum SpaceDonor {

    /** The sibling immediately before the insertion point donates the required space alone. */
    PREVIOUS_SIBLING,

    /** The sibling immediately after the insertion point donates the required space alone. */
    NEXT_SIBLING,

    /** Both immediate neighbors donate, proportionally to their current size. */
    NEAREST_SIBLINGS,

    /** Every existing item in the target SplitPane donates, proportionally to its current size. */
    ALL_SIBLINGS
}

