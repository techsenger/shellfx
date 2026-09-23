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

package com.techsenger.shellfx.layout.pagehost;

/**
 * A mutable accumulator {@link TreePageHostViewModel#match(com.techsenger.shellfx.core.page.TreePageItem,
 * java.util.regex.Matcher, MatchCounts)} increments while walking the tree; once the walk finishes, its totals are
 * copied into a {@link PageFindResult}.
 *
 * @author Pavel Castornii
 */
final class MatchCounts {

    private int totalItems;

    private int totalMatches;

    int getTotalItems() {
        return totalItems;
    }

    int getTotalMatches() {
        return totalMatches;
    }

    void incrementTotalItems() {
        totalItems++;
    }

    void incrementTotalMatches() {
        totalMatches++;
    }
}
