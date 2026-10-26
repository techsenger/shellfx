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
 * An asynchronous operation of a {@link FileProvider} or a {@link FileRefiner} that is running and can be cancelled.
 *
 * @author Pavel Castornii
 */
public interface AsyncTask {

    /**
     * Cancels the operation: the callbacks with data that have not started yet are not called, and the end callback
     * gets {@link AsyncStatus#CANCELLED} unless the operation has already ended. A callback that is running, and the
     * work that has been started, is not interrupted. Calling it again has no effect.
     */
    void cancel();

    /**
     * Tells whether the operation has been cancelled.
     *
     * @return {@code true} if {@link #cancel} has been called
     */
    boolean isCancelled();
}
