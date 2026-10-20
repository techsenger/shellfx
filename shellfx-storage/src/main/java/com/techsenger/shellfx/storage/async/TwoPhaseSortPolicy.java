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

package com.techsenger.shellfx.storage.async;

/**
 * Defines when files that have a second phase are sorted automatically: after the first phase, after the second one,
 * after each of them, or never. The second phase refines the fields whose values are final only after it. A directory
 * whose files are all ready after the first phase is sorted right after it, whatever the policy is.
 *
 * @author Pavel Castornii
 */
public enum TwoPhaseSortPolicy {

    /**
     * Files are never sorted automatically. The sorting becomes available when the second phase is finished, and is
     * applied only when the user asks for it.
     */
    ON_REQUEST,

    /**
     * Files are sorted once, after the first phase. A file whose refined value is not known yet is placed after the
     * others, and the order is not changed when the second phase finishes.
     */
    AFTER_FIRST_PHASE,

    /**
     * Files are sorted once, after the second phase. Until then they stay in the order the storage listed them, so the
     * list does not move twice.
     */
    AFTER_SECOND_PHASE,

    /**
     * Files are sorted after the first phase and once again after the second phase, so the order may change when
     * the refined values become known.
     */
    AFTER_EACH_PHASE
}
