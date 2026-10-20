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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The task of the operations of the default provider and the default refiner, which end exactly once.
 *
 * @author Pavel Castornii
 */
final class DefaultAsyncTask implements AsyncTask {

    private final AtomicBoolean finished = new AtomicBoolean();

    private volatile boolean cancelled;

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Gives the end of the operation to the callback, unless some other call has done it already.
     */
    void finish(Consumer<AsyncResult> onFinished, AsyncResult result) {
        if (finished.compareAndSet(false, true)) {
            onFinished.accept(result);
        }
    }
}
