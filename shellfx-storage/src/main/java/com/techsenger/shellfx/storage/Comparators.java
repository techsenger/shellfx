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

import java.util.Comparator;

/**
 *
 * @author Pavel Castornii
 */
public final class Comparators {

    /**
     * Wraps an existing aggregate comparator with a higher-priority rule that ensures directories are always sorted
     * before regular files, regardless of the underlying comparison logic.
     *
     * <p>This method does not modify the provided comparator. Instead, it returns a new comparator that delegates to
     * the given aggregate comparator after applying the directory-first rule.</p>
     *
     * <p>Evaluation order:</p>
     * <ol>
     *     <li>Directory precedence (directories come before non-directories)</li>
     *     <li>If both elements are of the same type (both directories or both files),
     *         delegation to the provided aggregate comparator</li>
     * </ol>
     *
     * <p>If the provided comparator is {@code null}, it is treated as a neutral comparator that always returns 0
     * (i.e., elements are considered equal).</p>
     *
     * @param aggregateComparator the base comparator produced by JavaFX (e.g. from TableView),
     *                            representing user-defined column sorting logic
     * @return a comparator that enforces directory-first ordering on top of the base comparator
     */
    public static Comparator<GenericFile> directoryFirst(Comparator<GenericFile> aggregateComparator) {
        return (a, b) -> {
            boolean aDir = a.isDirectory();
            boolean bDir = b.isDirectory();
            if (aDir && !bDir) {
                return -1;
            }
            if (!aDir && bDir) {
                return 1;
            }
            if (aggregateComparator != null) {
                return aggregateComparator.compare(a, b);
            }
            return 0;
        };
    }

    private Comparators() {
        // empty
    }
}
