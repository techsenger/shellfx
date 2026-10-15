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

package com.techsenger.shellfx.storage;

/**
 * Tells how far a listed file is in the two-phase loading of a directory: whether the data the first phase gave is
 * final, or the second phase still has to refine it.
 *
 * @author Pavel Castornii
 */
public enum FileState {

    /**
     * The data of the file is not final: the second phase has not refined it yet. The file is shown with a state
     * icon instead of its own.
     */
    PENDING,

    /**
     * The data of the file is final, either because it needed no refining or because the second phase has refined
     * it.
     */
    READY,

    /**
     * The second phase could not refine the file, so the data of the first phase is all there is. The file is shown
     * with an error icon.
     */
    FAILED
}
