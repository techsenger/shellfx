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

import com.techsenger.annotations.Nullable;

/**
 * The end of an asynchronous operation of a {@link FileProvider} or a {@link FileRefiner}: how it ended and, if it
 * failed, the exception that failed it, so the client can tell the user why.
 *
 * @author Pavel Castornii
 */
public final class AsyncResult {

    /**
     * The operation has given all its data.
     */
    public static final AsyncResult SUCCESS = new AsyncResult(AsyncStatus.SUCCESS, null);

    /**
     * The operation was cancelled before it ended.
     */
    public static final AsyncResult CANCELLED = new AsyncResult(AsyncStatus.CANCELLED, null);

    /**
     * Creates the result of an operation that failed.
     *
     * @param exception the exception that failed the operation
     * @return the result with the status {@code FAILED}
     */
    public static AsyncResult failed(Exception exception) {
        return new AsyncResult(AsyncStatus.FAILED, exception);
    }

    private final AsyncStatus status;

    private final @Nullable Exception exception;

    private AsyncResult(AsyncStatus status, @Nullable Exception exception) {
        this.status = status;
        this.exception = exception;
    }

    public AsyncStatus getStatus() {
        return status;
    }

    /**
     * Returns the exception that failed the operation.
     *
     * @return the exception, or {@code null} if the operation did not fail
     */
    public @Nullable Exception getException() {
        return exception;
    }
}
