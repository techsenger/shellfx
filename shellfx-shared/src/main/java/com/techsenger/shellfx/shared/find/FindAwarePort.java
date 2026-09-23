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

package com.techsenger.shellfx.shared.find;

import java.util.concurrent.CompletableFuture;

/**
 * @param <R> the kind of {@link FindResult} the search completes with
 * @author Pavel Castornii
 */
public interface FindAwarePort<R extends FindResult> {

    /**
     * Runs a find and returns its eventual outcome. May complete synchronously or from a background thread; either
     * way, the caller applies the result on the JavaFX Application Thread and discards it if a newer find has been
     * started in the meantime.
     */
    CompletableFuture<R> onFind();

    void onFindCleared();
}
