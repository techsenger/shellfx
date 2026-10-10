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
 * Classifies a {@link FileOperation} reported to a {@link StorageListener} as directly user-initiated or as a step
 * of bulk/internal machinery. Every method of a {@link FileStorage} that reaches the storage takes it. Unlike a
 * suppression flag, it never stops a listener from being called - every operation is always reported to every
 * listener; it only tells each listener which kind this one is, leaving the decision of what to do with that (if
 * anything) entirely up to the listener.
 *
 * @author Pavel Castornii
 */
public enum OperationType {

    /**
     * A directly user-initiated action (open, save, rename, ...) that is itself a meaningful "recently used"
     * signal.
     */
    PRIMARY,

    /**
     * One step of bulk/internal machinery (e.g. copying a whole directory tree, or scanning one to plan a
     * copy) whose many individual operations aren't a meaningful signal on their own, unlike the same
     * operation performed once as a direct user action.
     */
    SECONDARY
}
