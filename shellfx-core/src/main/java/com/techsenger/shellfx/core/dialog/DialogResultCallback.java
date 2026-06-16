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

package com.techsenger.shellfx.core.dialog;

/**
 * A callback interface for handling dialog operation results, cancellations, and errors.
 * Provides separate methods for different dialog lifecycle events.
 *
 * @param <T> the type of successful result produced by the dialog.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface DialogResultCallback<T> {

    /**
     * Handles a successful dialog completion with a result.
     *
     * @param result
     */
    void onResult(T result);

    /**
     * Handles dialog cancellation by the user. Default implementation does nothing.
     */
    default void onCancel() { }

    /**
     * Handles dialog errors that prevent normal completion. Default implementation does nothing.
     *
     * @param ex
     */
    default void onError(Exception ex) { }
}
