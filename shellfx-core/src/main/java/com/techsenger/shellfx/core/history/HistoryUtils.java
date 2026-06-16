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

package com.techsenger.shellfx.core.history;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public final class HistoryUtils {

    private static final int COLLECTION_MAX_SIZE = 25;

    /**
     * Limits the collection to the specified maximum size. If the collection exceeds the max size, it will be
     * truncated.
     *
     * @param col
     */
    public static void limit(Collection<?> col) {
        if (col.size() > COLLECTION_MAX_SIZE) {
            var iterator = col.iterator();
            var index = 0;
            while (iterator.hasNext()) {
                iterator.next();
                if (index >= COLLECTION_MAX_SIZE) {
                    iterator.remove();
                }
                index++;
            }
        }
    }

    /**
     * Adds value to head of the list and checks if this value is not present in the list somewhere else.
     *
     * @param <T>
     * @param list
     * @param value
     */
    public static <T> void addFirst(List<T> list, T value) {
        list.remove(value);
        list.add(0, value);
    }

    private HistoryUtils() {
        //empty
    }
}
